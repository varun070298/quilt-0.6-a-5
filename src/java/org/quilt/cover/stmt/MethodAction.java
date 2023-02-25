/* MethodAction.java */
package org.quilt.cover.stmt;

import org.apache.bcel.generic.*;

/** 
 * <p>Process a MethodGen method before and after graph creation and
 * manipulation.  A QuiltClassLoader can have any number of such
 * pre/post-processors.  The preprocessors will be applied in order
 * before graph generation and then the postprocessors will be 
 * applied in reverse order after graph manipulation is complete.</p>
 *
 * <p>If any fatal errors occur, transformers must issue a warning
 * message on System.err and either undo any changes or set method
 * to null.</p>
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class MethodAction implements org.quilt.cl.MethodXformer {

    /** Coverage registry. */
    private static StmtRegistry stmtReg; 
    
    /** Name of processor for use in reports. */
    private static String name_ = null;
  
    private ClassGen        clazz_  = null;
    private MethodGen       method_ = null;
    private InstructionList ilist_  = null;

    public MethodAction () {
    }
    public MethodAction(StmtRegistry reg) {
        stmtReg = reg;
        setName(this.getClass().getName());
    }
   
    private void dumpIList() {
        ilist_ = method_.getInstructionList();
        if (ilist_ != null) {
            int i = 0;
            for (InstructionHandle ih = ilist_.getStart(); ih != null;
                                                    ih = ih.getNext() ) {
                System.out.println("  " + (i++) + "  " + ih.getInstruction());
            }
        }
    }
    /** 
     * Apply processing to method before graph is generated. 
     */
    public void preGraph( ClassGen clazz, MethodGen method) {
        clazz_  = clazz;
        method_ = method;
    }
    /** 
     * Process method after graph generation and manipulation is
     * complete.
     */
    public void postGraph( ClassGen clazz, MethodGen method) {
        clazz_  = clazz;
        method_ = method;
    }

    // XFORMER INTERFACE ////////////////////////////////////////////
    /** Get the report name for method processor. */
    public String getName () {
        return name_;
    }
    /** Assign a name to the method processor. */
    public void setName (String name) {
        name_ = name;
    }
}
