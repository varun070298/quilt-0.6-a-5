/* SummaryAttribute.java */

package org.quilt.frontend.ant;

import org.apache.tools.ant.BuildException;

/**
 * Accept a limited set of String values for the summary attribute. 
 */
public class SummaryAttribute {

    private String value;
    private boolean bVal;

    /**
     * Constructor.
     * @param v A candidate value; causes exception if invalid.
     */
    public SummaryAttribute (String v) {
        setValue (v);
    }
    /**
     * Return the value successfully set.
     * 
     * @return The String passed as the value of the summary attribute. 
     */
    public String getValue() {
        return value;
    }
    /** 
     * Ant-compatible set method.  Interprets on/true/withoutanderr/yes
     * as true, false/no/off as false, and throws exception otherwise.
     * 
     * @param v String, converted to lower case before checking.
     */
    public final void setValue (String value) throws BuildException {
        this.value = value.toLowerCase();
        if ( value.equals("on") || value.equals("true")  
                || value.equals("withoutanderr") || value.equals("yes") ) { 
            bVal = true;
        } else if ( value.equals("false") || value.equals("no") 
                                                || value.equals("off" ) ) {
            bVal = false;
        } else {
            throw new BuildException(
                    "invalid summary attribute value: " + value);
        }
    }
    /** 
     * @return True if the value was on/true/withoutanderr/yes, false
     * otherwise.
     */
    public boolean asBoolean() {
        return bVal;
    }
}
