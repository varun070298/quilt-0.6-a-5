/* TestSamples.java */
package org.quilt.cl;

import java.io.*;
import java.lang.Class;
import java.lang.reflect.*;
import java.net.*;
import junit.framework.*;

/** 
 * Checks to make sure that the samples in test-data will load
 * and execute correctly.  Some of them wouldn't ;-)
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class TestSamples extends TestCase {

    public TestSamples (String name) {
        super(name);
    }

    public void setUp () {
    }
    
    private RunTest loadAsRunTest (String name) {
        Class clazz = null;
        try {
            clazz = Class.forName (name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            fail ("exception loading " + name + " using loadClass");
        }
        RunTest rt  = null;
        try {
            rt =  (RunTest) clazz.newInstance();
        } catch ( InstantiationException e ) {
            fail ("InstantiationException instantiating loaded class " + name);
        } catch ( IllegalAccessException e ) {
            fail ("IllegalAccessException instantiating loaded class " + name);
        } catch (ClassCastException e) {
            fail ("ClassCastException instantiating loaded class " + name);
        }
        return rt;
    }
    public void testInvokeTestData() {
        RunTest rt = loadAsRunTest("AnonymousClass");
        // AnonymousClass.runTest(x) returns x
        assertEquals ("AnonymousClass isn't working", 47, rt.runTest(47));

        rt = loadAsRunTest("BasicLoad");
        // BasicLoad.runTest(x) returns x*x
        assertEquals ("BasicLoad isn't working", 49, rt.runTest(7));

        rt = loadAsRunTest("ComplicatedConstructor");
        assertEquals("ComplicatedConstructor isn't working", 
                                                    61, rt.runTest(3));
        // XXX causes ClassCastException when instantiating
        rt = loadAsRunTest("ExceptionLoad");
        // ExceptionLoad.runTest(x) also returns x*x
        assertEquals ("ExceptionLoad isn't working", 121, rt.runTest(11));
    }
    public void testInvokeTestData2() {
        RunTest rt = loadAsRunTest("Finally");
        // Finally.runTest(x) returns -1
        assertEquals ("Finally isn't working", -1, rt.runTest(11));
        assertEquals ("Finally isn't working", -1, rt.runTest(1));

        rt = loadAsRunTest("Finally2Catches");
        // what Finally.runTest(x) returns is a bit complicated ...
        assertEquals ("Finally2Catches isn't working", 3600, rt.runTest(11));

        rt = loadAsRunTest("InnerClass");
        // InnerClass.runTest(x) also returns x*x
        assertEquals ("InnerClass isn't working", 9, rt.runTest(3));

        rt = loadAsRunTest("Looper");
        assertEquals("Looper isn't working", 127008000, rt.runTest(5));

        rt = loadAsRunTest("NestedTryBlocks");
        assertEquals ("NestedTryBlocks isn't working", 22, rt.runTest(7));

        rt = loadAsRunTest("OddSwitches");
        // we like to play
        assertEquals( 91, rt.runTest(1001));
        assertEquals( 31, rt.runTest(3));
        assertEquals(  9, rt.runTest(9));
        assertEquals(101, rt.runTest(1005));
        assertEquals(-41, rt.runTest(-1));
        assertEquals( -3, rt.runTest(-51));
        assertEquals(  7, rt.runTest(-2));
    }

    public void testInvokeTestData3() {
        RunTest rt;
        try {
            Class clazz = Class.forName("PrivateClass");
            rt =  (RunTest) clazz.newInstance();
            fail ("Expected IllegalAccessException");
        } catch ( Exception e) {
            ;   // ignore it
        }

        rt = loadAsRunTest("StaticInit");
        assertEquals("StaticInit isn't working",    10, rt.runTest(7));

        rt = loadAsRunTest("SuperClass");
        // returns 3*x
        assertEquals ("SuperClass isn't working", 21, rt.runTest(7));
        
        rt = loadAsRunTest("SwitchLoad");
        assertEquals ("SwitchLoad isn't working", 42, rt.runTest(7));

    }
}
