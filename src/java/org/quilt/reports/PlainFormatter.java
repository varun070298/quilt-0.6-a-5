/* PlainFormatter */

package org.quilt.reports;

import org.apache.tools.ant.taskdefs.optional.junit.*; 
import org.apache.tools.ant.BuildException;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Hashtable;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.quilt.runner.Runner;
import org.quilt.framework.*;

/** Plain text output of JUnit test results. */

public class PlainFormatter extends BaseFormatter {

    private Hashtable testStarts = new Hashtable();
    private Hashtable failed = new Hashtable();

    /** No-arg constructor. */
    public PlainFormatter() {
        results = new StringWriter();
        resultWriter = new PrintWriter(results);
    }

    // INTERFACE FORMATTER //////////////////////////////////////////

    public void endTestSuite(QuiltTest qt) throws BuildException {
        StringBuffer sb = new StringBuffer("Testsuite: " + qt.getName() 
            + "\nTests run: " + qt.runCount()
            + ", Failures: "  + qt.failureCount()
            + ", Errors: "    + qt.errorCount()
            + ", Time elapsed: " + numberFormat.format( 
                                                qt.getRunTime() / 1000.0)
            + " sec\n");
        
        if (systemOutput != null && systemOutput.length() > 0) {
            sb.append(
                  "------------- Standard Output ----------------\n"
                + systemOutput
                + "------------- ---------------- ---------------\n");
        }
        
        if (systemError != null && systemError.length() > 0) {
            sb.append(
                  "------------- Standard Error -----------------\n"
                + systemError
                + "------------- ---------------- ---------------\n");
        }

        sb.append("\n");

        if (out != null) {
            try {
                out.write(sb.toString().getBytes());
                resultWriter.close();
                out.write(results.toString().getBytes());
                out.flush();
            } catch (IOException ioex) {
                throw new BuildException("Unable to write output", ioex);
            } finally {
                if (out != System.out && out != System.err) {
                    try {
                        out.close();
                    } catch (IOException e) {}
                }
            }
        }
    } 

    // INTERFACE TESTLISTENER ///////////////////////////////////////
    public void startTest(Test t) {
        testStarts.put(t, new Long(System.currentTimeMillis()));
        failed.put(t, Boolean.FALSE);
    }

    public void endTest(Test test) {
        synchronized (resultWriter) {
            // requires JUnit 3.7 or later
            resultWriter.print("Testcase: " + getTestName(test));

            // remember that the hash holds objects, not primitives
            if (Boolean.TRUE.equals(failed.get(test))) {
                return;
            }
            Long t0 = (Long) testStarts.get(test);
            double seconds = 0;
            // can be null if an error occured in setUp
            if (t0 != null) {
                seconds = 
                    (System.currentTimeMillis() - t0.longValue()) / 1000.0;
            }
            
            resultWriter.println(" took " 
                                + numberFormat.format(seconds) + " sec");
        }
    }
    public void addFailure(Test test, Throwable t) {
        formatError("\tFAILED", test, t);
    }
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }
    public void addError(Test test, Throwable t) {
        formatError("\tCaused an ERROR", test, t);
    }
    // OTHER METHODS ////////////////////////////////////////////////
    private void formatError(String type, Test test, Throwable t) {
        synchronized (resultWriter) {
            if (test != null) {
                endTest(test);
                failed.put(test, Boolean.TRUE);
            }

            resultWriter.println(type);
            resultWriter.println(t.getMessage());
            String strace = runner.getFilteredTrace(t, filtertrace);
            resultWriter.print(strace);
            resultWriter.println("");
        }
    }
} 
