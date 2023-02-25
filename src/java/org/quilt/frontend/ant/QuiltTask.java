/* QuiltTask.java */

package org.quilt.frontend.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.optional.junit.Enumerations;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.quilt.cl.QuiltClassLoader;
import org.quilt.framework.QuiltTest;
import org.quilt.reports.FmtSelector;
import org.quilt.cover.stmt.StmtRegistry;
//import org.quilt.runner.*;

/**
 * Ant task for running Quilt and JUnit.
 *
 * <p>The Quilt Ant task is meant to be a plug-in replacement for
 * the Ant JUnitTask.  Whatever build.xml works with JUnitTask
 * should behave identically with QuiltTask.  The opposite is not
 * true: using QuiltTask allows you to run coverage tests in
 * addition to JUnit unit tests</p>
 *
 * <p>Parameter names / build file options are compatible with
 * the build.xml options for JUnitTask as of Ant 1.5.3-1, so that
 * if &lt;junit&gt; and &lt;/junit&gt; are replaced with &lt;quilt&gt; and &lt;/quilt&gt;
 * respectively in the build.xml file, test behavior should be the same.</p>
 *
 * <p>Build file options either control the individual test and so
 * are passed to Quilt and JUnit, or manage QuiltTask and the test
 * process.</p>
 *
 * <p>Most test options will go through Quilt to JUnit.
 * All are set by Ant set* methods, where the name for the method
 * setting the variable 'var' is 'setVar'.  That is, the first
 * letter of the variable name is capitalized, then the modified
 * name is appended to 'set'.</p>
 *
 * <p>Task control parameters are NOT passed through to Quilt or JUnit.
 * These variables are modified by Ant add*, set*, and create* routines, where
 * the names are determined as described above.</p>
 *
 * <p>QuiltTest options can be set at three levels.  First, then can
 * be set as attributes to the &lt;quilt&gr; element.  In this case, 
 * they are the defaults for all tests.</p>
 *
 * <p>Next, they can be set at the &lt;batchtest&gt; leve..  In this case,
 * these attributes will be used for all files in the batch test.</p>
 *
 * <p>Finally, they can be set at the &lt;test&gt; level, in which case
 * they will override the defaults set higher up.</p>
 * 
 * <p>QuiltTask collects filesets from batch test elements and the 
 * names of individual tests.  These are then passed to a Scheduler
 * which unpacks the batch tests and schedules all tests for running.</p>
 *
 * <p>It may be important to understand that under certain circumstances
 * batches of tests will be run more than once result. This will normally
 * be the result of an error in the way that dependencies are structured
 * in build.xml. </p>
 *
 * @see QuiltTest
 * @see Scheduler
 * @see TaskControl
 */
public class QuiltTask extends Task {
    private Scheduler  sch = null;
    private TaskControl tc = null;

//  // THESE DUPLICATE ELEMENTS OF TaskControl //////////////////
//  // XXX and this apparently isn't set -- leading to bugs 
//  private CommandlineJava commandline = new CommandlineJava();

    // DO THESE BELONG IN TaskControl ?
    private boolean includeAntRuntime = true;
    private Path antRuntimeClasses = null;

    // COLLECT ANT PARAMETERS USING ADD/CREATE/SET //////////////////
    // Please keep in order by variable name ////////////////////////

