/* BriefFormatter.java */

package org.quilt.reports;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.tools.ant.BuildException;

import org.quilt.framework.*;
import org.quilt.runner.Runner;

public class BriefFormatter extends BaseFormatter {

    /** No-arg constructor. */
    public BriefFormatter() {
        results = new StringWriter();
        resultWriter = new PrintWriter(results);
    }

    // INTERFACE FORMATTER //////////////////////////////////////////
    
    /**
     * End the test suite: overrides basic mehod.
     */
    public void endTestSuite(QuiltTest suite) throws BuildException {
        StringBuffer sb = new StringBuffer("Testsuite: " + suite.getName()
            + "\nTests run: "   + suite.runCount()
            + ", Failures: "    + suite.failureCount()
            + ", Errors: "      + suite.errorCount()
            + ", Time elapsed: " 
                + numberFormat.format( suite.getRunTime() / 1000.0) 
                + " sec\n\n" );

        if (systemOutput != null && systemOutput.length() > 0) {
            sb.append(
                  "------------- Standard Output ----------------\n"
                +  systemOutput
                + "------------- ---------------- ---------------\n");
        }

        if (systemError != null && systemError.length() > 0) {
            sb.append(
                  "------------- Standard Error -----------------\n"
                +  systemError
                + "------------- ---------------- ---------------\n");
        }

        if (output != null) {
            try {
                output.write(sb.toString());
                resultWriter.close();
                output.write(results.toString());
                output.flush();
            } finally {
                if (out != System.out &&
                        out != System.err) {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }
    
    // INTERFACE TESTLISTENER ///////////////////////////////////////
    /**
     * A test failed - overrides BasicFormattter method.
     */
    public void addFailure(Test test, Throwable t) {
        formatError("\tFAILED", test, t);
    }

    /**
     * A test failed - overrides BasicFormattter method.  Interface for 
     * JUnit &gt 3.4. 
     */
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }

    /**
     * A test caused an unexpected error.
     */
    public void addError(Test test, Throwable error) {
        formatError("\tCaused an ERROR", test, error);
    }

    // OTHER METHODS ////////////////////////////////////////////////

    /** Format an error and print it. */
    private synchronized void formatError(String msg, Test test,
                                            Throwable error) {
        if (test == null) {
            resultWriter.println("Null test: " + msg);
            
        } else {
            endTest(test);
            resultWriter.println("Testcase: " + test + ": " + msg);
        }
        resultWriter.println(error.getMessage());
        String strace = runner.getFilteredTrace(error, filtertrace);
        resultWriter.println(strace);
        resultWriter.println();
    }
}
