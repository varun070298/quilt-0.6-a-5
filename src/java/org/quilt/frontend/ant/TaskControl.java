/* TaskControl.java */

package org.quilt.frontend.ant;

import java.io.File;
import java.io.OutputStream;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog; 
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;

import org.quilt.cl.QuiltClassLoader;
import org.quilt.framework.QuiltTest;
import org.quilt.reports.FmtSelector;
import org.quilt.runner.Runner;

/////////////////////////////////////////////////////////////////////
// NEEDS A LOT OF ATTENTION
/////////////////////////////////////////////////////////////////////

/**
 * Run control parameters from the Ant build.xml file.
 */
public class TaskControl {

    // set by constructor or created at construction  ////////////////
    /** Command line, clone of which is used to fork tests. */
    private CommandlineJava commandline 
                                        = new CommandlineJava();
    /** Run-time environment for command line. */
    private Environment env           = new Environment();

    /** The running Task. */
    private QuiltTask task;

    // values assigned by Ant using add/create/set //////////////////
    private File    dir                 = null;
    private Vector  formatters          = new Vector();
    private boolean includeAntRuntime   = true;
    private boolean mockExec            = false;
    private boolean newEnvironment      = false;
    private boolean showOutput          = false;
    private boolean summary             = false;    // printsummary
    private Vector  tests               = new Vector();
    private Long    timeout             = null;

    // ancillary variables //////////////////////////////////////////
    private Path antRuntimeClasses      = null;
    private Runner runner               = null;
    private String summaryValue         = "";
   
    // Quilt-specific, not available from Ant ///////////////////////
    protected QuiltClassLoader loader   = null;
    
    /////////////////////////////////////////////////////////////////
    /** No-arg constructor for clone() */
    private TaskControl () { }

    /** 
     * Constructor.
     * @param t The QuiltTask being executed. 
     */
    public TaskControl (QuiltTask t) {
        task = t;
    }
    // //////////////////////////////////////////////////////////////
    // ADD/CREATE/SET METHODS.  Please keep in 
    // alphabetical order by the name of the variable being set (so
    // addXYZ sorts after createABC).
    // //////////////////////////////////////////////////////////////
    public void setAntRuntimeClasses (Path p) {
        antRuntimeClasses = p;
    }
    public Path createClasspath() {
        return commandline.createClasspath(task.getProject()).createPath();
    }
    public void setCommandline (CommandlineJava cmd) {
        commandline = cmd;
    }
    public void setDir(File dir) {
        this.dir = dir;
    }
    public void addEnv(Environment.Variable var) {
        env.addVariable(var);
    }
    
    public void addFormatter(FmtSelector fs) {
        formatters.addElement(fs);
    }
    public void setFormatters (Vector v) {
        formatters = v;
    }

    public void setIncludeAntRuntime(boolean b) {
        includeAntRuntime = b;
    }
    public void setJvm(String value) {
        commandline.setVm(value);
    }
    public Commandline.Argument createJvmarg() {
        return commandline.createVmArgument();
    }
    // not for use from Ant
    public void setLoader (QuiltClassLoader qcl) {
        loader = qcl;
    }
    public void setMaxmemory(String max) {
        commandline.setMaxmemory(max);
    }
    public void setMockExec(boolean b) {
        mockExec = b;
    }
    public void setNewEnvironment(boolean newenv) {
        newEnvironment = newenv;
    }
    
    public void setPrintsummary(SummaryAttribute value) {
        summaryValue = value.getValue();
        summary = value.asBoolean();
    }
    public void setRunner (Runner r) {
        runner = r;
    }
    public void setShowOutput (boolean b) {
        showOutput = b;
    }
    public void setSummary(boolean b) {
        summary = b;
    }
    public void setSummaryValue (String s) {
        summaryValue = s;
    }

    public void addSysproperty(Environment.Variable sysp) {
        commandline.addSysproperty(sysp);
    }
    public void addTest(QuiltTest t) {
        tests.addElement(t);
    }
    public void setTests (Vector v) {
        tests = v;
    }
    public void setTask(QuiltTask t) {
        task = t;
    }
    public void setTimeout(Long value) {
        timeout = value;
    }
    // GET METHODS //////////////////////////////////////////////////
    public CommandlineJava  getCommandline()       { return commandline;    }
    public File             getDir()               { return dir;            }
    public Environment      getEnv()               { return env;            }
    public Vector           getFormatters()        { return formatters;     }
    public boolean          getIncludeAntRuntime() { return includeAntRuntime;}
    public QuiltClassLoader getLoader()            { return loader;         }
    public boolean          getMockExec()          { return mockExec;       }
    public boolean          getNewEnvironment()    { return newEnvironment; }
    public boolean          getSummary()           { return summary;        }
    public Task             getTask()              { return task;           }
    public Vector           getTests()             { return tests;          }
    public Long             getTimeout()           { return timeout;        }

