/* ForkTest.java */

package org.quilt.frontend.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;   // need this
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;

import org.quilt.framework.*;
import org.quilt.reports.*;

// //////////////////////////////////////////////////////////////////
// NEEDS LOTS OF ATTENTION //////////////////////////////////////////
// //////////////////////////////////////////////////////////////////
//
/** 
 * Run an individual test in a separate JVM.  Search on Bug 23150;
 * needs to return correct status code if interrupted or times out.
 */
public class ForkTest { 
    
    private Project     project = null;
    private Task        task    = null;
    private TaskControl tc      = null;
    private QuiltTest   qt      = null;
    private boolean     mockery = false;
    private boolean     checkingCoverage = false; 

    /** No-arg constructor. */
    public ForkTest() { }
    
    /** 
     * Fork an individual test, running it in a separate Java virtual
     * machine.
     *
     * @param qt       Data structure holding test parameters.
     * @param tc       Structure holding parameters for the entire run.
     * @param watchdog Sets timeout in ms for this test.
     */
    protected int execTest (QuiltTest qt, TaskControl tc,
            ExecuteWatchdog watchdog) throws BuildException {

        this.tc = tc;
        task    = tc.getTask();
        project = task.getProject();
        mockery = qt.getMockTestRun();
        checkingCoverage = qt.getCheckCoverage();

        CommandlineJava cmd = (CommandlineJava) tc.getCommandline().clone();

        if (mockery) {
            task.log("ForkTest: setting class name to  MockTestRunner");
            cmd.setClassname( "org.quilt.textui.MockTestRunner");
        } else { 
            cmd.setClassname( "org.quilt.textui.TestRunner");
        }
        // //////////////////////////////////////////////////////////
        // THIS INTERFACE MUST BE KEPT IN SYNC WITH textui.TestRunner
        //   and textui.MockTestRunner
        // //////////////////////////////////////////////////////////
        cmd.createArgument().setValue(qt.getName());
        cmd.createArgument().setValue("checkCoverage=" + checkingCoverage);
        if (checkingCoverage) {
            String excluded = qt.getCheckExcludes();
            String included = qt.getCheckIncludes();
            if (excluded != null ) {
                cmd.createArgument().setValue(
                        "checkExcludes=" + excluded);
            }
            if (included != null ) {
                cmd.createArgument().setValue(
                        "checkIncludes=" + included);
            }
        }
        cmd.createArgument().setValue("filtertrace=" 
                                        + qt.getFiltertrace());
        cmd.createArgument().setValue("haltOnError=" 
                                        + qt.getHaltOnError());
        cmd.createArgument().setValue("haltOnFailure=" 
                                        + qt.getHaltOnFailure());

        if (tc.getIncludeAntRuntime()) {
            task.log("Adding " + tc.getAntRuntimeClasses() 
                                                + " to CLASSPATH",
                Project.MSG_VERBOSE);
            cmd.createClasspath(project).createPath()
                .append(tc.getAntRuntimeClasses());
        }

        if (tc.getSummary()) {
            task.log("Running " + qt.getName(), Project.MSG_INFO);
            cmd.createArgument().setValue(
                "formatter=org.quilt.reports.SummaryFormatter");
        }

        cmd.createArgument().setValue("showoutput=" 
                                    + String.valueOf(qt.getShowOutput()));

        StringBuffer formatterArg = new StringBuffer(256);
        final FmtSelector[] selectors = tc.mergeSelectors(qt);
        for (int i = 0; i < selectors.length; i++) {
            FmtSelector fs = selectors[i];
            formatterArg.append("formatter=");
            formatterArg.append(fs.getClassname());
            File outFile = tc.getOutput(fs, qt);
            if (outFile != null) {
                formatterArg.append(",");
                formatterArg.append(outFile);
            }
            cmd.createArgument().setValue(formatterArg.toString());
            formatterArg.setLength(0);
        }

        File propsFile = 
            FileUtils.newFileUtils().
                        createTempFile("quilt", ".properties",
                                        project.getBaseDir());
        cmd.createArgument().setValue("propsfile=" 
                                      + propsFile.getAbsolutePath());
        Hashtable p = project.getProperties();
        Properties props = new Properties();
        for (Enumeration e = p.keys(); e.hasMoreElements(); ) {
            Object key = e.nextElement();
            props.put(key, p.get(key));
        }
        try {
            FileOutputStream outstream = 
                                    new FileOutputStream(propsFile);
            // props.save() is deprecated
            props.save(outstream, 
                        "Ant QuiltTask generated properties file");
            outstream.close();
        } catch (java.io.IOException e) {
            propsFile.delete();
            throw new BuildException(
                    "Error creating temporary properties "
                                     + "file.", e, task.getLocation());
        }
        // prepare to fork the test
        Execute forker = new Execute(
                new LogStreamHandler(task, 
                    Project.MSG_INFO, Project.MSG_WARN), watchdog);
        forker.setCommandline(cmd.getCommandline());
        forker.setAntRun(project);
        if (tc.getDir() != null) {
            forker.setWorkingDirectory(tc.getDir());
        }

        String[] environment = tc.getEnv().getVariables();
        if (environment != null) {
            for (int i = 0; i < environment.length; i++) {
                task.log("Setting environment variable: " + environment[i],
                    Project.MSG_VERBOSE);
            }
        }
        forker.setNewenvironment(tc.getNewEnvironment());
        forker.setEnvironment(environment);

        task.log(cmd.describeCommand(), Project.MSG_VERBOSE);

        int retVal;
        try {
            retVal = forker.execute (); // do the actual fork
        } catch (IOException e) {
            throw new BuildException(
                    "Error forking test", e, task.getLocation());
        } finally {
            if (watchdog != null && watchdog.killedProcess()) {
                logTimeout(selectors, qt);
                // see Bug 23150; also needs to be set to 1 if interrrupted
                retVal = 1;
            }

            if (!propsFile.delete()) {
                throw new BuildException(
                        "Error deleting temporary properties file.");
            }
        }
        return retVal;
    }

    // DO WE REALLY WANT TO DO THIS, ONCE PER FORMATTER ?
    
    private void logTimeout(FmtSelector[] selectors, 
                                                QuiltTest qt) { 

        for (int i = 0; i < selectors.length; i++) {
            FmtSelector fs      = selectors[i];
            File outFile        = tc.getOutput(fs, qt);
            Formatter formatter = fs.createFormatter();
            if (outFile != null && formatter != null) {
                try {
                    OutputStream out = new FileOutputStream(outFile);
                    formatter.setOutput(out);
                    formatter.startTestSuite(qt);
                    qt.setCounts(0, 0, 1);
                    Test t = new Test() {
                        public int countTestCases() { return 0; }
                        public void run(TestResult r) { 
                            throw new AssertionFailedError( 
                                            "Timeout during test run.");
                        }
                    };
                    formatter.startTest(t);
                    formatter.addError(t, new AssertionFailedError( 
                                            "Timeout during test run."));

                    formatter.endTestSuite(qt);
                } catch (IOException e) {
                    // just ignore any more exceptions
                }
            }
        }
    }
}
