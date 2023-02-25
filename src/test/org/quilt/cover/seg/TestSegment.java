/* TestSegment.java */
package org.quilt.cover.seg;

import junit.framework.*;

public class TestSegment extends TestCase {

    public TestSegment ( String name ) {
        super (name);
    }

    public void testDefaults() {
        Segment s = new Segment();
        assertEquals ("error in default index",  -1, s.getIndex());
        assertEquals ("error in default visits",  0, s.getVisits());
        assertEquals ("error in default From",   -1, s.getFrom());
        assertEquals ("error in default To",     -1, s.getTo());
    }

    public void testSimple() {
        Segment s = new Segment(47);
        assertEquals ("wrong segment index", 47, s.getIndex());

        s.setFrom(97);
        assertEquals ("setting from doesn't work", 97, s.getFrom());

        s.setIndex(3);
        assertEquals ("setting index doesn't work", 3, s.getIndex());

        s.setTo(101);
        assertEquals ("setting to doesn't work", 101, s.getTo());

        final int biggish = 497;
        s.setVisits(biggish);
        assertEquals ("setting visit counts doesn't work", 
                                            biggish, s.getVisits());
        s.visit();
        s.visit();
        assertEquals ("error after visit() calls", 
                                            biggish + 2, s.getVisits());

        s.reset();
        assertEquals ("reset doesn't work", 0, s.getVisits());
    }

    public void testAddition () {
        Segment s11 = new Segment (11);
        Segment s22 = new Segment (22);
        Segment s33 = new Segment (33);
        
        assertEquals ("error setting index in constructor", 
                                                    11, s11.getIndex());
        assertEquals ("error setting index in constructor", 22, 
                                                    s22.getIndex());
        assertEquals ("error setting index in constructor", 
                                                    33, s33.getIndex());

        s11.setVisits(11);
        s22.setVisits(13);
        s33.setVisits(17);

        assertEquals ("error in segment addition", 41, 
                                        s11.add(s22.add(s33)).getVisits());
    } 
}
