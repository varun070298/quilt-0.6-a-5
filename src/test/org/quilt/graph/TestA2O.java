/* TestA2O.java */

package org.quilt.graph;

import junit.framework.*;

public class TestA2O extends TestCase {

    Directed graph = null;
    Entry    entry = null;
    Exit     exit  = null;
    Directed subgraph = null;
    Vertex   A, C, D, E, F, G, H, I, J, K, L, M, N, O;
    Entry    Try = null;
    Exit     subExit = null;

    public TestA2O(String name) {
        super(name);
    }
   
    public class Hansel implements Visitor {
        private StringBuffer crumbs = new StringBuffer();
        public Hansel () {}
        public void discoverGraph  (Directed g) {}
        public void finishGraph    (Directed g) {}
        public void discoverVertex (Vertex v)   {
            String d = v.getLabel();
            if (d != null)  {
                crumbs.append( d ); 
            }
        }
        public void finishVertex   (Vertex v)   {}
        public void discoverEdge   (Edge e)     {}
        public void finishEdge     (Edge e)     {}

        public String getCrumbs () {
            return crumbs.toString();
        }
    }
    public void setUp() {
        graph = new Directed();
        entry = graph.getEntry();
        entry.setLabel( "W");
        exit  = graph.getExit();
        exit.setLabel( "Z");
        
        Edge e = entry.getEdge();

        A   = graph.insertVertex(e);
        A.setLabel( "A");
        e = A.getEdge();

        // try with three catch clauses
        subgraph = graph.subgraph (e, 3);
        Try = subgraph.getEntry();
        Try.setLabel( "X");

        Exit Y = subgraph.getExit();
        Y.setLabel ("Y");

        e = Try.getEdge();
        // block ending with if
        F = subgraph.insertVertex(e);
        F.setLabel( "F");
        e = F.makeBinary();             // returns 'other' or 'then' edge

        // return block; its target should be exit
        G = subgraph.insertVertex(e);
        G.setLabel( "G");
        
        e = F.getEdge();
        // else leads to another if
        L = subgraph.insertVertex(e);
        L.setLabel( "L");
        e = L.getEdge();                // else edge
        // else leads to a throw; M's edge should go to exit
        M = subgraph.insertVertex(e);
        M.setLabel( "M");

        e = L.makeBinary();             // then edge
        H = subgraph.insertVertex(e);
        H.setLabel( "H");
        H.makeMulti(3);
        // all of H's children should have exit as target
        e = ((MultiConnector)H.getConnector()).getEdge(0);
        I = subgraph.insertVertex(e); 
        I.setLabel( "I");
        e = ((MultiConnector)H.getConnector()).getEdge(1);
        J = subgraph.insertVertex(e);
        J.setLabel( "J");
        e = ((MultiConnector)H.getConnector()).getEdge(2);
        K = subgraph.insertVertex(e);
        K.setLabel( "K");

        // catch blocks; these should also all have exit as target
        e = ((ComplexConnector) Try.getConnector()).getEdge(0);
        C = subgraph.insertVertex(e);
        C.setLabel( "C");
        e = ((ComplexConnector) Try.getConnector()).getEdge(1);
        D = subgraph.insertVertex(e);
        D.setLabel( "D");
        e = ((ComplexConnector) Try.getConnector()).getEdge(2);
        E = subgraph.insertVertex(e);
        E.setLabel( "E");

        subExit = subgraph.getExit();
        e = subExit.getEdge();

        // vertices in the main graph
        N = graph.insertVertex(e);
        N.setLabel( "N");
        e = N.getEdge();
        O = graph.insertVertex(e);
        O.setLabel( "O");
    }

    public void testRecheck() {
        assertNotNull("graph is null",    graph);
        assertNotNull("subgraph is null", subgraph);
        
        // C D E F G H I J K L M entry exit, total of 13
        assertEquals ("wrong number of vertices in subgraph",
                                               13, subgraph.size() );
        // subgraph A N O entry exit = 13 + 5 = 18
        assertEquals ("wrong number of vertices in graph",
                                               18, graph.size() );
        assertEquals ("Vertex O's target is not exit",
                                                exit, O.getTarget() );
        assertEquals ("subgraph's exit doesn't point to Vertex N",
                                                N, subExit.getTarget() );
        // catch blocks
        assertEquals ("Vertex C's target is not subgraph exit",
                                                subExit, C.getTarget() );
        assertEquals ("Vertex D's target is not subgraph exit",
                                                subExit, D.getTarget() );
        assertEquals ("Vertex E's target is not subgraph exit",
                                                subExit, E.getTarget() );

        // return blocks; arguably these should point to top-level exit
        assertEquals ("Vertex G's target is not subgraph exit",
                                                subExit, G.getTarget() );

        // select targets 
        assertEquals ("Vertex I's target is not subgraph exit",
                                                subExit, I.getTarget() );
        assertEquals ("Vertex J's target is not subgraph exit",
                                                subExit, J.getTarget() );
        assertEquals ("Vertex K's target is not subgraph exit",
                                                subExit, K.getTarget() );
        // throw block
        assertEquals ("Vertex M's target is not subgraph exit",
                                                subExit, M.getTarget() );
        
    } 
    public void testHanselAndGretel() {
       Walker gretel = new Walker();
       Hansel hansel = new Hansel();
       gretel.visit(subgraph, hansel);
       String crumbs = hansel.getCrumbs();
       assertEquals ("unexpected path taken through subgraph", 
               "XFLMYHIJKGCDE", crumbs);
       
       gretel = new Walker();
       hansel = new Hansel();
       gretel.visit(graph, hansel);
       crumbs = hansel.getCrumbs();
       assertEquals ("unexpected path taken through graph", 
               "WAXFLMYHIJKGCDENOZ", crumbs);
   }
}
        
