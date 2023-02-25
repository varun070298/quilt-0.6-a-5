/* TestRegistry.java */

package org.quilt.reg;

import junit.framework.*;

public class TestRegistry extends TestCase {

    private Registry reg = null;

    public TestRegistry(String name) {
        super(name);
    }
    
    String salutation[] = {"hello", "there"};

    String key1[]       = {"Quilt", "ARG0"};
    String key2[]       = {"Elvis", "lives"};
    String key3[]       = {"One plus ", "one"};
    String key4[]       = {"Elvis", "wombat"};
    String key5[]       = {"Elvis", "heart-throb"};
    String obj1         = "froggy boy";
    String obj2         = "Topeka, Kansas";
    Integer obj3        = new Integer(3);
    Boolean obj4        = new Boolean(true);
    Float   obj5        = new Float ((float)5.47);

    public void setUp () {
        reg = new Registry();
        reg.clear();
    } 

    public void testEmpty () {
        assertEquals ("Empty registry has items in it", 0, reg.size());
        assertNull   ("Retrieved item from empty registry",
                                                reg.get(salutation) );
        assertTrue   ("Empty registry is not empty", reg.isEmpty() );
    }

    public void testSimple () {
        assertNull ("Put with no previous value - ", reg.put(key1, obj1));
        assertEquals (1, reg.size());
        assertTrue (reg.containsKey(key1));
        assertNull ("Put with no previous value - ", reg.put(key2, obj2));
        assertEquals (2, reg.size());
        assertTrue (reg.containsKey(key2));
        assertNull ("Put with no previous value - ", reg.put(key3, obj3));
        assertEquals (3, reg.size());
        assertTrue (reg.containsKey(key3));

        // make sure all three are present and have correct values
        assertEquals ("'Quilt' get returns wrong value", obj1, reg.get(key1));
        assertEquals ("'Elvis' get returns wrong value", obj2, reg.get(key2));
        assertEquals ("'One plus' get returns wrong value", 
                                                        obj3, reg.get(key3));

        // overwrite, check return value is previous value
        assertEquals ("Put did not return previous value",
                                    obj1, reg.put(key1, obj2));
        assertEquals (3, reg.size());
        assertEquals ("Put did not return previous value",
                                    obj2, reg.put(key2, obj3));
        assertEquals (3, reg.size());
        assertEquals ("Put did not return previous value",
                                    obj3, reg.put(key3, obj1));
        assertEquals (3, reg.size());

        // check value returned on deletion is stored value
        assertEquals ("remove did not return value stored", 
                                            obj2, reg.remove (key1));
        assertTrue ("Item deleted but still present", !reg.containsKey(key1));
        assertEquals (2, reg.size());

        assertEquals ("remove did not return value stored", 
                                            obj3, reg.remove (key2));
        assertTrue ("Item deleted but still present", !reg.containsKey(key2));
        assertEquals (1, reg.size());

        assertEquals ("remove did not return value stored", 
                                            obj1, reg.remove (key3));
        assertTrue ("Item deleted but still present", !reg.containsKey(key3));
        assertEquals (0, reg.size());
        assertTrue   ("Empty registry is not empty", reg.isEmpty() );
    }
 
