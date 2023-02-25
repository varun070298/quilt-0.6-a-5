/* QuiltClassLoader.java */
package org.quilt.cl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

// DEBUG
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
// END

import org.quilt.reg.QuiltRegistry;

/**
 * <p>Quilt's transforming class loader.  Can be directed to instrument
 * a set of classes, matching class names against a list of prefixes
 * and another list excluding classes from instrumentation, the
 * exclusion list taking priority.  Will delegate loading to a parent
 * class loader where explicitly directed to; otherwise will be the
 * defining loader.  By default the loading of classes whose names
 * begin with <tt>java., javax., junit., org.apache.bcel., 
 * org.apache.tools.ant.</tt> and <tt>org.quilt.</tt> is delegated.</p>
 *
 * <p>Classes whose names begin with a reserved prefix, currently
 * <tt>test.data.Test</tt>, are synthesized instead of being
 * loaded.  This must be specifically enabled.</p>
 *
 *
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 *
 * @see ClassFactory
 */
public class QuiltClassLoader extends URLClassLoader {

    /** Operating system specific */
    public static final char   FILE_PATH_DELIM_CHAR = File.separatorChar;
    public static final String FILE_PATH_DELIM_STR  = File.separator;
    public static final char   CLASSPATH_DELIM_CHAR = File.pathSeparatorChar;
    public static final String CLASSPATH_DELIM_STR  = File.pathSeparator;

    /** 
     * Names of classes which must be loaded by the parent. There is one
     * exception to this list: org.quilt.QIC, which is not delegated and
     * not instrumented.
     */
    public static final String[] DELEGATED =
        {"java.", "javax.", "junit.", "org.apache.bcel.",
         "org.apache.tools.ant.", "org.quilt.", "sun."};

    /** XXX This is misleading! What's wanted is a copy. */
    private String[] dels = DELEGATED;

    private List delegated = new Vector();
    /**
     * Names of classes NOT to be instrumented. Names are matched
     * as above.  The excluded list is consulted first.
     */
    private List excluded  = new Vector();

    /**
     * Names of classes to be instrumented. At this time no
     * wildcards are permitted.  Any class whose name begins
     * with a string in the array will be instrumented,
     * unless it is on the excluded list.
     */
    private List included  = new Vector();

    /**
     * URLs in the order in which they are to be searched.  Those
     * ending in '/' are directories.  Any others are jars.
     */
    private List classPath    = new Vector();

    /** Delegation class loader. Unless a class is to be instrumented
     * (is on the inclusion list and not on the exclusion list),
     * loading will be delegated to this class loader.
     */
    private ClassLoader parent = null;

    /** Prefix indicating that the class should be synthesized. */
    public static final String SYNTH_PREFIX = "test.data.Test";
    private String synthPrefix   = SYNTH_PREFIX;
    private boolean synthEnabled = false;

    /** Responsible for instrumenting classes. */
    public ClassTransformer xformer = null;
    /** Configurable class transformers. */
    List cxf = new Vector();
    /** Configurable method transformers. */
    List mxf = new Vector();
    /** Configurable graph transformers. */
    List gxf = new Vector();
    /** QuiltRegistry list. */
    List regList = new Vector();

