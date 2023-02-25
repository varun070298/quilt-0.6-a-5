/* CounterVertex.java */
package org.quilt.cover.stmt;

import org.apache.bcel.generic.*;
import org.quilt.cl.*;
import org.quilt.graph.*;

/**
 * <p>A CodeVertex which carries counter instrumentation and a label.
 * The counter has an index.  Whenever the flow of execution passes
 * through this vertex, the counter code adds 1 to the hit count table, 
 * to <code>q$$q[n]</code>, where <code>n</code> is the counter index.</p>
 *
 * <p>Counter indexes are unique and assigned consecutively.  They are
 * not the same as vertex indexes.  In the current revision of the 
 * software, counter vertices are also labeled with the counter index.</p>
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class CounterVertex extends CodeVertex {
   
    /**
     * Create a code vertex with default bytecode offset, line number,
     * empty instruction list, and no label.
     *
     * @param g Graph which the vertex belongs to.
     */
    public CounterVertex (ControlFlowGraph g) {
        super(g);
    }
    /**
     * Create a counter vertex, specifying a label
     *
     * @param g Graph which the vertex belongs to.
     * @param l The String label applied to the vertex.
     */
    public CounterVertex (ControlFlowGraph g, String lbl) {
        this(g);
        label_ = lbl;
    }

    // OTHER METHODS ////////////////////////////////////////////////
    /** 
     * @return Graph index and Vertex index in a neatly formatted String,
     *         including the label if there is one, *not* newline-terminated.
     */
    public String toString () {
        StringBuffer sb = new StringBuffer().append("Counter ")
                                .append(getGraph().getIndex()) 
                                .append(":").append(getIndex());
        if (label_ != null) {
            sb.append(" {").append(label_).append("}");
        }
        return sb.toString();
    }
    /** 
     * @param  b If true, add label (if any) and instruction list.
     * @return A neatly formatted String ending with a newline. 
     */
    public String toString (boolean b) {
        
        StringBuffer sb = new StringBuffer().append(toString());
        if (b) {
            sb.append("\n    ilist: ");
            InstructionHandle ih = ilist.getStart();
            while ( ih != null) {
                sb.append(ih.getInstruction());
            }
        }
        return sb.toString();
    }
}