    public void testBoolean () {
        // put some random stuff in the registry ...
        assertNull ("Put with no previous value - ", reg.put(key1, obj1));
        assertEquals (1, reg.size());
        assertTrue (reg.containsKey(key1));
        // first Elvis 
        assertNull ("Put with no previous value - ", reg.put(key2, obj2));
        assertEquals (2, reg.size());
        assertTrue (reg.containsKey(key2));
        assertNull ("Put with no previous value - ", reg.put(key3, obj3));
        assertEquals (3, reg.size());
        assertTrue (reg.containsKey(key3));

        Boolean t = new Boolean(true);
        Boolean f = new Boolean(false);
        String tKey[] = {"the", "truth"};
        String fKey[] = {"a", "falsehood"};
        assertNull ("Put with no previous value - ", reg.put(tKey, t));
        assertEquals (4, reg.size());
        assertTrue ("No truth in the database", reg.containsKey(tKey));
        assertNull ("Put with no previous value - ", reg.put(fKey, f));
        assertEquals (5, reg.size());
        assertTrue ("No lies in the database", reg.containsKey(fKey));

        assertEquals ("Can't get truth out of the registry", t, reg.get(tKey));
        assertEquals ("Can't get lies out of the registry" , f, reg.get(fKey));
        
        assertTrue ( t.booleanValue() );
        assertTrue ( ((Boolean)reg.get(tKey)).booleanValue() );
        assertTrue (!f.booleanValue() );
        assertTrue (!((Boolean)reg.get(fKey)).booleanValue() );
    }
    public void testPartialOverlap() {
        assertNull ("Put with no previous value - ", reg.put(key1, obj1));
        assertEquals (1, reg.size());
        assertTrue (reg.containsKey(key1));
        // Elvis/lives --> Topeka, Kansas
        assertNull ("Put with no previous value - ", reg.put(key2, obj2));
        assertEquals (2, reg.size());
        assertTrue (reg.containsKey(key2));
        assertNull ("Put with no previous value - ", reg.put(key3, obj3));
        assertEquals (3, reg.size());
        assertTrue (reg.containsKey(key3));
        
        // the next two have the same value (Elvis) in key[0]
        // Elvis/wombat --> Boolean(true)
        assertNull ("Put with no previous value - ", reg.put(key4, obj4));
        assertEquals (4, reg.size());
        assertTrue (reg.containsKey(key4));
        // Elvis/heart-throb --> Float(5.47)
        assertNull ("Put with no previous value - ", reg.put(key5, obj5));
        assertEquals (5, reg.size());
        assertTrue (reg.containsKey(key5));

        // overwrite, check return value is previous value
        assertEquals ("Put did not return previous value",
                                    obj1, reg.put(key1, obj2));
        assertEquals (5, reg.size());
        assertEquals ("Put did not return previous value",
                                    obj2, reg.put(key2, obj3));
        assertEquals (5, reg.size());
        assertEquals ("Put did not return previous value",
                                    obj3, reg.put(key3, obj1));
        assertEquals (5, reg.size());
        
        // Elvis/wombat returns Integer(3) 
        assertEquals ("Elvis/wombat put did not return previous value",
                                    obj4, reg.put(key4, obj5));
        assertEquals (5, reg.size());
        assertEquals ("Elvis/heart-throb put did not return previous value",
                                    obj5, reg.put(key5, obj4));
        assertEquals (5, reg.size());

        // check value returned on deletion is stored value
        // first Elvis ...
        assertEquals ("remove did not return value stored", 
                                            obj4, reg.remove (key5));
        assertTrue ("Item deleted but still present", !reg.containsKey(key5));
        assertEquals (4, reg.size());
        assertEquals ("remove did not return value stored", 
                                            obj5, reg.remove (key4));
        assertTrue ("Item deleted but still present", !reg.containsKey(key4));
        assertEquals (3, reg.size());
        // then the rest
        assertEquals ("remove did not return value stored", 
                                            obj2, reg.remove (key1));
        assertTrue ("Item deleted but still present", !reg.containsKey(key1));
        assertEquals (2, reg.size());

        assertEquals ("remove did not return value stored", 
                                            obj3, reg.remove (key2));
        assertTrue ("Item deleted but still present", !reg.containsKey(key2));
        assertEquals (1, reg.size());

        assertEquals ("remove did not return value stored", 
                                            obj1, reg.remove (key3));
        assertTrue ("Item deleted but still present", !reg.containsKey(key3));
        assertEquals (0, reg.size());
        assertTrue   ("Empty registry is not empty", reg.isEmpty() ); // GEEP
    }
    public void testElvis () {
        // keys{2,4,5} have "Elvis" as key[0]
        
        // Elvis/lives --> Topeka, Kansas
        assertNull ("Put with no previous value - ", reg.put(key2, obj2));
        assertEquals (1, reg.size());
        assertTrue (reg.containsKey(key2));
        
        // Elvis/wombat --> Boolean(true)
        assertNull ("Put with no previous value - ", reg.put(key4, obj4));
        assertEquals (2, reg.size());
        assertTrue (reg.containsKey(key4));
        
        // Elvis/heart-throb --> Float(5.47)
        assertNull ("Put with no previous value - ", reg.put(key5, obj5));
        assertEquals (3, reg.size());
        assertTrue (reg.containsKey(key5));

        // make sure all three are present and have correct values
        assertEquals ("Elvis/lives get error",       obj2, reg.get(key2));
        assertEquals ("Elvis/wombat get error",      obj4, reg.get(key4));
        assertEquals ("Elvis/heart-throb get error", obj5, reg.get(key5));

        // overwrite, check return value is previous value
        assertEquals ("Put did not return previous value",
                                    obj2, reg.put(key2, obj4));
        assertEquals (3, reg.size());
        assertEquals ("Put did not return previous value",
                                    obj4, reg.put(key4, obj5));
        assertEquals (3, reg.size());
        assertEquals ("Put did not return previous value",
                                    obj5, reg.put(key5, obj2));
        assertEquals (3, reg.size());
        
        // check value returned on deletion is stored value
        assertEquals ("remove did not return value stored", 
                                            obj4, reg.remove (key2));
        assertTrue ("Item deleted but still present", !reg.containsKey(key2));
        assertEquals (2, reg.size());

        assertEquals ("remove did not return value stored", 
                                            obj5, reg.remove (key4));
        assertTrue ("Item deleted but still present", !reg.containsKey(key4));
        assertEquals (1, reg.size());

        assertEquals ("remove did not return value stored", 
                                            obj2, reg.remove (key5));
        assertTrue ("Item deleted but still present", !reg.containsKey(key5));
        assertEquals (0, reg.size());
        assertTrue   ("Empty registry is not empty", reg.isEmpty() ); // GEEP
    }
    public void testRG2 () {
        // duplicating bug found in the field
        String keys1[] = {"rg2", "msg"};
        Boolean val1   = new Boolean(true);
        String keys2[] = {"rg2", "test"};
        String val2    = "hope this works";
       
        assertEquals(0, reg.size());

        assertNull ("Put with no previous value - ", reg.put(keys1, val1));
        assertEquals (1, reg.size());
        assertTrue (reg.containsKey(keys1));
        
        assertNull ("Put with no previous value - ", reg.put(keys2, val2));
        assertEquals (2, reg.size());
        assertTrue (reg.containsKey(keys2));
        
        assertEquals ("rg2/msg get error",          val1, reg.get(keys1));
        assertEquals ("rg2/test get error",         val2, reg.get(keys2));
    }
    public void testExceptions () {
        // database is empty
        try {
            reg.get(null);
            fail ("get with null key did not throw exception");
        } catch (IllegalArgumentException e) { }

        try {
            reg.put(key1, obj1);
            reg.put(null, obj2);
            fail ("put with null key did not throw exception");
        } catch (IllegalArgumentException e) { 
            assertEquals (1, reg.size());
        }
        try {
            reg.put(key2, obj2);
            reg.containsKey(null);
            fail ("containsKey with null key did not throw exception");
        } catch (IllegalArgumentException e) { 
            assertEquals (2, reg.size());
        }
        try {
            reg.put(key3, obj3);
            reg.remove(null);
            fail ("remove with null key did not throw exception");
        } catch (IllegalArgumentException e) { 
            assertEquals (3, reg.size());
        }
    }
    // COMING ATTRACTIONS:
    //
    // test sequential operations
    //
    // test set-wise operations
    //
    // test sub-map operations

}
        