    /** Constructor with abbreviated argument list. */
    public QuiltClassLoader (URL[] cp, String [] inc) {
        this(cp, null, null, inc, null);
    }
    /**
     * Constructor with full argument list.
     *
     * @param cp     Class path, an array of paths
     * @param parent Class loader which we delegate to.
     * @param del    String array, names of classes to be delegated
     * @param inc    String array, names of classes to be instrumented
     * @param exc    String array, names of classes not to be instrumented.
     */
    public QuiltClassLoader (URL[] cp, ClassLoader parent,
                            String [] del, String [] inc, String [] exc) {
        super(cp == null ? new URL[0] : cp, parent);

        if (cp != null) {
            for (int i = 0; i < cp.length; i++) {
                classPath.add(cp[i]);
            }
        }
        if (parent == null) {
            this.parent = getSystemClassLoader();
        } else {
            this.parent = parent;
        }
        for (int i = 0; i < dels.length; i++) {
            delegated.add( dels[i] );
        }
        if (del != null) {
            for (int i = 0; i < del.length; i++ ) {
                delegated.add(del[i]);
            }
        }
        if (inc != null) {
            for (int i = 0; i < inc.length; i++) {
                included.add(inc[i]);
            }
        }
        if (exc != null) {
            for (int i = 0; i < exc.length; i++) {
                excluded.add(exc[i]);
            }
        }
    }
    /** Do we delegate loading this to the parent? */
    private boolean delegateTheClass (final String name) {
        if (name.equals("org.quilt.QIC")) {
            return false;
        }
        for (int i = 0; i < delegated.size(); i++) {
            if (name.startsWith( (String)delegated.get(i)) ) {
                return true;
            }
        }
        return false;
    }
    /** Should class be instrumented? */
    private boolean instrumentTheClass (final String name) {
        if (name.equals("org.quilt.QIC")) {
            return false;
        }
        for (int i = 0; i < excluded.size(); i++) {
            if ( name.startsWith ( (String)excluded.get(i) ) ) {
                return false;
            }
        }
        for (int i = 0; i < included.size(); i++) {
            if ( name.startsWith ( (String)included.get(i) ) ) {
                return true;
            }
        }
        return false;
    }
    /**
     * Convert a class name into a file name by replacing dots with
     * forward slashes and appending ".class".
     */
    public static String classFileName (final String className) {
         return className.replace('.', FILE_PATH_DELIM_CHAR) + ".class";
    }
    /**
     * Class loader.  Delegates the loading if specifically instructed
     * to do so.  Returns the class if it has already been loaded.
     * Otherwise creates a class transformer if necessary and then
     * passes the name to <code>findClass.</code>
     */
    public synchronized Class loadClass (String name)
                                        throws ClassNotFoundException {
        if (name == null) {
            throw new IllegalArgumentException("null class name");
        }
        if (delegateTheClass(name)) {
            // DEBUG
            // System.out.println("QCL.loadClass: delegating " + name);
            // END
            return parent.loadClass(name);
        }
        Class c = findLoadedClass (name);
        if (c != null) {
            return c;
        }
        if (xformer == null) {
            xformer = new ClassTransformer( cxf, mxf, gxf );
        }
        return findClass (name);
    }
    /**
     * Locate the class whose name is passed and define it. If the
     * class name has the appropriate prefix and synthesizing it is
     * enabled, it synthesizes it.  Otherwise it searches for it
     * along the class path.  If indicated, it transforms (instruments)
     * the class.  Finally, it defines and returns the result.
     *
     * @param name Class name in embedded dot (.) form.
     */
    protected Class findClass (String name)
                                        throws ClassNotFoundException {
        // we only instrument the class if we have transformers at
        // class, method, or graph level
        boolean instIt  = instrumentTheClass(name)
                  && (cxf.size() > 0 || mxf.size() > 0 || gxf.size() > 0);
        byte [] b = null;
        if ( name.startsWith ( synthPrefix )) {
            JavaClass jc = ClassFactory.getInstance()
                                .makeClass(name, classFileName(name))
                                .getJavaClass();
            if (instIt) {
                jc = xformer.xform(jc);
            }
            b = jc.getBytes();  // convert it into a byte array
        } else {
            // DEBUG
            //System.out.println("QCL.findClass: locating " + name);
            // END
            try {
                b = getClassData (name);
                if (instIt) {
                    // DEBUG
                    // System.out.println("QCL.findClass: instrumenting " + name);
                    // END
                    // convert to bcel JavaClass -
                    //      throws IOException, ClassFormatException
                    JavaClass jc = new ClassParser (
                        new ByteArrayInputStream(b), classFileName(name) )
                            .parse();
                    JavaClass temp = xformer.xform (jc);
//                  // DEBUG
//                  Field [] myFields  = temp.getFields();
//                  StringBuffer fieldData = new StringBuffer();
//                  for (int k = 0; k < myFields.length; k++)
//                      fieldData.append("  ")
//                          .append(myFields[k]).append("\n");

//                  Method[] myMethods = temp.getMethods();
//                  StringBuffer methodData = new StringBuffer();
//                  for (int k = 0; k < myMethods.length; k++)
//                      methodData.append("  ")
//                          .append(myMethods[k].getName()).append("\n");

//                  System.out.println(
//                      "QCL.findClass after instrumenting JavaClass for "
//                          + name
//                          + "\nFIELDS  (" + myFields.length + ") :\n"
//                              + fieldData.toString()
//                          +   "METHODS (" + myMethods.length + ") :\n"
//                              + methodData.toString()     );
//                  // END

                    //b = xformer.xform (jc).getBytes();
                    b = temp.getBytes();
                }
            } catch (IOException e) {
                e.printStackTrace();    // DEBUG
                throw new ClassNotFoundException(name, e);
            }
        }

        // this can throw a ClassFormatError or IndexOutOfBoundsException
        return defineClass (name, b, 0, b.length);
    }
    /** @return Classpath as a newline-terminated String. */
    public String urlsToString(){
        StringBuffer sb = new StringBuffer() .append("classpath:\n");
        URL[] urls = getURLs();
        for (int k = 0; k < urls.length; k++) {
            sb.append("    ").append(k).append("  ")
                                       .append(urls[k]).append("\n");
        }
        return sb.toString();
    }
    /** Find a class along the class path and load it as a byte array. */
    protected byte[] getClassData (String className)
                                            throws IOException {
        URL fileURL = findResource ( classFileName(className) );
        // DEBUG XXX
        if (fileURL == null) {
            System.err.println("QCL.getClassData mapping " + className
                + " to " + classFileName(className) );
            System.err.println("    findResource returned null\n"
                + urlsToString());
        }
        // END
        if (fileURL == null) {
            // ClassNotFoundException();
            throw new IOException("null fileURL for " + className);
        }
        InputStream ins = fileURL.openStream();
        ByteArrayOutputStream outs = new ByteArrayOutputStream (65536);
        byte [] buffer = new byte [4096];
        int count;
        while ( (count = ins.read(buffer)) != -1 ) {
            outs.write(buffer, 0, count);
        }
        return outs.toByteArray();
    }

