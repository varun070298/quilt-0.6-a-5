/* QuiltTest.java */

package org.quilt.framework;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.apache.tools.ant.Project;
//import org.apache.tools.ant.taskdefs.optional.junit.*; 

import org.quilt.reports.*;

/** 
 * Parameters for controlling an individual task.  These are and
 * must be compatible with names used in Ant build.xml build control 
 * files.  Most are set by methods whose name is derived from
 * the variable name -- for example <b>fork</b> is set by 
 * <b>setFork(b)</b>.
 */
public class QuiltTest implements Cloneable {
  
    /** Class name of the test */
    private String name = null;

    // MAINTAINERS please keep the variable names and their get/set
    // methods in alphabetical order

    /** If true, running Quilt coverage checks. */
    private boolean checkCoverage    = false;
    
    /** 
     * Classes not be to instrumented, a comma-separated list of class
     * names.  Wildcards are OK. com.xyz.* means all classes whose 
     * name begins with com.xyz, but does not include com.xyz itself.
     * 
     */
    private String  checkExcludes;
    
    /** 
     * Classes to instrument if checking coverage. Comma-separated, 
     * wildcards OK.
     */
    private String checkIncludes;

    /** Set this property to True if an error occurs.*/
    private String errorProperty;
    
    /** Name of property to set to True if a failure or error occurs. */
    private String failureProperty;
    
    /** Filter Quilt and JUnit error/failure messages from output. */
    private boolean filtertrace   = true;
    
    /** Run the test in a separate JVM. */
    private boolean fork          = false;
    
    /** Names of formatters to be used. */
    private Vector formatters     = new Vector();

    /** Halt the test if an unexpected error occurs. */
    private boolean haltOnError   = false;
    
    /** Halt the test if a failure (expected) or error occurs. */
    private boolean haltOnFailure = false;

    /** Run the test only if this property is True. */
    private String ifProperty = null;
    
    /** Don't run the test, just report back the parameters. */
    private boolean mockTestRun   = false;
 
    /** Where to write the results */
    private String outfile = null;

    /** Copy of Ant properties */
    private Properties props = new Properties();

    /** 
     * Ant documentation seems contradictory on this, but it MUST
     * be a QuiltTest field.  Used in BaseTestRunner. XXX
     */
    private boolean showOutput    = false;

    /** Report destination directory. */
    private File todir            = null;

    /** Run the test unless this property is True. */
    private String unlessProperty = null;
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    /** No-arg constructor used by clone() */
    public QuiltTest() { }
    
    /** 
     * Single-arg constructor. 
     *
     * @param name Full class name of test to be run.
     */
    public QuiltTest(String name) {   
        this.name = name;
    }
    // NON-STANDARD VARIABLES AND GET/SET METHODS ///////////////////
    /** 
     * DON'T BELONG HERE - TEST RESULTS.  These are not accessible
     * from Ant
     */
    private long errors     = (long) 0;
    private long failures   = (long) 0;
    private long runs       = (long) 0;
    private long runTime    = (long) 0; 

    public void setCounts (long runs, long failures, long errors) {
        this.errors   = errors;
        this.failures = failures;
        this.runs     = runs;
    }
    public long errorCount  () { return errors; }
    public long failureCount() { return failures; }
    public long runCount    () { return runs; }
    // END ERRATIC NAMES ////////////////////////////////////////////
   
    /** @return A reference to the test's Properties */
    public Properties getProperties() {  return props; }
    /** 
     * Replace the test's Properties. This is quite different from
     * the method in JUnitTask, which seems to contain errors. 
     * 
     * @param val Hashtable containing new values.
     */

    public void setProperties (Hashtable val) { 
        Enumeration e = val.keys();
        props.clear();
        while (e.hasMoreElements() ) {
            Object key = e.nextElement();
            props.put(key, val.get(key));
        }
    }
    public long getRunTime() {  return runTime; }
    public void setRunTime (long val) { runTime = val; }
    
    // //////////////////////////////////////////////////////////////
    // GET RID OF THIS METHOD ///////////////////////////////////////
    // //////////////////////////////////////////////////////////////
    
    /** Add this test's formatters to a vector.  A convenience method
     * with a mildly confusing name. Inherited from JUnitTask. */
    public void addFormattersTo(Vector v) {
        for (int j = 0; j < formatters.size(); j++){
            v.addElement(formatters.elementAt(j));
        }
    }

    // ADD/GET/SET METHODS /////////////////////////////////
    public boolean getCheckCoverage() { return checkCoverage; }
    public void setCheckCoverage(boolean b) { checkCoverage = b; }
    
    public String getCheckExcludes() { return checkExcludes; }
    public String[] getCheckExcludesArray() {
        String [] val = null;
        if (checkExcludes != null) {
            val = checkExcludes.split(",");
        }
        return val;
    }
    public void setCheckExcludes(String val) { checkExcludes = val; }
    
    public String getCheckIncludes() { return checkIncludes; }
    public String[] getCheckIncludesArray() {
        String [] val = null;
        if (checkIncludes != null) {
            val = checkIncludes.split(",");
        }
        return val;
    }
    public void setCheckIncludes(String val) { checkIncludes = val; }

