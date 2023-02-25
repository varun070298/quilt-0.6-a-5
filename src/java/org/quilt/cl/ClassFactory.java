/* ClassFactory.java */

package org.quilt.cl;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

/** 
 * Class synthesizer.  Currently intended for debugging Quilt 
 * development and limited to instantiating classes with a 
 * no-argument constructor and a single method whose bytecode
 * depends upon the base name of the class.
 *
 * By default classes whose name begins with <code>test.data.Test</code>
 * will be synthesized.  This can be set to a different string by a 
 * QuiltClassLoader method.
 *
 * @see QuiltClassLoader.
 *
 * @todo Add code for generating a method with nested tries.
 * @todo Need a test method with multiple catches on a single try.
 * @todo Add code for a method with a finally clause.
 * @todo Make the prefix used to flag classes to be synthesized
 *         either a static constant of the class or a parameter
 *         to the class constructor.
 * @todo Longer term: come up with a more generalized way for 
 *         specifying classes to be synthesized; these should allow
 *         for more than just the constructor and runTest() methods.
 *
 * @author <a href="ddp@apache.org">David Dixon-Peugh</a>
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a> -- major 
 *              changes to the original code.
 */
public class ClassFactory {

    private static ClassFactory instance = new ClassFactory();
    private String interfaces[] = new String[] { 
                                "org.quilt.cl.RunTest" };

    /** No-arg constructor, private because this is a singleton. */
    private ClassFactory() { }

