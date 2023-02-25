/* Finally.java */

import org.quilt.cl.RunTest;

/** A class with a finally clause in it. */

public class Finally implements RunTest {
    private int value = -2;
    
    public Finally() { }

    public void setValue( int i ) {
        value = i;
    }

    /** 
     * @return -1, because the finally clause is always executed
     */
    public int runTest(int x)  {
        try {
            if (x == 1) {
                throw new Exception("Finally Exception");
            }
        } catch ( Exception e) {
            ;
        } finally {
            setValue( -1 );
        }
        return value;
    }
}
