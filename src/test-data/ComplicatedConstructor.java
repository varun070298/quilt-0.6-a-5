/**
 * ComplicatedConstructor
 *
 * This class requires a call to Super.
 *
 * @author <a href="ddp@apache.org">David Dixon-Peugh</a>
 */

public class ComplicatedConstructor extends SuperClass {
    
    public ComplicatedConstructor() {
        super("ComplexConstructor");
    }

    public int runTest(int x) {
        if (getValue().equals("SuperConstructor")) {
            throw new RuntimeException(
                                "Default SuperClass Constructor called.");
        } else if (!getValue().equals("ComplexConstructor")) {
            throw new RuntimeException(
                                "default constructor returned " + getValue());
        }

        return 61;
    }
}
