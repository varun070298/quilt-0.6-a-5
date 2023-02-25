/* GraphTalker.java */
package org.quilt.cl;

import org.apache.bcel.generic.*;
import org.quilt.graph.*;

/**
 * Walks through a control flow graph, displaying information about
 * each vertex and edge.  Useful for debugging and as a model 
 * GraphXformer implementation.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class GraphTalker implements GraphXformer, org.quilt.graph.Visitor {

    private static String name = null;

    public GraphTalker () { }

    // INTERFACE VISITOR ////////////////////////////////////////
    public void discoverGraph (Directed graph) {
        ControlFlowGraph g = (ControlFlowGraph) graph;
        System.out.println("--------------------------------------" );
        if (g.getParent() != null) 
            System.out.print("SUB");
        System.out.println("GRAPH with " + g.size() + " vertices");
    }
    public void finishGraph (Directed graph) {
        System.out.println("--------------------------------------" );
    }
    public void discoverVertex (Vertex v) {
        Connector conn = v.getConnector();
        // MAKE THIS A STRINGBUFFER
        StringBuffer sb = new StringBuffer().append("VERTEX ").append(v);
        if (v instanceof Entry) {
            ControlFlowGraph parent 
                            = (ControlFlowGraph)v.getGraph().getParent();
            sb.append("  graph " ).append(v.getGraph().getIndex())
                .append(" whose parent is graph ");
            if (parent == null) {
                sb.append ("<null>\n");
            } else {
                sb.append(parent.getIndex()).append("\n");
            }
            if (conn instanceof ComplexConnector) {
                int k = conn.size();
                sb.append("    HANDLERS\n");
                for (int i = 0; i < k; i++) {
                    sb.append("        -> ")
                      .append(((ComplexConnector)conn).getEdge(i).getTarget())
                      .append("\n");
                }
            }
        } else if (v instanceof Exit) {
            sb.append("  EXIT\n");
        } else {
            CodeVertex cv = (CodeVertex) v;
            sb.append("\n    instructions:\n");
            InstructionList ilist = cv.getInstructionList();
            Instruction[] inst = ilist.getInstructions();
            if (inst.length == 0) 
                sb.append("        none\n");
            for (int i = 0; i < inst.length; i++) {
                sb.append("    ").append(inst[i]).append("\n");
            }
            Instruction connInst = cv.getConnInst();
            if (connInst == null) {
                sb.append(
                    "    NO CONNECTING INSTRUCTION (flows through)\n");
            } else {
                sb.append("    CONNECTING INST: ")
                                        .append(connInst).append("\n");
            } 
        }
        System.out.print(sb.toString());
    }
    public void finishVertex (Vertex v) {
    }
    public void discoverEdge (Edge e) {
        System.out.println("  EDGE " + e.getSource() 
                                            + " -> " + e.getTarget() );
    }
    public void finishEdge (Edge e) {
    }
    
    // INTERFACE GRAPHXFORMER ///////////////////////////////////
    public void xform (final ClassGen cg, final MethodGen method, 
                                                    ControlFlowGraph cfg) {
        Walker walker = new Walker();
        walker.visit (cfg, this);
    }
    public static String getName() {
        return name;
    }
    public static void setName (String s) {
        name = s;
    }
} 