    // ADD/GET/SET METHODS //////////////////////////////////////////
    /**
     * Add a path to the class loader's classpath.
     * @param url Path to be added.
     */
    public void addPath (URL url) {
        classPath.add(url);
    }
    /** @return The classpath used by this QuiltClassLoader. */
    public URL[] getClassPath() {
        URL[] myURLs = new URL[ classPath.size() ];
        return (URL[]) (classPath.toArray(myURLs));
    }
    /** 
     * Convert domain name in classpath to file name, allowing for
     * initial dots.  Need to cope with ../../target/big.jar and
     * similar constructions.
     */
    public static final String THIS_DIR = "."  + FILE_PATH_DELIM_STR;
    public static final String UP_DIR   = ".." + FILE_PATH_DELIM_STR;
    public static final int    THIS_DIR_LEN = THIS_DIR.length();
    public static final int    UP_DIR_LEN   = UP_DIR.length();

    /** 
     * Convert a dotted domain name to its path form, allowing for
     * leading ./ and ../ and terminating .jar
     */
    public static String domainToFileName (String name) {
        // ignore any leading dots
        int startNdx;
        for (startNdx = 0; startNdx < name.length(); ) {
            if ( name.substring(startNdx).startsWith(THIS_DIR)) {
                startNdx += THIS_DIR_LEN;
            } else if (name.substring(startNdx).startsWith(UP_DIR)) {
                startNdx += UP_DIR_LEN;
            } else {
                break;
            }
        }
        // leave .jar intact
        int endNdx;
        if ( name.endsWith(".jar") ){
            endNdx = name.length() - 4;
        } else {
            endNdx = name.length();
        }
        
        StringBuffer sb = new StringBuffer();
        if (startNdx > 0) {
            sb.append(name.substring(0, startNdx));
        }
        sb.append(name.substring(startNdx, endNdx)
                                    .replace('.', FILE_PATH_DELIM_CHAR));
        if (endNdx != name.length()) {
            sb.append(".jar");
        }
        return sb.toString();
    }
    /**
     * Convert classpath in normal form to URL[]
     */
    public static URL[] cpToURLs (String cp ) {
        URL[] urls;
        if (cp == null) {
            urls = new URL[0];
        } else {
            String [] elements = cp.split(":");
            List urlList = new Vector();
            int urlCount = 0;
            for (int i = 0; i < elements.length; i++) {
                String noDots = domainToFileName(elements[i]);
                boolean foundJar = noDots.endsWith(".jar");
                File file      = new File (noDots);
                String urlForm = "file://" + file.getAbsolutePath();
                if (!foundJar && !urlForm.endsWith(FILE_PATH_DELIM_STR)) {
                    urlForm += FILE_PATH_DELIM_STR;
                }
                try {
                    URL candidate = new URL(urlForm);
                    urlCount++;         // didn't throw exception
                    urlList.add(candidate);
                } catch (MalformedURLException e) {
                    System.err.println ("WARNING: ignoring malformed URL "
                        + urlForm);
                }
            }
            urls = new URL[urlCount];
            for (int k = 0; k < urls.length; k++) {
                urls[k] = (URL)  urlList.get(k);
            }
        }
        return urls;
    }
    /**
     * Convert classpath in normal form to URL[] and sets loader
     * classpath to the corresponding value.
     *
     * @param cp   Class path in colon- or semicolon-delimited form.
     */
    public void setClassPath (String cp) {
        classPath.clear();
        URL[] urls = cpToURLs(cp);
        for (int i = 0; i < urls.length; i++) {
            classPath.add(urls[i]);
            addURL( urls[i] );
        }
//      // DEBUG
//      System.out.println("after setting classpath, new classpath is:");
//      URL[] currURLs = getURLs();
//      for (int k = 0; k < currURLs.length; k++) {
//          System.out.println("  " + k + "  " + currURLs[k].getPath() );
//      }
//      // END
    }
    /**
     * Add a class name prefix to the list of those to be delegated
     * to the parent.
     * @param prefix Prefix to be added.
     */
    public void addDelegated (final String prefix) {
        delegated.add(prefix);
    }
    /**
     * @return As a String array the list of class name prefixes
     *          whose loading is to be delegated to the parent.
     */
    public String[] getDelegated() {
        String[] myDels = new String[ delegated.size() ];
        return (String[]) (delegated.toArray(myDels));
    }

