/* SuperClass.java */

/** Tests complex constructor. */

public class SuperClass implements org.quilt.cl.RunTest {

    private String value = "not initialized";

    public SuperClass( String v ) {
        value = v;
    }

    public SuperClass() {
        value = "SuperClass";
    }

    public String getValue() {
        return value;
    }

    public int runTest(int x)  {
        if (!getValue().equals("SuperClass"))
            throw new RuntimeException(
                                    "DefaultConstructor not called.");
        return 3*x;
    }
}
