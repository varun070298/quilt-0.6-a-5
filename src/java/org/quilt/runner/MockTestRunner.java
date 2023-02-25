/* MockTestRunner.java */

package org.quilt.runner;

import org.quilt.framework.QuiltTest;

/**
 * Echoes parameters for a given QuiltTest back to the Ant user.
 */
public class MockTestRunner extends QuiltTest {

    public MockTestRunner () {}
    
    /**
     * Set mockTestRunner="true" or mockExec="true" in the Quilt task
     * line in build.xml to get this output on all tests run.  Example:
     * 
     *  &lt;quilt dir="." mockTestRunner="true" haltOnError="true"&gr;
     *
     * @param qt The QuiltTest being run 
     */
    public void run (QuiltTest qt) {

        System.out.println (" ** MockTestRunner **\n==== QuiltTest ==== \n" 
                                                                + qt);
    }
}
