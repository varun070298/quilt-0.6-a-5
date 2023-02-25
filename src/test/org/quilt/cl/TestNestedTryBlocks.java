/* TestNestedTryBlocks.java */
package org.quilt.cl;

import java.io.*;
import java.lang.Class;
import java.lang.reflect.*;
import java.net.*;
import junit.framework.*;

/** 
 * A stripped-down TestTransformer. 
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class TestNestedTryBlocks extends TestCase {

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
    private QuiltClassLoader qLoader = null;

    private GraphXformer spy;
    private GraphXformer talker;
    public TestNestedTryBlocks (String name) {
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
        spy = new GraphSpy();
        qLoader.addGraphXformer(spy);
        talker = new GraphTalker();
        qLoader.addGraphXformer(talker);
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
    public void testNestedTries() {
        RunTest rt = loadAsRunTest("NestedTryBlocks");
        assertEquals ("NestedTryBlocks isn't working", 22, rt.runTest(7));
    }

}
