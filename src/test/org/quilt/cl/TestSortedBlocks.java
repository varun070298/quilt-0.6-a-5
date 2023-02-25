/* TestSortedBlocks.java */
package org.quilt.cl;

import junit.framework.*;
import org.apache.bcel.generic.*;
import org.quilt.graph.*;
/**
 */
public class TestSortedBlocks extends TestCase {

    private SortedBlocks     blox;
    private ControlFlowGraph graph;
    
    public TestSortedBlocks (String name) {
        super(name);
    }

    public void setUp () {
        blox  = new SortedBlocks ();
        graph = new ControlFlowGraph();
    }

    public void testNewIndex() {
        assertEquals("empty index has something in it", 0, blox.size() );
        Edge e = graph.getEntry().getEdge();
        CodeVertex v1  = blox.find(0, graph, e);
        assertEquals("wrong number of vertices in index", 1, blox.size() );
        CodeVertex v1b = blox.find(0, graph, e);
        assertEquals("index has two vertices at position 0?", v1, v1b);
        assertEquals("wrong number of vertices in index", 1, blox.size() );

        e = v1.getEdge();
        CodeVertex v2 = blox.find(2, graph, e);
        e = v2.getEdge();
        CodeVertex v3 = blox.find(4, graph, e);
        assertEquals("wrong number of vertices in index", 3, blox.size() );

        assertEquals("vertex is at wrong position", 0, v1.getPosition() );
        assertEquals("vertex is at wrong position", 2, v2.getPosition() );
        assertEquals("vertex is at wrong position", 4, v3.getPosition() );

    }
    
}