    public String getErrorProperty() { return errorProperty; }
    public void setErrorProperty(String eP) { errorProperty = eP; }

    public String getFailureProperty() { return failureProperty; }
    public void setFailureProperty(String fP) { failureProperty = fP; }
   
    public boolean getFiltertrace() { return filtertrace; }
    public void setFiltertrace(boolean b) { filtertrace = b; }
    
    public boolean getFork() { return fork; }
    public void setFork(boolean b) { fork = b; }

    public void addFormatter(FmtSelector elem) { 
        formatters.addElement(elem);
    }
    public Vector getFormatters () { return formatters; }

    public boolean getHaltOnError() { return haltOnError; }
    public void setHaltOnError(boolean b) { haltOnError = b; }

    public boolean getHaltOnFailure() { return haltOnFailure; }
    public void setHaltOnFailure(boolean b) { haltOnFailure = b; }

    // non-standard name required for compatibility
    public String getIfProperty () { return ifProperty; }
    public void setIf(String name) { ifProperty = name; }

    public boolean getMockTestRun() { return mockTestRun; }
    public void setMockTestRun(boolean b) { mockTestRun = b; }

    public String getName() {  return name; }
    public void setName (String val) { name = val; }

    public String getOutfile() {  return outfile; }
    public void setOutfile (String val) { outfile = val; }
   
    public boolean getShowOutput() { return showOutput; }
    public void setShowOutput(boolean b) { showOutput = b; }

    public String getTodir(){
        if (todir != null){
            return todir.getAbsolutePath();
        }
        return null;
    } 
    public void setTodir(File dir) { this.todir = dir; }

    // non-standard name required for compatibility
    public String getUnlessProperty () { return unlessProperty; }
    public void setUnless(String name) { unlessProperty = name; }

    // IMPLEMENT Cloneable //////////////////////////////////////////
    /**
     * Clones, resetting the error/failure/run counts and runTime to zero. 
     * 
     * @return An Object, a copy of this QuiltTest.
     */
    public Object clone() {
        QuiltTest t = new QuiltTest();
        t.name              = name;
        
        // counts and runTime are not copied

        t.checkCoverage     = checkCoverage;
        t.checkExcludes     = checkExcludes;
        t.checkIncludes     = checkIncludes;
        t.errorProperty     = errorProperty;
        t.failureProperty   = failureProperty;
        t.filtertrace       = filtertrace;
        t.formatters        = (Vector) formatters.clone();
        t.fork              = fork;
        t.haltOnError       = haltOnError;
        t.haltOnFailure     = haltOnFailure;
        t.ifProperty        = ifProperty;
        t.mockTestRun       = mockTestRun;
        t.outfile           = outfile;
        t.showOutput        = showOutput;
        t.todir             = todir;
        t.unlessProperty    = unlessProperty;

        // real Properties are harder to clone
        Properties props     = getProperties();
        if (props != null) {
            t.setProperties( (Properties) props.clone() );
        }
        return t;
    }
    // TOSTRING () //////////////////////////////////////////////////
    public String toString() {
        String fmtStr = "";
        for (int i = 0; i < formatters.size(); i++) 
            fmtStr += formatters.elementAt(i) + "  ";
        
        String pStr;
        if (props != null) {
            pStr = "";
            Enumeration e = props.keys();
            while (e.hasMoreElements()) {
                String name  = (String) e.nextElement();
                String value = (String) props.getProperty(name);
                pStr += "\n      (" + name + " --> " + value + ")";
            }
        } else {
            pStr = "<none>";
        }
        String s = 
                "    test name:       " + name

            + "\n    checkCoverage:   " + checkCoverage
            + "\n    checkExcludes:   " + (checkExcludes == null? "" 
                                                    : checkExcludes)
            + "\n    checkIncludes:   " + (checkIncludes == null? "" 
                                                    : checkIncludes)
            + "\n    errorProperty:   " + errorProperty
            + "\n    failureProperty: " + failureProperty
            + "\n    filtertrace:     " + filtertrace
            + "\n    fork:            " + fork
            + "\n    formatters:      " + fmtStr
            + "\n    haltOnError:     " + haltOnError
            + "\n    haltOnFailure:   " + haltOnFailure
            + "\n    ifProperty:      " + ifProperty
            + "\n    mockTestRun:     " + mockTestRun
            + "\n    outfile:         " + outfile
            + "\n    showOutput:      " + showOutput
            + "\n    todir:           " + todir
            + "\n    unlessProperty:  " + unlessProperty
           
            // counts - not cloned but part of toString()
            + "\n    errors:          " + errors
            + "\n    failures:        " + failures
            + "\n    runs:            " + runs
            
            + "\n    other properties:" + pStr;
        return s;
    }
    // CONVENIENCE METHOD ///////////////////////////////////////////
    /** 
     * Run this test if project properties permit.
     * 
     * @param p The project that the QuiltTask is part of.
     * @return  True if this test should be run, false otherwise.
     */
    public boolean runMe (Project p) {
        if (ifProperty != null) {
            if (p.getProperty(ifProperty) == null) 
                return false;
        } 
        if (unlessProperty != null) {
            if (p.getProperty(ifProperty) != null)
                return false;
        }
        return true;
    }
}
