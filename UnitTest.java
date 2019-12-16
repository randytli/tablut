package tablut;

import org.junit.Test;

import static org.junit.Assert.*;

import ucb.junit.textui;

/**
 * The suite of all JUnit tests for the enigma package.
 *
 * @author Randy Li
 */
public class UnitTest {

    /**
     * Run the JUnit tests in this package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /**
     * A dummy test as a placeholder for real ones.
     */
    @Test
    public void test1() {
        assertTrue("11", 1 == 1);
    }

}


