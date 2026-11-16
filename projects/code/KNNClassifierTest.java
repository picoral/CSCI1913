public class KNNClassifierTest {

    private static DataPoint pt(String label, double... f) {
        return new DataPoint(f, label);
    }

    /** Points on a line: A at 0,1,2   B at 10,11,12. */
    private static DataPoint[] line() {
        return new DataPoint[]{
                pt("A", 0), pt("A", 1), pt("A", 2),
                pt("B", 10), pt("B", 11), pt("B", 12),
        };
    }

    public static void testErrors() {
        try {
            new KNNClassifier(0);
            Assert.fail("k < 1 should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        KNNClassifier c = new KNNClassifier(3);
        try {
            c.predict(new double[]{1});
            Assert.fail("predict before train should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public static void testSimple() {
        for (boolean standardize : new boolean[]{true, false}) {
            KNNClassifier c = new KNNClassifier(3, standardize);
            c.train(line());
            Assert.assertEquals("A", c.predict(new double[]{-5}));
            Assert.assertEquals("A", c.predict(new double[]{4}));
            Assert.assertEquals("B", c.predict(new double[]{7}));
            Assert.assertEquals("B", c.predict(new double[]{100}));
        }
    }

    public static void testMajority() {
        DataPoint[] data = {pt("A", 0), pt("B", 4), pt("B", 5), pt("B", 6)};
        KNNClassifier one = new KNNClassifier(1, false);
        one.train(data);
        Assert.assertEquals("with k = 1 the nearest point decides", "A", one.predict(new double[]{1.9}));
        KNNClassifier three = new KNNClassifier(3, false);
        three.train(data);
        Assert.assertEquals("with k = 3, two B votes beat one A vote", "B", three.predict(new double[]{1.9}));
    }

    public static void testTies() {
        KNNClassifier c = new KNNClassifier(2, false);
        c.train(new DataPoint[]{pt("A", 0), pt("B", 10)});
        Assert.assertEquals("a tied vote goes to the label of the closest neighbor", "A", c.predict(new double[]{4}));
        Assert.assertEquals("a tied vote goes to the label of the closest neighbor", "B", c.predict(new double[]{6}));

        KNNClassifier c4 = new KNNClassifier(4, false);
        c4.train(new DataPoint[]{pt("Zed", 0), pt("Amy", 1), pt("Amy", 2), pt("Zed", 3), pt("Bob", 50)});
        // neighbors of 0.1 closest-first: Zed, Amy, Amy, Zed -> 2-2 tie, Zed is closest
        Assert.assertEquals("Zed", c4.predict(new double[]{0.1}));
        // neighbors of 2.9 closest-first: Zed, Amy, Amy, Zed -> tie, Zed is closest
        Assert.assertEquals("Zed", c4.predict(new double[]{2.9}));
        // neighbors of 1.6 closest-first: Amy(2), Amy(1), Zed(3), Zed(0) -> tie, Amy is closest
        Assert.assertEquals("Amy", c4.predict(new double[]{1.6}));
    }

    public static void testLabels() {
        KNNClassifier c = new KNNClassifier(1);
        c.train(new DataPoint[]{pt("gentoo", 1, 1), pt("adelie", 2, 2), pt("gentoo", 3, 3),
                pt("chinstrap", 4, 4), pt("adelie", 5, 5)});
        Assert.assertArrayEquals(new String[]{"adelie", "chinstrap", "gentoo"}, c.getLabels());
    }

    public static void testStandardizationMatters() {
        DataPoint[] data = {
                pt("A", 100, 0), pt("A", 300, 0),
                pt("B", 200, 10), pt("B", 400, 10),
        };
        double[] query = {110, 9};
        KNNClassifier raw = new KNNClassifier(1, false);
        raw.train(data);
        Assert.assertEquals("without standardizing, feature 0 dominates the distance", "A", raw.predict(query));
        KNNClassifier z = new KNNClassifier(1, true);
        z.train(data);
        Assert.assertEquals("after standardizing, feature 1 matters too", "B", z.predict(query));
        Assert.assertEquals("predict must not modify its input", 110.0, query[0], 0.0);
    }

    public static void testAccuracy() {
        KNNClassifier c = new KNNClassifier(1, false);
        c.train(line());
        Assert.assertEquals(1.0, c.accuracy(line()), 1e-12);
        DataPoint[] test = {pt("A", 0.5), pt("B", 1.5), pt("A", 9), pt("A", 11)};
        Assert.assertEquals(0.25, c.accuracy(test), 1e-12);
    }

    public static void testConfusion() {
        KNNClassifier c = new KNNClassifier(1, false);
        c.train(new DataPoint[]{pt("cat", 0), pt("bird", 10), pt("dog", 20)});
        // labels sorted: bird, cat, dog
        DataPoint[] test = {
                pt("cat", 1), pt("cat", 9), pt("cat", 19),
                pt("bird", 11), pt("dog", 21), pt("dog", 12),
        };
        int[][] expected = {
                {1, 0, 0},  // actual bird
                {1, 1, 1},  // actual cat
                {1, 0, 1},  // actual dog
        };
        int[][] m = c.confusionMatrix(test);
        Assert.assertEquals(3, m.length);
        for (int r = 0; r < 3; r++) {
            Assert.assertArrayEquals("confusion matrix row " + r + " is wrong (rows = actual, columns = predicted, in getLabels() order)",
                    expected[r], m[r]);
        }
    }

    private static DataPoint[][] penguinSplit() {
        DataPoint[] penguins = PenguinData.load();
        PenguinData.shuffle(penguins, 1913);
        int trainSize = (int) (penguins.length * 0.8);
        return new DataPoint[][]{
                PenguinData.slice(penguins, 0, trainSize),
                PenguinData.slice(penguins, trainSize, penguins.length)
        };
    }

    public static void testPenguinAccuracy() {
        DataPoint[][] split = penguinSplit();
        KNNClassifier raw = new KNNClassifier(5, false);
        raw.train(split[0]);
        Assert.assertEquals("raw (unstandardized) k = 5 accuracy", 55.0 / 69, raw.accuracy(split[1]), 1e-9);
        KNNClassifier z = new KNNClassifier(5, true);
        z.train(split[0]);
        Assert.assertEquals("standardized k = 5 accuracy", 1.0, z.accuracy(split[1]), 1e-9);
    }

    public static void testPenguinConfusion() {
        DataPoint[][] split = penguinSplit();
        KNNClassifier c = new KNNClassifier(5);
        c.train(PenguinData.selectFeatures(split[0], 2, 3));
        Assert.assertArrayEquals(new String[]{"Adelie", "Chinstrap", "Gentoo"}, c.getLabels());
        int[][] m = c.confusionMatrix(PenguinData.selectFeatures(split[1], 2, 3));
        int[][] expected = {{21, 4, 0}, {5, 8, 0}, {1, 0, 30}};
        for (int r = 0; r < 3; r++) {
            Assert.assertArrayEquals("confusion matrix row " + r + " is wrong", expected[r], m[r]);
        }
    }

    // ---------------------------------------------------------------------
    public static void main(String[] args) {
        int passed = 0;
        passed += run("KNNClassifier errors before training", KNNClassifierTest::testErrors);
        passed += run("KNNClassifier simple predictions", KNNClassifierTest::testSimple);
        passed += run("KNNClassifier majority vote beats the single nearest", KNNClassifierTest::testMajority);
        passed += run("KNNClassifier tie-breaking", KNNClassifierTest::testTies);
        passed += run("KNNClassifier getLabels is distinct and sorted", KNNClassifierTest::testLabels);
        passed += run("KNNClassifier standardization changes the answer", KNNClassifierTest::testStandardizationMatters);
        passed += run("KNNClassifier accuracy", KNNClassifierTest::testAccuracy);
        passed += run("KNNClassifier confusion matrix", KNNClassifierTest::testConfusion);
        passed += run("KNNClassifier on the penguins (accuracy)", KNNClassifierTest::testPenguinAccuracy);
        passed += run("KNNClassifier on the penguins (confusion matrix, flipper + mass)", KNNClassifierTest::testPenguinConfusion);
        System.out.println();
        System.out.println(passed + " / 10 tests passed");
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
