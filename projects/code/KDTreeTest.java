import java.util.Random;

public class KDTreeTest {

    private static DataPoint pt(double... f) {
        return new DataPoint(f, null);
    }

    private static DataPoint[] randomPoints(Random rng, int n, int dims) {
        DataPoint[] pts = new DataPoint[n];
        for (int i = 0; i < n; i++) {
            double[] f = new double[dims];
            for (int d = 0; d < dims; d++) {
                f[d] = rng.nextDouble() * 100;
            }
            pts[i] = new DataPoint(f, "p" + i);
        }
        return pts;
    }

    private static double[] randomQuery(Random rng, int dims) {
        double[] q = new double[dims];
        for (int d = 0; d < dims; d++) {
            q[d] = rng.nextDouble() * 100;
        }
        return q;
    }

    private static double dist(DataPoint p, double[] q) {
        double total = 0;
        for (int i = 0; i < q.length; i++) {
            double diff = p.getFeature(i) - q[i];
            total += diff * diff;
        }
        return Math.sqrt(total);
    }

    /** Brute-force k nearest distances, sorted ascending. */
    private static double[] bruteDistances(DataPoint[] pts, double[] q, int k) {
        double[] all = new double[pts.length];
        for (int i = 0; i < pts.length; i++) {
            all[i] = dist(pts[i], q);
        }
        java.util.Arrays.sort(all);
        return java.util.Arrays.copyOf(all, Math.min(k, all.length));
    }

    public static void testEmpty() {
        KDTree t = new KDTree(3);
        Assert.assertEquals(0, t.size());
        Assert.assertEquals("an empty tree has height 0", 0, t.height());
        Assert.assertNull("nearest on an empty tree should return null", t.nearest(new double[]{1, 2, 3}));
        Assert.assertEquals(0, t.kNearest(new double[]{1, 2, 3}, 4).length);
        Assert.assertFalse(t.contains(new double[]{1, 2, 3}));
    }

