/* QuiltException.java */

package org.quilt.exception;

/** A superclass for all exceptions that can be thrown by Quilt. */

public class QuiltException extends Exception {

    public QuiltException() { 
        super(); 
    }
    public QuiltException(String msg) { 
        super(msg); 
    }
}
