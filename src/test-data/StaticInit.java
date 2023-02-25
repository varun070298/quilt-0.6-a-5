/**
 * StaticInit
 *
 * Make sure static initializers work.
 *
 * @author <a href="ddp@apache.org">David Dixon-Peugh</a>
 */

public class StaticInit implements org.quilt.cl.RunTest {
    static StaticInit _instance;
    static int value = -1;

    static {
        _instance = new StaticInit();
        value = _instance.getValue();
    }

    public StaticInit() { }

    public int getValue() {
        return 3;
    }

    public static StaticInit getInstance() {
        return _instance;
    }
    
    public int runTest(int y) {
        StaticInit si = getInstance();
        int x = si.getValue();
        return x + y;
    }
}
