/* MockExec.java */
package org.quilt.frontend.ant;

import java.util.Vector;

import org.quilt.framework.QuiltTest;
import org.quilt.runner.*;

/**
 * Echoes attributes for a given QuiltTest back to the Ant user.
 * Useful in finding out how Ant and Quilt interpret your batch.xml.
 */
public class MockExec {   
    private boolean firstRun   = true;
    private MockTestRunner mtr = null;
    private TaskControl tc2    = null;

    /** No-arg constructor. */
    public MockExec () {}
    
    /** 
     * Dump the parameters for the task and the test.  If the task
     * parameters have been shown before, this will not be repeated.
     * 
     * @param qt   The test which would have been run.
     * @param tc   Task control information.
     */
    public void run (QuiltTest qt, TaskControl tc) {

        System.out.println("** MockExec **");
        if (firstRun) {
            // if it's the first time through, set up the runner ...
            firstRun = false;
            mtr = new MockTestRunner();
            // keep a copy of the task control info ...
            tc2 = (TaskControl) tc.clone();
            // then print it out
            System.out.println ("====    TaskControl   ====\n" + tc);
            
        } else if (!tc.equals(tc2)) {
            // if the task control info has changed, copy it and dump it
            System.out.println ("==== NEW TASK CONTROL ====\n" + tc);
            tc2 = (TaskControl) tc.clone();
        } 
        System.out.println ("** " + qt.getName() + " **");
    }
}