    /**
     * Use this method to get to the ClassFactory singleton.
     * 
     * <p>XXX Is there any benefit to this being a singleton?</p>
     */
    public static ClassFactory getInstance() {
		return instance;
    }
    /**
     * Get the bytecode for a synthesized class.  The name passed
     * looks like "test/data/TestMyStuff.class".  This is 
     * converted to "test.data.TestMyStuff".  The "test.data.Test"
     * prefix will later be stripped off and the base name, "MyStuff"
     * in this case, used to determine which version of the runTest
     * method to generate.
     *
     * @param resName Name of the class to be synthesized.
     */
    public InputStream getResourceAsStream( final String resName ) {
		String className = 
		    resName.substring(0, resName.indexOf(".class"));
		className = className.replace( File.separatorChar, '.');

		try {
		    PipedInputStream returnStream =
				new PipedInputStream();
		    PipedOutputStream outputStream =
				new PipedOutputStream( returnStream );
		    ClassGen clazz = ClassFactory.getInstance().
                                    makeClass( className, resName);
		    clazz.getJavaClass().dump( outputStream );
		    return returnStream;
		} catch (IOException exception) {
            // DEBUG ////////////////////////////////////////
	    	System.out.println("Unable to return Resource as InputStream.");
	    	exception.printStackTrace();
	    	return null;
		}
    }
    /**
     * Generate a class with a single no-arg constructor and a runTest
     * method.  By convention, if there is an underscore (_) in the 
     * class name, the underscore and everything after it are 
     * ignored in choosing method code.  For example, if the class
     * name is testWhile_1, then the method code comes from mgWhile
     *
     * <p>Methods available at this time are:</p>
     * <ul>
     * <li> mgDefault
     * <li> mgIfThen
     * <li> mgNPENoCatch
     * <li> mgNPEWithCatch
     * <li> mgSelect
     * <li> mgWhile
     * </ul>
     *
     * @param className Name of the class to be constructed.
     * @param fileName  Associated file name (??? XXX)
     */
    public ClassGen makeClass( String className, String fileName ) {
		ClassGen newClass =
		    new ClassGen( className, "java.lang.Object", fileName,
				  Constants.ACC_PUBLIC, interfaces);

		MethodGen constructor = makeConstructor( newClass );
		newClass.addMethod( constructor.getMethod() );

		MethodGen testMethod = makeMethod( newClass );
		org.apache.bcel.classfile.Method m = testMethod.getMethod();
	
		newClass.addMethod( m );

		return newClass; 
    }
    /**
     * Creates the constructor for the synthesized class.  It is a no-arg
     * constructor that calls super.
     *
     * @param clazz Template for the class being synthesized.
     * @return      Method template for the constructor.
     */
    public MethodGen makeConstructor( ClassGen clazz ) {
		InstructionFactory factory = 
		    new InstructionFactory( clazz );

		InstructionList instructions = new InstructionList();

		instructions.append( new ALOAD(0) );
		instructions.append( factory.createInvoke( 
                                "java.lang.Object", "<init>", Type.VOID, 
                                new Type[0], Constants.INVOKESPECIAL ) );
		instructions.append( new RETURN() );

		MethodGen returnMethod =
		    new MethodGen( Constants.ACC_PUBLIC, Type.VOID, 
				   new Type[0], new String[0],
				   "<init>", clazz.getClassName(),
				   instructions, clazz.getConstantPool() );

		returnMethod.setMaxStack();
		return returnMethod;
    }
    /**
     * Creates a method with bytecode determined by the name of 
     * the class.
     *
     * If we have class test.data.TestBogus, then we strip off the 
     * "test.data.Test" prefix and call mgBogus() to get an 
     * instruction list.
     *
     * In the current version, if there is an underscore in the 
     * class name, then the underscore and everything following it
     * will be ignored.  So test.data.TestBogus_27 would result in a
     * call to mgBogus(), not mgBogus_27().
     *
     * If the method (mgBogus in this case) doesn't exist, then we call
     * mgDefault() to generate the bytecode.
     *
     * @param clazz Template for the class being produced.
     * @return      Template method with bytecode.
     */
    public MethodGen makeMethod( ClassGen clazz ) {
		String className = clazz.getClassName();
		String reflectMeth = "mg" + className.substring( 
                                            "test.data.Test".length() );
        int underscore = reflectMeth.indexOf('_');
        if (underscore  > 0 ) {	
            reflectMeth = reflectMeth.substring(0, underscore);
        }
		InstructionList instructions = null;
		List catchBlocks = new ArrayList();

		MethodGen synthMethod = null;
		try {
		    Method method = 
			this.getClass().getMethod( reflectMeth, 
						   new Class[] { ClassGen.class } );
	    	synthMethod = 
				(MethodGen) method.invoke( this,
										   new Object[] { clazz });
		} catch (Exception e) {
	    	if (! (e instanceof NoSuchMethodException) ) {
				e.printStackTrace();
	    	}
		    System.out.println(
                "WARNING: ClassFactory using Default bytecode for " 
                    + className );
	    	synthMethod = mgDefault( clazz );
		}
		synthMethod.setMaxStack();     // as suggested by BCEL docs
        // jdd //////////////////////////////////////////////////////
        if (reflectMeth.compareTo("test.data.TestNPEWithCatch") == 0 ) {
            CodeExceptionGen cegs[] = synthMethod.getExceptionHandlers();
            if (cegs.length != 1) {
                System.out.println ("INTERNAL ERROR: " + reflectMeth 
                    + "\n  should have one exception handler, has "
                    + cegs.length);
            }
        }
        // end jdd
		return synthMethod;
    } 
    /**
     * Generates bytecode for a method which simply returns 2. This
     * is the method used if the class name is test.data.TestDefault.  
     *
     * This method is also used if ClassFactory doesn't recognize the name; 
     * for example, if the class name is test.data.TestBogus, because there
     * is no mgBogus method, this default method is used to generate the 
     * bytecode.
     * 
     * <pre>
     * public int runTest( int x ) {
     *   return 2;
     * }
     * </pre>
     */    
    public MethodGen mgDefault( ClassGen clazz) {
		InstructionList instructions = new InstructionList();
		instructions.append( new ICONST( 2 ));
		instructions.append( new IRETURN() );
		
		MethodGen returnMethod =
		    new MethodGen( Constants.ACC_PUBLIC, 
		                   Type.INT, 
		                   new Type[] { Type.INT },
				    	   new String[] { "x" }, 
				    	   "runTest", 
				    	   clazz.getClassName(),
				   		   instructions, 
				   		   clazz.getConstantPool());
		
		return returnMethod;
    }
    /**
     * Generates instructions for a method consisting of a single
     * if-then clause.
     * 
     * <pre>
     *   public int runTest( int x ) {
     *     if (x > 0) {
     *       return 3;
     *     } else {
     *       return 5;
     *     }
     *   }
     * </pre>
     */
    public MethodGen mgIfThen( ClassGen clazz ) {
		InstructionList instructions = new InstructionList();
		instructions.append( new ILOAD( 1 ));

		InstructionList thenClause = new InstructionList();
		thenClause.append( new ICONST( 3 ));
		thenClause.append( new IRETURN() );

		InstructionList elseClause = new InstructionList();
		elseClause.append( new ICONST( 5 ));
		elseClause.append( new IRETURN() );
	
		InstructionHandle elseHandle = 
	    	instructions.append( elseClause );
		InstructionHandle thenHandle =
	    	instructions.append( thenClause );
		instructions.insert( elseHandle, new IFGT( thenHandle ));

		MethodGen returnMethod =
		    new MethodGen( Constants.ACC_PUBLIC, 
		                   Type.INT, 
		                   new Type[] { Type.INT },
				    	   new String[] { "x" }, 
				    	   "runTest", 
				    	   clazz.getClassName(),
				   		   instructions, 
				   		   clazz.getConstantPool());
		
		return returnMethod;
    }
    /**
     * Creates bytecode which will throw a NullPointerException
     * without a catch block.
     * 
     * <pre>
     *     public int runTest(int x) {
     *         null.runTest( 0 );
     *         return 0;
     *     }
     * </pre>
     */
    public MethodGen mgNPENoCatch( ClassGen clazz ) {
		InstructionFactory factory = new InstructionFactory( clazz );
		InstructionList instructions = new InstructionList();
		Type argTypes[] = new Type[1];

		argTypes[0] = Type.INT;

		instructions.append( new ACONST_NULL() );
		instructions.append( new ICONST( 0 ));
		instructions.append( factory.createInvoke( clazz.getClassName(),
							   "runTest",
							   Type.INT,
							   argTypes,
							   Constants.INVOKEVIRTUAL));

		instructions.append( new ICONST( 0 ));
		instructions.append( new IRETURN() );
					
		MethodGen returnMethod =
		    new MethodGen( Constants.ACC_PUBLIC, 
		                   Type.INT, 
		                   new Type[] { Type.INT },
				    	   new String[] { "x" }, 
				    	   "runTest", 
				    	   clazz.getClassName(),
				   		   instructions, 
				   		   clazz.getConstantPool());
		
		return returnMethod;
    }
    /**
     * Returns bytecode which will throw a NullPointerException,
     * but it will catch the NPE.
     * 
     * <pre>
     *     try {
     *         null.runTest( 0 );
     *         return -1;
     *     } catch (NullPointerException npe) {
     *         return 3;
     *     }
     * </pre>
     */
    public MethodGen mgNPEWithCatch( ClassGen clazz ) {
		InstructionFactory factory = new InstructionFactory( clazz );
		InstructionList instructions = new InstructionList();
	
		Type argTypes[] = new Type[1];

		argTypes[0] = Type.INT;

		ObjectType npeType = new ObjectType( 
                                        "java.lang.NullPointerException" );
		instructions.append( new ACONST_NULL() );
		instructions.append( new ICONST( 0 ));
		instructions.append( factory.createInvoke( clazz.getClassName(),
							   "runTest",
							   Type.INT,
							   argTypes,
							   Constants.INVOKEVIRTUAL));
				
		instructions.append( new ICONST( -1 ));
		instructions.append( new IRETURN() );

		InstructionHandle handler =
		    instructions.append( new ICONST( 3 ));
		instructions.append( new IRETURN() );

        // we expect an exception to occur, and then 3 to be
        // returned by the handler
		MethodGen returnMethod =
		    new MethodGen( Constants.ACC_PUBLIC, 
		                   Type.INT, 
		                   new Type[] { Type.INT },
				    	   new String[] { "x" }, 
				    	   "runTest", 
				    	   clazz.getClassName(),
				   		   instructions, 
				   		   clazz.getConstantPool());
		// jdd
        CodeExceptionGen ceg =
        // end jdd
		returnMethod.addExceptionHandler(instructions.getStart(),
										 handler.getPrev(), handler,
										 npeType );

        // jdd: at this point the MethodGen should have a table of 
        // exception handlers with one entry
        returnMethod.addException("java.lang.NullPointerException"); //jdd
        CodeExceptionGen cegs[] = returnMethod.getExceptionHandlers();
        
        // IN PRACTICE, THIS WORKS: the two are the same
        if (ceg != cegs[0]) {
            System.out.println(
                    "  INTERNAL ERROR: exception handler added not found");
        }
        // end jdd
		return returnMethod;
    }
    /**
     * Generates bytecode for a switch statement:
     * 
     * <pre>
     * int runTest (int x) {
     *     switch (x) {
     *     case 1:  return 1;
     *     case 2:  return 3;
     *     case 3:  return 5;
     *     default: return 2;
     *     }
     * }
     * </pre>
     */
    public MethodGen mgSelect( ClassGen clazz ) {

		InstructionList instructions = new InstructionList();
		instructions.append( new ILOAD( 1 ));
		InstructionHandle caseHandles[] = new InstructionHandle[3];
		int caseMatches[] = new int[3];

		for (short i = 0; i < 3; i++) {
		    InstructionList caseList = new InstructionList();
	    	caseList.append( new SIPUSH( (short) (2*i + 1) ));
		    caseList.append( new IRETURN() );

		    caseHandles[i] = instructions.append( caseList );
		    caseMatches[i] = i + 1;
		}	    
		InstructionList dCase = new InstructionList();
		dCase.append( new SIPUSH( (short) 2 ));
		dCase.append( new IRETURN() );
		InstructionHandle dHand = instructions.append( dCase );

		instructions.insert( caseHandles[0],
				     new LOOKUPSWITCH( caseMatches,
								       caseHandles,
								       dHand ));
		MethodGen returnMethod =
		    new MethodGen( Constants.ACC_PUBLIC, 
		                   Type.INT, 
		                   new Type[] { Type.INT },
				    	   new String[] { "x" }, 
				    	   "runTest", 
				    	   clazz.getClassName(),
				   		   instructions, 
				   		   clazz.getConstantPool());
		
		return returnMethod;
    }
    /**
     * Generates code for a while loop.  The while loop returns 0 if
     * the parameter x is greater than or equal to zero, but x 
     * otherwise.
     *
     * <pre>
     * int runTest(int x) {
     *     while (x > 0) {
     *         x --;
     *     }
     *     return x;
     * }
     * </pre>
     * 
     * <p>The actual bytecode produced is:</p>
     *
     * <table cellspacing="10">
     * <tr><th>Label</th><th>Instruction</th>  <th>Stack</th>
     * <tr><td />        <td>ILOAD</td>        <td>_ -&gt; I</td></tr>
     * <tr><td>if:</td>  <td>DUP</td>          <td>I -&gt; II</td></tr>
     * <tr><td />        <td>IFLE (ret)</td>   <td>II -&gt; I</td></tr>
     * <tr><td>loop:</td><td>ICONST_1</td>     <td>I -&gt; II</td></tr>
     * <tr><td />        <td>ISUB</td>         <td>II -&gt; I</td></tr>
     * <tr><td />        <td>GOTO (if)</td>    <td>I -&gt; I</td></tr>
     * <tr><td>ret:</td> <td>IRETURN</td>      <td>I -&gt; _</td></tr>
     * </table>
     */
    public MethodGen mgWhile( ClassGen clazz ) {
		InstructionList instructions = new InstructionList();
		instructions.append( new ILOAD( 1 ));
		InstructionHandle ifHandle = 
	    	instructions.append( new DUP() );

		InstructionHandle loopHandle =
		    instructions.append( new ICONST( 1 ) );
		instructions.append( new ISUB() );
		instructions.append( new GOTO( ifHandle ));

		InstructionHandle retHandle = 
		    instructions.append( new IRETURN() );
	
		instructions.insert( loopHandle, new IFLE( retHandle )); 

		MethodGen returnMethod =
		    new MethodGen( Constants.ACC_PUBLIC, 
		                   Type.INT, 
		                   new Type[] { Type.INT },
				    	   new String[] { "x" }, 
				    	   "runTest", 
				    	   clazz.getClassName(),
				   		   instructions, 
				   		   clazz.getConstantPool());
		
		return returnMethod;
    }
}
