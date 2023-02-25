/* Msg.java */

package org.quilt.runner;

/** 
 * Debug message module.
 *
 * @author <a href="jdd@dixons.org">Jim Dixon</a>
 */
public class Msg {

    private String baseName;
    private static final int  DEFAULT_WIDTH = 60;
    private static final char DEFAULT_BIG   = '=';
    private static final char DEFAULT_SMALL = '-';
   
    /** If false, nothing is output */
    private boolean talking = true;
    /** Number of fill characters in banner, excluding newline. */
    private int bannerWidth;
    /** Fill character for 'big' banners. */
    private char bigChar    = '=';
    /** Fill character for normal banners. */
    private char smallChar  = '-';
    /** String filled with 'big' fill character, ends with newline */
    private String myBigBanner;
    /** String filled with normal fill character, ends with newline */
    private String myBanner;
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    // @todo add a way to specify where the output goes
    
    /** @param base Name of class or other module. */
    public Msg (final String base) {
        this(base, DEFAULT_WIDTH, DEFAULT_BIG, DEFAULT_SMALL);
    }
    /**
     * @param base  Name of module.
     * @param width Width in characters of banners (default = 60)
     */
    public Msg (final String base, final int width) {
        this(base, width, DEFAULT_BIG, DEFAULT_SMALL);
    }
    /**
     * @param base  Name of module.
     * @param width Width of banners.
     * @param big   Fill character in 'big' banners (default is =).
     */
    public Msg (final String base, final int width, final char big) {
        this(base, width, big, DEFAULT_SMALL);
    }
    /**
     * @param base  Name of module.
     * @param width Width of banners.
     * @param big   Fill character in 'big' banners.
     * @param small Fill character in normal banners (default is -).
     */
    public Msg (final String base, final int width, final char big,
                                                    final char small) {
        baseName    = base;
        // EXCEPTION IF width < 1, characters are not printing //////
        bannerWidth = width;
        bigChar     = big;
        smallChar   = small;

        // there must be a cheaper way of doing this!
        StringBuffer bigUn   = new StringBuffer (width + 1);
        StringBuffer smallUn = new StringBuffer (width + 1);
        for (int i = 0; i < width; i++) {
            bigUn.append(big);
            smallUn.append(big);
        }
        bigUn.append('\n');
        myBigBanner = bigUn.toString();

        smallUn.append('\n');
        myBanner   = smallUn.toString();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Turns output off or on.
     * @param b If true, there will be output; if false, not.
     */
    public void talk (final boolean b) {
        talking = b;
    }
    public void trace(final String where) {
        if (talking) {
            System.out.println ("---> " + baseName + where );
        }
    }
    public void banner (final String where) {
        if (talking) {
            System.out.print ( 
                    myBanner + baseName + where + "\n" + myBanner );
        }
    }
    public void bannerAnd (final String where, final String stuff) {
        if (talking) {
            System.out.print ( myBanner + baseName + where 
                                   + "\n" + myBanner + stuff + "\n");
        }
    }
    public void bigBanner (final String where) {
        if (talking) {
            System.out.print ( myBigBanner + baseName + where 
                                                    + "\n" + myBigBanner );
        }
    }
    public boolean isTalking() {
        return talking;
    }
}
