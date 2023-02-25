/* MockTestRunner.java */

package org.quilt.textui;

import java.util.Vector;

import org.quilt.framework.QuiltTest;
import org.quilt.runner.*;

/** Minimal test runner; just echoes the test description. */

public class MockTestRunner extends Textui implements RunnerConst {

    public MockTestRunner () { }
   
    public static void main (String [] args ) {
        System.exit( new MockTestRunner().handleArgs(args) );
    }

    int runWithIt (QuiltTest qt, Vector myFormatters) {
        int fmtCount = myFormatters.size();
        System.out.println ("==== MockTestRunner ====\n"
            + "    there are " + fmtCount + " formatters\n"
            + qt );
        return SUCCESS;
    }
}
