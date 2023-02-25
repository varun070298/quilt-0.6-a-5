/* BatchTest.java */

package org.quilt.frontend.ant;

import java.util.Vector;
import java.io.File;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import org.quilt.framework.*;

/**
 * Stores Ant filesets and convert them to QuiltTests.
 * 
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class BatchTest extends QuiltTest { 

    /** The project this batch test was found in. */
    private Project project;
    /** Filesets named in the batchtest element in the build.xml. */
    private Vector filesets = new Vector();

    /////////////////////////////////////////////////////////////////
    /** 
     * One-arg constructor. 
     *
     * @param p The project that the QuiltTask is in. 
     */
    public BatchTest(Project p){
        project = p;
    }

    /**
     * Method used by Ant to add filesets. Ant first calls 
     * createBatchTest, then in a second pass calls this method
     * to add filesets.
     * 
     * @param fs One of possibly many filesets within the batch test
     */
    public void addFileSet(FileSet fs) {
        filesets.addElement(fs);
    }

    /////////////////////////////////////////////////////////////////
    /** 
     * Converts the fileset names into QuiltTests and adds them to the
     * list of tests maintained by the scheduler.  This can only be
     * done once; after the tests are unbatched, the vector of batch
     * tests is deleted.
     *
     * @param sch Scheduler responsible for maintaining list of tests
     *            and scheduling the actual running of tests.
     */
    public void unbatch (Scheduler sch) {
        // don't schedule tests that won't get run
        if (runMe(project)) {
            Vector v = new Vector();
            int size = filesets.size();
            for (int i = 0; i < size; i++) {
                FileSet fs = (FileSet) filesets.elementAt(i);
                DirectoryScanner ds = fs.getDirectoryScanner(project);
                ds.scan();
                String[] files = ds.getIncludedFiles();
                for (int j = 0; j < files.length; j++) {
                    boolean gottaMatch = false;
                    String name = files[j];
                    String className = "";
                    if (name.endsWith(".class")) {
                        gottaMatch = true;
                        className = name.substring(0, 
                                name.length() - ".class".length());
                    } else if (name.endsWith(".java")) {
                        gottaMatch = true;
                        className = name.substring(0, 
                                name.length() - ".java".length());
                    }
                    if (gottaMatch) {
                        // replace forward or reverse slashes with dots
                        className = 
                            className.replace(File.separatorChar, '.');
                        // make a QuiltTest with default attributes
                        QuiltTest qt = (QuiltTest) clone();
                        qt.setName(className);
                        // add the cloned QuiltTest to the list of tests
                        sch.addTest(qt);
                    }
                }
            }
        }
    }
}
