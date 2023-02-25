/* BaseFormatter.java */

// //////////////////////////////////////////////////////////////////
// NEEDS DOCUMENTATION //////////////////////////////////////////////
// //////////////////////////////////////////////////////////////////

package org.quilt.reports;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;

import junit.framework.*;

import org.apache.tools.ant.BuildException;

import org.quilt.framework.*;
import org.quilt.runner.Runner;

/**
 * Basic test result formatter for Ant/Quilt/JUnit.
 */
public abstract class BaseFormatter implements Formatter {

    protected boolean         filtertrace     = true;
    protected NumberFormat    numberFormat    = NumberFormat.getInstance();
    protected OutputStream    out;
    protected PrintWriter     output;
    protected Runner          runner          = null;
    protected StringWriter    results;
    protected PrintWriter     resultWriter;
    protected String          systemError     = null;
    protected String          systemOutput    = null;

    /** 
     * Root around in a junit Test and find a getName method.
     *
     * Test may be TestCase or TestSuite; in either case there will be a
     * getName method in JUnit 3.7 or later.
     *
     * @return Test/suite name.
     */
    public static String getTestName (Test test) {
        if ( test instanceof TestSuite ) {
            return ( (TestSuite) test ) . getName();
        } else if ( test instanceof TestCase ) {
            return ( (TestCase) test ) . getName();
        } else {
            return "unknown";
        }
    }
    // INTERFACE FORMATTER //////////////////////////////////////////
    // Formatter extends JUnit TestListener; these are the extra ////
    // methods. /////////////////////////////////////////////////////
   
    /** Called at end of Quilt/JUnit test run. */
    public void endTestSuite(QuiltTest suite) throws BuildException {}
    
    /** Whether to filter Ant/Quilt/JUnit lines from stack traces. */
    public void setFiltertrace (boolean b) {
        filtertrace = b;
    }
    
    /** Where to send the report output. */
    public void setOutput(OutputStream out) {
        this.out = out;
        output = new PrintWriter(out);
    }
    
    /** Determines which test runner to use. */
    public void setRunner(Runner testrunner) {
        runner = testrunner;
    }
    
    /** Where to send system output. */
    public void setSystemOutput(String out) {
        systemOutput = out;
    }
    
    /** Where to send system error output. */
    public void setSystemError(String err) {
        systemError = err;
    }

    /** Called at the start of the Ant/Quilt/JUnit test run. */
    public void startTestSuite(QuiltTest suite) throws BuildException {}
    
    // INTERFACE TESTLISTENER ///////////////////////////////////////
    // Stubs for the JUnit TestListener methods /////////////////////
   
    /** A test caused an unexpected error. */
    public void addError(Test test, Throwable error) {}

    /** A test failed. */
    public void addFailure(Test test, Throwable t) {}
    
    /** A test failed. */
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }

    /** 
     * Method called at beginning of test. 
     * @param test JUnit Test (TestCase or TestSuite) 
     */
    public void startTest(Test test) { }
    /** 
     * Method called at end of test. 
     * @param test JUnit Test (TestCase or TestSuite) 
     */
    public void endTest(Test test) { }
}
