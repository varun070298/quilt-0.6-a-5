/* TestQuiltClassLoader.java */
package org.quilt.cl;

import java.io.*;
import java.lang.Class;
import java.lang.reflect.*;
import java.net.*;
import junit.framework.*;

/** 
 * Tests the basic loadClass/findClass capabilities of the 
 * QuiltClassLoader, without transforming any classes.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class TestQuiltClassLoader extends TestCase {

    private URL [] cp = null;

    // all of these arrays are checked in testConstructor(), so if
    // you change them here, change the test too
    private String[] delegating = {
        "org.quilt"
    };
    private String[] include = {
        // EMPTY -- nothing should be instrumented
    };
    private String[] exclude = {
        "AnonymousClass",          "AnonymousClass2Catches",
        "InnerClass",       "NestedTryBlocks",
        "PrivateClass",     "SuperClass",
        "Wimple"
    };
    private QuiltClassLoader qLoader = null;

    public TestQuiltClassLoader (String name) {
        super(name);
    }

    public void setUp () {
        File sam1 = new File ("target/test-data-classes/");
        File sam2 = new File ("target/classes");
        File sam3 = new File ("target/test-classes");

//      String fullPath1 = sam1.getAbsolutePath() + "/";
//      String fullPath2 = sam2.getAbsolutePath() + "/";
//      String fullPath3 = sam3.getAbsolutePath() + "/";
//      try {
//          // Terminating slash is required.  Relative paths don't
//          // work.
//          URL [] samples =  {
//              new URL ( "file://" + fullPath1),
//              new URL ( "file://" + fullPath2),
//              new URL ( "file://" + fullPath3)
//          };
//          cp = samples;
//      } catch (MalformedURLException e) {
//          e.printStackTrace();
//          fail ("problem creating class path");
//      } // GEEP

        qLoader = new QuiltClassLoader(
                        QuiltClassLoader.cpToURLs( 
                                        sam1 + ":" + sam2 + ":" + sam3),
                        null,       // parent
                        delegating, // delegated classes
                        include,    // being instrumented
                        exclude);   // do NOT instrument
    }
    public void testDomainToFileName() {
        assertEquals ("../my.jar", 
                QuiltClassLoader.domainToFileName("../my.jar"));
        assertEquals ("../../target/my.jar", 
                QuiltClassLoader.domainToFileName("../../target/my.jar"));
        assertEquals ("../../../target/my.jar", 
                QuiltClassLoader.domainToFileName("../../../target/my.jar"));
        assertEquals ("ab/cd/ef/gh", 
                QuiltClassLoader.domainToFileName("ab.cd.ef.gh"));
        assertEquals ("./abc/def", 
                QuiltClassLoader.domainToFileName("./abc.def"));
        assertEquals ("./.././abc", 
                QuiltClassLoader.domainToFileName("./.././abc"));
        // ignore pathological cases like .abc
    }
    // NEEDS ELABORATION
    public void testConstructor() {
        assertNotNull("Error creating Quilt class loader", qLoader);
        URL[]    cp2  = qLoader.getClassPath();
        assertEquals("wrong size classpath", 3, cp2.length);

        String[] del2 = qLoader.getDelegated();
        assertEquals ("wrong number of delegated classes",
                                delegating.length
                                    + QuiltClassLoader.DELEGATED.length,
                                del2.length);

        String[] exc2 = qLoader.getExcluded();
        assertEquals ("wrong number of excluded classes",
                                exclude.length, exc2.length);

        String[] inc2 = qLoader.getIncluded();
        assertEquals ("wrong number of delegated classes",
                                include.length, inc2.length);
    }
    public void testLoader() {
        Class a1 = null;
        Class a2 = null;
        try {
            a1 = qLoader.loadClass("AnonymousClass");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            fail ("Error loading AnonymousClass using loadClass");
        }
        try {
            a2 = qLoader.loadClass("AnonymousClass");
        } catch (ClassNotFoundException e) {
            fail ("Error loading AnonymousClass using loadClass");
        }
        assertNotNull("qLoader returned null", a1);
        assertNotNull("qLoader returned null", a2);
        assertEquals ("second load returned a different class",
                (Object)a1, (Object)a2);
    }
    public void testParentage() {
        qLoader.setSynthEnabled(true);
        Class a1 = null;
        Class a2 = null;
        try {
            a1 = qLoader.loadClass("AnonymousClass");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            fail ("Error loading AnonymousClass using loadClass");
        }
        try {
            a2 = qLoader.loadClass("test.data.TestDefault");
        } catch (ClassNotFoundException e) {
            fail ("Error loading test.data.TestDefault using loadClass");
        }
        assertNotNull("qLoader returned null", a1);
        assertNotNull("qLoader returned null", a2);

        assertEquals ("AnonymousClass has wrong parent", 
                                        qLoader, a1.getClassLoader());
        assertEquals ("testDefault has wrong parent", 
                                        qLoader, a2.getClassLoader());
        // exploratory
        assertEquals ("qLoader's parent is not my parent",
                                this.getClass().getClassLoader(),
                                qLoader.getClass().getClassLoader() );
    }
  
    public void testInvokeTestData() {
        Class a1 = null;
        Class a2 = null;
        try {
            a1 = qLoader.loadClass("AnonymousClass");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            fail ("Error loading AnonymousClass using loadClass");
        }
        try {
            a2 = qLoader.loadClass("BasicLoad");
        } catch (ClassNotFoundException e) {
            fail ("Error loading BasicLoad using loadClass");
        }
        RunTest rt = null;
        // exercise AnonymousClass ////////////////////////
        try {
            rt =  (RunTest) a1.newInstance();
        } catch ( InstantiationException e ) {
            fail ("error instantiating loaded AnonymousClass");
        } catch ( IllegalAccessException e ) {
            fail ("error instantiating loaded AnonymousClass");
        }
        // AnonymousClass.runTest(x) returns x
        assertEquals ("AnonymousClass isn't working", 47, rt.runTest(47));

        // exercise BasicLoad ////////////////////////
        try {
            rt =  (RunTest) a2.newInstance();
        } catch ( InstantiationException e ) {
            fail ("error instantiating loaded BasicLoad");
        } catch ( IllegalAccessException e ) {
            fail ("error instantiating loaded BasicLoad");
        }
        // BasicLoad.runTest(x) returns x*x
        assertEquals ("BasicLoad isn't working", 49, rt.runTest(7));
    }

    public void testSynth () {
        assertEquals ("synthesizing isn't disabled in loader",
                false, qLoader.getSynthEnabled() );
        qLoader.setSynthEnabled(true);
        assertEquals ("enabling synthesizing failed",
                true, qLoader.getSynthEnabled() );

        Class a1 = null;
        Class a2 = null;
        try {
            a1 = qLoader.loadClass("test.data.TestDefault");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            fail ("Error loading Default");
        }
        try {
            a2 = qLoader.loadClass("test.data.TestIfThen");
        } catch (ClassNotFoundException e) {
            fail ("Error loading testIfThen using loadClass");
        }
        RunTest rt = null;
        // exercise testDefault ///////////////////////////
        try {
            rt =  (RunTest) a1.newInstance();
        } catch ( InstantiationException e ) {
            fail ("error instantiating loaded testDefault");
        } catch ( IllegalAccessException e ) {
            fail ("error instantiating loaded testDefault");
        }
        // testDefault.runTest(x) returns 2 whatever the input is
        assertEquals ("testDefault isn't working", 2, rt.runTest(47));
        assertEquals ("testDefault isn't working", 2, rt.runTest(-7));

        // exercise testIfThen ////////////////////////////
        try {
            rt =  (RunTest) a2.newInstance();
        } catch ( InstantiationException e ) {
            fail ("error instantiating loaded testIfThen");
        } catch ( IllegalAccessException e ) {
            fail ("error instantiating loaded testIfThen");
        }
        // testIfThen.runTest(x) returns 3 if x > 0, 5 otherwise
        assertEquals ("testIfThen isn't working", 3, rt.runTest(47));
        assertEquals ("testIfThen isn't working", 5, rt.runTest(-7));

        Class xNoCatch   = null;
        Class xWithCatch = null;
        Class xSelect    = null;
        Class xWhile     = null;
        try {
            xNoCatch    = qLoader.loadClass("test.data.TestNPENoCatch");
            xWithCatch  = qLoader.loadClass("test.data.TestNPEWithCatch");
            xSelect     = qLoader.loadClass("test.data.TestSelect");
            xWhile      = qLoader.loadClass("test.data.TestWhile");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            fail ("Error loading synthesized class");
        }

        // exercise testNPEWithCatch //////////////////////
        try {
            rt =  (RunTest) xWithCatch.newInstance();
        } catch ( InstantiationException e ) {
            fail ("error instantiating loaded testNPEWithCatch");
        } catch ( IllegalAccessException e ) {
            fail ("error instantiating loaded testNPEWithCatch");
        }
        // testNPEWithCatch.runTest(x) always returns 3
        assertEquals ("testNPEWithCatch isn't working", 3, rt.runTest(47));
        assertEquals ("testNPEWithCatch isn't working", 3, rt.runTest(-7));

        // exercise testNPENoCatch ////////////////////////
        try {
            rt =  (RunTest) xNoCatch.newInstance();
        } catch ( InstantiationException e ) {
            fail ("error instantiating loaded testNPENoCatch");
        } catch ( IllegalAccessException e ) {
            fail ("error instantiating loaded testNPENoCatch");
        }
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

        // exercise testSelect ////////////////////////////
        try {
            rt =  (RunTest) xSelect.newInstance();
        } catch ( InstantiationException e ) {
            fail ("error instantiating loaded testSelect");
        } catch ( IllegalAccessException e ) {
            fail ("error instantiating loaded testSelect");
        }
        // testSelect.runTest(x)  returns
        //  1 if x == 1; 3 if x == 2; 5 if x == 3; 2 otherwise
        assertEquals ("testSelect isn't working", 2, rt.runTest(47));
        assertEquals ("testSelect isn't working", 2, rt.runTest(-7));
        assertEquals ("testSelect isn't working", 1, rt.runTest(1));
        assertEquals ("testSelect isn't working", 3, rt.runTest(2));
        assertEquals ("testSelect isn't working", 5, rt.runTest(3));

        // exercise testWhile /////////////////////////////
        try {
            rt =  (RunTest) xWhile.newInstance();
        } catch ( InstantiationException e ) {
            fail ("error instantiating loaded testWhile");
        } catch ( IllegalAccessException e ) {
            fail ("error instantiating loaded testWhile");
        }
        // testWhile.runTest(x)  returns
        //  0 if x >= 0, x otherwise
        assertEquals ("testWhile isn't working", 0, rt.runTest(47));
        assertEquals ("testWhile isn't working",-7, rt.runTest(-7));

    }
    public void testParentage2() {
        qLoader.setSynthEnabled(true);
    }
    public void testSetters() {
        qLoader.setClassPath("abc.jar:def:ghi.a.b.c:froggy.pickle.jar");
        URL[] urls = qLoader.getClassPath();

        assertNotNull("classpath is null", urls);
        assertEquals ("wrong number of URLs in classpath", 4, urls.length);
        assertTrue(urls[0].getFile().endsWith("abc.jar"));
        assertTrue(urls[1].getPath().endsWith("def/"));
        assertTrue(urls[2].getPath().endsWith("ghi/a/b/c/"));
        // path and file seem to be identical
        assertTrue(urls[3].getPath().endsWith("froggy/pickle.jar"));
        assertTrue(urls[3].getFile().endsWith("froggy/pickle.jar"));
                
        qLoader.setExcluded("this,that,theOther");
        String[] exc = qLoader.getExcluded();
        assertEquals("wrong excluded count", 3, exc.length);
        assertEquals("this",     exc[0]);
        assertEquals("theOther", exc[2]);

        qLoader.setIncluded("eenie,meenie,minee,mo");
        String[] inc = qLoader.getIncluded();
        assertEquals("wrong included count", 4, inc.length);
        assertEquals("eenie", inc[0]);
        assertEquals("mo",    inc[3]);
    }
}
