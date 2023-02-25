/* MethodTransformer.java */
package org.quilt.cl;

import java.util.Hashtable;
import java.util.List;

import org.apache.bcel.classfile.*;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;
import org.quilt.graph.*;

/**
 * Optionally preprocesses a method, optionally transforms
 * its control flow graph, optionally postprocesses it.  The list of
 * preprocessors is applied in forward order before graph transformation.
 * The corresponding list of postprocessors is applied in reverse order
 * after graph transformation.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class MethodTransformer {

    /** For passing application-specific data.  XXX Not used, probably
     * best dropped. */
    private Hashtable methodHash = new Hashtable();

    /** Ordered list of method pre- and post-processors.*/
    private List mxf;

    /** Graph processors. */
    private List gxf;

    /** Main graph processor. */
    private GraphTransformer xformer;

    /**
     * Sets up method transformer, saving MethodXformer vector here
     * and passing GraphXformer vector to that transformer.
     */
    public MethodTransformer( List mxf, List gxf) {
        this.mxf = mxf;
        this.gxf = gxf;
        xformer = new GraphTransformer (gxf);
    }

    /** 
     * Get a reference to the hash used to share data between this
     * and other transformers.
     */
    public Hashtable getMethodHash() {
        return methodHash;
    }
    /** 
     * Zero out a badly-behaved method Xformer.
     * @param mxf The tranformer being killed.
     * @param e   The exception responsible.
     */
    private void zapMethodXformer ( MethodXformer mxf, Exception e) {
        System.err.println("WARNING: exception in "
            + mxf.getName() + ": transformation will not be applied" );
        e.printStackTrace();
        mxf = null;
    }
    /**
     * <p>Transform a specific method, first applying preprocessors,
     * then transforming the graph, then applying method postprocessors.</p>
     *
     * @param clazz  The class to which the method belongs
     * @param method The MethodGen being transformed.
     */
    public MethodGen xform ( ClassGen clazz, Method orig )  {
        if (orig == null) {
            throw new IllegalArgumentException("null method");
        }
        MethodGen method = new MethodGen (orig, clazz.getClassName(),
                                                clazz.getConstantPool());
        // New instances of the MethodXformers.
        MethodXformer [] xf = new MethodXformer[ mxf.size() ];

        // apply preprocessors in order
        for (int i = 0; i < xf.length; i++) {
            try {
               xf[i] = (MethodXformer) (
                        (mxf.get(i)).getClass().newInstance() );
            } catch (IllegalAccessException e) {
                zapMethodXformer (xf[i], e);
            } catch (InstantiationException e) {
                zapMethodXformer (xf[i], e);
            }
            if (xf[i] != null && method != null) {
                xf[i].preGraph (clazz, method);
            }
        }
        // transform the graph
        if (gxf.size() > 0 && method != null) {
            InstructionList ilist = xformer.xform (clazz, method);
            if (ilist == null) {
                System.out.println("MethodTransformer.xformer: WARNING: "
                    + "xformer returned null instruction list");
                return null;
            }
            // Quilt 0.5 approach, turns out to be unnecessary; the
            // exception handlers are based on instruction handles,
            // which in turn contain references to the method's instructions,
            // which remain valid while the graph is being transformed.
            //
            // XXX But we may want to allow handlers to be added or changed.

            method.removeExceptionHandlers();   // new ones come from graph

            //checkAndSetPos(ilist);
            ilist.setPositions(true);
            method.setInstructionList (ilist);

            CodeExceptionGen [] cgs = xformer.getExceptionHandlers();
            for (int i = 0; i < cgs.length; i++) {
                // DEBUG
                  System.out.println("adding exception " + i
                    + " to method " + method.getName()
                    + ": ["     + cgs[i].getStartPC().getPosition()
                    + ".."      + cgs[i].getEndPC().getPosition()
                    + "] --> "  + cgs[i].getHandlerPC().getPosition()
                  );
                // END
  
                method.addExceptionHandler (
                    cgs[i].getStartPC(),   cgs[i].getEndPC(),
                    cgs[i].getHandlerPC(), cgs[i].getCatchType() );
            }
            method.removeNOPs();
            method.update();    // to be called after editing MethodGen
            // during development, exceptions were thrown in setMaxStack
            try {
                method.setMaxStack();
                method.setMaxLocals();
                // method.stripAttributes(true);    // Quilt 0.5 legacy
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.out.println("GraphTransformer.xformer:\n"
                    + "    EXCEPTION finishing method " + e);
                throw e;
            }
//          // DEBUG ONLY ///////////////////////////////////////////
//          dumpIList (method, "after fixups");
//          cgs = method.getExceptionHandlers();
//          dumpExceptionHandlers(method, "after fixups", cgs.length);
//          /////////////////////////////////////////////////////////
        }

        // apply postprocessors in reverse order
        for (int i = xf.length-1; i >= 0; i--) {
            if (xf[i] != null && method != null) {
                xf[i].postGraph (clazz, method);
            }
        }
        // IF THERE HAVE BEEN ANY FATAL ERRORS, SHOULD RETURN NULL
        return method;
    }
    // UTILITY METHODS //////////////////////////////////////////////
    void dumpExceptionHandlers (MethodGen method, String where, int len) {
        CodeExceptionGen handlers[] = method.getExceptionHandlers();
        if (handlers.length != len) {
            System.out.println("EXPECTED " + len
                    + " exception handlers, found " + handlers.length);
        }
        if (handlers.length > 0) {
            System.out.println("Exception handlers for method "
                                + method.getName() + " " + where + ":");
            
            for (int j = 0; j < handlers.length; j++) {
                System.out.println("    " + j
                    + ": ["     + handlers[j].getStartPC().getPosition()
                    + ".."      + handlers[j].getEndPC().getPosition()
                    + "] --> "  + handlers[j].getHandlerPC().getPosition()
                );
            }
        }
    }
    void dumpIList(MethodGen method, String where) {
        InstructionList myList =  method.getInstructionList();
        System.out.println(
            "MethodTransformer: instruction list " + where + ":");
        int i = 0;
        for (InstructionHandle ih = myList.getStart(); ih != null;
                                                    ih = ih.getNext() ) {
            System.out.println( "  " + (i++) + "  " + ih);
        }
    }
}
