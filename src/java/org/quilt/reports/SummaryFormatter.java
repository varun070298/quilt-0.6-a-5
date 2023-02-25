/* SummaryFormatter.java */

// //////////////////////////////////////////////////////////////////
// MODIFY TO EXTEND BaseFormatter ? /////////////////////////////////
// //////////////////////////////////////////////////////////////////

package org.quilt.reports;

import java.text.NumberFormat;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.tools.ant.BuildException;

import org.quilt.framework.*;
import org.quilt.runner.Runner;

public class SummaryFormatter implements Formatter {

    private boolean filtertrace = false;
    private Runner runner = null;

    private NumberFormat nf = NumberFormat.getInstance();
    private OutputStream out;

    private boolean withOutAndErr = false;
    private String systemOutput = null;
    private String systemError = null;

    public SummaryFormatter() {}

    // FORMATTER INTERFACE //////////////////////////////////////////

    /** Called at the end of the Ant/Quilt test run. */
    public void endTestSuite(QuiltTest qt) throws BuildException {
        StringBuffer sb = new StringBuffer("Tests run: " + qt.runCount()
            + ", Failures: "    + qt.failureCount()
            + ", Errors: "      + qt.errorCount()
            + ", Time elapsed: "+ nf.format(qt.getRunTime() / 1000.0)
            + " sec\n" );

        if (withOutAndErr) {
            if (systemOutput != null && systemOutput.length() > 0) {
                sb.append("Output:\n" + systemOutput + "\n");
            }
            if (systemError != null && systemError.length() > 0) {
                sb.append("Error:\n " + systemError + "\n");
            }
        }

        try {
            out.write(sb.toString().getBytes());
            out.flush();
        } catch (IOException e) {
            throw new BuildException("Unable to write summary output", e);
        } finally {
            if (out != System.out && out != System.err) {
                try {
                    out.close();
                } catch (IOException e) {}
            }
        } 
    } 
    /** Enable filtering of Ant/Quilt/JUnit lines from stack traces. */
    public void setFiltertrace(boolean b) {
        filtertrace = b;            // NEVER USED 
    }
    /** Where to direct output. */
    public void setOutput(OutputStream out) {
        this.out = out;
    }
    /** Select the runner to be used. */
    public void setRunner(Runner testrunner) {
        runner = testrunner;
    }
    /** Where to send system error output. */
    public void setSystemError(String err) {
        systemError = err;
    }
    /** Where to send standard output. */
    public void setSystemOutput(String out) {
        systemOutput = out;
    }
    /** Called at start of Ant/Quilt test run. */
    public void startTestSuite(QuiltTest suite) {}

    // INTERFACE TESTLISTENER ///////////////////////////////////////
    /** An unexpected error occurred. */
    public void addError(Test test, Throwable t) {}
    /** A failure occurred. */
    public void addFailure(Test test, Throwable t) {}
    /** A failure occurred. */
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }
    /** Called at end of JUnit test. */
    public void endTest(Test test) {}
    /** Called at beginning of JUnit test. */
    public void startTest(Test t) {}
   
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Ant-compatible method determining whether System.out and 
     * System.err should be echoed to the summary report. 
     */
    public void setWithOutAndErr(boolean value) {
        withOutAndErr = value;
    }
}
