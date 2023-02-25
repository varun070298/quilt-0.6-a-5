/* BasicLoad.java */

/**
 * This file is done separately to make sure it will not be found on 
 * the classpath.
 *
 * It needs to be findable by the system class loader as a resource.
 */

public class BasicLoad implements org.quilt.cl.RunTest {

    public int field0 = 6;
    public static final int FIELD1 = 1;

    public BasicLoad() { }

    public void doSomething() { field0++; }
    public void doSomethingElse() { doSomething(); }
    public void doException() throws Exception { }

    public int runTest(int x) {
        doSomething();
        doSomethingElse();
        return x*x;
    }
}