    public static void testSize() {
        KDTree t = new KDTree(2);
        t.insert(pt(1, 2));
        Assert.assertEquals(1, t.size());
        Assert.assertEquals(1, t.height());
        t.insert(pt(3, 4));
        t.insert(pt(1, 2)); // duplicates are allowed
        Assert.assertEquals(3, t.size());
        try {
            t.insert(pt(1, 2, 3));
            Assert.fail("inserting a point with the wrong number of dimensions should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        Assert.assertEquals("a failed insert should not change the size", 3, t.size());
    }

    public static void testAxisAlternation() {
        KDTree t = new KDTree(2);
        t.insert(pt(5, 5)); // root, splits on x
        t.insert(pt(3, 7)); // x 3 < 5 -> left;               splits on y
        t.insert(pt(7, 3)); // x 7 >= 5 -> right
        t.insert(pt(4, 2)); // left, then y 2 < 7 -> left;    splits on x
        Assert.assertEquals(3, t.height());
        t.insert(pt(4, 1)); // left, left, then x 4 >= 4 -> right
        Assert.assertEquals(4, t.height());
        t.insert(pt(9, 9)); // right, then y 9 >= 3 -> right
        Assert.assertEquals(4, t.height());
        Assert.assertEquals(6, t.size());
    }

    public static void testTiesGoRight() {
        KDTree t = new KDTree(2);
        t.insert(pt(5, 5));
        t.insert(pt(5, 1)); // tie on x -> right
        t.insert(pt(4, 9)); // x 4 < 5 -> left
        Assert.assertEquals("points equal on the splitting axis belong in the RIGHT subtree", 2, t.height());
    }

    public static void testDegenerate() {
        KDTree t = new KDTree(1);
        for (int i = 0; i < 200; i++) {
            t.insert(pt(i));
        }
        Assert.assertEquals(200, t.height());
        KDTree t2 = new KDTree(3);
        for (int i = 0; i < 200; i++) {
            t2.insert(pt(i, i, i));
        }
        Assert.assertEquals(200, t2.height());
    }

    public static void testContains() {
        KDTree t = new KDTree(2);
        DataPoint[] pts = randomPoints(new Random(1), 300, 2);
        for (DataPoint p : pts) {
            t.insert(p);
        }
        for (DataPoint p : pts) {
            Assert.assertTrue("contains missed an inserted point " + p, t.contains(p.getFeatures()));
        }
        t.insert(pt(50, 50));
        Assert.assertTrue(t.contains(new double[]{50, 50}));
        Assert.assertFalse("matching one coordinate is not enough", t.contains(new double[]{50, 51}));
        Assert.assertFalse(t.contains(new double[]{-1, -1}));
        try {
            t.contains(new double[]{1});
            Assert.fail("contains with the wrong number of dimensions should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public static void testNearestSmall() {
        KDTree t = new KDTree(2);
        DataPoint a = new DataPoint(new double[]{2, 3}, "a");
        DataPoint b = new DataPoint(new double[]{5, 4}, "b");
        DataPoint c = new DataPoint(new double[]{9, 6}, "c");
        DataPoint d = new DataPoint(new double[]{4, 7}, "d");
        DataPoint e = new DataPoint(new double[]{8, 1}, "e");
        DataPoint f = new DataPoint(new double[]{7, 2}, "f");
        for (DataPoint p : new DataPoint[]{f, b, c, a, d, e}) {
            t.insert(p);
        }
        Assert.assertEquals("c", t.nearest(new double[]{9, 5}).getLabel());
        Assert.assertEquals("a", t.nearest(new double[]{0, 0}).getLabel());
        Assert.assertEquals("b", t.nearest(new double[]{6.5, 3.9}).getLabel());
        // (7.2, 4.5) starts down the right side of the root (x >= 7), but its nearest point is b, on the left.
        Assert.assertEquals("b", t.nearest(new double[]{7.2, 4.5}).getLabel());
        Assert.assertEquals("an exact match is its own nearest neighbor", "d", t.nearest(new double[]{4, 7}).getLabel());
    }

    public static void testNearestRandom() {
        Random rng = new Random(1913);
        for (int dims : new int[]{2, 4}) {
            DataPoint[] pts = randomPoints(rng, 1000, dims);
            KDTree t = new KDTree(dims);
            for (DataPoint p : pts) {
                t.insert(p);
            }
            for (int q = 0; q < 300; q++) {
                double[] query = randomQuery(rng, dims);
                DataPoint found = t.nearest(query);
                Assert.assertNotNull(found);
                Assert.assertEquals("nearest returned a point that is not the closest (dims = " + dims + ")",
                        bruteDistances(pts, query, 1)[0], dist(found, query), 1e-9);
            }
        }
    }

    public static void testKNearestRandom() {
        Random rng = new Random(42);
        DataPoint[] pts = randomPoints(rng, 800, 3);
        KDTree t = new KDTree(3);
        for (DataPoint p : pts) {
            t.insert(p);
        }
        for (int k : new int[]{1, 2, 5, 17}) {
            for (int q = 0; q < 100; q++) {
                double[] query = randomQuery(rng, 3);
                DataPoint[] found = t.kNearest(query, k);
                double[] expected = bruteDistances(pts, query, k);
                Assert.assertEquals("kNearest returned the wrong number of points", k, found.length);
                for (int i = 0; i < k; i++) {
                    Assert.assertNotNull("kNearest result contains null", found[i]);
                    Assert.assertEquals("kNearest result " + i + " is wrong (results must be closest-first) k = " + k,
                            expected[i], dist(found[i], query), 1e-9);
                }
            }
        }
    }

    public static void testKNearestEdges() {
        KDTree t = new KDTree(2);
        t.insert(pt(0, 0));
        t.insert(pt(3, 0));
        t.insert(pt(1, 0));
        DataPoint[] found = t.kNearest(new double[]{0, 0}, 10);
        Assert.assertEquals("asking for more neighbors than points should return every point", 3, found.length);
        Assert.assertEquals(0.0, found[0].getFeature(0), 1e-12);
        Assert.assertEquals(1.0, found[1].getFeature(0), 1e-12);
        Assert.assertEquals(3.0, found[2].getFeature(0), 1e-12);
        try {
            t.kNearest(new double[]{0, 0}, 0);
            Assert.fail("k < 1 should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            t.nearest(new double[]{0, 0, 0});
            Assert.fail("a query with the wrong number of dimensions should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public static void testDistanceCounter() {
        KDTree t = new KDTree(2);
        Assert.assertEquals(0, t.getDistanceCount());
        t.insert(pt(1, 1));
        t.insert(pt(2, 2));
        Assert.assertEquals("insert should not compute distances", 0, t.getDistanceCount());
        t.nearest(new double[]{0, 0});
        Assert.assertTrue("nearest should compute at least one distance", t.getDistanceCount() >= 1);
        Assert.assertTrue("nearest on 2 points should compute at most 2 distances", t.getDistanceCount() <= 2);
        t.resetDistanceCount();
        Assert.assertEquals(0, t.getDistanceCount());
    }

    public static void testNearestEfficiency() {
        Random rng = new Random(7);
        int n = 20000;
        KDTree t = new KDTree(2);
        for (DataPoint p : randomPoints(rng, n, 2)) {
            t.insert(p);
        }
        int queries = 500;
        t.resetDistanceCount();
        for (int q = 0; q < queries; q++) {
            t.nearest(randomQuery(rng, 2));
        }
        double avg = (double) t.getDistanceCount() / queries;
        Assert.assertTrue("distance counter is not being incremented", avg >= 1);
        Assert.assertTrue("nearest computed " + avg + " distances per query on " + n
                + " random points -- the goal is under 100 (a linear scan uses " + n + ")", avg < 100);
    }

    public static void testKNearestEfficiency() {
        Random rng = new Random(8);
        int n = 20000;
        KDTree t = new KDTree(3);
        for (DataPoint p : randomPoints(rng, n, 3)) {
            t.insert(p);
        }
        int queries = 500;
        t.resetDistanceCount();
        for (int q = 0; q < queries; q++) {
            t.kNearest(randomQuery(rng, 3), 10);
        }
        double avg = (double) t.getDistanceCount() / queries;
        Assert.assertTrue("distance counter is not being incremented", avg >= 10);
        Assert.assertTrue("kNearest(k = 10) computed " + avg + " distances per query on " + n
                + " random points -- the goal is under 500 (a linear scan uses " + n + ")", avg < 500);
    }

    // ---------------------------------------------------------------------
    public static void main(String[] args) {
        int passed = 0;
        passed += run("KDTree empty tree", KDTreeTest::testEmpty);
        passed += run("KDTree insert and size", KDTreeTest::testSize);
        passed += run("KDTree alternates splitting axis by depth", KDTreeTest::testAxisAlternation);
        passed += run("KDTree ties on the splitting axis go right", KDTreeTest::testTiesGoRight);
        passed += run("KDTree height of a degenerate (sorted) tree", KDTreeTest::testDegenerate);
        passed += run("KDTree contains", KDTreeTest::testContains);
        passed += run("KDTree nearest on a small example", KDTreeTest::testNearestSmall);
        passed += run("KDTree nearest matches brute force (random 2D and 4D)", KDTreeTest::testNearestRandom);
        passed += run("KDTree kNearest matches brute force and is sorted", KDTreeTest::testKNearestRandom);
        passed += run("KDTree kNearest with k larger than the tree, and errors", KDTreeTest::testKNearestEdges);
        passed += run("KDTree distance counter counts and resets", KDTreeTest::testDistanceCounter);
        passed += run("KDTree EFFICIENCY: nearest prunes the search", KDTreeTest::testNearestEfficiency);
        passed += run("KDTree EFFICIENCY: kNearest prunes the search", KDTreeTest::testKNearestEfficiency);
        System.out.println();
        System.out.println(passed + " / 13 tests passed");
    }

    private static int run(String name, Runnable test) {
        try {
            test.run();
            System.out.println("PASS  " + name);
            return 1;
        } catch (Throwable e) {
            System.out.println("FAIL  " + name + "\n        " + e);
            return 0;
        }
    }
}
