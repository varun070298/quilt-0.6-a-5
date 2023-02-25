/**
 * Looper
 *
 * This has a Loop in it.
 *
 * @author < a href="ddp@apache.org">David Dixon-Peugh</a>
 */

public class Looper implements org.quilt.cl.RunTest {
    
    public Looper() { }
    public static int I = -10;
    
    public void doSomething( int i ) {
        I += i;
    }

    public void doSomethingElse( int i ) {
        I *= i;
    }

    public int runTest(int x) {
        int i;
        for (i = 0; i < 10; i++) {
            doSomething( i );
        }

        while (i > 0) {
            doSomethingElse( i );
            i--;
        }
        return I;
    }
}