    /**
     * Add a class name prefix to the list of those to be excluded
     * from instrumentation.
     *
     * @param prefix Prefix to be added.
     */
    public void addExcluded (final String prefix) {
        excluded.add(prefix);
    }
    /**
     * @return As a String array the list of class name prefixes
     *          which are NOT to be instrumented.
     */
    public String[] getExcluded() {
        String[] myExc = new String [ excluded.size() ];
        return (String[]) (excluded.toArray(myExc));
    }
    /**
     * Sets the list of classes to be excluded from instrumentation.
     *
     * @param s List of classes in comma-separated String form.
     */
    public void setExcluded(String s) {
        excluded.clear();
        if (s != null) {
            String [] newExc = s.split(",");
            for (int i = 0; i < newExc.length; i++) {
                excluded.add ( newExc[i] );
            }
        }
    }
    /**
     * Add a class name prefix to the list of those to be
     * instrumented.
     *
     * @param prefix Prefix to be added.
     */
    public void addIncluded (final String prefix) {
        included.add(prefix);
    }
    /**
     * @return As a String array the list of class name prefixes
     *          which ARE to be instrumented.
     */
    public String[] getIncluded() {
        String[] myInc = new String [ included.size() ];
        return (String[]) (included.toArray(myInc));
    }
    /**
     * Sets the list of classes to be instrumented.
     *
     * @param s List of classes in comma-separated String form.
     */
    public void setIncluded(String s) {
        included.clear();
        if (s != null) {
            String [] newInc = s.split(",");
            for (int i = 0; i < newInc.length; i++) {
                included.add ( newInc[i] );
            }
        }
    }
    /** Get synthesizing-enabled flag. */
    public boolean getSynthEnabled() {
        return synthEnabled;
    }
    /** Enable class synthesizing. */
    public void setSynthEnabled (boolean b) {
        synthEnabled = b;
    }

    /**
     * @return The prefix signifying that a class is to be synthesized.
     */
    public String getSynthPrefix() {
        return synthPrefix;
    }
    /** Add a class transformer. */
    public void addClassXformer( ClassXformer xf) {
        cxf.add (xf);
    }
    /** Add a method transformer. */
    public void addMethodXformer (MethodXformer xf) {
        mxf.add (xf);
    }
    /** Add a graph transformer. */
    public void addGraphXformer (GraphXformer xf) {
        gxf.add (xf);
    }

    /** Map of registries by String name. */
    public Map regMap = new Hashtable();

    /** Get a reference to a Quilt registry. */
    public QuiltRegistry getRegistry (String regName) {
        QuiltRegistry qr = null;
        if (regMap.containsKey(regName)) {
            qr = (QuiltRegistry) regMap.get (regName);
        }
        return qr;
    }
    /**
     * Add a new QuiltRegistry to the list.  An example of the
     * argument is "org.quilt.cover.stmt.StmtRegistry".
     *
     * @param regName The domain name of the registry in dotted form.
     */
    public QuiltRegistry addQuiltRegistry (String regName) {
        QuiltRegistry qr = null;
        if (regMap.containsKey(regName)) {
            qr = (QuiltRegistry) regMap.get(regName);
        } else try {
            Class o = Class.forName(regName, false, parent);
            Constructor con = o.getConstructor( new Class[] {
                                                    QuiltClassLoader.class });
            qr = (QuiltRegistry)con.newInstance(new Object[] {this});
            regList.add(qr);
            regMap.put (regName, qr);
        } catch (Exception e) {
            System.out.println (
                "\nQuiltClassLoader.addQuiltRegistry:"
                + "\n    EXCEPTION while trying to add " + regName
                + "\n    Is it on the parent's CLASSPATH?"
                + "\n    Exception: " + e);
        }
        return qr;
    }
    /**
     * Get reports from any or all registries.  XXX This should not
     * be returning a String -- it might be huge.
     */
    public String getReport() {
        StringBuffer sb = new StringBuffer();
        if (!regList.isEmpty()) {
            Iterator i = regList.iterator();
            while (i.hasNext()) {
                QuiltRegistry reg = (QuiltRegistry)i.next();
                sb.append(reg.getReport());
            }
        }
        return sb.toString();
    }
}
