/* AnonymousClass.java */

import org.quilt.cl.RunTest;

public class AnonymousClass implements RunTest {

    public void doSomething( MyListener listener ) {
        listener.setValue( "3" );
    }

    public int runTest(int x) {
        doSomething( new MyListener() { 
            private String p;
            public void setValue( String v ) { 
                p = v; 
            } 
        } );
        return x;
    }
}
