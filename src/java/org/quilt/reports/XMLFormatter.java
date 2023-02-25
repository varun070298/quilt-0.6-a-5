/* XMLFormatter.java */

package org.quilt.reports;

import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.DOMElementWriter;

import junit.framework.*;

import org.quilt.framework.*;
import org.quilt.runner.Runner;

/** 
 * Produce an XML document containing the test data for the run.
 * This will in general contain the results of many tests, but 
 * only those resulting from one QuiltTest.
 *
 * @todo Restructure to produce one XML document for the entire 
 *       Ant/Quilt run.  This needs to be held by the class 
 *       managing the whole run, QuiltTask if it is an Ant run.
 *       Simplest solution: when the test is not forked, pass back
 *       the document as a tree instead of serializing it in text
 *       form to output.  The manager can then merge the trees and
 *       output a single document for the run.
 *
 * @todo Add flag to suppress generation of (quite verbose) properties 
 *       element.
 */
public class XMLFormatter implements Formatter {

    /** Whether to filter Ant/Quilt/JUnit stack traces. */
    private boolean filtertrace = false;
   
    // MODIFY TO EXTEND BaseFormatter, then drop this ///////////////
    /** 
     * Root around in a junit Test and find a name, should there be one.
     * @return Test/suite name.
     */
    protected static String getTestName (Test test) {
        if ( test instanceof TestSuite ) {
            return ( (TestSuite) test ) . getName();
        } else if ( test instanceof TestCase ) {
            return ( (TestCase) test ) . getName();
        } else {
            return "unknown";
        }
    }
    // END BaseFormatter CODE ///////////////////////////////////////
   
    private static DocumentBuilderFactory dbf = null;

    // none of this is thread safe; JavaDocs recommend ensuring that
    // there is only one DocumentBuilder per thread
    private static DocumentBuilder getDocumentBuilder() {
        try {
            if (dbf == null) {
                dbf = DocumentBuilderFactory.newInstance();
            }
            return dbf.newDocumentBuilder();
        } catch (Exception exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }

    /** 
     * The XML document. Unfortunately one XML Document is produced
     * per QuiltTest.  Generally there will be many QuiltTests per run.
     */
    private Document doc;
    /** Where the output goes. */
    private OutputStream out;
    /** Root node, the Ant/Quilt test run as a whole. */
    private Element rootNode;
    /** The runner, usually an instance of runner.BaseTestRunner. */
    private Runner runner = null;
    /** Nodes in the document that tests hang off of. */
    private Hashtable testNodes = new Hashtable();  // key = Test test
    /** Hash holding information about individual tests. */
    private Hashtable testStarts = new Hashtable();

    public XMLFormatter() {}

    // FORMATTER INTERFACE //////////////////////////////////////////
    
    /** Method called at end of test run. */
    
    public void endTestSuite(QuiltTest qt) throws BuildException {
        rootNode.setAttribute("tests",      "" + qt.runCount());
        rootNode.setAttribute("errors",     "" + qt.errorCount());
        rootNode.setAttribute("failures",   "" + qt.failureCount());
        rootNode.setAttribute("time",   "" + (qt.getRunTime() / 1000.0));
        if (out != null) {
            Writer wri = null;
            try {
                wri = new OutputStreamWriter(out, "UTF8");
                wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                (new DOMElementWriter())
                    .write(rootNode, wri, 0, "  ");
                wri.flush();
            } catch (IOException e) {
                throw new BuildException("Unable to write log file", e);
            } finally {
                if (out != System.out && out != System.err) {
                    if (wri != null) {
                        try {
                            wri.close();
                        } catch (IOException e) {}
                    }
                }
            }
        }
    } 
    /** Enable filtering of Ant/Quilt/JUnit lines from stack traces. */
    public void setFiltertrace (boolean b) {
        filtertrace = b; 
    }
    /** Set the output file. */
    public void setOutput(OutputStream out) {
        this.out = out;
    }
    /** Set the test runner to be used. */
    public void setRunner(Runner testrunner) {
        runner = testrunner;
    }
    /** Direct the error output. */
    public void setSystemError(String out) {
        formatOutput("system-err", out);
    }
    /** Direct standard output. */
    public void setSystemOutput(String out) {
        formatOutput("system-out", out);
    }
    /** Method called at the beginning of the test run. */
    public void startTestSuite(QuiltTest qt) {
        doc = getDocumentBuilder().newDocument();
        rootNode = doc.createElement("testsuite");
        rootNode.setAttribute("name", qt.getName());

        // Output properties - this creates a lot of meaningless
        // data, far exceeding the JUnit data of real interest.
        Element propsElement = doc.createElement("properties");
        rootNode.appendChild(propsElement);
        Properties props = qt.getProperties();
        if (props != null) {
            Enumeration e = props.propertyNames();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                Element propElement = doc.createElement("property");
                propElement.setAttribute("name", name);
                propElement.setAttribute("value", props.getProperty(name));
                propsElement.appendChild(propElement);
            }
        }
    }

    // TESTLISTENER INTERFACE ///////////////////////////////////////
    /** Method called when an unexpected error occurs. */
    public void addError(Test test, Throwable t) {
        formatError("error", test, t);
    }
    /** Method called when a failure (or unexpected error) occurs. */
    public void addFailure(Test test, Throwable t) {
        formatError("failure", test, t);
    }
    /** Method called when a failure (or unexpected error) occurs. */
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }
    /** Method called when a JUnit test ends. */
    public void endTest(Test test) {
        Element testNode = (Element) testNodes.get(test);
        if (testNode == null) {
            startTest(test);
            testNode = (Element) testNodes.get(test);
        }
        Long l = (Long) testStarts.get(test);
        testNode.setAttribute("time",
            "" + ((System.currentTimeMillis() - l.longValue()) / 1000.0));
    } 
    /** Called at the beginning of a JUnit test. */
    public void startTest(Test test) {
        testStarts.put(test, new Long(System.currentTimeMillis()));
        Element testNode = doc.createElement("testcase");
        testNode.setAttribute("name", getTestName(test));
        rootNode.appendChild(testNode);
        testNodes.put(test, testNode);
    }

    // OTHER METHODS ////////////////////////////////////////////////
    /** Hang an error message off the document tree. */
    private void formatError(String type, Test test, Throwable t) {
        if (test != null) {
            endTest(test);
        }
        Element msgNode = doc.createElement(type);
        Element curNode = null;
        if (test != null) {
            curNode = (Element) testNodes.get(test);
        } else {
            curNode = rootNode;
        }

        curNode.appendChild(msgNode);

        String message = t.getMessage();
        if (message != null && message.length() > 0) {
            msgNode.setAttribute("message", t.getMessage());
        }
        msgNode.setAttribute("type", t.getClass().getName());

        String strace = runner.getFilteredTrace(t, filtertrace);
        Text trace = doc.createTextNode(strace);
        msgNode.appendChild(trace);
    }
    /** 
     * Hang a text message off the document tree.  The message 
     * might be quite long, for example all System.err output
     * for the run.
     */
    private void formatOutput(String type, String output) {
        Element txtNode = doc.createElement(type);
        rootNode.appendChild(txtNode);
        txtNode.appendChild(doc.createCDATASection(output));
    }
} 
