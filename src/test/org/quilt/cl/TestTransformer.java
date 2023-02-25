/* TestTransformer.java */
package org.quilt.cl;

import java.io.*;
import java.lang.Class;
import java.lang.reflect.*;
import java.net.*;
import junit.framework.*;

/** 
 * A somewhat prettified TestQuiltClassLoader with transforming
 * enabled.
 *
 * XXX KNOWN PROBLEMS: NestedTryBlocks and SuperClass generate
 * control flow graphs with serious errors.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class TestTransformer extends TestCase {

    private URL [] cp = null;

    private String[] delegating = {
        // NOTHING beyond standard defaults
    };
    // we want everything to be instrumented 
    private String[] include = {
        "test.data.",
        "AnonymousClass",          "AnonymousClass2Catches",
        "BasicLoad",
        "ExceptionLoad",    "ExceptionThrow",
        "InnerClass",       "NestedTryBlocks",
        "PrivateClass",     "SuperClass",
        "Wimple"
    };
    private String[] exclude = {
        // NOTHING
    };

    private GraphXformer spy;
    private GraphXformer talker;
   
    private QuiltClassLoader qLoader = null;

    public TestTransformer (String name) {
        super(name);
    }


    public void setUp () {
        File sam1 = new File ("target/test-data-classes/");
        String fullPath1 = sam1.getAbsolutePath() + "/";
        File sam2 = new File ("target/classes");
        String fullPath2 = sam2.getAbsolutePath() + "/";
        File sam3 = new File ("target/test-classes");
        String fullPath3 = sam3.getAbsolutePath() + "/";
        try {
            // Terminating slash is required.  Relative paths don't
            // work.
            URL [] samples =  {
                new URL ( "file://" + fullPath1),
                new URL ( "file://" + fullPath2),
                new URL ( "file://" + fullPath3)
            };
            cp = samples;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            fail ("problem creating class path");
        }
        qLoader = new QuiltClassLoader(
                        cp,
                        null,       // parent
                        delegating, // delegated classes
                        include,    // being instrumented
                        exclude);   // do NOT instrument
       
        // this is sufficient to force graph generation
        spy = new GraphSpy();
        qLoader.addGraphXformer(spy);
        // this makes for a very verbose output
        // talker = new GraphTalker();
        // qLoader.addGraphXformer(talker);
    }
  public void testLoader() {
      Class a1 = null;
      try {
          a1 = qLoader.loadClass("AnonymousClass");
      } catch (ClassNotFoundException e) {
          e.printStackTrace();
          fail ("Error loading AnonymousClass using loadClass");
      }
      assertNotNull("qLoader returned null", a1);
  } 
 
    private RunTest loadAsRunTest (String name) {
        Class clazz = null;
        try {
            clazz = qLoader.loadClass(name);
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

        // XXX causes ClassCastException when instantiating
        rt = loadAsRunTest("ExceptionLoad");
        // ExceptionLoad.runTest(x) also returns x*x
        assertEquals ("ExceptionLoad isn't working", 121, rt.runTest(11));
    }
      public void testInvokeTestData2() {
        RunTest 
            rt = loadAsRunTest("Finally");
        // Finally.runTest(x) returns -1
        assertEquals ("Finally isn't working", -1, rt.runTest(11));
        assertEquals ("Finally isn't working", -1, rt.runTest(1));

        rt = loadAsRunTest("Finally2Catches");
        // what Finally.runTest(x) returns is a bit complicated ...
        assertEquals ("Finally2Catches isn't working", 3600, rt.runTest(11));

        rt = loadAsRunTest("InnerClass");
        // InnerClass.runTest(x) also returns x*x
        assertEquals ("InnerClass isn't working", 9, rt.runTest(3));

        rt = loadAsRunTest("NestedTryBlocks");
        assertEquals ("NestedTryBlocks isn't working", 22, rt.runTest(7));
    } 

    public void testInvokeTestData3() {
        RunTest 
        rt = loadAsRunTest("PrivateClass");
        // returns 4
        assertEquals ("PrivateClass isn't working", 4, rt.runTest(7));

        rt = loadAsRunTest("SuperClass");
        // returns 3*x
        assertEquals ("SuperClass isn't working", 21, rt.runTest(7));
        
        rt = loadAsRunTest("Wimple");
        // returns ??
        assertEquals ("Wimple isn't working", 92, rt.runTest(7)); 
    }

    public void testInvokeTestData4 () {
        RunTest
        rt = loadAsRunTest("ComplicatedConstructor");
        assertEquals("ComplicatedConstructor isn't working", 
                                                    61, rt.runTest(3));
        rt = loadAsRunTest("Looper");
        assertEquals("Looper isn't working", 127008000, rt.runTest(5));

        rt = loadAsRunTest("StaticInit");
        assertEquals("StaticInit isn't working",    10, rt.runTest(7));
    }

    // test all synthesized classes
    public void testSynth () {
        assertEquals ("synthesizing isn't disabled in loader",
                false, qLoader.getSynthEnabled() );
        qLoader.setSynthEnabled(true);
        assertEquals ("enabling synthesizing failed",
                true, qLoader.getSynthEnabled() );

        RunTest rt = loadAsRunTest("test.data.TestDefault");
        // testDefault.runTest(x) returns 2 whatever the input is
        assertEquals ("testDefault isn't working", 2, rt.runTest(47));
        assertEquals ("testDefault isn't working", 2, rt.runTest(-7));

        rt = loadAsRunTest("test.data.TestIfThen");
        // testIfThen.runTest(x) returns 3 if x > 0, 5 otherwise
        assertEquals ("testIfThen isn't working", 3, rt.runTest(47));
        assertEquals ("testIfThen isn't working", 5, rt.runTest(-7));

        rt = loadAsRunTest("test.data.TestNPEWithCatch");
        // testNPEWithCatch.runTest(x) always returns 3
        assertEquals ("testNPEWithCatch isn't working", 3, rt.runTest(47));
        assertEquals ("testNPEWithCatch isn't working", 3, rt.runTest(-7));

        rt = loadAsRunTest("test.data.TestNPENoCatch");
        // testNPENoCatch.runTest(x) always throws a NullPointerException
        int x;
        try {
            x = rt.runTest(47);
            fail ("testNPENoCatch didn't throw exception");
        } catch (NullPointerException e) {
            ; // ignore it
        }
        try {
            x = rt.runTest(-7);
            fail ("testNPENoCatch didn't throw exception");
        } catch (NullPointerException e) {
            ; // ignore it
        }

        rt = loadAsRunTest("test.data.TestSelect");
        // testSelect.runTest(x)  returns
        //  1 if x == 1; 3 if x == 2; 5 if x == 3; 2 otherwise
        assertEquals ("testSelect isn't working", 2, rt.runTest(47));
        assertEquals ("testSelect isn't working", 2, rt.runTest(-7));
        assertEquals ("testSelect isn't working", 1, rt.runTest(1));
        assertEquals ("testSelect isn't working", 3, rt.runTest(2));
        assertEquals ("testSelect isn't working", 5, rt.runTest(3));

        rt = loadAsRunTest("test.data.TestWhile");
        // testWhile.runTest(x)  returns
        //  0 if x >= 0, x otherwise
        assertEquals ("testWhile isn't working", 0, rt.runTest(47));
        assertEquals ("testWhile isn't working",-7, rt.runTest(-7));
    } 

}
