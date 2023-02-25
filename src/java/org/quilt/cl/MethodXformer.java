/* MethodXformer.java */
package org.quilt.cl;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;

/** 
 * Process a MethodGen method before and after graph creation and
 * manipulation.  A QuiltClassLoader can have any number of such
 * pre/post-processors.  The preprocessors will be applied in order
 * before graph generation and then the postprocessors will be 
 * applied in reverse order after graph manipulation is complete.
 *
 * If any fatal errors occur, transformers must issue a warning
 * message on System.err and either undo any changes or set method
 * to null.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public interface MethodXformer {

    /** 
     * Apply processing to method before graph is generated. 
     */
    public void preGraph( ClassGen clazz, MethodGen method);
    /** 
     * Process method after graph generation and manipulation is
     * complete.
     */
    public void postGraph( ClassGen clazz, MethodGen method);

    /** Get the report name for method processor. */
    public String getName ();
    
    /** Assign a name to the method processor. */
    public void setName (String name);
}
