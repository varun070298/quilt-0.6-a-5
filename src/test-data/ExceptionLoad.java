/* ExceptionLoad.java */

/** A class which catches an exception. */

public class ExceptionLoad implements org.quilt.cl.RunTest {

    public ExceptionLoad() { }

    public void doSomething() { }

    public int runTest(int x) {
        try {
            throw new Exception("Exception.");
        } catch (Exception e) {
            doSomething();
        }
        return x*x;
    }
}
