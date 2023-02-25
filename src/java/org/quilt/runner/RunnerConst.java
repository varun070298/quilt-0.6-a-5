/* RunnerConst.java */

package org.quilt.runner;

public interface RunnerConst {
    
    /** All tests in suite succeeded. */
    public static final int SUCCESS = 0;
    
    /** One or more tests failed (or an error occurred). */
    public static final int FAILURES = 1;
    
    /** An unexpected error occurred. */
    public static final int ERRORS = 2;

    /** 
     * Lines containing these substrings will be filtered if 
     *   filtertrace == true 
     */
    public static final String[] DEFAULT_TRACE_FILTERS = new String[] {
        "java.lang.reflect.Method.invoke(",
        
        // legacy -- Quilt doesn't actually use these
        "junit.awtui.TestRunner",
        "junit.swingui.TestRunner",
        "junit.textui.TestRunner",

        "junit.framework.TestCase",
        "junit.framework.TestResult",
        "junit.framework.TestSuite",
        // notice the . -- AssertionFailure not filtered
        "junit.framework.Assert.", 

        "org.apache.tools.ant.",
        "org.quilt"
    };
}
