/* Formatter.java */

package org.quilt.reports;

import java.io.OutputStream;

import junit.framework.TestListener;
import org.apache.tools.ant.BuildException;

import org.quilt.framework.QuiltTest;
import org.quilt.runner.Runner;

/** 
 * Interface presented by all Ant/Quilt-compatible test result formatters.
 * Extend BaseFormatter to build new formatters.
 */

public interface Formatter extends TestListener {
    /** Enable filtering of Ant/Quilt/JUnit lines from stack traces. */
    public void setFiltertrace(boolean b);
    public void setOutput(OutputStream out);
    /** Select test runner to use. */
    public void setRunner(Runner testrunner);
    public void setSystemError(String err);
    public void setSystemOutput(String out);
   
    /** Called at beginning of test suite run. */
    public void startTestSuite (QuiltTest suite) throws BuildException;
    /** Called at end of test suite run. */
    public void endTestSuite   (QuiltTest suite) throws BuildException;
}
