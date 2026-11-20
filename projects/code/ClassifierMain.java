import java.util.Random;

/**
 * Runs the penguin classifier experiments. Use the output of this program to
 * answer the questions in the written analysis.
 *
 * PROVIDED -- you may modify this file to run extra experiments, but you do not
 * submit it.
 */
public class ClassifierMain {
    public static void main(String[] args) {
        DataPoint[] penguins = PenguinData.load();
        PenguinData.shuffle(penguins, 1913);
        int trainSize = (int) (penguins.length * 0.8);
        DataPoint[] train = PenguinData.slice(penguins, 0, trainSize);
        DataPoint[] test = PenguinData.slice(penguins, trainSize, penguins.length);
        System.out.println("Training on " + train.length + " penguins, testing on " + test.length);

        // ---- Part 1: describe the training data ----
        Scaler scaler = new Scaler();
        scaler.fit(train);
        System.out.println();
        System.out.printf("%-20s %10s %10s%n", "feature", "mean", "std dev");
        for (int i = 0; i < PenguinData.FEATURE_NAMES.length; i++) {
            System.out.printf("%-20s %10.2f %10.2f%n",
                    PenguinData.FEATURE_NAMES[i], scaler.getMean(i), scaler.getStdDev(i));
        }

        // ---- Part 2: accuracy as a function of k, with and without standardizing ----
        System.out.println();
        System.out.printf("%4s %14s %14s%n", "k", "raw accuracy", "z accuracy");
        for (int k = 1; k <= 25; k += 2) {
            KNNClassifier raw = new KNNClassifier(k, false);
            raw.train(train);
            KNNClassifier z = new KNNClassifier(k, true);
            z.train(train);
            System.out.printf("%4d %14.3f %14.3f%n", k, raw.accuracy(test), z.accuracy(test));
        }

        // ---- Part 3: a harder problem -- only flipper length and body mass ----
        DataPoint[] train2 = PenguinData.selectFeatures(train, 2, 3);
        DataPoint[] test2 = PenguinData.selectFeatures(test, 2, 3);
        System.out.println();
        System.out.println("Using only flipper_length_mm and body_mass_g (standardized):");
        System.out.printf("%4s %14s%n", "k", "z accuracy");
        for (int k = 1; k <= 25; k += 2) {
            KNNClassifier z = new KNNClassifier(k, true);
            z.train(train2);
            System.out.printf("%4d %14.3f%n", k, z.accuracy(test2));
        }

        // ---- Part 4: confusion matrix ----
        KNNClassifier model = new KNNClassifier(5);
        model.train(train2);
        String[] labels = model.getLabels();
        int[][] matrix = model.confusionMatrix(test2);
        System.out.println();
        System.out.println("Confusion matrix (k = 5, flipper + mass, standardized). Rows = actual, columns = predicted");
        System.out.printf("%12s", "");
        for (String label : labels) {
            System.out.printf("%12s", label);
        }
        System.out.println();
        for (int r = 0; r < labels.length; r++) {
            System.out.printf("%12s", labels[r]);
            for (int c = 0; c < labels.length; c++) {
                System.out.printf("%12d", matrix[r][c]);
            }
            System.out.println();
        }

        // ---- Part 5: how much work does the KDTree save? ----
        System.out.println();
        benchmark(100_000, 4, false);
        benchmark(5_000, 4, false);
        benchmark(5_000, 4, true);
    }

    /**
     * Build a tree of n random points and time 1000 nearest-neighbor queries.
     * If sorted is true the points are (i/n, i/n, i/n, ...) for i = 0, 1, 2, ...
     * inserted in that (increasing) order.
     */
    private static void benchmark(int n, int dims, boolean sorted) {
        Random rng = new Random(42);
        DataPoint[] points = new DataPoint[n];
        for (int i = 0; i < n; i++) {
            double[] f = new double[dims];
            for (int d = 0; d < dims; d++) {
                f[d] = sorted ? (double) i / n : rng.nextDouble();
            }
            points[i] = new DataPoint(f, null);
        }
        long start = System.currentTimeMillis();
        KDTree tree = new KDTree(dims);
        for (DataPoint p : points) {
            tree.insert(p);
        }
        long built = System.currentTimeMillis();

        int queries = 1000;
        tree.resetDistanceCount();
        for (int q = 0; q < queries; q++) {
            double[] query = new double[dims];
            for (int d = 0; d < dims; d++) {
                query[d] = rng.nextDouble();
            }
            tree.nearest(query);
        }
        long done = System.currentTimeMillis();

        System.out.println((sorted ? "SORTED" : "RANDOM") + " insertion order, n = " + n + ", dimensions = " + dims);
        System.out.println("  build time: " + (built - start) + " ms, tree height: " + tree.height());
        System.out.println("  " + queries + " queries: " + (done - built) + " ms, average distance computations per query: "
                + (tree.getDistanceCount() / queries) + " (a linear scan would use " + n + ")");
    }
}
