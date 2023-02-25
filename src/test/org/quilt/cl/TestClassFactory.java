/* TestClassFactory.java */

package org.quilt.cl;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="ddp@apache.org">David Dixon-Peugh</a>
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a> - mods Jul 2003

 */
public class TestClassFactory extends TestCase {
    private ClassLoader loader = new CFClassLoader();
    private String name = null;
    private RunTest tinter = null;
    
    public class CFClassLoader
    	extends URLClassLoader 
    {
    	public CFClassLoader() {
    		super(new URL[0], TestClassFactory.class.getClassLoader());	
    	}
    	
    	public Class findClass( String className ) 
    		throws ClassNotFoundException
    	{
    		String classFile = className.replace('.', File.separatorChar) 
    				+ ".class";
        	InputStream bytecodeIS = getResourceAsStream( classFile );
    		
    		byte bytecode[] = new byte[4096];
    		int len = 0;
    		try {
                len = bytecodeIS.read(bytecode);
            } catch (IOException e) {
            	e.printStackTrace();
            	throw new ClassNotFoundException( e.getMessage() );
            }
    		
    		Class RC = defineClass( className, 
    			bytecode, 0, 
    			len);
    		return RC;
    	}
    
    /**
     * This is where we generate bytecode for the 
     * Instrumenting Class Loader to instrument.
     *
     * The resourceName looks like "test/data/TestMyStuff.class"
     * it needs to be converted to "test.data.TestMyStuff"
     */
    	public InputStream getResourceAsStream( String resName ) 
    	{
    		return ClassFactory.getInstance().getResourceAsStream( resName );
    	}
    }
    
    /**
     * Constructor for TestClassFactory.
     * @param arg0
     */
    public TestClassFactory(String arg0) {
        super(arg0);
    	this.name = arg0;
    }

    public void setUp() {
    	try {
    		Class clazz = loader.loadClass("test.data." + name);
    		tinter = (RunTest) clazz.newInstance();		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void testDefault() 
    { 
    	assertEquals( 2, tinter.runTest( 5 ) );
    }
    public void testIfThen() { 
    	assertEquals( 3, tinter.runTest( 5 ) );
		assertEquals( 5, tinter.runTest( -1 ) );
    }

    public void testSelect() { 		
    	assertEquals( 2, tinter.runTest( 0 ) );
    	assertEquals( 1, tinter.runTest( 1 ) );
    	assertEquals( 3, tinter.runTest( 2 ) );
    	assertEquals( 5, tinter.runTest( 3 ) );
    	assertEquals( 2, tinter.runTest( 4 ) );
    }
   
    public void testWhile() { 		
    	assertEquals( 0, tinter.runTest( 0 ) );
  		assertEquals( -1, tinter.runTest( -1 ));
 	 	assertEquals( 0, tinter.runTest( 1 ) );
  		assertEquals( 0, tinter.runTest( 5 ) );
  	}

    public void testNPENoCatch() { 
    	try {
    	    tinter.runTest( 0 );
        	fail("Expecting Null Pointer Exception.");
    	} catch (NullPointerException npe) { }
    }

    public void testNPEWithCatch() { 
        int retValue = 0;
    	try {
            retValue = tinter.runTest( 0 );
        } catch (NullPointerException npe) {
            fail ("Unexpected null pointer exception");
        }
        assertEquals ( "NPEWithCatch did not handle the exception",
                3, retValue); 
    }
}
