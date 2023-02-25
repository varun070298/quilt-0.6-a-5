/* GraphSpy.java */
package org.quilt.cl;

import org.apache.bcel.generic.*;
import org.quilt.graph.*;

/**
 * Makes the control flow graph available to a wider audience.  
 * Used in testing.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class GraphSpy implements GraphXformer {
    
    private static String name;
    
    public static ControlFlowGraph theGraph = null;
    
    public GraphSpy () { }

    public static ControlFlowGraph getTheGraph() {
        return theGraph;
    }
    // INTERFACE GRAPHXFORMER ///////////////////////////////////
    public void xform (final ClassGen cg, final MethodGen method, 
                                                ControlFlowGraph cfg) {
//      System.out.println(
//              "==================\n" 
//          +   "Hello from " + name 
//          + "\n==================" );
        theGraph = cfg;
    }
    public static String getName() {
        return name;
    }
    public static void setName (String s) {
        name = s;
    }
} 
