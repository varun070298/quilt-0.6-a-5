/**
 * A class with two catches and a finally clause.
 *
 * @author < a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class Finally2Catches implements org.quilt.cl.RunTest {
    private int a, 
                b, 
                c;
    private int array[] = {0, 1, 2, 3, 4, 5};

    public Finally2Catches() {
    }

    public int runTest(int x) {
        a = 2;              // A: catch tracker
        b = 3;              // finally tracker
        c = 5;              // got-to-F
        int d;              // causes exceptions
        try {
            a *= a;         // B: in try block
            d = 1/x;        // ArithEx if x is 0
            d = array[x];   // out of bounds if x < 0 or x > 5
            if (x==2) {
                throw new IllegalArgumentException();
            }
        } catch (ArithmeticException e) {
            a *= a;         // C:
            throw e;
        } catch (ArrayIndexOutOfBoundsException e) {
            a *= a;         // D:
        } finally {
            b *= b;         // E:
        }
        c *= 5;             // F:
        return a*b*c;       // encodes path in powers of prime numbers
    }
}
