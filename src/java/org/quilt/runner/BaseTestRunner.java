/* BaseTestRunner.java */

package org.quilt.runner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import junit.framework.*;

import org.quilt.cl.*;
import org.quilt.framework.QuiltTest;
import org.quilt.reports.*;

/**
 * Stand-along Quilt test runner, fully compatible with Ant's JUnit
 * options.  Accepts options from Ant via QuiltTask; can also be run
 * from the command line using TestRunner.
 *
 * @see QuiltTask
 * @see QuiltTest
 * @see TestRunner
 */

public class BaseTestRunner extends Runner {

    /** The QuiltTest we are currently running. */
    private QuiltTest qt;

    /** The JUnit test suite for this QuiltTest. Built by the constructor.*/
    private Test suite = null;

    /** Exception caught in constructor. */
    private Exception exception = null;

    /** Status code returned by main() */
    private int retCode = SUCCESS;

    /** JUnit test result - which has the run() method. */
    private TestResult res;

    // RUN PARAMETERS ///////////////////////////////////////////////

    /** Formatters for this particular test. */
    private Vector formatters = new Vector();

    /** Do we stop on errors? */
//  private boolean haltOnError = false;

    /** Do we stop on test failures? */
//  private boolean haltOnFailure = false;

    /**
     * Do we send output to System.out/.err as well as to the formatters.
     * XXX BUG or inconsistency in documentation.
     */
//  private boolean showOutput = false;

    /** Error output during the test */
    private PrintStream systemError;

    /** Output written during the test */
    private PrintStream systemOut;

    // CONSTRUCTORS /////////////////////////////////////////////////

    /**
     * Constructor used by command line test runner.  And so also
     * used when Ant forks the test runner.  XXX No longer true.
     *
     * @param test Data structure holding parameters for a single
     *            test suite.
     */
    public BaseTestRunner (QuiltTest test) {
        this (test, null);
    }
    /**
     * Constructor used when not using Quilt class loader. XXX Not true.
     * Uses BaseTestRunner.retCode to signal whether construction
     * successful or not.  If the operation fails, the exception
     * involved is passed back in BaseTestRunner.exception.
     *
     * @param test   Data structure holding parameters for a single
     *               test suite.
     * @param loader Class loader passed from parent.
     */
    public BaseTestRunner(QuiltTest test, ClassLoader loader) {
        qt = test;

        try {
            Class testClass = null;
            if (loader == null) {
                testClass = Class.forName(qt.getName());
            } else {
                testClass = loader.loadClass(qt.getName());
                if (!(loader instanceof QuiltClassLoader)) {
                    // trick JVM into initializing class statics
                    AntClassLoader.initializeClass(testClass);
                }
            }

            Method suiteMethod = null;
            try {
                // check if there is a no-arg "suite" method in the class
                suiteMethod = testClass.getMethod("suite", new Class[0]);
            } catch (Exception e) {
                // not found
            }
            if (suiteMethod != null){
                // we somehow have a suiteMethod; try to use it to
                // extract the suite
                suite = (Test) suiteMethod.invoke(null, new Class[0]);
            } else {
                // use the JUnit TestSuite constructor to extract a
                // test suite
                suite = new TestSuite(testClass);
            }

        } catch (Exception e) {
            retCode = ERRORS;
            exception = e;
        }
    }
//  /**
//   * Constructor using Quilt class loader.  Uses
//   * BaseTestRunner.retCode to signal whether construction
//   * successful or not.  If the operation fails, the exception
//   * involved is passed back in BaseTestRunner.exception.
//   *
//   * @param test   Data structure holding parameters for a single
//   *               test suite.
//   * @param loader QuiltClassLoader passed from parent.
//   */
//  public BaseTestRunner(QuiltTest test, QuiltClassLoader loader) {
//      this.qt = test;

//      try {
//          Class testClass = null;
//          if (loader == null) {
//              testClass = Class.forName(test.getName());
//          } else {
//              testClass = loader.loadClass(test.getName());
//              // trick JVM into initializing class statics
//              AntClassLoader.initializeClass(testClass);
//          }
//
//          Method suiteMethod = null;
//          try {
//              // check if there is a no-arg "suite" method in the class
//              suiteMethod = testClass.getMethod("suite", new Class[0]);
//          } catch (Exception e) {
//              // not found
//          }
//          if (suiteMethod != null){
//              // if there is a suite method available, then try
//              // to extract the suite from it. If there is an error
//              // here it will be caught below and reported.
//              suite = (Test) suiteMethod.invoke(null, new Class[0]);
//          } else {
//              // use the JUnit TestSuite constructor to extract a
//              // test suite
//              suite = new TestSuite(testClass);
//          }
//
//      } catch (Exception e) {
//          retCode = ERRORS;
//          exception = e;
//      }
//  } // GEEP

