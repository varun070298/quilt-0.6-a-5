/**
 * OddSwitches
 *
 * @author < a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class OddSwitches implements org.quilt.cl.RunTest {
    
    public OddSwitches() { }

    // called for values >= 1000
    private int fallsThrough ( int i ) {
       switch (i) {
            case 1001:
            case 1003:
                return 91;
            case 1005:
            default:
                return 101;
       } 
    }

    // positive values < 1000
    private int noDefault (int i) {
        switch (i) {
            case 3:     return 31;
            case 5:     return 53;
            case 7:     return 71;
        }
        int x = 9;
        return x;
    }

    // values < 0
    private int oddDefault(int i) {
        switch (i) {
            case -1:    return -41;
            default:    return -3;
            case -2:    return 7;
        }
    }

    public int runTest(int x) {
        if (x >= 1000) 
            return fallsThrough (x);
        else if (x >= 0) 
            return noDefault(x);
        else 
            return oddDefault(x);
    }
}
