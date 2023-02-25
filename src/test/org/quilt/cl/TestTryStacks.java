/* TestTryStacks.java */
package org.quilt.cl;

import java.util.Comparator;
import junit.framework.*;
import org.apache.bcel.generic.*;
import org.quilt.graph.*;
/**
 * Exercise quilt.cl.TryStacks.
 *
 * @author < a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class TestTryStacks extends TestCase {

    private CodeExceptionGen[] handlers = null;
    private ControlFlowGraph   graph    = null;
    private InstructionList    ilist    = null;
    private SortedBlocks       blox     = null;
    private TryStacks          ts       = null;

    private InstructionHandle 
        startMain, midMain, endMain,
        tryStart2, tryEnd2, catch2A, catch2B,
        tryMid1,   tryEnd1, catch1A, catch1B, catch1C;
    private CodeExceptionGen
        handler1A, handler1B, handler1C, handler2A, handler2B;
    
    public TestTryStacks (String name) {
        super(name);
    }

    public void setUp () {
        blox  = new SortedBlocks();
        graph = new ControlFlowGraph();
        ilist = new InstructionList();

        // a method with two try blocks starting at the same position
        // A
        // try {
        //     try {
        //         B
        //     } catch (...) {
        //         catch2A
        //     } catch (...) {
        //         catch2B
        //     }
        //     tryMid1
        // } catch ( ...) {
        //       catch 1A
        // } catch ( ...) {
        //       catch 1B
        // } catch ( ...) {
        //       catch 1C
        // }
        // midMain
        // endMain
        startMain = ilist.append ( new NOP() ); // 0
                    ilist.append ( new NOP() );
        tryStart2 = ilist.append ( new NOP() ); // 2
                    ilist.append ( new NOP() );
        tryEnd2   = ilist.append ( new NOP() ); // 4
                    ilist.append ( new NOP() );
        catch2A   = ilist.append ( new NOP() ); // 6
                    ilist.append ( new NOP() );
        catch2B   = ilist.append ( new NOP() ); // 8
                    ilist.append ( new NOP() );
        tryMid1   = ilist.append ( new NOP() ); // 10
                    ilist.append ( new NOP() );
        tryEnd1   = ilist.append ( new NOP() ); // 12
                    ilist.append ( new NOP() );
        catch1A   = ilist.append ( new NOP() ); // 14
                    ilist.append ( new NOP() );
        catch1B   = ilist.append ( new NOP() ); // 16
                    ilist.append ( new NOP() );
        catch1C   = ilist.append ( new NOP() ); // 18
                    ilist.append ( new NOP() );
        midMain   = ilist.append ( new NOP() ); // 20
                    ilist.append ( new NOP() );
        endMain   = ilist.append ( new NOP() ); // 22

        ilist.setPositions();
        handler1A = new CodeExceptionGen (tryStart2, tryEnd1, catch1A,
                                      new ObjectType("Exception"));
        handler1B = new CodeExceptionGen (tryStart2, tryEnd1, catch1B,
                                      new ObjectType("Exception"));
        handler1C = new CodeExceptionGen (tryStart2, tryEnd1, catch1C,
                                      new ObjectType("Exception"));
        
        handler2A = new CodeExceptionGen (tryStart2, tryEnd2, catch2A,
                                      new ObjectType("Exception"));
        handler2B = new CodeExceptionGen (tryStart2, tryEnd2, catch2B,
                                      new ObjectType("Exception"));
    }

    public void testPositions () {
        assertEquals ("startMain offset wrong", 0, startMain.getPosition() );
        assertEquals ("tryStart2 offset wrong", 2, tryStart2.getPosition() );
        assertEquals ("tryEnd1 offset wrong",  12, tryEnd1.getPosition() );
        assertEquals ("midMain offset wrong",  20, midMain.getPosition() );
        assertEquals ("endMain offset wrong",  22, endMain.getPosition() );
    }
    public void testNewEmpty () {
        assertEquals ("new graph has wrong number of vertices", 
                                                    2, graph.size());
        handlers = new CodeExceptionGen[0];
        ts = new TryStacks (handlers, blox, graph);
        assertEquals ("no handlers, but TryStacks constructor changed graph", 
                                                    2, graph.size());
    }
    public void testComparator () {
        CodeExceptionGen[] handlers = {
            handler1A, handler1B, handler1C, handler2A, handler2B };
        ts = new TryStacks (handlers, blox, graph);
        Comparator cmp = ts.getComparator();
        assertEquals ("handler1A doesn't equal itself ;-)",
                                    0, cmp.compare(handler1A, handler1A) );
        assertEquals ("handler1A doesn't sort before 1B",
                                    -1, cmp.compare(handler1A, handler1B) );
        assertEquals ("handler1C doesn't sort after 1B",
                                    1, cmp.compare(handler1C, handler1B) );
        assertEquals ("shorter try block doesn't sort after longer",
                                    1, cmp.compare(handler2A, handler1B) );
        assertEquals ("longer try block doesn't sort before shorter",
                                    -1, cmp.compare(handler1C, handler2B) );
    }
    /**
     * Graph should look like so (E = Entry, X = Exit, Tk = try block
     * e0, nA/B/C = catch block, Vn = code vertex)
     *   E
     *     T1           T2          V2
     *                     2A 2B
     *                  X
     *       1A 1B 1C
     *     X
     *   X
     */
    private void checkScrambledHandlers (int n) {
        Entry e0, e1, e2;               // (sub)graph entry vertices
        Connector connE0, connE1, connE2;//their connectors
        Edge e0out, e1out, e2out;       // their preferred edges
        // connector multi-edges
        Edge connE1out0, connE1out1, connE1out2;
        Edge connE2out0, connE2out1;

        Exit  x0, x1, x2;               // (sub)graph exit vertices
        Connector connX0, connX1, connX2;
        Edge x0out, x1out, x2out;

        CodeVertex v1A, v1B, v1C, v2A, v2B; // catch vertices
        Edge v1Aout, v1Bout, v1Cout, v2Aout, v2Bout;

        CodeVertex v2;                      // code vertex
        Connector connV2;
        Edge v2out;

        GraphTalker talker = new GraphTalker();
        talker.xform (null, null, graph);
        System.out.print (ts);
        
        e0     = graph.getEntry();                  // 1
        connE0 = e0.getConnector();
        e0out  = e0.getEdge();
        assertEquals ("graph Entry should have 1 edge out",
                                                1, connE0.size() );

        e1     = (Entry)e0out.getTarget();          // 2
        connE1 = e1.getConnector();
        e1out  = e1.getEdge();
        assertEquals ("first try block should have 3 edges out", 
                                                3, connE1.size() );

        e2     = (Entry)e1out.getTarget();          // 3
        connE2 = e2.getConnector();
        e2out  = e2.getEdge();
        assertEquals ("second try block should have 2 edges out",
                                                2, connE2.size() );
        
        v2     = (CodeVertex)e2out.getTarget();     // 4
        connV2 = v2.getConnector();
        e2out  = v2.getEdge();
        assertEquals ("code vertex should have 1 edge out",
                                                1, connV2.size() );
        assertEquals ("catch block has wrong offset",
                                tryStart2.getPosition(), v2.getPosition() );
        
        assertEquals ("graph Entry has wrong index", 0, e0.getIndex());
        assertEquals ("graph Entry has wrong index", 0, e1.getIndex());
        assertEquals ("graph Entry has wrong index", 0, e2.getIndex());
        
        assertEquals ("e0's target should be e1", e1, e0.getTarget());
        assertEquals ("e1's target should be e2", e2, e1.getTarget());
        assertEquals ("e2's target should be v2", v2, e2.getTarget());

        assertEquals ("v2 should be in graph 2", 2, v2.getGraph().getIndex());
        // handlers are 2.2 and 2.3
        assertEquals ("v2 has wrong index",  4, v2.getIndex() );
                
        x2     = (Exit)e2out.getTarget();           // 5
        connX2 = x2.getConnector();
        x2out  = x2.getEdge();
        assertEquals ("second subgraph exit should have 1 edge out",
                                                1, connX2.size() );

        x1     = (Exit)x2out.getTarget();           // 6
        connX1 = x1.getConnector();
        x1out  = x1.getEdge();
        assertEquals ("first subgraph exit should have 1 edge out",
                                                1, connX1.size() );
        
        x0     = (Exit)x1out.getTarget();           // 7
        connX0 = x0.getConnector();
        x0out  = x0.getEdge();
        assertEquals ("main graph exit should have 1 edge out",
                                                1, connX0.size() );
        assertEquals ("graph exit doesn't loop back to graph e0",
                                                e0, x0out.getTarget() );
        
        assertEquals ("v2's target should be x2", x2, v2.getTarget());
        assertEquals ("x2's target should be x1", x1, x2.getTarget());
        assertEquals ("x2's target should be x0", x0, x1.getTarget());

        assertEquals ("graph exit has wrong index", 1, x2.getIndex());
        assertEquals ("graph exit has wrong index", 1, x1.getIndex());
        assertEquals ("graph exit has wrong index", 1, x0.getIndex());
        
        // CATCH BLOCKS (1)
        ComplexConnector cc = (ComplexConnector) connE1;
        connE1out0 = cc.getEdge(0);
        v1A = (CodeVertex)connE1out0.getTarget();   // 8
        v1Aout = v1A.getEdge();
        assertEquals ("catch block edge doesn't go to exit",
                                            v1Aout.getTarget(), x1 );
        assertEquals ("catch block has wrong offset",
                                catch1A.getPosition(), v1A.getPosition() );
        assertEquals ("catch block has wrong index", 2, v1A.getIndex());
                                
        connE1out1 = cc.getEdge(1);
        v1B = (CodeVertex)connE1out1.getTarget();   // 9
        v1Bout = v1B.getEdge();
        assertEquals ("catch block edge doesn't go to exit",
                                            v1Bout.getTarget(), x1 );
        assertEquals ("catch block has wrong offset",
                                catch1B.getPosition(), v1B.getPosition() );
        assertEquals ("catch block has wrong index", 3, v1B.getIndex());

        connE1out2 = cc.getEdge(2);
        v1C = (CodeVertex)connE1out2.getTarget();   // 10
        v1Cout = v1C.getEdge();
        assertEquals ("catch block edge doesn't go to exit",
                                            v1Cout.getTarget(), x1 );
        assertEquals ("catch block has wrong offset",
                                catch1C.getPosition(), v1C.getPosition() );
        assertEquals ("catch block has wrong index", 4, v1C.getIndex());

        // CATCH BLOCKS (2)
        cc = (ComplexConnector) e2.getConnector();
        connE2out0 = cc.getEdge(0);
        v2A = (CodeVertex)connE2out0.getTarget();   // 11
        v2Aout = v2A.getEdge();
        assertEquals ("catch block edge doesn't go to exit",
                                            v2Aout.getTarget(), x2 );
        assertEquals ("catch block has wrong offset",
                                catch2A.getPosition(), v2A.getPosition() );
        assertEquals ("catch block has wrong index", 2, v2A.getIndex());

        connE2out1 = cc.getEdge(1);
        v2B = (CodeVertex)connE2out1.getTarget();   // 12
        v2Bout = v2B.getEdge();
        assertEquals ("catch block edge doesn't go to exit",
                                            v2Bout.getTarget(), x2 );
        assertEquals ("catch block has wrong offset",
                                catch2B.getPosition(), v2B.getPosition() );
        assertEquals ("catch block has wrong index", 3, v2B.getIndex());

    }
    public void testGraphSize() {
        CodeExceptionGen[] handlers = {
            handler1B, handler2B, handler1C, handler2A, handler1A};
        ts = new TryStacks (handlers, blox, graph);
//      // DEBUG
//      System.out.print(ts);
//      GraphTalker talker = new GraphTalker();
//      talker.xform (null, null, graph);
//      // END
        assertEquals ("wrong number of vertices in complex graph ",
                                                12, graph.size() );
    }
    public void testScrambledHandlers1 () {
        CodeExceptionGen[] handlers = {
            handler1B, handler2B, handler1C, handler2A, handler1A};
        ts = new TryStacks (handlers, blox, graph);
        checkScrambledHandlers (1);
    }  
    public void testScrambledHandlers2 () {
        CodeExceptionGen[] handlers = {
            handler2B, handler1C, handler2A, handler1A, handler1B};
        ts = new TryStacks (handlers, blox, graph);
        checkScrambledHandlers (2);
    } 
    public void testScrambledHandlers3 () {
        CodeExceptionGen[] handlers = {
            handler2B, handler2A, handler1C, handler1B, handler1A};
        ts = new TryStacks (handlers, blox, graph);
        checkScrambledHandlers (3);
    }
}
