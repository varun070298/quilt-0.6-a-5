/* Ephemera.java */
package org.quilt.cover.stmt;

import java.util.List;
import java.util.Vector;

/**
 * 
 * @author < a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */

class Ephemera {

    /** Name of the class we are collecting information on */
    private String className;

    /** Number of counters added so far */
    private static int counterCount;

    /** Counter index at the end of each instrumented method */
    private List methodEnds;

    /** Names of methods that have been instrumented */
    private List methodNames;
    
    /** Constructor, initializes class name */
    Ephemera (String name) {
        className    = name;
        counterCount = 0;
        methodEnds   = new Vector();
        methodNames  = new Vector();
    }
            
    /** 
     * Used by GraphXformers to report the cumulative number of
     * counter vertices added after instrumenting method n.
     *
     * @param n     Method index.
     * @param count Cumulative number of counter vertices added.
     */
    void setEndCount (String meName, int count) {
        if (meName == null) {
            throw new IllegalArgumentException("null name");
        }
        if (count < 0) {
            throw new IllegalArgumentException("negative count");
        }
        if (count < counterCount) {
            System.out.println("Ephemera.setEndCount WARNING: "
                + "count now " + counterCount 
                + " but resetting to " + count);
        }
        if (methodEnds == null || methodNames == null) {
            System.out.println(
            "Ephemera.setEndCount INTERNAL ERROR: methodEnds/Names is null");
        } else {
            methodNames.add(meName);
            methodEnds.add(new Integer(count));
        }
        counterCount = count;
    }
    /** @return class name for debugging */
    String getClassName() {
        return className;
    }
    /** @return cumulative counter count */
    int getCounterCount() {
        return counterCount;
    }
    /** @return a list of names of methods seen so far */
    List getMethodNames () {
        return methodNames;
    }
    /** @return a list of cumulative counter counts after instrumenting */
    List getMethodEnds () {
        return methodEnds;
    }
}
