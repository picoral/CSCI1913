public class ScalerTest {
    private static final double EPS = 1e-9;

    private static DataPoint[] simple() {
        return new DataPoint[]{
                new DataPoint(new double[]{1, 10}, "a"),
                new DataPoint(new double[]{2, 20}, "b"),
                new DataPoint(new double[]{3, 30}, "c"),
        };
    }

    public static void testMeans() {
        Scaler s = new Scaler();
        s.fit(simple());
        Assert.assertEquals("wrong mean for feature 0", 2.0, s.getMean(0), EPS);
        Assert.assertEquals("wrong mean for feature 1", 20.0, s.getMean(1), EPS);
    }

    public static void testStdDev() {
        Scaler s = new Scaler();
        s.fit(simple());
        Assert.assertEquals("wrong standard deviation for feature 0", 1.0, s.getStdDev(0), EPS);
        Assert.assertEquals("wrong standard deviation for feature 1", 10.0, s.getStdDev(1), EPS);

        s.fit(new DataPoint[]{
                new DataPoint(new double[]{2}, "x"), new DataPoint(new double[]{4}, "x"),
                new DataPoint(new double[]{4}, "x"), new DataPoint(new double[]{4}, "x"),
                new DataPoint(new double[]{5}, "x"), new DataPoint(new double[]{5}, "x"),
                new DataPoint(new double[]{7}, "x"), new DataPoint(new double[]{9}, "x"),
        });
        Assert.assertEquals("refitting should replace the old mean", 5.0, s.getMean(0), EPS);
        Assert.assertEquals("wrong standard deviation (did you divide by n instead of n - 1?)",
                Math.sqrt(32.0 / 7.0), s.getStdDev(0), EPS);
    }

    public static void testTransform() {
        Scaler s = new Scaler();
        s.fit(simple());
        double[] input = {4, 5};
        double[] z = s.transform(input);
        Assert.assertEquals(2, z.length);
        Assert.assertEquals(2.0, z[0], EPS);
        Assert.assertEquals(-1.5, z[1], EPS);
        Assert.assertEquals("transform must not modify its input", 4.0, input[0], EPS);
        Assert.assertEquals("transform must not modify its input", 5.0, input[1], EPS);
    }

    public static void testTransformPoint() {
        Scaler s = new Scaler();
        s.fit(simple());
        DataPoint original = new DataPoint(new double[]{1, 30}, "penguin");
        DataPoint z = s.transform(original);
        Assert.assertEquals("penguin", z.getLabel());
        Assert.assertEquals(-1.0, z.getFeature(0), EPS);
        Assert.assertEquals(1.0, z.getFeature(1), EPS);
        Assert.assertEquals("transform must not modify the original point", 1.0, original.getFeature(0), EPS);
    }

    public static void testConstantFeature() {
        Scaler s = new Scaler();
        s.fit(new DataPoint[]{
                new DataPoint(new double[]{7, 1}, "a"),
                new DataPoint(new double[]{7, 3}, "a"),
        });
        Assert.assertEquals(0.0, s.getStdDev(0), EPS);
        double[] z = s.transform(new double[]{9, 2});
        Assert.assertFalse("constant feature produced NaN/Infinity", Double.isNaN(z[0]) || Double.isInfinite(z[0]));
        Assert.assertEquals("a constant feature should only be centered", 2.0, z[0], EPS);
        Assert.assertEquals(0.0, z[1], EPS);
    }

    public static void testErrors() {
        Scaler s = new Scaler();
        try {
            s.transform(new double[]{1, 2});
            Assert.fail("transform before fit should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        try {
            s.getMean(0);
            Assert.fail("getMean before fit should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        try {
            s.fit(new DataPoint[0]);
            Assert.fail("fit on an empty array should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        s.fit(simple());
        try {
            s.transform(new double[]{1, 2, 3});
            Assert.fail("transform with the wrong number of features should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public static void testPenguins() {
        Scaler s = new Scaler();
        s.fit(PenguinData.load());
        double[] means = {43.9219298246, 17.1511695906, 200.9152046784, 4201.7543859649};
        double[] sds = {5.4595837139, 1.9747931568, 14.0617136794, 801.9545356981};
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals("wrong mean for " + PenguinData.FEATURE_NAMES[i], means[i], s.getMean(i), 1e-6);
            Assert.assertEquals("wrong std dev for " + PenguinData.FEATURE_NAMES[i], sds[i], s.getStdDev(i), 1e-6);
        }
    }

    // ---------------------------------------------------------------------
    public static void main(String[] args) {
        int passed = 0;
        passed += run("Scaler computes means", ScalerTest::testMeans);
        passed += run("Scaler computes SAMPLE standard deviation (n - 1)", ScalerTest::testStdDev);
        passed += run("Scaler transform computes z-scores", ScalerTest::testTransform);
        passed += run("Scaler transform of a DataPoint keeps the label", ScalerTest::testTransformPoint);
        passed += run("Scaler handles a constant feature", ScalerTest::testConstantFeature);
        passed += run("Scaler error handling", ScalerTest::testErrors);
        passed += run("Scaler on the penguin data matches R", ScalerTest::testPenguins);
        System.out.println();
        System.out.println(passed + " / 7 tests passed");
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