    public void run() {
        res = new TestResult();
        res.addListener(this);
        for (int i = 0; i < formatters.size(); i++) {
            res.addListener((TestListener) formatters.elementAt(i));
        }

        long start = System.currentTimeMillis();

        fireStartTestSuite();
        if (exception != null) { // had an exception in the constructor
            for (int i = 0; i < formatters.size(); i++) {
                ((TestListener) formatters.elementAt(i)).addError(null,
                                                                 exception);
            }
            qt.setCounts(1, 0, 1);
            qt.setRunTime(0);
        } else {


            ByteArrayOutputStream errStrm = new ByteArrayOutputStream();
            systemError = new PrintStream(errStrm);

            ByteArrayOutputStream outStrm = new ByteArrayOutputStream();
            systemOut = new PrintStream(outStrm);

            PrintStream savedOut = null;
            PrintStream savedErr = null;

            if ( qt.getFork() ) {
                savedOut = System.out;
                savedErr = System.err;
                if (!qt.getShowOutput()) {
                    System.setOut(systemOut);
                    System.setErr(systemError);
                } else {
                    System.setOut(new PrintStream(
                                      new TeeOutputStream(
                                          new OutputStream[] {savedOut,
                                                              systemOut}
                                          )
                                      )
                                  );
                    System.setErr(new PrintStream(
                                      new TeeOutputStream(
                                          new OutputStream[] {savedErr,
                                                              systemError}
                                          )
                                      )
                                  );
                }
            }


            try {
                suite.run(res);
            } finally {
                if (savedOut != null) {
                    System.setOut(savedOut);
                }
                if (savedErr != null) {
                    System.setErr(savedErr);
                }

                systemError.close();
                systemError = null;
                systemOut.close();
                systemOut = null;
                sendOutAndErr(new String(outStrm.toByteArray()),
                              new String(errStrm.toByteArray()));

                qt.setCounts(res.runCount(), res.failureCount(),
                                    res.errorCount());
                qt.setRunTime(System.currentTimeMillis() - start);
            }
        }
        fireEndTestSuite();

        if (retCode != SUCCESS || res.errorCount() != 0) {
            retCode = ERRORS;
        } else if (res.failureCount() != 0) {
            retCode = FAILURES;
        }
    }

    /**
     * Get status code from run.
     *
     * @return Status codes from RunnerConst
     */
    public int getRetCode() {
        return retCode;
    }

    /////////////////////////////////////////////////////////////////
    // INTERFACE TESTLISTENER
    //
    // NEEDS TO BE CHECKED CAREFULLY
    /////////////////////////////////////////////////////////////////

    /** Called at start of test run. */
    public void startTest(Test t) {}

    /** Called at end of test suite. */
    public void endTest(Test test) {}

    /** A test failure (or error) has occurred. */
    public void addFailure(Test test, Throwable t) {
        if (qt.getHaltOnFailure()) {
            res.stop();
        }
    }

    /** A test failure (or error) has occurred. */
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }

    /** An unexpected error occurred. */
    public void addError(Test test, Throwable t) {
        if (qt.getHaltOnError()) {
            res.stop();
        }
    }

    /** Handle a block of output. */
    public void handleOutput(String line) {
        if (systemOut != null) {
            systemOut.println(line);
        }
    }

    /** Process an error message. */
    public void handleErrorOutput(String line) {
        if (systemError != null) {
            systemError.println(line);
        }
    }
    /** Flush standard output. */
    public void handleFlush(String line) {
        if (systemOut != null) {
            systemOut.print(line);
        }
    }
    /** Flush error output. */
    public void handleErrorFlush(String line) {
        if (systemError != null) {
            systemError.print(line);
        }
    }
    /**
     * Whether to duplicate test output to standard output and error
     * output streams.
     */
    private void sendOutAndErr(String out, String err) {
        for (int i = 0; i < formatters.size(); i++) {
            Formatter formatter =
                ((Formatter) formatters.elementAt(i));

            formatter.setSystemOutput(out);
            formatter.setSystemError(err);
        }
    }

    /** Notifies each formatter at start of processing test suite. */
    private void fireStartTestSuite() {
        for (int i = 0; i < formatters.size(); i++) {
            ((Formatter) formatters.elementAt(i))
                .startTestSuite(qt);
            // actually has nothing to do with fireStart but it's
            // convenient to drop it in here
            ((Formatter) formatters.elementAt(i))
                .setFiltertrace(qt.getFiltertrace());
        }
    }
    /** Called at end of test suite, notifies each formatter. */
    private void fireEndTestSuite() {
        for (int i = 0; i < formatters.size(); i++) {
            ((Formatter) formatters.elementAt(i))
                .endTestSuite(qt);
        }
    }

    /** Add a result formatter */
    public void addFormatter(Formatter f) {
        formatters.addElement(f);
    }

    // OTHER METHODS ////////////////////////////////////////////////
    private class TeeOutputStream extends OutputStream {

        private OutputStream[] outs;

        private TeeOutputStream(OutputStream[] outs) {
            this.outs = outs;
        }

        public void write(int b) throws IOException {
            for (int i = 0; i  < outs.length; i++) {
                outs[i].write(b);
            }
        }
    }
}
