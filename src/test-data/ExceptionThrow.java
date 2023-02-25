/* ExceptionThrow.java */

/** Intentionally throws a NullPointerException.  */

public class ExceptionThrow {

    private static ExceptionThrow _instance = null;

    public void doSomething() { };

    public int runTest(int x) {
	    _instance.doSomething();
        return x*x;
    }
}
