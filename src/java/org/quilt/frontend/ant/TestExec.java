/* TestExec.java */

package org.quilt.frontend.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;   // need

import org.quilt.framework.*;
import org.quilt.runner.*;

/**
 * Manage the running of a single test suite.
 */
public class TestExec {
    private Project     project = null;
    private Task        task    = null;
    private TaskControl tc      = null;
    private QuiltTest   qt      = null;
   
    /** No-arg constructor */
    public TestExec() { }

    /** 
     * Run an individual test, in a separate JVM if appropriate. 
     *
     * @see CallTest
     * @see ForkTest
     * @param arg Descriptor for the test to be run.
     * @param tc  Task control descriptor.
     */
    protected void execute(QuiltTest arg, TaskControl tc) {
        QuiltTest test = (QuiltTest) arg.clone();
        this.tc = tc;
        task    = tc.getTask();
        project = task.getProject();

        // THIS SHOULD BE DONE BEFORE THE INDIVIDUAL TESTS ARE CLONED;
        // MOVE INTO QuiltTask <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        if (test.getTodir() == null) {
            test.setTodir(project.resolveFile("."));
        }
        if (test.getOutfile() == null) {
            test.setOutfile("TEST-" + test.getName());
        }
        // END SETTING DEFAULTS
        
        // execute the test and get the return code
        int exitValue = Runner.ERRORS;
        boolean timedOut = false;
        if (!test.getFork()) {
            CallTest ct = new CallTest();
            exitValue = ct.execTest(test, tc);
        } else {
            ForkTest ft = new ForkTest();
            ExecuteWatchdog watchdog = tc.createWatchdog();
            exitValue = ft.execTest(test, tc, watchdog);
            if (watchdog != null) {
                timedOut = watchdog.killedProcess();
            }
        }
        boolean errorOccurredHere 
                            = exitValue == Runner.ERRORS;
        // errors are also failures
        boolean failureOccurredHere 
                            = exitValue != Runner.SUCCESS;
        // ... so this includes errors
        if (failureOccurredHere) {
            if ( (errorOccurredHere   && test.getHaltOnError  ())
              || (failureOccurredHere && test.getHaltOnFailure()) ) {
                throw new BuildException(
                    "Test " + test.getName() + " failed"
                    + (timedOut ? " (timeout)" : ""), task.getLocation());
            } else {
                task.log("TEST " + test.getName() + " FAILED"
                    + (timedOut ? " (timeout)" : ""), 
                    Project.MSG_ERR);
                if (errorOccurredHere 
                            && test.getErrorProperty() != null) {
                    project.setNewProperty(
                                test.getErrorProperty(), "true");
                }
                if (failureOccurredHere 
                            && test.getFailureProperty() != null) {
                    project.setNewProperty(
                            test.getFailureProperty(), "true");
                }
            }
        }
    }
}
