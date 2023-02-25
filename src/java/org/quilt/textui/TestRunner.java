/* TestRunner.java */

package org.quilt.textui;

import java.util.Vector;

import org.quilt.cover.stmt.StmtRegistry;
import org.quilt.framework.QuiltTest;
import org.quilt.reports.*;
import org.quilt.runner.*;

/** Text-interface test runner for Quilt. */

public class TestRunner extends Textui implements RunnerConst {

    public TestRunner () { }
   
    public static void main (String [] args ) {
        System.exit( new TestRunner().handleArgs(args) );
    }

    /** 
     * Run an individual test.
     *
     * @param qt           Quilt test descriptor.
     * @param myFormatters Formatters pulled off the command line.
     */
    int runWithIt (QuiltTest qt, Vector myFormatters) {
        // DEBUG
        System.out.println("TestRunner.runWithIt - test " + qt.getName());
        // END
        int fmtCount = myFormatters.size();
        Runner runner;
        if (quiltLoader == null) {
            runner = new BaseTestRunner(qt);
        } else {
            runner = new BaseTestRunner(qt, quiltLoader);
        }
        for (int i = 0; i < myFormatters.size(); i++) {
            // it would be cruel to make this a one-liner ;-)
            FmtSelector fs = (FmtSelector) myFormatters.elementAt(i);
            runner.addFormatter ( (Formatter) fs.createFormatter() ); 
        }
        runner.run();
        if (quiltLoader != null) {
            if (stmtReg == null) {
                System.out.println("TestRunner.runWithIt INTERNAL ERROR: "
                    + "no StmtRegistry found");
            } else {
                System.out.println( stmtReg.getReport() );
            }
        }
        return runner.getRetCode();
    }
}
