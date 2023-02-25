/* TestDirected.java */

package org.quilt.graph;

import junit.framework.*;
import org.quilt.cl.GraphTalker;

public class TestDirected extends TestCase {

    Directed graph = null;
    Entry    entry = null;
    Exit     exit  = null;
    
    public TestDirected(String name) {
        super(name);
    }
    
    public void setUp() {
        graph = new Directed();
        entry = graph.getEntry();
        exit  = graph.getExit();
    }

    public void testTwosome() {
        assertEquals ("wrong index on entry",   0, entry.getIndex() );
        assertEquals ("wrong index on exit",    1, exit.getIndex() );
        assertEquals ("wrong number of vertices in new graph",
                                                2, graph.size() );
    }

    public void testBasicSubgraph() {
        Edge e = entry.getEdge();
        Directed subgraph = graph.subgraph(e, 5);
        Entry subEntry = subgraph.getEntry();

        assertEquals("wrong number of vertices in subgraph",
                                                2, subgraph.size() );

        assertEquals("wrong number of vertices in nested graph",
                                                4, graph.size() );

        // should return the size of the Multi part of the connector
        assertEquals("size of subgraph ComplexConnector wrong",
                                 5, subEntry.getConnector().size() );
    }

    public void testThreesome() {
        Edge e = entry.getEdge();
        try {
            graph.insertVertex(e) ;
        } catch (Exception exc)  { 
            fail ("error inserting third vertex");
        }
        assertEquals ("wrong number of vertices in new graph",
                                                3, graph.size() );
    }
    public void testLotsInARow() {
        Edge e = entry.getEdge();
        
        Vertex A = graph.insertVertex(e);
        Vertex B = graph.insertVertex(A.getEdge());
        Vertex C = graph.insertVertex(B.getEdge());
        Vertex D = graph.insertVertex(C.getEdge());
        Vertex E = graph.insertVertex(D.getEdge());
        assertEquals ("wrong number of vertices in graph",
                                                7, graph.size() );
    } 
}
        
