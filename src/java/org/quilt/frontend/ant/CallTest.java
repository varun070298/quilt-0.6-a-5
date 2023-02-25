/* CallTest.java */

package org.quilt.frontend.ant;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;

import java.io.File;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.quilt.cl.QuiltClassLoader;
import org.quilt.framework.*;
import org.quilt.reports.*;
import org.quilt.runner.*;

/** 
 * Handles an individual QuiltTest suite by directly calling it 
 * rather than forking it. 
 */
public class CallTest {
    private Project     project = null;
    private Task        task    = null;
    private TaskControl tc      = null;
    private QuiltTest   qt      = null; 

    private CommandlineJava cmdLine = null;
    private Runner runner;
    
    /** No-arg constructor. */
    public CallTest() { }

    /** 
     * Run the test suite in this Java virtual machine.
     *
     * @param test Data structure describing the individual test.
     * @param tc   Task control which sets parameters for the entire run.
     */
    protected int execTest (QuiltTest test, TaskControl tc) 
                                                throws BuildException {
        // structure gets altered during the test run, so needs to be
        // cloned
        QuiltTest qt = (QuiltTest) test.clone();
        this.tc = tc;
        task    = tc.getTask();
        project = task.getProject();
       
        boolean usingQuilt = (tc.getLoader() != null) 
            && qt.getCheckCoverage() && qt.getCheckIncludes() != null;
        
        cmdLine = (CommandlineJava) tc.getCommandline().clone();
        qt.setProperties(project.getProperties());
        if (tc.getDir() != null) {
            task.log(
                "Dir attribute ignored, running in the same virtual machine", 
                Project.MSG_WARN);
        }
        if (tc.getNewEnvironment() || null != tc.getEnv().getVariables()) {
            task.log( "Changes to environment variables are ignored, "
                + "running in the same virtual machine.", Project.MSG_WARN);
        }

        CommandlineJava.SysProperties sysProperties = 
                                        cmdLine.getSystemProperties();
        if (sysProperties != null) {
            sysProperties.setSystem();
        }
        AntClassLoader antLoader = null;
        QuiltClassLoader quiltLoader = tc.getLoader();
        try {
            task.log("Using System properties " + System.getProperties(), 
                Project.MSG_VERBOSE);
            Path userClasspath = cmdLine.getClasspath();
            Path classpath = userClasspath == null ? 
                                null : (Path) userClasspath.clone();
            if (usingQuilt) {
                String pathForQuilt 
                    = (classpath == null) ? null : classpath.toString();
                quiltLoader.setClassPath(pathForQuilt);
                quiltLoader.setIncluded(qt.getCheckIncludes());
                quiltLoader.setExcluded(qt.getCheckExcludes());
            } else if (classpath != null) {
                // XXX either document the fact that this option isn't
                // supported with Quilt - or support it
                if (tc.getIncludeAntRuntime()) {
                    task.log("Adding " + tc.getAntRuntimeClasses() 
                        + " to CLASSPATH", Project.MSG_VERBOSE);
                    classpath.append(tc.getAntRuntimeClasses());
                }

                antLoader = new AntClassLoader(null, project, 
                                                classpath, false);
                task.log("Using CLASSPATH " + antLoader.getClasspath(),
                    Project.MSG_VERBOSE);

                antLoader.addSystemPackageRoot("junit");
            }
            if (usingQuilt) {
//              // DEBUG
//              System.out.println(
//                  "CallTest invoking BaseTestRunner with classpath "
//                  + classpath);
//              // END
                runner = new BaseTestRunner(qt, quiltLoader);
            } else {
                runner = new BaseTestRunner(qt, antLoader);
            }
            if (tc.getSummary()) {
                task.log("Running " + qt.getName(), Project.MSG_INFO);

                SummaryFormatter fmt = new SummaryFormatter();
                fmt.setWithOutAndErr("withoutanderr"
                                   .equals(tc.getSummaryValue()));
                fmt.setOutput(tc.getDefaultOutput());
                runner.addFormatter(fmt);
            }

            final FmtSelector[] selectors = tc.mergeSelectors(qt);
            for (int i = 0; i < selectors.length; i++) {
                FmtSelector fs = selectors[i];
                File outFile = tc.getOutput(fs, qt);
                if (outFile != null) {
                    fs.setOutfile(outFile);
                } else {
                    fs.setOutput(tc.getDefaultOutput());
                }
                runner.addFormatter(fs.createFormatter());
            }
            runner.run();
            return runner.getRetCode();

        } finally{
            if (sysProperties != null) {
                sysProperties.restoreSystem();
            }
            if (antLoader != null) {
                antLoader.resetThreadContextLoader();
            }
        }
    }
}