    // TEST PARAMETERS //////////////////////////////////////////////
    // Ant creates child elements (individual tests and BatchTests),
    // then assigns values to task attributes, and THEN assigns
    // values to child element attributes.  This means that the
    // default values assigned at the task level can be overridden
    // by test and batch level assignments, IF the task-level values
    // are applied immediately to all tests and batch tests.
    //
    // The methods that follow assign such task-level defaults.  It
    // is important to bear in mind that Ant uses methods of the same
    // name and call QuiltTask to assign defaults and to call
    // QuiltTest and BatchTest to override these defaults.
    // //////////////////////////////////////////////////////////////
    private QuiltTest qt        // scratch variable
        = new QuiltTest();      // to keep the compiler quiet
    public void setCheckCoverage (boolean b) {
        sch.schedule();
        while ( (qt = sch.nextTest()) != null) {
            qt.setCheckCoverage(b);
        }
    }
    public void setCheckExcludes (String s) {
        sch.schedule();
        while ( (qt = sch.nextTest()) != null) {
            qt.setCheckExcludes(s);
        }
    }
    public void setCheckIncludes (String s) {
        sch.schedule();
        while ( (qt = sch.nextTest()) != null) {
            qt.setCheckIncludes(s);
        }
    }
    public void setErrorProperty(String propertyName) {
        sch.schedule();
        while ( (qt = sch.nextTest()) != null) {
            qt.setErrorProperty(propertyName);
        }
    }
    public void setFailureProperty(String propertyName) {
        sch.schedule();
        while ( (qt = sch.nextTest()) != null) {
            qt.setFailureProperty(propertyName);
        }
    }
    public void setFiltertrace(boolean b) {
        sch.schedule();
        while ( (qt = sch.nextTest()) != null) {
            qt.setFiltertrace(b);
        }
    }
    public void setFork(boolean b) {
        sch.schedule();
        while ( (qt = sch.nextTest()) != null) {
            qt.setFork(b);
        }
    }
    public void setHaltOnError(boolean b) {
        sch.schedule();
        while ( (qt = sch.nextTest()) != null) {
            qt.setHaltOnError(b);
        }
    }
    public void setHaltOnFailure(boolean b) {
        sch.schedule();
        while ( (qt = sch.nextTest()) != null) {
            qt.setHaltOnFailure(b);
        }
    }
    public void setMockTestRun(boolean b) {
        sch.schedule();
        while ( (qt = sch.nextTest()) != null) {
            qt.setMockTestRun (b);
        }
    }
    public void setShowOutput(boolean b) {
        tc.setShowOutput(b);        // NEEDS FIXING
        sch.schedule();
        while ( (qt = sch.nextTest()) != null) {
            qt.setShowOutput(b);
        }
    }

    // TASK PARAMETERS //////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////

    public BatchTest createBatchTest () {
        BatchTest bt = new BatchTest (getProject());
        sch.addBatchTest(bt);
        return bt;      // returns to Ant a reference to the object created
    }

    public Path createClasspath() {
//      commandline.createClasspath(getProject()).createPath(); // XXX
        return tc.createClasspath();
    }
    public void setDir(File dir) {
        tc.setDir(dir);
    }
    public void addEnv(Environment.Variable var) {
        tc.addEnv(var);
    }
    public void addFormatter(FmtSelector fe) {
        tc.addFormatter(fe);
    }
    public void setIncludeAntRuntime(boolean b) {
        tc.setIncludeAntRuntime(b);
    }
    public void setJvm(String value) {
        tc.setJvm(value);
    }
    public Commandline.Argument createJvmarg() {
        return tc.createJvmarg();
    }
    public void setMaxmemory(String max) {
        tc.setMaxmemory (max);
    }
    public void setMockExec(boolean b) {
        tc.setMockExec(b);
    }
    public void setNewenvironment(boolean b) {
        tc.setNewEnvironment(b);
    }
    // compatible with JUnitTask but kludgey
    public void setPrintsummary(String sValue) {
        SummaryAttribute sa = new SummaryAttribute(sValue);
        tc.setSummary (sa.asBoolean());
        tc.setSummaryValue (sa.getValue());
    }
    public void addSysproperty(Environment.Variable sysp) {
        tc.addSysproperty(sysp);
    }
    public void addTest(QuiltTest qt) {
        sch.addTest (qt);
    }
    public void setTimeout(Long t) {
        tc.setTimeout (t);
    }

    // CONSTRUCTOR //////////////////////////////////////////////////
    public QuiltTask() throws Exception {
        sch = new Scheduler (this);
        tc  = sch.getTaskControl();
    }

