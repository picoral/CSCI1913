import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

/**
 * Loads the Palmer Penguins data set (Gorman, Williams & Fraser, 2014;
 * distributed with R's datasets package as `penguins`).
 *
 * The penguins.csv file should be in your IntelliJ project folder, but NOT in
 * the src folder.
 *
 * PROVIDED -- do not modify.
 */
public class PenguinData {
    public static final String[] FEATURE_NAMES = {
            "bill_length_mm", "bill_depth_mm", "flipper_length_mm", "body_mass_g"
    };

    /** Load all penguins from penguins.csv. */
    public static DataPoint[] load() {
        return load("penguins.csv");
    }

    /** Load all penguins from the given csv file. */
    public static DataPoint[] load(String filename) {
        Scanner scan;
        try {
            scan = new Scanner(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            // If you get this error the csv file isn't where it is expected to be.
            throw new RuntimeException(e);
        }
        scan.nextLine(); // skip the header

        // We don't know how many rows there are, so grow an array as needed.
        DataPoint[] points = new DataPoint[16];
        int count = 0;
        while (scan.hasNextLine()) {
            String line = scan.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split(",");
            // parts: species, island, bill_length, bill_depth, flipper_length, body_mass
            double[] features = new double[FEATURE_NAMES.length];
            for (int i = 0; i < features.length; i++) {
                features[i] = Double.parseDouble(parts[i + 2]);
            }
            if (count == points.length) {
                DataPoint[] bigger = new DataPoint[points.length * 2];
                for (int i = 0; i < points.length; i++) {
                    bigger[i] = points[i];
                }
                points = bigger;
            }
            points[count] = new DataPoint(features, parts[0]);
            count++;
        }
        scan.close();

        DataPoint[] result = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            result[i] = points[i];
        }
        return result;
    }

    /**
     * Randomly shuffle an array in place (Fisher-Yates). Using the same seed
     * always gives the same order, which makes experiments repeatable.
     */
    public static void shuffle(DataPoint[] data, long seed) {
        Random rng = new Random(seed);
        for (int i = data.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            DataPoint tmp = data[i];
            data[i] = data[j];
            data[j] = tmp;
        }
    }

    /** Copy data[start] through data[end - 1] into a new array. */
    public static DataPoint[] slice(DataPoint[] data, int start, int end) {
        DataPoint[] result = new DataPoint[end - start];
        for (int i = start; i < end; i++) {
            result[i - start] = data[i];
        }
        return result;
    }

    /**
     * Make new DataPoints that keep only the listed feature columns (in the
     * order given). For example selectFeatures(data, 2, 3) keeps only
     * flipper length and body mass.
     */
    public static DataPoint[] selectFeatures(DataPoint[] data, int... columns) {
        DataPoint[] result = new DataPoint[data.length];
        for (int i = 0; i < data.length; i++) {
            double[] f = new double[columns.length];
            for (int c = 0; c < columns.length; c++) {
                f[c] = data[i].getFeature(columns[c]);
            }
            result[i] = new DataPoint(f, data[i].getLabel());
        }
        return result;
    }

    public static void main(String[] args) {
        DataPoint[] penguins = load();
        System.out.println("Loaded " + penguins.length + " penguins (expected 342).");
        System.out.println("First penguin: " + penguins[0]);
    }
}
