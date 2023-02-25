/* TestVertexData.java */
package org.quilt.cl;

import junit.framework.*;
import org.apache.bcel.generic.*;
import org.quilt.graph.*;

/**
 * Vertex data is the additional data differentiating a CodeVertex 
 * from a simple Vertex.  It consists of a bytecode position, a
 * label, and the InstructionList.
 *
 * XXX This code tested a now-obsolete data structure; it should be
 * beefed up or dropped.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class TestVertexData extends TestCase {

    private ControlFlowGraph cfg;
    
    private CodeVertex node;
    
    public TestVertexData (String name) {
        super(name);
    }

    public void setUp () {
        cfg = new ControlFlowGraph();
        node = new CodeVertex (cfg, 3);   // position of no significance
    }

    public void testNewNode() {
        assertEquals ("empty node has wrong position", 
                                        3, node.getPosition() );
        assertTrue ("empty node has something in it", 
                                        node.getInstructionList().isEmpty() );
        assertEquals("empty node has something in instruction list",
                                        0, node.getInstructionList().size() );
    }
    // in fact this would become several nodes, but for testing ...
    public void testWhileNode() {
        InstructionList ilist = node.getInstructionList();
        ilist.append ( new ILOAD (1) );
        InstructionHandle ifHandle   = ilist.append ( new DUP  ()  );
        InstructionHandle loopHandle = ilist.append ( new ICONST( 1 ) );
        ilist.append( new ISUB() );
        ilist.append( new GOTO( ifHandle ));
        InstructionHandle retHandle  = ilist.append ( new IRETURN() );
        ilist.insert( loopHandle, new IFLE( retHandle ));

        assertEquals ("'while' node has wrong position", 
                                        3, node.getPosition() );
        assertTrue ("'while' node has nothing in it", 
                                        !node.getInstructionList().isEmpty() );
        assertEquals("'while' node has wrong size instruction list",
                                        7,  node.getInstructionList().size() );

        ControlFlowGraph graph = new ControlFlowGraph();
        CodeVertex v = graph.insertCodeVertex ( graph.getEntry().getEdge() );
        v.setPos(node.getPosition());
        assertEquals ("new vertex has wrong bytecode position",
                                    node.getPosition(), 
                                    v.getPosition() );
    }
}
