/* TestStmtCoverage.java */
package org.quilt.cover.stmt;

import java.io.*;
import java.lang.Class;
import java.lang.reflect.*;
import java.net.*;

// DEBUG
import java.lang.reflect.Field;
import java.lang.reflect.Method;
// END

import junit.framework.*;
import org.apache.bcel.generic.*;
import org.quilt.cl.*;
import org.quilt.cover.stmt.*;

/**
 * This is derived from org.quilt.cl.TestTransformer; it instruments
 * the test-data classes and the classes synthesized by ClassFactory.
 * All of the instrumented classes are run and checked to see that
 * they produce normal output, despite the instrumentation ;-)
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class TestStmtCoverage extends TestCase {

    private ControlFlowGraph cfg;

    /** Classpath. */
    private URL [] cp = null;       // this is built up below

    private String[] delegating = {
        // NOTHING beyond standard defaults
    };
    // we want everything to be instrumented
    private String[] include = {
        "test.data.",
        "AnonymousClass",       "AnonymousClass2Catches",
        "BasicLoad",            "ComplicatedConstructor",
        "ExceptionLoad",        "ExceptionThrow",
        "Finally",              "Finally2Classes",
        "InnerClass",           "Looper",
        "NestedTryBlocks",      "OddSwitches",
        "PrivateClass",         "StaticInit",
        "SuperClass",           "SwitchLoad",
        "Wimple"
    };
    private String[] exclude = {
        // NOTHING
    };

    private GraphXformer spy;
    private GraphXformer talker, talker2;

    private StmtRegistry stmtReg;

    private QuiltClassLoader qLoader = null;

    public TestStmtCoverage (String name) {
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
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        qLoader = new QuiltClassLoader(
                        cp,
                        parent,             // parent
                        delegating,         // delegated classes
                        include,            // being instrumented
                        exclude);           // do NOT instrument

        // Graph Xformers /////////////////////////////////
        spy = new GraphSpy();
        qLoader.addGraphXformer(spy);

        // dumps graph BEFORE transformation
        talker  = new GraphTalker();
        qLoader.addGraphXformer(talker);

        // adds xformers for statement coverage to qLoader
        // EXPERIMENT
        stmtReg = (StmtRegistry) qLoader.addQuiltRegistry(
                                    "org.quilt.cover.stmt.StmtRegistry");
        // old way
        //stmtReg = new StmtRegistry(qLoader);
        //qLoader.addQuiltRegistry(stmtReg);

        // dumps graph AFTER transformation
        talker2 = new GraphTalker();
        qLoader.addGraphXformer(talker2);
    }

    // SUPPORT METHODS //////////////////////////////////////////////
    private RunTest loadAsRunTest (String name) {
        Class clazz = null;
        try {
            clazz = qLoader.loadClass(name);
//          // DEBUG -- trying to get fields causes an error
//          Field[]  fields  = clazz.getFields();
//          StringBuffer fieldData = new StringBuffer();
//          for (int k = 0; k < fields.length; k++)
//              fieldData.append("  ").append(fields[k].toString())
//                  .append("\n");

//          Method[] methods = clazz.getMethods();
//          StringBuffer methodData = new StringBuffer();
//          for (int k = 0; k < methods.length; k++)
//              methodData.append("  ").append(methods[k].toString())
//                  .append("\n");
//         
//          System.out.println("TestStmtRegistry: loading class " + name
//              + "\nFIELDS  (" + fields.length  + ") :\n" 
//                                              + fieldData.toString()
//              + "\nMETHODS (" + methods.length + ") :\n" 
//                                              + methodData.toString()  );
//          // END
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
    /**
     * Check that the class has been properly instrumented by
     * org.quilt.cover.stmt.  At the moment this means only that the
     * hitcount array, public static int[] q$$q is one of the class's
     * fields.  We also check that it has NOT been initialized,
     * although we will eventually make sure that it has been.
     */
    private void checkInstrumentation(Object rt) {
        String name = rt.getClass().getName();
        System.out.println("checkInstrumentation for class " + name);
        try {
            Field qField  = rt.getClass().getField("q$$q");
            if (qField == null) {
                System.out.println(name + " has no hit count array");
                fail(name + " has NO hit count array");
            } else try {
                int [] hitCounts = (int[]) qField.get(null);
                assertNotNull("q$$q has not been initialized", hitCounts);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            // check version
            qField  = rt.getClass().getField("q$$qVer");
            if (qField == null) {
                System.out.println(name + " has no version field");
                fail(name + " has NO version field");
            } else try {
                int version = qField.getInt(qField);
                assertEquals("q$$q has wrong version number", 0, version);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            // check StmtRegistry
            
        } catch (NoSuchFieldException e) {
            fail (name + " has no q$$q field");
        }
    }

    // ACTUAL TESTS /////////////////////////////////////////////////
    public void testGetReg() {
        StmtRegistry regInLoader = (StmtRegistry)qLoader.getRegistry(
                                    "org.quilt.cover.stmt.StmtRegistry");
        assertNotNull("qLoader StmtRegistry is null", regInLoader);
        assertSame ("qLoader has different StmtRegistry", 
                                                stmtReg, regInLoader);
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
    }  // END

//  /**
//   * Test classes from src/test-data. All of these (should) have a
//   * RunTest interface.  They are loaded and then run to see that
//   * they produce expected values.
//   *
//   * By and large the test inputs are primes greater than 2; in earlier
//   * tests some failed, returned 0, and were judged successful because
//   * 0 was the expected result.
//   *
//   * At the moment these are split into three groups, so that if they
//   * fail we will see more results.
//   */
    public void testInvokeTestData() {
        RunTest
        rt = loadAsRunTest("AnonymousClass");
        checkInstrumentation(rt);
        // AnonymousClass.runTest(x) returns x
        assertEquals ("AnonymousClass isn't working", 47, rt.runTest(47));

        rt = loadAsRunTest("BasicLoad");
        checkInstrumentation(rt);
        // BasicLoad.runTest(x) returns x*x
        assertEquals ("BasicLoad isn't working", 49, rt.runTest(7));

        rt = loadAsRunTest("ComplicatedConstructor");
        checkInstrumentation(rt);
        assertEquals("ComplicatedConstructor isn't working",
                                                    61, rt.runTest(3));
        rt = loadAsRunTest("ExceptionLoad");
        checkInstrumentation(rt);
        // ExceptionLoad.runTest(x) also returns x*x
        assertEquals ("ExceptionLoad isn't working", 121, rt.runTest(11));

        rt = loadAsRunTest("InnerClass");
        checkInstrumentation(rt);
        // InnerClass.runTest(x) also returns x*x
        assertEquals ("InnerClass isn't working", 9, rt.runTest(3));

        rt = loadAsRunTest("Looper");
        assertEquals("Looper isn't working", 127008000, rt.runTest(5));

//      rt = loadAsRunTest("OddSwitches");
//      // we like to play
//      assertEquals( 91, rt.runTest(1001));
//      assertEquals( 31, rt.runTest(3));
//      assertEquals(  9, rt.runTest(9));
//      assertEquals(101, rt.runTest(1005));
//      assertEquals(-41, rt.runTest(-1));
//      assertEquals( -3, rt.runTest(-51));
//      assertEquals(  7, rt.runTest(-2));

        rt = loadAsRunTest("PrivateClass");
        checkInstrumentation(rt);
        // returns 4
        assertEquals ("PrivateClass isn't working", 4, rt.runTest(7));

        rt = loadAsRunTest("SuperClass");
        checkInstrumentation(rt);
        // returns 3*x
        assertEquals ("SuperClass isn't working", 21, rt.runTest(7));

        // This would normally not be here, it would be at a higher
        // level in the testing process.  But we are testing the
        // registry itself.  XXX Collect the string and extract
        // run results from it.
        String runResults = stmtReg.getReport();
        System.out.println("\nQuilt coverage report:\n" + runResults);
    } // END TESTDATA

    // SYNTHESIZED CLASSES //////////////////////////////////////////
//  public void testSynth () {
//      assertEquals ("synthesizing isn't disabled in loader",
//              false, qLoader.getSynthEnabled() );
//      qLoader.setSynthEnabled(true);
//      assertEquals ("enabling synthesizing failed",
//              true, qLoader.getSynthEnabled() );

//      RunTest
//      rt = loadAsRunTest("test.data.TestDefault");
//      checkInstrumentation(rt);
//      // testDefault.runTest(x) returns 2 whatever the input is
//      assertEquals ("testDefault isn't working", 2, rt.runTest(47));
//      assertEquals ("testDefault isn't working", 2, rt.runTest(-7));
//        String
//      runResults = stmtReg.getReport();           /// <-----------
//      System.out.println("\nQuilt coverage report:\n" + runResults);

//      rt = loadAsRunTest("test.data.TestIfThen");
//      checkInstrumentation(rt);
//      // testIfThen.runTest(x) returns 3 if x > 0, 5 otherwise
//      assertEquals ("testIfThen isn't working", 3, rt.runTest(47));
//      assertEquals ("testIfThen isn't working", 5, rt.runTest(-7));

//      runResults = stmtReg.getReport();           /// <-----------
//      System.out.println("\nQuilt coverage report:\n" + runResults);

//      rt = loadAsRunTest("test.data.TestNPEWithCatch");
//      checkInstrumentation(rt);
//      // testNPEWithCatch.runTest(x) always returns 3
//      assertEquals ("testNPEWithCatch isn't working", 3, rt.runTest(47));
//      assertEquals ("testNPEWithCatch isn't working", 3, rt.runTest(-7));

//      rt = loadAsRunTest("test.data.TestNPENoCatch");
//      checkInstrumentation(rt);
//      // testNPENoCatch.runTest(x) always throws a NullPointerException
//      int x;
//      try {
//          x = rt.runTest(47);
//          fail ("testNPENoCatch didn't throw exception");
//      } catch (NullPointerException e) {
//          ; // ignore it
//      }
//      try {
//          x = rt.runTest(-7);
//          fail ("testNPENoCatch didn't throw exception");
//      } catch (NullPointerException e) {
//          ; // ignore it
//      }

//      rt = loadAsRunTest("test.data.TestSelect");
//      checkInstrumentation(rt);
//      // testSelect.runTest(x)  returns
//      //  1 if x == 1; 3 if x == 2; 5 if x == 3; 2 otherwise
//      assertEquals ("testSelect isn't working", 2, rt.runTest(47));
//      assertEquals ("testSelect isn't working", 2, rt.runTest(-7));
//      assertEquals ("testSelect isn't working", 1, rt.runTest(1));
//      assertEquals ("testSelect isn't working", 3, rt.runTest(2));
//      assertEquals ("testSelect isn't working", 5, rt.runTest(3));

//      rt = loadAsRunTest("test.data.TestWhile");
//      checkInstrumentation(rt);
//      // testWhile.runTest(x)  returns
//      //  0 if x >= 0, x otherwise
//      assertEquals ("testWhile isn't working", 0, rt.runTest(47));
//      assertEquals ("testWhile isn't working",-7, rt.runTest(-7));
//  }  // END999

//  /////////////////////////////////////////////////////////////////
//  // BUGS BUGS BUGS BUGS //////////////////////////////////////////
//  /////////////////////////////////////////////////////////////////
//
//  /////////////////////////////////////////////////////////////////
//  // LESSER BUGS //////////////////////////////////////////////////
//  // "edge not in this graph" -- FIXED (a bit crudely)
//  public void testNestedTryBlocks() {
//      RunTest
//      rt = loadAsRunTest("NestedTryBlocks");
//      checkInstrumentation(rt);
//      assertEquals ("NestedTryBlocks isn't working", 22, rt.runTest(7));
//  } // END NESTED
    
    /////////////////////////////////////////////////////////////////
    // SERIOUS BUGS /////////////////////////////////////////////////

//  // STATICINIT ///////////////////////////////////////////////////
//  // XXX BUG java.lang.NullPointerException
//  // at org.apache.bcel.generic.LineNumberGen
//  //                          .getLineNumber(LineNumberGen.java:109)
//  // at org.apache.bcel.generic.MethodGen
//  //                          .getLineNumberTable(MethodGen.java:420)
//  // at org.apache.bcel.generic.MethodGen.getMethod(MethodGen.java:599)
//  public void testStaticInit() {
//      RunTest
//      rt = loadAsRunTest("StaticInit");
//      checkInstrumentation(rt);
//      assertEquals("StaticInit isn't working",    10, rt.runTest(7));
//  } // END STATIC

//  // TESTFINALLY //////////////////////////////////////////////////
//  // XXX BUG Invalid start_pc/length in local var table BUG XXX
//  public void testFinally() {
//      RunTest
//      rt = loadAsRunTest("Finally");
//      checkInstrumentation(rt);

//      // Finally.runTest(x) returns -1
//      assertEquals ("Finally isn't working", -1, rt.runTest(11));
//      assertEquals ("Finally isn't working", -1, rt.runTest(1));
//  } // END FINALLY

//  // TESTFINALLY2CATCHES //////////////////////////////////////////
//  // XXX BUG Mismatched stack types BUG XXX
//  public void testFinally2Catches() {
//      RunTest
//      rt = loadAsRunTest("Finally2Catches");
//      checkInstrumentation(rt);

//      // what Finally.runTest(x) returns is a bit complicated ...
//      assertEquals ("Finally2Catches isn't working", 3600, rt.runTest(11));
//  } // END

//  // SWITCHLOAD
//  // XXX BUG Falling off the end of the code BUG XXX
//  public void testFinally2Catches() {
//      RunTest
//      rt = loadAsRunTest("SwitchLoad");
//      checkInstrumentation(rt);
//      // returns 42
//      assertEquals ("SwitchLoad isn't working", 42, rt.runTest(7));
//  } // END
        
//  // WIMPLE ///////////////////////////////////////////////////////
//  // XXX BUG java.lang.NullPointerException
//  // at org.apache.bcel.generic.LineNumberGen
//  //                          .getLineNumber(LineNumberGen.java:109)
//  // at org.apache.bcel.generic.MethodGen
//  //                          .getLineNumberTable(MethodGen.java:420)
//  // at org.apache.bcel.generic.MethodGen.getMethod(MethodGen.java:599)
//  public void testWimple() {
//      RunTest
//      rt = loadAsRunTest("Wimple");
//       checkInstrumentation(rt);
//      // returns ??
//      assertEquals ("Wimple isn't working", 92, rt.runTest(7));
//  } // END WIMPLE
    
}
