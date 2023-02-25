/* TestArrComparator.java */

package org.quilt.reg;

import java.util.*;
import junit.framework.*;

public class TestArrComparator extends TestCase {

    private Registry reg   = new Registry();
    private Comparator cmp = reg.comparator();
   
    int compare (String[] s1, String [] s2) {
        return cmp.compare(s1, s2);
    }
    int objCompare (Object o1, Object o2) {
        return cmp.compare(o1, o2);
    }
    final String key0[]  = {"Quilt"};
    final String key1[]  = {"Quilt", "ARG0"};
    final String key2[]  = {"Elvis", "lives"};
    final String key20[] = {"Elvis", "lives", "forever"};
    final String key22[] = {"Elvis", "lives", "forever", "in", "Graceland"};
    final String key3[]  = {"One plus ", "one"};
    final String key4[]  = {"Elvis", "wombat"};
    final String key5[]  = {"Elvis", "heart-throb"};
    final String key6[]  = {"rg2", "msg"};
    final String key7[]  = {"rg2", "test"};
    final String key8[]  = {"rg2", "test", "a"};
    final String key9[]  = {"rg2", "test", "b"};

    public void testSelfCompare () {
        // self-comparisons should always return 0
        assertEquals ("compare to self does not return 0",  
                                                0, compare(key0,  key0) );
        assertEquals ("compare to self does not return 0",  
                                                0, compare(key1,  key1) );
        assertEquals ("compare to self does not return 0", 
                                                0, compare(key20, key20));
        assertEquals ("compare to self does not return 0", 
                                                0, compare(key22, key22));
    }
    public void testDiffLen () {
        // shorter to otherwise identical but longer string returns -1
        assertEquals ("compare to longer but otherwise identical",  
                                                -1, compare(key0, key1) );
        assertEquals ("compare to longer but otherwise identical",  
                                                -1, compare(key2, key20) );
        assertEquals ("compare to longer but otherwise identical",  
                                                -1, compare(key20, key22));
            
        // longer to otherwise identical but shorter string returns +1
        assertEquals ("compare to longer but otherwise identical",  
                                                1, compare(key1, key0)   );
        assertEquals ("compare to longer but otherwise identical",  
                                                1, compare(key20, key2)  );
        assertEquals ("compare to longer but otherwise identical",  
                                                1, compare(key22, key20) );
    }
    public void testMiscCompare () {
        // seem to fail in the field
        assertEquals ("rg2/msg vs rg2/test", -1, compare(key6, key7));
        assertEquals ("rg2/msg vs rg2/test",  1, compare(key7, key6));

        // two-string arrays
        assertEquals ("Elvis/heart-throb vs lives", -1, compare(key5, key2));
        assertEquals ("Elvis/lives vs wombat",      -1, compare(key2, key4));
        assertEquals ("Elvis/lives vs heart-throb",  1, compare(key2, key5));
        assertEquals ("Elvis/wombat vs lives",       1, compare(key4, key2));

        // three-string arrays
        assertEquals ("rg2/test/a vs b", -1, compare(key8, key9));
        assertEquals ("rg2/test/b vs a",  1, compare(key9, key8));
    }
    public void testExceptions () {
        try {
            objCompare ("this is a string", new Boolean(true) );
            fail (
            "args not String[], Comparator did not throw ClassCastException");
        } catch (ClassCastException e) {
            // success
        }
    }
}
        
