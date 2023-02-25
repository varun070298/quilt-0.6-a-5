/* ClassTransformer.java */
package org.quilt.cl;

import java.util.Hashtable;
import java.util.List;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;

/**
 * Transform a JavaClass, if there are any class, method, or graph
 * transformers.  Methods whose names begin with "q$$q" are not
 * transformed.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class ClassTransformer {

    /** For passing application-specific data. */
    private Hashtable classHash = new Hashtable();

    /** Class pre- and post-processors. */
    private List cxf;
    /** Method pre- and post-processors. */
    private List mxf;
    /** Graph processors. */
    private List gxf;
    /** Method processor. */
    private MethodTransformer xformer;
   
    /** Aborted by xformer. */
    boolean aborted_ = false;

    /**
     * Creates class transformer and lower-level transformers.
     * The method transformer is created only if there are processor
     * lists for it or the graph transformer.  
     * 
     * @param cxf List of class pre/post processors.
     * @param mxf List of method pre/post processors.
     * @param gsf List of graph processors.
     */
    public ClassTransformer (List cxf, List mxf, List gxf ) {
        this.cxf = cxf;
        this.mxf = mxf;
        this.gxf = gxf;
        xformer = new MethodTransformer (mxf, gxf);
    }

    public Hashtable getClassHash() {
        return classHash;
    }
    private void zapClassXformer ( ClassXformer cxf, Exception e) {
        System.err.println("WARNING: exception in " 
            + cxf.getName() + ": transformation will not be applied" );
        e.printStackTrace();
        cxf = null;
    }
    public JavaClass xform (JavaClass jc)  {
        if ( jc == null || ! jc.isClass() ) {
            throw new IllegalArgumentException("null or corrupt JavaClass");
        }
        ClassGen clazz = new ClassGen(jc);

        // NEEDS THOUGHT -- do we really want to make all classes 
        // public by default??
        makePublic(clazz);  // in xf[0] ??

        // apply any preprocessors 
        aborted_ = false;
        ClassXformer[] xf = new ClassXformer[ cxf.size() ];
        for (int i = 0; i < xf.length; i++) {
            try {
                xf[i] = (ClassXformer) (
                      (cxf.get(i)).getClass().newInstance() );
                xf[i].setClassTransformer(this);
            } catch (IllegalAccessException e) {
                zapClassXformer (xf[i], e);
            } catch (InstantiationException e) {
                zapClassXformer (xf[i], e);
            }
            if ( xf[i] != null && !aborted_) {
                xf[i].preMethods(clazz);
            }
        }

        // IF there are transformations to be done at the method or 
        // graph level ...
        if ( mxf.size() > 0 || gxf.size() > 0 && !aborted_) {
            // extract the methods and do wondrous things with them
            Method [] methods = clazz.getMethods();
            for (int i = 0; i < methods.length; i++) {
                String methodName = methods[i].getName();
                if (methods[i].getCode() != null 
                                        && ! methodName.startsWith("q$$q") ) {
                    // transform the method
                    MethodGen result = xformer.xform ( clazz, methods[i] );
                    // put it in the ClassGen if it's OK
                    if (result != null) {
                        clazz.replaceMethod(methods[i], result.getMethod());
                        // be a good citizen:
                        InstructionList ilist = result.getInstructionList();
                        ilist.dispose();
                        result = null;
                    }
                }
            } 
        }
        // apply any postprocessors
        for (int i = xf.length - 1; i >= 0; i--) {
            if (xf[i] != null && !aborted_) {
                xf[i].postMethods(clazz);
            }
        }
        if (clazz == null || aborted_) {
            System.out.println(
                "ClassTransformer WARNING could not transform class");
            return jc;
        } else {
            return clazz.getJavaClass();
        }
    }

    /** 
     * Make the class public.  XXX Consider making this optional. 
     *
     * @param cg ClassGen template for class to be made public.
     */
    protected static void makePublic (ClassGen cg) {
        int flags = cg.getAccessFlags();
        flags &= ~ (org.apache.bcel.Constants.ACC_PRIVATE 
                  | org.apache.bcel.Constants.ACC_PROTECTED);
        flags |= org.apache.bcel.Constants.ACC_PUBLIC;
        cg.setAccessFlags(flags);
        if (!cg.isPublic()) {
            System.out.println("WARNING: makePublic for "
                + cg.getClassName() + " failed");
        }
    }
    /** Abort the class transformation. */
    public void abort() {
        aborted_= true;
    }
}
