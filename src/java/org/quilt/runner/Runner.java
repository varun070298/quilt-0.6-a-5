/* Runner.java */

package org.quilt.runner;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.*;
import org.apache.tools.ant.util.StringUtils;

import org.quilt.framework.QuiltTest;
import org.quilt.reports.Formatter;

/** 
 * Abstract class extended by Quilt test runners.
 */
public abstract class Runner extends QuiltTest 
                                    implements RunnerConst, TestListener {

    public abstract void run ();    // constructor provides QuiltTest instance
    public abstract int getRetCode();

    // STATIC METHODS ///////////////////////////////////////////////
    public static String getFilteredTrace(Throwable t, boolean filtertrace) {
        String trace = StringUtils.getStackTrace(t);
        return filterStack(trace, filtertrace);
    }

    /////////////////////////////////////////////////////////////////
    // FOCUS  -- LOOK AT THIS CAREFULLY
    /////////////////////////////////////////////////////////////////
    
    /**
     * Filters stack traces from Ant, Quilt, and JUnit classes.  These
     * are usually meaningless to users, who wants to concentrate on
     * their own code.
     */
    public static String filterStack(String stack, boolean filtertrace) {
        if ( ! filtertrace ) {
            return stack;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StringReader sr = new StringReader(stack);
        BufferedReader br = new BufferedReader(sr);

        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (!filterLine(line)) {
                    pw.println(line);
                }
            }
        } catch (Exception IOException) {
            return stack; // return the stack unfiltered
        }
        return sw.toString();
    }

    private static boolean filterLine(String line) {
        for (int i = 0; i < DEFAULT_TRACE_FILTERS.length; i++) {
            if (line.indexOf(DEFAULT_TRACE_FILTERS[i]) > 0) {
                return true;
            }
        }
        return false;
    }
    // NON-STATIC METHODS ///////////////////////////////////////////
    /** Add a result formatter. */
    public abstract void addFormatter(Formatter f);
    // TestListener INTERFACE ///////////////////////////////////////
    /** A failure (or error) has occurred. */
    public abstract void addFailure(Test test, Throwable t);
    /** A failure (or error) has occurred. */
    public abstract void addFailure(Test test, AssertionFailedError t);
    /** An unexpected error has occurred. */
    public abstract void addError(Test test, Throwable t);
    /** Method called at start of individual test suite. */
    public abstract void startTest(Test t);
    /** Method called at end of individual test run. */
    public abstract void endTest(Test test);
    /** Process a block of output. */
    public abstract void handleOutput(String line);
    /** Process an error message */
    public abstract void handleErrorOutput(String line);
    /** Flush standard output. */
    public abstract void handleFlush(String line);
    /** Flush the error output stream. */
    public abstract void handleErrorFlush(String line);
}
