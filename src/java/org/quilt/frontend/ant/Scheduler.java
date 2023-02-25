/* Scheduler.java */

package org.quilt.frontend.ant;

import java.util.Vector;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.quilt.framework.QuiltTest;

/** 
 * Collects individual and batch tests during initial processing of the
 * Ant build.xml file, then schedules tests for running.  Before running
 * any tests, batch tests are unpacked and attributes assigned.
 */
public class Scheduler {

    private QuiltTask task      = null;
    private TaskControl tc      = null;
    private boolean batched     = true;
    
    /** Vector of QuiltTests */
    private Vector tests        = new Vector();
    /** Index into that Vector. */
    private int testIndex        = 0;
    
    /** Vector of BatchTests */
    private Vector batchTests   = new Vector();
    /** Index into Vector of BatchTests. */
    private int batchIndex       = 0;
    
    /** All tests are clones of this one. */
    private QuiltTest modelTest = new QuiltTest();

    /** One-arg constructor called at beginning of run */
    public Scheduler ( QuiltTask t) {
        task = t;
        tc = new TaskControl (task);
    }
   
    // SCHEDULING METHODS /////////////////////////////////
    /**
     * Ant-compatible method for adding a batch test.  When Ant
     * encounters a &lt;batchtest ... it calls QuiltTask.createBatchTest
     * to get an instance, then uses the set* methods to set test
     * parameters for the batch.  QuiltTask uses this method to pass
     * the batch test to the Scheduler.
     *
     * @param bt BatchTest instance created by QuiltTask
     */
    public void addBatchTest (BatchTest bt) {
        task.log("--> Scheduler.addBatchTest", Project.MSG_VERBOSE);
        batchTests.addElement(bt);
    }
    /** 
     * Ant-compatible method for adding test to queue.  When Ant 
     * counters a &lt;test name=... it uses this method to create
     * the QuiltTest object and then calls set* methods to set
     * test parameters.
     * 
     * @param test QuiltTest structure containing test parameters.
     */
    public void addTest (QuiltTest test) {
        task.log("--> Scheduler.addTest", Project.MSG_VERBOSE);
        tests.addElement (test);
    }
    /** 
     * Zero out indexes into lists of tests and batch tests.  This
     * will be called whenever the user wants to rescan the lists of
     * tests and batch tests.
     */
    public void schedule () {
        testIndex        = 0;
        batchIndex       = 0;
    
        task.log ( 
            "\n===========================================================\n"
            + "--> Scheduler.schedule: there are "
            + tests.size()      + " tests and " 
            + batchTests.size() + " batch tests"
          + "\n===========================================================\n",
          Project.MSG_DEBUG
        );
    }
    /**
     * Returns the next test or batch test available.
     * 
     * @return QuiltTest-compatible structure 
     */
    public QuiltTest nextTest() {
        if (testIndex < tests.size()) {
            return (QuiltTest) tests.elementAt( testIndex++ );
        } else if (batchIndex < batchTests.size()) {
            return (BatchTest) batchTests.elementAt( batchIndex++ );
        } else {
            return null;
        }
    }
    /**
     * Pass through the list of batch tests, creating individual tests
     * and passing these to the scheduler.
     */
    public void unbatch() {
        task.log("--> Scheduler.unbatch", Project.MSG_VERBOSE);
        for (int i = 0; i < batchTests.size(); i++) {
            ((BatchTest)batchTests.elementAt(i)).unbatch(this); 
        } 
        batched = false;
        batchTests = new Vector();
    }

    // GET/SET METHODS //////////////////////////////////////////////
    /** 
     * Get a reference to the TaskControl object created by the 
     * constructor. 
     * */
    public TaskControl getTaskControl() {
        return tc;
    }
}