    public Path             getAntRuntimeClasses() { return antRuntimeClasses;}
    public Runner           getRunner()            { return runner;         }
    public String           getSummaryValue()      { return summaryValue;   }

    // I/O HANDLING /////////////////////////////////////////////////
    void handleOutput(String line) {
        if (runner != null) {
            runner.handleOutput(line);
            if (showOutput) {
                task.handleTheOutput(line);
            }
        } else {
            task.handleTheOutput(line);
        }
    } 
    void handleFlush(String line) {
        if (runner != null) {
            runner.handleFlush(line);
            if (showOutput) {
                task.handleTheFlush(line);
            }
        } else {
            task.handleTheFlush(line);
        }
    }  
    void handleErrorOutput(String line) {
        if (runner != null) {
            runner.handleErrorOutput(line);
            if (showOutput) {
                task.handleTheErrorOutput(line);
            }
        } else {
            task.handleTheErrorOutput(line);
        }
    }
    void handleErrorFlush(String line) {
        if (runner != null) {
            runner.handleErrorFlush(line);
            if (showOutput) {
                task.handleTheErrorFlush(line);
            }
        } else {
            task.handleTheErrorFlush(line);
        }
    } 

    OutputStream getDefaultOutput(){
        return new LogOutputStream(task, Project.MSG_INFO);
    }
    FmtSelector[] mergeSelectors(QuiltTest test){
        Vector fsVector = (Vector) getFormatters().clone();
        test.addFormattersTo(fsVector);
        FmtSelector[] fsArray = 
                            new FmtSelector[fsVector.size()];
        fsVector.copyInto(fsArray);
        return fsArray;
    }
    File getOutput(FmtSelector fs, QuiltTest test){
        if (fs.getUseFile()) {
            String filename = test.getOutfile() + fs.getExtension();
            File destFile = new File(test.getTodir(), filename);
            String absFilename = destFile.getAbsolutePath();
            return task.getProject().resolveFile(absFilename);
        }
        return null;
    } 
    ExecuteWatchdog createWatchdog() throws BuildException {
        if (timeout == null){
            return null;
        }
        return new ExecuteWatchdog(timeout.longValue());
    }

    // EQUALS() /////////////////////////////////////////////////////
    public boolean equals( TaskControl tc2 ) {
        if (this == tc2) 
            return true;
        else 
            return 
                formatters. equals( tc2.getFormatters() )         &&
                tests.      equals( tc2.getTests() )              &&
                
                task                == tc2.getTask()              &&
                commandline         == tc2.getCommandline()       &&
                dir                 == tc2.getDir()               &&    
                includeAntRuntime   == tc2.getIncludeAntRuntime() &&    
                loader              == tc2.getLoader()            &&
                mockExec            == tc2.getMockExec()          &&    
                newEnvironment      == tc2.getNewEnvironment()    &&    
                summary             == tc2.getSummary()           &&    
                timeout             == tc2.getTimeout()           &&    
                antRuntimeClasses   == tc2.getAntRuntimeClasses() &&    
                runner              == tc2.getRunner()            &&    
                summaryValue.equals(tc2.getSummaryValue());
    }
    // CLONE() //////////////////////////////////////////////////////
    public Object clone() {
        TaskControl tc2 = new TaskControl();
        tc2.setTask(task);
        tc2.setCommandline(commandline);

        // the Vectors
        tc2.setFormatters((Vector)formatters.clone());
        tc2.setTests((Vector)tests.clone());

        tc2.setDir(dir);
        tc2.setIncludeAntRuntime(includeAntRuntime);
        tc2.setLoader(loader);
        tc2.setMockExec(mockExec);
        tc2.setNewEnvironment(newEnvironment);
        tc2.setSummary(summary);
        tc2.setTimeout(timeout);
        tc2.setAntRuntimeClasses(antRuntimeClasses);
        tc2.setRunner(runner);
        tc2.setSummaryValue(summaryValue);
        return tc2;
    }
    // TOSTRING () //////////////////////////////////////////////////
    public String toString() {
        // the Vectors
        String f  = "";
        for (int i = 0; i < formatters.size(); i++) 
            f += formatters.elementAt(i) + " ";
        
        String s = 
                "    task:           " + task.getTaskName() 
            + "\n    antRuntimeClasses: " + antRuntimeClasses
            + "\n    commandline:    " 
            + "\n      as string:    " + commandline
            + "\n      classname:    " + commandline.getClassname()
            + "\n      classpath:    " + commandline.getClasspath()
            + "\n    dir:            " + dir
            + "\n    includeAntRuntime: " + includeAntRuntime
            // loader ignored
            + "\n    mockExec:       " + mockExec
            + "\n    newEnvironment: " + newEnvironment
            + "\n    runner:         " + runner
            + "\n    summary:        " + summary
            + "\n    summaryValue:   " + summaryValue
            + "\n    timeout:        " + timeout
            + "\n    formatters:     " + f
            + "\n ---------------------------------------------\n";
        return s;
    }
}
