/* BytecodeCollector.java */

package org.quilt.cl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

import org.quilt.graph.*;

/**
 * Collects the bytecode for a method while walking the method
 * control flow graph.
 *
 * Inspired by Quilt 0.5's BytecodeLayout,
 * @author <a href="mailto:ddp@apache.org">David Dixon-Peugh</a> and the
 * rest of the Quilt development team.
 *
 * However, errors and omissions in this version are entirely due to
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class BytecodeCollector implements org.quilt.graph.Visitor {

    private ControlFlowGraph graph = null;

    private Map startHandles      = null;
    private Map endHandles        = null;
    private Map gotoFixMeUps      = null;
    private InstructionList ilist = null;

    /** No-arg constructor. */
    public BytecodeCollector() { }

    /**
     * Action taken upon beginning to visit the graph.
     * @param g Directed graph to be visited.
     */
    public void discoverGraph( Directed g ) {
        graph         = (ControlFlowGraph) g;
        ilist         = graph.getInstructionList();
        startHandles  = graph.getStartHandles();
        endHandles    = graph.getEndHandles();
        gotoFixMeUps  = graph.getGotoFixMeUps();
    }
    /**
     * Action taken upon beginning to visit a graph vertex.  Unless
     * this is an Entry or Exit vertex, save the starting position in
     * the bytecode to give a offset for edges with this vertex as target.
     * Save the end instruction in the bytecode so that we can insert any
     * connector code their later, in finishVertex.
     *
     * Entry and Exit vertices carry no bytecode and never have a
     * connecting instruction, so they can be ignored when collecting
     * code.
     *
     * @param v The vertex; carries a block of code and a final
     *          instruction in its Connector.
     */
    public void discoverVertex(Vertex vtx) {
        if (vtx == null) {
            throw new IllegalArgumentException ("null vertex");
        }
        // no code in Entry or Exit vertices
        if (! (vtx instanceof Entry || vtx instanceof Exit) ) {
            CodeVertex v = (CodeVertex) vtx;
            InstructionList   vIList = v.getInstructionList();
            InstructionHandle vStart = null;

            if (vIList.size() == 0) {
                vIList = new InstructionList ( new NOP() );
            }
            vStart = ilist.append(vIList);  // consumes vIList
            if (vStart == null) {
                System.out.println("discoverVertex INTERNAL ERROR: vertex "
                    + v + " has null position in bytecode\n");
            }
            // DEBUG
            // System.out.println("discoverVertex " + v + " - adding vStart");
            if (vStart == null) {
                System.out.println("    ** vStart is null **");
            }
            // END
            startHandles.put(v, vStart);    // v a CodeVertex

            Instruction inst = v.getConnInst();
            if (inst != null) {
                // CONSOLIDATE THESE ////////////////////////////////
                if (inst instanceof GotoInstruction) {
                    BranchHandle bh = ilist.append ((GotoInstruction)inst);
                    endHandles.put (v, bh);
                } else if (inst instanceof IfInstruction) {
                    BranchHandle bh = ilist.append((IfInstruction) inst);
                    endHandles.put (v, bh);
                } else if ( inst instanceof JsrInstruction ) {
                    BranchHandle bh = ilist.append((JsrInstruction) inst);
                    endHandles.put (v, bh);
                } else if ( inst instanceof Select) {
                    BranchHandle bh = ilist.append((Select) inst);
                    endHandles.put  (v, bh );
                } else if ( (inst instanceof ExceptionThrower)
                         || (inst instanceof ReturnInstruction)
                         || (inst instanceof RET)
                         || (inst instanceof InvokeInstruction) ) {
                    InstructionHandle ih = ilist.append(inst);
                    endHandles.put  (v, ih );
                } else {
                    ilist.append(inst);
                }
            }
        }
    }

    /**
     * Action taken upon first encountering an edge.
     * @param e A FlowControlEdge, a weighted, directed edge.
     */
    public void discoverEdge( Edge e ) {
    }

    /**
     * Action taken before departing an edge.
     *
     * @param e The control flow graph edge.
     */
    public void finishEdge( Edge e ) {
    }

    /**
     * Where the immediate target of an edge is an Entry or Exit,
     * determine the ultimate target.  That is, follow preferred
     * edges until a code Vertex (neither Entry nor Exit) is found
     * and return that.  XXX Should throw exception if target becomes
     * null.
     *
     * @param e Edge we are searching along.
     * @return  Ultimate target of the edge.
     */
    private CodeVertex getEffectiveTarget (final Edge e) {
        Vertex target = e.getTarget();
        // DEBUG
        //System.out.println (
        //            "getEffectiveTarget: initially target is " + target);
        // END
        while (target != null
         && (target instanceof Entry || target instanceof Exit) ) {
            // DEBUG
            // System.out.print ("    replacing " + target + " with");
            // END

            target = target.getTarget();

            //System.out.println (" " + target );   // DEBUG too ;-)
        }
        // DEBUG
        // System.out.println ("    returning: " + target);
        // END
        return (CodeVertex) target;
    }
    /**
     * Action taken before leaving a vertex in the graph.  The
     * connection instruction, if any, is fixed up by setting
     * its target(s).
     *
     * If the connection instruction is a goto, which might jump
     * into another graph, fixup is delayed until the user asks
     * for the instruction list, when the processing of all graphs
     * has been completed.
     *
     * @see CodeVertex.getInstructionList
     * @param v The vertex, either an Entry or Exit vertex, or a
     *          code vertex.
     */
    public void finishVertex( Vertex vtx ) {
        if (vtx instanceof CodeVertex) {
            CodeVertex v = (CodeVertex) vtx;
            Instruction inst = v.getConnInst();
            if (inst != null) {
                if ( inst instanceof GotoInstruction ) {
//                  // DEBUG
//                  Connector conn = v.getConnector();
//                  System.out.println("GotoInstruction in vertex " + v);
//                  if (conn instanceof UnaryConnector) {
//                      System.out.println("  has unary connector!!");
//                  } else if (conn instanceof BinaryConnector) {
//                      System.out.println("  has binary connector");
//                      System.out.println ("  'other' edge: "
//                          + ((BinaryConnector)conn).getOtherEdge());
//                  }
//                  // END
                    CodeVertex effTarget = getEffectiveTarget(
                        ((BinaryConnector)v.getConnector()).getOtherEdge());
                    InstructionHandle target
                        = (InstructionHandle) startHandles.get(effTarget);
                    gotoFixMeUps.put (v, effTarget);   // delayed fixup

                } else if ( inst instanceof IfInstruction ) {
                    Edge workingEdge
                        = ((BinaryConnector)v.getConnector()).getOtherEdge();
                    InstructionHandle target
                        = (InstructionHandle) startHandles.get(
                            getEffectiveTarget(workingEdge));
                    BranchHandle bh = (BranchHandle) endHandles.get(v);
                    bh.setTarget(target);

                } else if ( inst instanceof JsrInstruction ) {
                    InstructionHandle target
                        = (InstructionHandle) startHandles.get(
                                getEffectiveTarget(v.getEdge()));
                    BranchHandle bh = (BranchHandle) endHandles.get(v);
                    bh.setTarget(target);

                } else if (inst instanceof Select) {
                    // has a complex connector - deal with default edge
                    InstructionHandle target
                        = (InstructionHandle) startHandles.get(
                                getEffectiveTarget(v.getEdge()));
                    // DEBUG
    //              if (target == null) {
    //                  System.out.println(
    //                      "BytecodeCollector.finishVertex "
    //                      + v + " INTERNAL ERROR: \n"
    //                      + "    " + inst + " has null default target");
    //              }
                    // END
                    BranchHandle bh = (BranchHandle) endHandles.get(v);
                    bh.setTarget(target);

                    // now take care of the other edges
                    ComplexConnector conn = (ComplexConnector)v.getConnector();
                    int edgeCount = conn.size();
                    InstructionHandle[] targets = new InstructionHandle[edgeCount];
                    for (int i = 0; i < edgeCount; i++) {
                        target = (InstructionHandle) startHandles.get(
                                    getEffectiveTarget(conn.getEdge(i)));
                        // DEBUG
                        if (target == null) {
                            System.out.println(
                                "BytecodeCollector.finishVertex "
                                + v + " INTERNAL ERROR: \n"
                                + "    " + inst + " has null target " + i);
                        }
                        // END
                        ((Select)bh.getInstruction()).setTarget(i, target);
                    }
                }
            }
        }
    }

    /**
     * Dump the instruction list.  Debug method.
     */
    private void dumpIList(String where) {
        System.out.println("BytecodeCollector."
                                        + where + " instruction list:");
        int i = 0;
        for (InstructionHandle ih = ilist.getStart(); ih != null;
                                                    ih = ih.getNext() ) {
            System.out.println( "  " + (i++) + "  " + ih.getInstruction());
        }
    }
    /**
     * Finish the graph.
     *
     * @param g The control flow graph - ignored.
     */
    public void finishGraph( Directed g ) {

    }

    /**
     * Get the instruction list after completing walking the graph,
     * that is, after calling visit.
     *
     * @return A reference to the bytecode generated by walking
     *         the control flow graph.
     */
    public InstructionList getInstructionList() {
        // maps vertices with gotos to their effective target vertices
        Iterator k = gotoFixMeUps.keySet().iterator();
        while (k.hasNext()) {
            CodeVertex v = (CodeVertex) k.next();
            // maps vertices to the handles on their connecting instructions
            BranchHandle bh = (BranchHandle) endHandles.get(v);
            // gets the handle on the first instruction in the target vertex
            InstructionHandle target
                        = (InstructionHandle) startHandles.get(
                                                gotoFixMeUps.get (v));
            bh.setTarget(target);   // sets the goto target
        }
        // dumpIList("BytecodeCollector.getInstructions, before update");
        ilist.update();
        // dumpIList("BytecodeCollector.getInstructions, after update");
	    return ilist;
    }

    /**
     * Given an array of exception handler descriptions, return an
     * array of CodeExceptionGen.  The array may be empty.
     *
     * XXX This method has not been thoroughly tested.
     *
     * @param cd Array of exception handler descriptions in terms of vertices
     * @return   Array of BCEL exception handler structures.
     */
    public CodeExceptionGen[] getCEGs ( CatchData[] cd) {
        CodeExceptionGen [] ceg;
        if (cd == null) {
            ceg = new CodeExceptionGen[ 0 ];
        } else {
            ceg = new CodeExceptionGen[ cd.length ];
            for (int i = 0; i < cd.length; i++) {
                InstructionHandle start
                    = (InstructionHandle) startHandles.get(cd[i].tryStart);
                InstructionHandle end
                    = (InstructionHandle) startHandles.get(cd[i].tryEnd);
                InstructionHandle handler
                    = (InstructionHandle) startHandles.get(cd[i].handlerPC);
                if ( start == null || end == null || handler == null) {
                    System.out.println (
                    "BytecodeCollector.getCEGs: INTERNAL ERROR - null handler\n"
                        + "    CatchData[" + i + "] start: " + cd[i].tryStart
                        + " end: " + cd[i].tryEnd + " handler at: "
                        + cd[i].handlerPC);
                }
                ceg[i] = new CodeExceptionGen( start, end, handler,
                                                        cd[i].exception );
            }
        }
        return ceg;
    }
}
