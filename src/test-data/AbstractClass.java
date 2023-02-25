/**
 * AbstractClass
 * 
 * This class is used to make sure it can instrument partial classes.
 *
 * @author <a href="ddp@apache.org">David Dixon-Peugh</a>
 */

public abstract class AbstractClass {

    public int doSomething(int check) {
        if (check > 0) 
            return 1;
        return 0;
    }

    public abstract void runTest();
}
