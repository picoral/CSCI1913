/**
 * One labeled observation: an array of numeric features plus a class label.
 * For the penguin data, the features are (in order):
 *   bill length (mm), bill depth (mm), flipper length (mm), body mass (g)
 * and the label is the species ("Adelie", "Chinstrap", or "Gentoo").
 *
 * DataPoint is immutable -- once created it never changes.
 * PROVIDED -- do not modify.
 */
public class DataPoint {
    private final double[] features;
    private final String label;

    /**
     * Create a data point. The features array is copied, so later changes to
     * the caller's array do not affect this DataPoint.
     */
    public DataPoint(double[] features, String label) {
        this.features = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            this.features[i] = features[i];
        }
        this.label = label;
    }

    /** Number of features (the dimension of this point). */
    public int getDimensions() {
        return features.length;
    }

    /** The value of a single feature. This does NOT make a copy -- it is fast. */
    public double getFeature(int index) {
        return features[index];
    }

    /**
     * A COPY of the full features array. Because this makes a copy it is
     * slower than getFeature -- think about where you call it.
     */
    public double[] getFeatures() {
        double[] copy = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            copy[i] = features[i];
        }
        return copy;
    }

    /** The class label (may be null for unlabeled points). */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < features.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(features[i]);
        }
        sb.append(")");
        if (label != null) {
            sb.append(" -> ").append(label);
        }
        return sb.toString();
    }
}
