/* StmtRegistry.java */
package org.quilt.cover.stmt;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import org.quilt.cl.*;
import org.quilt.reg.*;

/**
 * <p>Registry for statement coverage information.  As Quilt-instrumented
 * classes are loaded, they register their <code>q$$q</code> hit count
 * arrays and are assigned an ID unique in the life of the registry.
 * The registry maintains: </p>
 * <ul>
 * <li><b>hit counts</b>, keyed on class name</li>
 * <li><b>method end counter indexes</b>, keyed on class and method name</li>
 * <li><b>line number ranges</b>, keyed on class and counter index</li>
 * </ul>
 * <p>This and other information in the registry allows it the generate
 * a number of reports summarizing coverage at</p>
 * <ul>
 * <li><b>package</b> level (soon)</li>
 * <li><b>class</b>  level (now)</li>
 * <li><b>method</b> level (now)</li>
 * <li><b>line</b> level (soonish)</li>
 * </ul>
 * 
 * <p>The registry is associated with the Quilt class loader when it
 * is created.  Information can been retrieved from the registry at 
 * any time.  It will be accumulated as new instances of Quilt-instrumented
 * classes are run.</p>
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class StmtRegistry extends QuiltRegistry {

    /** XXX */
    private static StmtRegistry INSTANCE = null;
   
    /** Returns a reference to the latest instance to announce itself. */
    public static StmtRegistry getInstance () {
        return INSTANCE;
    }

    /** Maps class name to array of names of instrumented methods */
    private Map methodNames = new Hashtable();  // key className, value String[]
    /** Maps class name to array of index of last counters for each method */
    private Map methodEnds  = new Hashtable();  // key className, value int[]

    /** 
     * Constructor specifying Quilt class loader the registry is
     * associated with.
     */
    public StmtRegistry (QuiltClassLoader qcl) {
        super(qcl);
        INSTANCE = this;            // XXX the horror
        ClassAction classAct = new ClassAction(this);
        
        cxf = new ClassXformer[]  { classAct                  };
        mxf = new MethodXformer[] { new MethodAction(this)        };
        gxf = new GraphXformer[]  { new GraphAction(this, classAct) };
        setTransformers();
    }

    /** 
     * Clear counters associated with registry entries.
     */
    public void reset() {
        // XXX DO NOTHING FOR NOW  XXX 
    }

    /**
     * Dump the registry as plain text.  
     * @todo More elaborate reports.
     */
    public String getReport() {
        StringBuffer sb = new StringBuffer()
            .append("\n=========================\n")
            .append(  "  QUILT COVERAGE REPORT \n");
        if (isEmpty()) {
            sb.append("* the registry is empty *\n");
        } else {
            Set keys = keySet();
            Iterator i = keys.iterator();
            while (i.hasNext()) {
                // class --> hit count arrays
                String[] name = (String[]) i.next();
                int [] counts = (int[])get(name);
                int count     = counts.length;
                String className = name[0];
    
                sb.append(className + ": " + count + " counters, "
                    + getClassCoverage(className) + "% coverage\n    ");
//              // DEBUG ONLY: DUMP COUNTER ARRAY
//              for (int k = 0; k < count; k++) {
//                  sb.append("  " + counts[k]);
//              }
//              sb.append("\n");
//              // END
    
                String [] methods = (String[])methodNames.get(className);
                if (methods == null) {
                    sb.append(" NULL\n");
                } else {
                    // there can be more than one <init> method :-(
                    for (int k = 0; k < methods.length; k++) {
                        sb.append("    ").append(methods[k])
                          .append("  ").append(getMethodCoverage(className,k))
                          .append("% coverage\n");
                    }
                }
            } 
        } 
        sb.append("=========================\n");
        return sb.toString();
    }

    /**
     * Get the percentage of counters in the class that have counts
     * greater than zero.  The percentage is rounded down.  If no
     * class information is found, returns zero.
     * 
     * @return an integer between 0 and 100, zero if class not found
     */
    int getClassCoverage (String className) {
        int nonZero = 0;
        int[] hitCounts = (int[]) get(new String[] {className});
        if (hitCounts != null) {
            for (int k = 0; k < hitCounts.length; k++) {
                if (hitCounts[k] > 0) {
                    nonZero++;
                }
            }
            nonZero = (nonZero*100)/hitCounts.length;
        }
        return nonZero;
    }

    /**
     * Get the percentage of counters in the Nth method that have counts
     * greater than zero.  The percentage is rounded down.  If no
     * class or method information is found, returns zero.
     * 
     * XXX The 'methodEnds' array actually contains cumulative counts,
     * so values must be reduced by one.
     * 
     * @return an integer between 0 and 100, zero if class not found
     */
    int getMethodCoverage (String className, int n) {
        int nonZero = 0;
        // XXX should report if either of these two is null or if
        //     cardinalities differ
        int[] hitCounts = (int[]) get(new String[] {className});
        int[] ends      = (int[]) methodEnds.get(className);
        if ( n < 0 || n >= hitCounts.length ) {
            throw new IllegalArgumentException("index out of range");
        }
        int counterCount = 0;
        int lastCounter = ends[n] -1 ;
        int firstCounter = n == 0 ? 0 : ends[n - 1];
        if (hitCounts != null && ends != null) {
            for (int k = firstCounter; k <= lastCounter; k++) {
                counterCount++;
                if (hitCounts[k] > 0) {
                    nonZero++;
                }
            }
            if (counterCount > 0) {
                nonZero = (nonZero*100)/counterCount;
            }
        }
        return nonZero;
    }
        
    // GET/PUT METHODS //////////////////////////////////////////////
    // CLASSID ////////////////////////////////////////////
    private static int nextClassID = 0;

    public int getClassID (String className) {
        int classID = -1;
        try {
            Field qField = Class.forName(className).getField("q$$qID");
            // a trifle uncertain about the argument
            classID = qField.getInt(qField);
        } catch (Exception e) {
            // just ignore any errors
            // DEBUG
            System.out.println("StmtRegistry.getClassID(" 
                                    + className + ") failed - " + e);
            // END
        }
        return classID;
    }
    // HIT COUNTS /////////////////////////////////////////
    /** 
     * Get a reference to the hit count array for a class.
     */
    public int [] getCounts (String className) {
        int[] counts = null;
        try {
            counts = (int[])get ( new String[] {className} );
        } catch (Exception e) {
            // just ignore any errors
            System.out.println("StmtRegistry.getCounts (" 
                + className + ") failed - " + e);
        }
        return counts;
    }
    /** 
     * Register a class by passing a reference to its integer hit count
     * array.  Returns a class ID which is unique within this run.  The
     * ID will be stored in a static in the class, public static int q$$qID
     *
     * XXX So far absolutely no value to using String array as key; could
     * just be String.
     *
     * @param className Name of the class being registered.
     * @param counts    Reference to the class's public static hit count array.
     * @return A unique class ID on success, -1 on failure.
     */
    
    public int registerCounts (String className, int [] counts) {
        int classID = -1;
        try {
            put ( new String[] {className} , counts );
            classID = nextClassID++;
        } catch (Exception e) {
            // just ignore any errors
            System.out.println("StmtRegistry.registerCounts for " 
                + className + ", q$$q) failed - " + e);
        }
        return classID;
    }

    public void registerMethods (String className, String [] methods,
                                                   int [] endCounts) {
        if (className == null || methods == null || endCounts == null) {
            throw new IllegalArgumentException("null parameter");
        }
        methodNames.put ( className, methods );
        methodEnds.put  ( className, endCounts );
    }
    // VERSION ////////////////////////////////////////////
    public int getQuiltVersion (String className) {
        int quiltVersion = -1;
        try {
            Field qField = Class.forName(className).getField("q$$qVer");
            // a trifle uncertain about the argument
            quiltVersion = qField.getInt(qField);
        } catch (Exception e) {
            // just ignore any errors
            // DEBUG
            System.out.println("StmtRegistry.getClassID(" 
                                    + className + ") failed - " + e);
            // END
        }
        return quiltVersion;
    }
    // EPHEMERA ///////////////////////////////////////////
    /** Maps class name to temporary data structure */
    private Map ephemera = new Hashtable();  // key className

    /** get reference to temporary data for class */
    Ephemera getEphemera (String className) {
        if (ephemera.containsKey(className)) {
            return (Ephemera) ephemera.get(className);
        } else {
            return null;
        }
    }
    /** add temporary data for class to registry */
    boolean putEphemera (String className, Ephemera eph) {
        if (ephemera.containsKey(className)) {
            return false;       // operation failed
        }
        // XXX Should allow for failure
        ephemera.put(className, eph);
        return true;
    }
    /** 
     * Remove the reference from the registry.
     *
     * @param className Name of the class we are storing information about
     * @return The reference or null if none was found or null was the 
     *         stored value.
     */
    Ephemera removeEphemera (String className) {
        return (Ephemera) ephemera.remove(className);
    }
    // EXPERIMENT ///////////////////////////////////////////////////
    /** STUB */
    public int registerClass(String name, org.quilt.cover.stmt.QIC junk) {
        System.out.println(
                "**************************"
            + "\nQCL.registerClass " + name
            + "\n**************************" );
        return 52;      // <=================================
    }
    
}
