/* ClassAction.java */
package org.quilt.cover.stmt;

import java.util.List;
import java.util.Vector;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import org.quilt.cl.ClassTransformer;
import org.quilt.cl.CodeVertex;
import org.apache.bcel.Constants;

/**
 * Add instrumentation at the class level, creating &lt;clinit&gt;
 * if necessary.  Three fields are added and initialized:
 * <ul>
 * <li><b>q$$q</b>, the int[] hit counts array
 * <li><b>q$$qID</b>, a class identifier unique within this run
 * <li><b>q$$qStmtReg</b>, reference to the StmtRegistry
 * <li><b>q$$qVer</b>, a Quilt class file format version
 * </ul>
 *
 * All of these fields are <em>public final static</em>.  They are
 * initialized by <code>clinit</code> when the class is loaded,
 * running bytecode inserted by Quilt.
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class ClassAction implements org.quilt.cl.ClassXformer {

    // XFORMER VARIABLES ////////////////////////////////////////////
    /** Name of the processor for use in reports. */
    private static String name_ = null;

    /** The ClassTransformer that invoked this Xformer */
    private ClassTransformer classTrans;

    /** The ClassGen we are working on. */
    private ClassGen clazz_;

    /** its name */
    private String className;

    /** the class name prefixed with "class$" XXX UNNECESSARY */
    private String prefixedClassName;

    /** Its constant pool */
    private ConstantPoolGen cpGen_;

    /** */
    private InstructionFactory factory;

    /** Does a static initializer class exist? */
    boolean clinitExists = false;

    /** Its index in the method array. */
    int clinitIndex = -1;

    // COVERAGE-RELATED VARIABLES ///////////////////////////////////
    /** Current statement coverage registry */
    private static StmtRegistry stmtReg = null;

    /** temporary data shared between xformers */
    private Ephemera eph;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public ClassAction() {
    }
    public ClassAction (StmtRegistry reg) {
        stmtReg = reg;
        setName(this.getClass().getName());  // default name
    }

    /**
     * Passes a reference to the controlling ClassTransformer.
     * XXX Inelegant - and unnecessary.  XXX REWORK TO USED stmtReg
     */
    public void setClassTransformer (ClassTransformer ct) {
        classTrans = ct;
    }
    // PRE- AND POST-PROCESSING /////////////////////////////////////
    /**
     * Add a q$$q hit count field to the class using
     *   public static int [] q$$q;
     * If there is already a field of this name, do not instrument
     * the class.
     *
     * This is a preprocessor applied to the class before looking at
     * methods.  Any such preprocessors will be applied in the order of
     * the ClassXformer vector.
     *
     * @param clazz ClassGen for the class being transformed.
     */
    public void preMethods( ClassGen clazz ) {
        clazz_ = clazz;
        cpGen_ = clazz.getConstantPool();
        className = clazz_.getClassName();
        // I have tried this with class_ ; still isn't found
        prefixedClassName = "class$QIC";
        eph     = new Ephemera(className);
        if (!stmtReg.putEphemera(className, eph)) {
            // XXX should throw exception
            System.out.println("ClassAction.preMethods INTERNAL ERRROR - "
                + " couldn't register ephemeral data");
        }
        FieldGen field;
        if (clazz.containsField("q$$q") != null) {
            System.out.println("ClassAction.preMethods WARNING - "
                + className + " already has q$$q field, aborting");
            classTrans.abort();
        } else {
            // ADD:  public static int [] q$$q
            field = new FieldGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                        new ArrayType(Type.INT, 1), "q$$q", cpGen_);
            clazz.addField(field.getField());
            // ADD: public static int q$$qID
            field = new FieldGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                        Type.INT, "q$$qID", cpGen_);
            clazz.addField(field.getField());
            // ADD: public static final org.quilt.cover.stmt.StmtRegistry
            field = new FieldGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC 
                | Constants.ACC_FINAL,
                new ObjectType("org.quilt.cover.stmt.StmtRegistry"),
                "q$$qStmtReg", cpGen_);
            clazz.addField(field.getField());
            
            // ADD: public static int q$$qVer
            field = new FieldGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                        Type.INT, "q$$qVer", cpGen_);
            clazz.addField(field.getField());

            // ADD: public static class class$QIC (for QIC.class value)
            field = new FieldGen(Constants.ACC_PUBLIC |Constants.ACC_STATIC, 
                new ObjectType("java.lang.Class"), 
                "class$QIC", cpGen_);
            clazz.addField(field.getField());

            // do we have a <clinit> ?
            Method[] m = clazz.getMethods();
            for (int i = 0; i < m.length; i++) {
                if (m[i].getName().equals("<clinit>")) {
                    clinitExists = true;
                    clinitIndex  = i;
                    break;
                }
            }
        }
    }

    private void dumpIList(InstructionList ilist, String where) {
        if (ilist != null) {
            System.out.println(where + ": instruction list");
            int i=0;
            for (InstructionHandle ih = ilist.getStart(); ih != null;
                                                    ih = ih.getNext()) {
                System.out.println("  " + (i++) + "  " + ih);
            }
        }
    }

    // the class$ method added by the Java compiler do deal with 
    // NAME.class constructs 
    // VIRTUALLY IDENTICAL TO BCEL DUMP /////////////////////////////
    private void addClass$Method() {
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(Constants.ACC_STATIC, 
            new ObjectType("java.lang.Class"), new Type[] { Type.STRING }, 
                new String[] { "arg0" }, "class$", className, il, cpGen_);
    
        // TRY BLOCK
        InstructionHandle ih_0 = il.append(factory.createLoad(Type.OBJECT, 0));
        InstructionHandle ih_1 = il.append(factory
                .createInvoke("java.lang.Class", "forName", 
                new ObjectType("java.lang.Class"), new Type[] { Type.STRING }, 
                    Constants.INVOKESTATIC));
        il.append(factory.createReturn(Type.OBJECT));
        
        // CATCH BLOCK
        InstructionHandle ih_5 = il.append(factory
                .createStore(Type.OBJECT, 1));
        InstructionHandle ih_6 = il.append(factory
                .createNew("java.lang.NoClassDefFoundError"));
        il.append(InstructionConstants.DUP);
        il.append(factory.createLoad(Type.OBJECT, 1));
        il.append(factory
                .createInvoke("java.lang.ClassNotFoundException", "getMessage", 
                Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        il.append(factory
                .createInvoke("java.lang.NoClassDefFoundError", "<init>", 
                Type.VOID, new Type[] { Type.STRING }, 
                    Constants.INVOKESPECIAL));
        InstructionHandle ih_17 = il.append(InstructionConstants.ATHROW);
        
        // EXCEPTION HANDLERS
        method.addExceptionHandler(ih_0, ih_1, ih_5, 
                new ObjectType("java.lang.ClassNotFoundException"));
        method.setMaxStack();
        method.setMaxLocals();
        clazz_.addMethod(method.getMethod());
        il.dispose();
    }   // END class$
    /**
     * Postprocessor applied to the class after looking at methods.
     * These will be applied in reverse order after completion of
     * method processing.
     */
    public void postMethods (ClassGen clazz ) {
        int counterCount = eph.getCounterCount();
        List methodNames = eph.getMethodNames();
        List methodEnds  = eph.getMethodEnds();
        if (clazz != clazz_) {
            // XXX modify to throw exception
            System.out.println("ClassAction.postMethods: INTERNAL ERROR:"
                + " preMethods class different from postMethods");
        }
        factory = new InstructionFactory (clazz_, cpGen_);
        addClass$Method();           // uses factory
        
        MethodGen mg;
        InstructionList ilist;
        InstructionHandle ih;
        if (clinitExists) {
            mg = new MethodGen (clazz_.getMethodAt(clinitIndex),
                    className, clazz_.getConstantPool() );
            ilist = mg.getInstructionList();
        } else {
            ilist = new InstructionList();
            mg = new MethodGen ( Constants.ACC_STATIC, Type.VOID, Type.NO_ARGS,
                    new String[] {}, "<clinit>", className,
                    ilist, clazz_.getConstantPool() );
        }
        // //////////////////////////////////////////////////////////
        // q$$q = new int[counterCount];
        // //////////////////////////////////////////////////////////

        // the first instruction MUST be insert
        ih = ilist.insert(new PUSH (cpGen_, counterCount));

        // dunno why, but the following produces an array of references; also
//      // the cast should not be necessary, according to the Javadocs,
//      // but there is a compilation error without it
//      ih = ilist.append(ih, (Instruction)factory.createNewArray(
//                              new ArrayType(Type.INT, 1), (short)1));

        ih = ilist.append(ih, new NEWARRAY(Type.INT));

        ih = ilist.append(ih, factory.createFieldAccess (
                    className, "q$$q",
                    new ArrayType (Type.INT, 1), Constants.PUTSTATIC));

        /////////////////////////////////////////////////////////////
        // q$$qVer = 0;
        /////////////////////////////////////////////////////////////
        ih = ilist.append(ih, new PUSH(cpGen_, 0));
        ih = ilist.append(ih, factory.createFieldAccess(
                    className, "q$$qVer", Type.INT,
                    Constants.PUTSTATIC));

        // //////////////////////////////////////////////////////////
        // public final static StmtRegistry q$$qStmtRegistry 
        //   = (StmtRegistry)
        //       (org.quilt.cl.QuiltClassLoader)QIC.class.getClassLoader())
        //           .getRegistry("org.quilt.cover.stmt.StmtRegistry");
        // //////////////////////////////////////////////////////////
        
        // GET QIC.class //////////////////////////////////////
        ih = ilist.append(ih, new PUSH(cpGen_, "org.quilt.QIC"));
        ih = ilist.append(ih, factory.createInvoke(className, "class$",
            new ObjectType("java.lang.Class"), new Type[] { Type.STRING },
                Constants.INVOKESTATIC));
        // this two instructions are unnecessary
        ih = ilist.append(ih, InstructionConstants.DUP);
        ih = ilist.append(ih, factory.createFieldAccess(className, 
            "class$QIC", new ObjectType("java.lang.Class"), 
                Constants.PUTSTATIC)); 

        // get the class loader 
        ih = ilist.append(ih, factory
            .createInvoke("java.lang.Class", "getClassLoader",
            new ObjectType("java.lang.ClassLoader"), Type.NO_ARGS,
                Constants.INVOKEVIRTUAL));
        // cast to QuiltClassLoader
        ih = ilist.append(ih, factory
            .createCheckCast(new ObjectType("org.quilt.cl.QuiltClassLoader")));
        // put method name on stack ...
        ih = ilist.append(ih, 
            new PUSH(cpGen_, "org.quilt.cover.stmt.StmtRegistry"));
        // invoke QuiltClassLoader.getRegistry("org.quilt.cover.stmt.S...")
        ih = ilist.append(ih, factory
            .createInvoke("org.quilt.cl.QuiltClassLoader", "getRegistry",
            new ObjectType("org.quilt.reg.QuiltRegistry"),
                new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
        // cast to StmtRegistry
        ih = ilist.append(ih, factory
            .createCheckCast(
                new ObjectType("org.quilt.cover.stmt.StmtRegistry")));
        // save to q$$qStmtReg
        ih = ilist.append(ih, factory
            .createFieldAccess(className, "q$$qStmtReg",
            new ObjectType("org.quilt.cover.stmt.StmtRegistry"),
                Constants.PUTSTATIC)); 
        
        /////////////////////////////////////////////////////////////
        // q$$qID = q$$qStmtRegistry.registerCounts(className, q$$q);
        /////////////////////////////////////////////////////////////
 
        ih = ilist.append(ih, factory.createFieldAccess(
                    className, "q$$qStmtReg", 
                    new ObjectType("org.quilt.cover.stmt.StmtRegistry"),
                        Constants.GETSTATIC));
        ih = ilist.append(ih, new PUSH(cpGen_, className));
        ih = ilist.append(ih, factory.createFieldAccess (
                    className, "q$$q",
                    new ArrayType (Type.INT, 1), Constants.GETSTATIC));
        ih = ilist.append(ih, factory.createInvoke(
                    "org.quilt.cover.stmt.StmtRegistry", "registerCounts",
                    Type.INT,
                    new Type[] { Type.STRING, new ArrayType(Type.INT,1) },
                    Constants.INVOKEVIRTUAL));
        ih = ilist.append(ih, factory.createFieldAccess (
                    className, "q$$qID", Type.INT,
                    Constants.PUTSTATIC));

        /////////////////////////////////////////////////////////////
        // return;
        /////////////////////////////////////////////////////////////
        if (!clinitExists) {
            ih = ilist.append(ih, factory.createReturn (Type.VOID));
        }
        ilist.setPositions();
        mg.setMaxStack();
        mg.setMaxLocals();

        boolean aborting = false;
        if (clinitExists) {
            /////////////////////////////////////////////////////////
            // XXX KNOWN PROBLEM: error in setMethod if clinitExists
            // probably because line number table not corrected
            /////////////////////////////////////////////////////////
            // aborting = true;
            // classTrans.abort();
            clazz_.setMethodAt(mg.getMethod(), clinitIndex);
        } else {
            clazz_.addMethod(mg.getMethod());
        }
        // ilist.dispose();     // when things are more stable ;-)
        
        // REGISTER method names and ends ///////////////////////////
        if (!aborting) {
            int len = methodNames.size();
            String [] myNames     = new String [len];
            int    [] myEndCounts = new int[len];

            for (int k = 0; k < len; k++) {
                myNames[k]     =  (String) methodNames.get(k);
                myEndCounts[k] = ((Integer)methodEnds. get(k)).intValue();
            }
            stmtReg.registerMethods(className, myNames, myEndCounts);
        } // if not aborting
        stmtReg.removeEphemera(className);
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /** Get the preprocessor's report name. */
    public String getName() {
        return name_;
    }

    /** Set the preprocessor's name for reports. */
    public void setName(String name) {
        name_ = name;
    }
}
