/* ClassXformer.java */
package org.quilt.cl;

import org.apache.bcel.generic.ClassGen;

/**
 * Application-specific pre- and post-processors for Quilt classes.
 * There may be any number of these.
 *
 * If pre/post-processors encounter fatal errors, they must issue
 * a warning message to System.err and either undo or set clazz to null.
 * 
 * @author < a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public interface ClassXformer {

    public void setClassTransformer (ClassTransformer ct);

    /** 
     * Preprocessor applied to the class before looking at methods.
     * Any such preprocessors will be applied in the order of 
     * the ClassXformer vector.
     */
    public void preMethods( ClassGen clazz );

    /**
     * Postprocessor applied to the class after looking at methods.
     * These will be applied in reverse order after completion of 
     * method processing.
     */
    public void postMethods (ClassGen clazz );

    /** Get the preprocessor's report name. */
    public String getName();

    /** Set the preprocessor's name for reports. */
    public void setName(String name);
}
