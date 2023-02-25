/* Textui.java */

package org.quilt.textui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

// XXX remove me
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import org.quilt.cl.QuiltClassLoader;
import org.quilt.cover.stmt.StmtRegistry;
import org.quilt.framework.QuiltTest;
import org.quilt.reports.*;
import org.quilt.runner.*;

/**
 * Template textui test runner.  Need to add main() and runWithIt().
 *
 * @todo Modify to permit types (brief, plain, summary, xml) on the
 *       command line.
 */

public abstract class Textui implements RunnerConst {

    protected QuiltClassLoader quiltLoader = null;

    protected StmtRegistry stmtReg = null;

    // //////////////////////////////////////////////////////////////
    // NEEDS WORK
    // //////////////////////////////////////////////////////////////

    /**
     * Command line usage message.
     *
     * XXX This must be fleshed out.
     */
    public void usage () {
        System.out.println ("usage:\n"
            + "java [javaOptions] testName [testOptions]\n"
            + "where the test options (all optional) are\n"
            + "    checkCoverage={true|false}\n"
            + "    checkIncludes={comma-separated list of class names}\n"
            + "    checkExcludes={comma-separated list of class names}\n"
            + "    haltOnError={true|false}\n"
            + "    haltOnFailure={true|false}\n"
            + "    filtertrace={true|false}\n"
            + "    formatter={className[,outputName]}\n"
            + "    propsfile={pathname}\n"
            + "    showOutput={true|false}\n"
    + "Parameter values are not quoted.  Anything in square brackets []\n"
            + "is optional.  Anything in curly braces {} is required.\n"
            + "\n");
        System.exit(ERRORS);
    }

    // YOU MUST IMPLEMENT A main() LIKE THIS:
    //  public static void main(String[] args) throws IOException {
    //      System.exit( new MockTestRunner().handleArgs(args) );
    //  }

    /**
     * Called by main - pulls arguments off a command line to build a
     * QuiltTest structure and formatter vector.
     *
     * @see QuiltTest
     * @see RunnerConst
     * @param args Usual command line argument vector
     * @return     RunnerConst status codes
     */
    protected int handleArgs (String[] args) {
        int retCode = ERRORS;
        try {
            Vector myFormatters = new Vector();
            if (args.length == 0) {
                usage();
            }
            // our goal here is to populate this structure
            QuiltTest qt = new QuiltTest(args[0]);
            qt.setFork(true);                   // always true if textui

            Properties p = new Properties();
            for (int i = 1; i < args.length; i++) {
                if (args[i].startsWith("checkCoverage=")) {
                    qt.setCheckCoverage (
                                Project.toBoolean(args[i].substring(14)));
                } else if (args[i].startsWith("checkExcludes=")) {
                    qt.setCheckExcludes (args[i].substring(14));
                } else if (args[i].startsWith("checkIncludes=")) {
                    qt.setCheckIncludes (args[i].substring(14));
                } else if (args[i].startsWith("haltOnError=")) {
                    qt.setHaltOnError (
                                Project.toBoolean(args[i].substring(12)));
                } else if (args[i].startsWith("haltOnFailure=")) {
                    qt.setHaltOnFailure (
                                Project.toBoolean(args[i].substring(14)));
                } else if (args[i].startsWith("filtertrace=")) {
                    qt.setFiltertrace (
                                Project.toBoolean(args[i].substring(12)));
                } else if (args[i].startsWith("formatter=")) {
                    // looks like: "formatter=CLASSNAME{.OUTFILENAME}"

                    // //////////////////////////////////////////////
                    // MODIFY TO PERMIT TYPES (brief, plain, summary, xml)
                    // AS WELL AS CLASS NAMES
                    // //////////////////////////////////////////////
                    try {
                        String selector = args[i].substring(10);
                        int commaAt = selector.indexOf(',');
                        FmtSelector fs = new FmtSelector();
                        if ( commaAt < 0) {
                            fs.setClassname (selector);
                        } else {
                            fs.setClassname (selector.substring(0, commaAt));
                            fs.setOutfile (new File(
                                        selector.substring(commaAt + 1)));
                        }
                        myFormatters.addElement ( fs );
                    } catch (BuildException be) {
                        System.err.println(be.getMessage());
                        System.exit(ERRORS);
                    }
                } else if (args[i].startsWith("propsfile=")) {
                    FileInputStream in = new FileInputStream(args[i]
                                                             .substring(10));
                    p.load(in);
                    in.close();
                } else if (args[i].startsWith("showoutput=")) {
                    qt.setShowOutput (Project.toBoolean(args[i].substring(11)));
                }
            }
            Hashtable sysP = System.getProperties();
            Enumeration e  = sysP.keys();
            while ( e.hasMoreElements()) {
                String key = (String)e.nextElement();
                p.put(key, (String)sysP.get(key));
            }
            qt.setProperties(p);
            // set up QuiltClassLoader if appropriate
//          // DEBUG
//          StringBuffer sb = new StringBuffer().append("Textui.handleArgs\n");
//          String [] inc =  qt.getCheckIncludesArray();
//          sb.append("  includes\n");
//          for (int j = 0; j < inc.length; j++)
//              sb.append("    ").append(inc[j]).append("\n");

//          String [] exc =  qt.getCheckExcludesArray();
//          sb.append("  excludes\n");
//          for (int j = 0; j < exc.length; j++)
//              sb.append("    ").append(exc[j]).append("\n");
//          System.out.println(sb.toString());
//          // END

            if (qt.getCheckCoverage() && (qt.getCheckIncludes() != null)) {
                String myClasspath = System.getProperty("java.class.path");
//              // DEBUG
//              System.out.println(
//                  "Textui.handleArgs: setting up QCL with classpath:\n"
//                  + myClasspath);
//              // END
                quiltLoader = new QuiltClassLoader (
                        QuiltClassLoader.cpToURLs(myClasspath),
                        (ClassLoader)null,  // classloader we delegate to
                        (String [])null,       // delegated classes
                        qt.getCheckIncludesArray(),  // needs to be String[]
                        qt.getCheckExcludesArray()); // ditto

                stmtReg = (StmtRegistry) quiltLoader.addQuiltRegistry(
                                        "org.quilt.cover.stmt.StmtRegistry");
            }
//          // DEBUG
//          else {
//              System.out.println("Textui.handleArgs: NOT USING QCL"
//                  + "\n    getCheckCoverage() => " + qt.getCheckCoverage());
//          }
//          // END

            retCode = runWithIt (qt, myFormatters);
        } catch (Exception e) {
            System.out.println("EXCEPTION " + e);
            e.printStackTrace();
            retCode = ERRORS;
        }
        return retCode;
    }
    /**
     * Override this method.  Runs the individual test.
     * @param qt      Quilt test descriptor
     * @param myFormatters Vector of formatters
     */
    int runWithIt (QuiltTest qt, Vector myFormatters) {
        // override this; MockTestRunner should be talkative
        System.out.println (
                "Textui.runWithIt: you should never see this message");
        return ERRORS;
    }
}