    private MockExec mockE = null;
    private TestExec testE = null;
    private boolean mockery = false;

    private boolean firstTimeThrough = true;
    private void addCPEs () {
        mockery = tc.getMockExec();
        if (mockery) {
            mockE = new MockExec();
        } else {
            testE = new TestExec();
        }
        addClasspathEntry("/junit/framework/TestCase.class");
        addClasspathEntry("/org/apache/tools/ant/Task.class");
        addClasspathEntry("/org/quilt/runner/BaseTestRunner.class");
    }
    // FIRST ANT ENTRY POINT: init() ////////////////////////////////
    // only called once /////////////////////////////////////////////
    public void init() {
        antRuntimeClasses = new Path(getProject());
    }

    // SECOND ANT ENTRY POINT: execute () ///////////////////////////
    // May be called many times /////////////////////////////////////
    public void execute() throws BuildException {
        if (firstTimeThrough) {
            firstTimeThrough = false;
            addCPEs();
            sch.unbatch();      // merge batch tests with other tests
        }
        sch.schedule();

        Path quiltClassPath = tc.getCommandline().getClasspath();
        if (tc.getIncludeAntRuntime()) {
            quiltClassPath.append ( tc.getAntRuntimeClasses() );
        }
        QuiltClassLoader qcl = new QuiltClassLoader (
            QuiltClassLoader.cpToURLs(
                            tc.getCommandline().getClasspath().toString()),
            this.getClass().getClassLoader(), // we delegate to ..
            // delegated    included        excluded
            (String[])null, (String[])null, (String[])null);
        StmtRegistry stmtReg = (StmtRegistry)qcl.addQuiltRegistry( 
                                    "org.quilt.cover.stmt.StmtRegistry" );
        if (stmtReg == null) {
            System.out.println(
    "QuiltTask.execute: org.quilt.cover.stmt.StmtRegistry not found\n" 
                + "  classpath error?");
        }
        tc.setLoader(qcl);
        while ( (qt = sch.nextTest()) != null) {
            if (tc.getMockExec()) {
                mockE.run(qt, tc);
            } else if (qt.runMe (getProject())) {
                testE.execute(qt, tc);
            }
        }
        if (stmtReg != null) {
            System.out.println (
                    stmtReg.getReport()
            );
        }
        // DEBUG
        else System.out.println(
            "QuiltTask.execute: after running tests, stmtReg is null");
        // END
    }

    // //////////////////////////////////////////////////////////////
    // CLEAN ME UP.  This code is per JUnit task, needs work.
    // //////////////////////////////////////////////////////////////
   
    protected void addClasspathEntry(String resource) {
        URL url = getClass().getResource(resource);
        if (url != null) {
            String u = url.toString();
            if (u.startsWith("jar:file:")) {
                int pling = u.indexOf("!");     // Ant standard term
                String jarName = u.substring(9, pling);
                log("Found " + jarName, Project.MSG_DEBUG);
                antRuntimeClasses.createPath()
                    .setLocation(new File((new File(jarName))
                                          .getAbsolutePath()));
            } else if (u.startsWith("file:")) {
                int tail = u.indexOf(resource);
                String dirName = u.substring(5, tail);
                log("Found " + dirName, Project.MSG_DEBUG);
                antRuntimeClasses.createPath()
                    .setLocation(new File((new File(dirName))
                                          .getAbsolutePath()));
            } else {
                log("Don\'t know how to handle resource URL " + u,
                    Project.MSG_DEBUG);
            }
        } else {
            log("Couldn\'t find " + resource, Project.MSG_DEBUG);
        }
    }
    // TASK CONTROL SUPPORT //////////////////////////////////////////
    public void handleTheOutput(String line) {
        super.handleOutput(line);
    }
    public void handleTheFlush(String line) {
        super.handleFlush(line);
    }
    public void handleTheErrorOutput(String line) {
        super.handleErrorOutput(line);
    }
    public void handleTheErrorFlush(String line) {
        super.handleErrorFlush(line);
    }
}
