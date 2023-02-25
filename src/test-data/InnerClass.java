/* InnerClass.java */

/** Test class with a public inner class. */

public class InnerClass implements org.quilt.cl.RunTest {

    public class Inner implements MyListener {
        public Inner() { }
        public void setValue( String s ) { }
    }

    public void doSomething( MyListener listener ) {
        listener.setValue( "3" );
    }

    public int runTest(int x) {
        doSomething( new Inner() );
        return x*x;
    }

}
