/* TestGraphTransformer.java */
package org.quilt.cl;

import java.util.List;
import java.util.Vector;
import junit.framework.*;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import org.quilt.graph.*;

/**
 * Test GraphTransformer using runTest methods from classes synthesized
 * by ClassFactory.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class TestGraphTransformer extends TestCase {

    private ClassFactory factory = ClassFactory.getInstance();

    private ClassGen clazz;

    private GraphTransformer xformer;

    private GraphSpy spy;

    private GraphTalker talker;

    private ControlFlowGraph theGraph;

    public TestGraphTransformer (String name) {
        super(name);
        GraphSpy.setName("the spy");
    }

    public void setUp () {
        List gxf   = new Vector();
        spy        = new GraphSpy();
        talker     = new GraphTalker();
        gxf.add ( spy );
        // uncomment if things stop working
//      gxf.add ( talker );
        xformer = new GraphTransformer ( gxf );
    }

    private MethodGen loadAndExtractRunTest (String name) {
        clazz = factory.makeClass(name, name);
        Method [] methods = clazz.getMethods();
        for (int i = 0 ; i < methods.length; i++) {
            if ( methods[i].getName().equals("runTest") ) {
                return new MethodGen (methods[i], clazz.getClassName(),
                        clazz.getConstantPool());
            }
        }
        return null;    // not found
    }
    public void testDefault () {
        MethodGen
        mgDefault = loadAndExtractRunTest ("test.data.TestDefault");
        assertNotNull ("extracted method is null", mgDefault);

        // build the graph, get the instruction list from it
        mgDefault.setInstructionList(xformer.xform ( clazz, mgDefault));
        mgDefault.removeNOPs();
        InstructionList ilist = mgDefault.getInstructionList();
        Instruction [] inst = ilist.getInstructions();
        assertEquals ("wrong number of instructions", 2, ilist.getLength() );
        assertTrue ("", inst[0] instanceof ICONST);
        assertTrue ("", inst[1] instanceof IRETURN);

        theGraph = GraphSpy.getTheGraph();
        // DEBUG
        //GraphTalker talker = new GraphTalker();
        //talker.xform (null, null, theGraph);
        // END
        assertNotNull ("failed to get reference to method CFG", theGraph);
        assertNotNull ("graph exit vertex is null", theGraph.getExit());
        assertEquals ("TestDefault graph has wrong size", 3, theGraph.size() );
    }

    public void testIfThen () {
        MethodGen
        mgIfThen = loadAndExtractRunTest ("test.data.TestIfThen");
        assertNotNull ("extracted method is null", mgIfThen);

        // build the graph, get the instruction list from it
        InstructionList ilist = xformer.xform ( clazz, mgIfThen);
        assertEquals ("wrong number of instructions", 6, ilist.getLength() );
        Instruction [] inst = ilist.getInstructions();
        assertTrue ("should be ILOAD: " + inst[0],
                                        inst[0] instanceof ILOAD);
        assertTrue ("should be IFGT:  " + inst[1],
                                        inst[1] instanceof IFGT);
        assertTrue ("should be ICONST:  " + inst[2],
                                        inst[2] instanceof ICONST);
        assertTrue ("should be IRETURN:  " + inst[3],
                                        inst[3] instanceof IRETURN);
        assertTrue ("should be ICONST:  " + inst[4],
                                        inst[4] instanceof ICONST);
        assertTrue ("should be IRETURN:  " + inst[5],
                                        inst[5] instanceof IRETURN);

        // EXAMINE GRAPH
        theGraph = GraphSpy.getTheGraph();
        assertNotNull ("failed to get reference to method CFG", theGraph);
        assertNotNull ("graph exit vertex is null", theGraph.getExit());
        assertEquals ("TestIfThen graph has wrong size", 5, theGraph.size() );
    }

    public void testWhile () {
        MethodGen
        mgWhile = loadAndExtractRunTest ("test.data.TestWhile");
        assertNotNull ("extracted method is null", mgWhile);

        // build the graph, get the instruction list from it
        mgWhile.setInstructionList(xformer.xform ( clazz, mgWhile));
        mgWhile.removeNOPs();
        InstructionList ilist = mgWhile.getInstructionList();
        assertEquals ("wrong number of instructions", 7, ilist.getLength() );
        Instruction [] inst = ilist.getInstructions();
        assertTrue ("", inst[0] instanceof ILOAD);
        assertTrue ("", inst[1] instanceof DUP);
        assertTrue ("", inst[2] instanceof IFLE);
        assertTrue ("", inst[3] instanceof ICONST);
        assertTrue ("", inst[4] instanceof ISUB);
        assertTrue ("", inst[5] instanceof GOTO);
        assertTrue ("", inst[6] instanceof IRETURN);

        // EXAMINE GRAPH
        theGraph = GraphSpy.getTheGraph();
        assertNotNull ("failed to get reference to method CFG", theGraph);
        assertNotNull ("graph exit vertex is null", theGraph.getExit());
        assertEquals ("TestWhile graph has wrong size", 6, theGraph.size() );
    } // GEEP

    private void dumpInstructions (Instruction [] ilist) {
        System.out.println("Instructions returned:");
        for (int i = 0; i < ilist.length; i++) {
            System.out.println ("    " + ilist[i] );
        }
    }
    public void testNPEWithCatch () {
        MethodGen
        mgNPEWithCatch = loadAndExtractRunTest ("test.data.TestNPEWithCatch");
        assertNotNull ("extracted method is null", mgNPEWithCatch);

        // build the graph, get the instruction list from it
        mgNPEWithCatch.setInstructionList(xformer.xform ( clazz, 
                                                            mgNPEWithCatch));
        mgNPEWithCatch.removeNOPs();
        InstructionList ilist = mgNPEWithCatch.getInstructionList();
        Instruction [] inst = ilist.getInstructions();

        theGraph = GraphSpy.getTheGraph();
        // talker.xform (null, null, theGraph);

        assertNotNull ("failed to get reference to method CFG", theGraph);
        assertNotNull ("graph exit vertex is null", theGraph.getExit());
        assertEquals ("TestNPEWithCatch graph has wrong size",
                                                    8, theGraph.size() );

        assertEquals ("wrong number of instructions", 7, ilist.getLength() );
        assertTrue ("expected ACONST_NULL", inst[0] instanceof ACONST_NULL);
        assertTrue ("expected ICONST", inst[1] instanceof ICONST);
        assertTrue ("expected INVOKEVIRTUAL", inst[2] instanceof INVOKEVIRTUAL);
        assertTrue ("expected ICONST", inst[3] instanceof ICONST);
        assertTrue ("expected IRETURN", inst[4] instanceof IRETURN);
        assertTrue ("expected ICONST", inst[5] instanceof ICONST);
        assertTrue ("expected IRETURN", inst[6] instanceof IRETURN);
    }

    public void testNPENoCatch () {
        MethodGen
        mgNPENoCatch = loadAndExtractRunTest ("test.data.TestNPENoCatch");
        assertNotNull ("extracted method is null", mgNPENoCatch);

        // build the graph, get the instruction list from it
        InstructionList ilist = xformer.xform ( clazz, mgNPENoCatch);
        assertEquals ("wrong number of instructions", 5, ilist.getLength() );
        Instruction [] inst = ilist.getInstructions();
        assertTrue ("expected ACONST_NULL", inst[0] instanceof ACONST_NULL);
        assertTrue ("expected ICONST", inst[1] instanceof ICONST);
        assertTrue ("expected INVOKEVIRTUAL", inst[2] instanceof INVOKEVIRTUAL);
        assertTrue ("expected ICONST", inst[3] instanceof ICONST);
        assertTrue ("expected IRETURN", inst[4] instanceof IRETURN);

        theGraph = GraphSpy.getTheGraph();
        // DEBUG
        //GraphTalker talker = new GraphTalker();
        //talker.xform (null, null, theGraph);
        // END
        assertNotNull ("failed to get reference to method CFG", theGraph);
        assertNotNull ("graph exit vertex is null", theGraph.getExit());
        assertEquals ("TestNPENoCatch graph has wrong size",
                                                    4, theGraph.size() );
    } 

    public void testSelect () {
        MethodGen
        mgSelect = loadAndExtractRunTest ("test.data.TestSelect");
        assertNotNull ("extracted method is null", mgSelect);

        // build the graph, get the instruction list from it
        mgSelect.setInstructionList(xformer.xform ( clazz, mgSelect));
        mgSelect.removeNOPs();
        InstructionList ilist = mgSelect.getInstructionList();
        Instruction [] inst = ilist.getInstructions();
        // DEBUG
        //dumpInstructions(inst);
        // END
        assertEquals ("wrong number of instructions", 10, inst.length );
        assertTrue ("expected ILOAD", inst[0] instanceof ILOAD);
        assertTrue ("expected LOOKUPSWITCH", inst[1] instanceof LOOKUPSWITCH);
        assertTrue ("expected SIPUSH", inst[2] instanceof SIPUSH);
        assertTrue ("expected IRETURN", inst[3] instanceof IRETURN);
        assertTrue ("expected SIPUSH", inst[4] instanceof SIPUSH);
        assertTrue ("expected IRETURN", inst[5] instanceof IRETURN);
        assertTrue ("expected SIPUSH", inst[6] instanceof SIPUSH);
        assertTrue ("expected IRETURN", inst[7] instanceof IRETURN);
        assertTrue ("expected SIPUSH", inst[8] instanceof SIPUSH);
        assertTrue ("expected IRETURN", inst[9] instanceof IRETURN);

        theGraph = GraphSpy.getTheGraph();
        // DEBUG
        //GraphTalker talker = new GraphTalker();
        //talker.xform (null, null, theGraph);
        // END
        assertNotNull ("failed to get reference to method CFG", theGraph);
        assertNotNull ("graph exit vertex is null", theGraph.getExit());
        assertEquals ("TestSelect graph has wrong size", 7, theGraph.size() );
   }
}
