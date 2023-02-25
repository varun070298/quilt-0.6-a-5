/* PrivateClass.java */

/** Test Quilt's ability to change private access to public. */

class PrivateClass implements org.quilt.cl.RunTest {
    
    public PrivateClass() { }

    public int doSomething() {
        return 2;
    }

    public int runTest(int x) {
        int z = doSomething();
        return z * z;
    }
}
