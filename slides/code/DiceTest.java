/**
 * Author: Adriana Picoral Fall 2026
 * A class that tests the Dice and BiasedDice classes (slides 08-01 practice).
 * Put this file in the same folder as Dice.java and BiasedDice.java, then run:
 *     javac Dice.java BiasedDice.java DiceTest.java
 *     java DiceTest
 * Every line should say "passed test". Because dice are random, some tests
 * roll many times and check that the results are close to the expected
 * percentages.
 */
public class DiceTest {

    private static final int ROLLS = 100000;
    private static final double TOLERANCE = 0.02;

    private static int passed = 0;
    private static int failed = 0;

    /**
     * doTest prints helpful messages informing the user whether or not
     * a test passed.
     * @param expected
     * @param output
     * @param message -- name of a method, or test
     */
    public static void doTest(Object expected, Object output, String message) {
        if (expected.equals(output)) {
            System.out.println(message + " passed test");
            passed++;
        } else {
            System.out.print(message + " failed, expected was " + expected);
            System.out.println(" but output was " + output);
            failed++;
        }
    }

    /**
     * Rolls the die many times and returns how often each side came up,
     * as a fraction of all rolls. Index 0 counts rolls that were out of range.
     */
    public static double[] rollMany(Dice die) {
        int sides = die.getSides();
        int[] counts = new int[sides + 1];
        for (int i = 0; i < ROLLS; i++) {
            int value = die.roll();
            if (value >= 1 && value <= sides) {
                counts[value]++;
            } else {
                counts[0]++;
            }
        }
        double[] fractions = new double[sides + 1];
        for (int i = 0; i <= sides; i++) {
            fractions[i] = (double) counts[i] / ROLLS;
        }
        return fractions;
    }

    /**
     * Returns true if actual is within TOLERANCE of expected.
     */
    public static boolean close(double expected, double actual) {
        return Math.abs(expected - actual) <= TOLERANCE;
    }

    public static void main(String[] args) {

        // ---------- Dice ----------
        System.out.println("===== Dice =====");

        Dice six = new Dice(6);
        doTest(6, six.getSides(), "new Dice(6).getSides()");

        Dice twenty = new Dice(20);
        doTest(20, twenty.getSides(), "new Dice(20).getSides()");

        double[] fair = rollMany(six);
        doTest(0.0, fair[0], "Dice(6).roll() always between 1 and 6 (fraction out of range)");
        for (int side = 1; side <= 6; side++) {
            doTest(true, close(1.0 / 6, fair[side]),
                   "Dice(6).roll() lands on " + side + " about 16.7% of the time (was "
                   + String.format("%.3f", fair[side]) + ")");
        }

        double[] fair20 = rollMany(twenty);
        doTest(0.0, fair20[0], "Dice(20).roll() always between 1 and 20 (fraction out of range)");
        doTest(true, fair20[1] > 0 && fair20[20] > 0, "Dice(20).roll() can land on 1 and on 20");

        // ---------- BiasedDice ----------
        System.out.println();
        System.out.println("===== BiasedDice =====");

        BiasedDice biased = new BiasedDice(6);
        doTest(true, biased instanceof Dice, "BiasedDice is a subclass of Dice");
        doTest(6, biased.getSides(), "new BiasedDice(6).getSides()");

        biased.setFavored(3, 0.5);
        doTest(3, biased.getFavored(), "setFavored(3, 0.5) then getFavored()");
        doTest(0.5, biased.getBias(), "setFavored(3, 0.5) then getBias()");

        double[] half = rollMany(biased);
        doTest(0.0, half[0], "BiasedDice(6).roll() always between 1 and 6 (fraction out of range)");
        doTest(true, close(0.5, half[3]),
               "favored 3, bias 0.5: lands on 3 about 50% of the time (was "
               + String.format("%.3f", half[3]) + ")");
        for (int side = 1; side <= 6; side++) {
            if (side != 3) {
                doTest(true, close(0.1, half[side]),
                       "favored 3, bias 0.5: lands on " + side + " about 10% of the time (was "
                       + String.format("%.3f", half[side]) + ")");
            }
        }

        biased.setFavored(6, 1.0);
        double[] always = rollMany(biased);
        doTest(1.0, always[6], "favored 6, bias 1.0: always lands on 6 (fraction)");

        biased.setFavored(1, 0.0);
        double[] never = rollMany(biased);
        doTest(0.0, never[1], "favored 1, bias 0.0: never lands on 1 (fraction)");
        doTest(true, close(0.2, never[4]),
               "favored 1, bias 0.0: lands on 4 about 20% of the time (was "
               + String.format("%.3f", never[4]) + ")");

        // A BiasedDice stored in a Dice variable should still use the biased roll()
        Dice polymorphic = new BiasedDice(4);
        ((BiasedDice) polymorphic).setFavored(2, 1.0);
        doTest(2, polymorphic.roll(), "Dice d = new BiasedDice(4) uses BiasedDice's roll()");

        // ---------- invalid setFavored values ----------
        // Invalid values should be rejected: either the old values are kept,
        // or an exception is thrown. Either way, the invalid values must not be stored.
        System.out.println();
        System.out.println("===== BiasedDice invalid input =====");

        BiasedDice checked = new BiasedDice(6);
        checked.setFavored(2, 0.3);

        tryInvalid(checked, 7, 0.3, "setFavored(7, 0.3) on 6-sided die (favored too big)");
        tryInvalid(checked, 0, 0.3, "setFavored(0, 0.3) (favored too small)");
        tryInvalid(checked, 4, 1.5, "setFavored(4, 1.5) (bias too big)");
        tryInvalid(checked, 4, -0.1, "setFavored(4, -0.1) (bias too small)");

        // ---------- summary ----------
        System.out.println();
        System.out.println(passed + " tests passed, " + failed + " tests failed");
        if (failed > 0) {
            System.out.println("Note: a percentage test can (rarely) fail by bad luck. "
                               + "If one fails, run again -- if it keeps failing, it's a bug.");
        }
    }

    /**
     * Calls setFavored with invalid values and checks that they were not stored.
     * Assumes the die currently has favored 2 and bias 0.3.
     */
    public static void tryInvalid(BiasedDice die, int favored, double bias, String message) {
        try {
            die.setFavored(favored, bias);
        } catch (Exception e) {
            // throwing an exception is an acceptable way to reject invalid input
        }
        doTest(2, die.getFavored(), message + " -- favored unchanged");
        doTest(0.3, die.getBias(), message + " -- bias unchanged");
    }
}
