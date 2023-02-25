/* GraphXformer.java */
package org.quilt.cl;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;

/**
 * Transform the graph, possibly using information from ClassGen 
 * and MethodGen.  If fatal errors occur, transformers must issue
 * a warning message on System.err and either undo any changes or
 * set cfg to null.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public interface GraphXformer {
    
    /** 
     * Apply the transformation to the graph. 
     *
     * @param cg     The class being transformed.
     * @param method MethodGen for the method being transformed.
     * @param cfg    The method's control flow graph.
     */
    public void xform (final ClassGen cg, final MethodGen method, 
                                          final ControlFlowGraph cfg);
    /** 
     * Get the name for the transformation. This is a static and so
     * can't be declared here, but implement it.
     */
    // public static String getName();

    /** 
     * Set a name for the transformation, to allow reports to refer
     * to it.
     */
    // public static void setName(String name);
}
