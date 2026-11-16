/**
 * Standardizes features to z-scores.
 *
 * Features measured on different scales (for example, one in the
 * thousands and another between 0 and 1) can distort distance-based
 * algorithms such as k-nearest neighbors: the feature with the largest
 * range dominates the distance. A Scaler fixes this by rescaling
 * every feature so that, across the training data, it has mean 0 and
 * standard deviation 1. Each value x of feature i becomes:
 *
 *     z = (x - mean[i]) / stdDev[i]
 *
 * Typical usage:
 * Create a Scaler and call DataPoint[] on the training data only.
 *     This learns each feature's mean and standard deviation.
 *       
 * Call transform(DataPoint) (or transform(double[])) on both training and 
 *     test points, so that all data is scaled using the same training statistics.
 *
 *     Scaler scaler = new Scaler();
 *     scaler.fit(trainingData);
 *     DataPoint scaled = scaler.transform(somePoint);
 */
public class Scaler {
    /** Mean of each feature, learned by fit(); null until fit() is called. */
    private double[] means;

    /**
     * Sample standard deviation of each feature, learned by fit();
     * null until fit() is called.
     */
    private double[] stdDevs;

    /**
     * Creates a new, unfitted Scaler. You must call
     * fit(DataPoint[]) before using any other method.
     */
    public Scaler() {
        means = null;
        stdDevs = null;
    }

    /**
     * Learns the mean and sample standard deviation of every feature in
     * the given data. Calling this again replaces any previously learned
     * values.
     *
     * The standard deviation uses the sample formula (dividing by
     * n - 1). If there is only one data point, all standard
     * deviations are left at 0.
     *
     * @param data the data points to learn from; all points are assumed
     *             to have the same number of features
     * @throws IllegalArgumentException if data is null or empty
     */
    public void fit(DataPoint[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("cannot fit a Scaler to no data");
        }
        int d = data[0].getDimensions(); // number of features per point
        int n = data.length;             // number of data points
        means = new double[d];
        stdDevs = new double[d];

        // pass 1: compute the mean of each feature.
        // first add up every point's value for each feature...
        for (DataPoint p : data) {
            for (int i = 0; i < d; i++) {
                means[i] += p.getFeature(i);
            }
        }
        // then divide each sum by the number of points.
        for (int i = 0; i < d; i++) {
            means[i] /= n;
        }

        // the sample standard deviation divides by (n - 1), which is
        // undefined for a single point, so stop here.
        if (n < 2) {
            return; // standard deviations stay 0
        }

        // pass 2: compute the standard deviation of each feature.
        // first add up the squared distance from the mean...
        for (DataPoint p : data) {
            for (int i = 0; i < d; i++) {
                double diff = p.getFeature(i) - means[i];
                stdDevs[i] += diff * diff;
            }
        }
        // then divide by (n - 1) and take the square root.
        for (int i = 0; i < d; i++) {
            stdDevs[i] = Math.sqrt(stdDevs[i] / (n - 1));
        }
    }

    /**
     * Makes sure fit(DataPoint[]) has been called.
     *
     * @throws IllegalStateException if this Scaler has not been fit
     */
    private void checkFitted() {
        if (means == null) {
            throw new IllegalStateException("Scaler has not been fit");
        }
    }

    /**
     * Returns the mean of one feature, as learned by
     * fit(DataPoint[])
     *
     * @param feature the index of the feature (0-based)
     * @return the mean of that feature in the training data
     * @throws IllegalStateException if this Scaler has not been fit
     * @throws ArrayIndexOutOfBoundsException if feature is not a
     *         valid feature index
     */
    public double getMean(int feature) {
        checkFitted();
        return means[feature];
    }

    /**
     * Returns the sample standard deviation of one feature, as learned by
     * fit(DataPoint[]).
     *
     * @param feature the index of the feature (0-based)
     * @return the standard deviation of that feature in the training data
     *         (0 if the feature never varies or only one point was used)
     * @throws IllegalStateException if this Scaler has not been fit
     * @throws ArrayIndexOutOfBoundsException if feature is not a
     *         valid feature index
     */
    public double getStdDev(int feature) {
        checkFitted();
        return stdDevs[feature];
    }

    /**
     * Converts an array of raw feature values into z-scores using the
     * learned means and standard deviations. The input array is not
     * modified; a new array is returned.
     *
     * If a feature's standard deviation is 0 (every training point had
     * the same value), dividing would be undefined, so that feature is only
     * centered (the mean is subtracted) and not divided.
     *
     * @param features the raw feature values to scale
     * @return a new array holding the scaled feature values
     * @throws IllegalStateException if this Scaler has not been fit
     * @throws IllegalArgumentException if features does not have
     *         the same number of features as the training data
     */
    public double[] transform(double[] features) {
        checkFitted();
        if (features.length != means.length) {
            throw new IllegalArgumentException("wrong number of features");
        }
        double[] result = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            // Shift so the training mean becomes 0.
            double centered = features[i] - means[i];
            // Divide by the standard deviation, unless it is 0
            // (avoids division by zero for constant features).
            result[i] = stdDevs[i] == 0 ? centered : centered / stdDevs[i];
        }
        return result;
    }

    /**
     * Returns a new DataPoint whose features are scaled to z-scores
     * and whose label is the same as the original. The original point is
     * not modified.
     *
     * @param p the data point to scale
     * @return a new, scaled data point with the same label as p
     * @throws IllegalStateException if this Scaler has not been fit
     * @throws IllegalArgumentException if p does not have the same
     *         number of features as the training data
     */
    public DataPoint transform(DataPoint p) {
        return new DataPoint(transform(p.getFeatures()), p.getLabel());
    }
}
