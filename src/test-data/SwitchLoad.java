/* SwitchLoad.java */


/** Tests the switch statement in various ways; also has try/catch. */

public class SwitchLoad implements org.quilt.cl.RunTest {
    public static final int   MAGICNUMBER   = 0xCAFEBABE;
    public static final long  MAGICLONG     = 0x00000000CAFEBABE;
    public static final short MAGICSHORT    = (short) 0xCAFE;

    public static final float  PI_FLOAT     = (float) 3.1415;
    public static final double PI_DOUBLE    = (double) 3.1415;
    public static final int    NORMALNUMBER = 188;
    public static final int    WEIRDNUMBER  = -1;

    private double twoPie;

    public boolean switchStatement( int stmt ) {
        
        switch( stmt ) {
        case WEIRDNUMBER:
            throw new RuntimeException("Weird Switch");
        case NORMALNUMBER:
            return false;
        case MAGICNUMBER:
            return true;
        default:
            throw new RuntimeException("Default Switch");
        }
    }
    
    public boolean doMagicSwitch() throws RuntimeException {
        return switchStatement( MAGICNUMBER );
        //return true;
    }

    public boolean doNormalSwitch( ) throws RuntimeException {
        return switchStatement( NORMALNUMBER );
        //return false;
    }

    public boolean doWeirdSwitch( ) throws RuntimeException {
        return switchStatement( WEIRDNUMBER );
        //throw new RuntimeException("Weird Switch");
    }

    public boolean doDefaultSwitch( ) throws RuntimeException {
        return switchStatement( 10 );
    }
        
    public int runTest(int x) {
        boolean weirdSwitchOK = true;
        boolean defaultSwitchOK = true;

        if (!doMagicSwitch())
            throw new RuntimeException("Magic Switch failed.");
        if (doNormalSwitch())
            throw new RuntimeException("Normal Switch failed.");
        try {
            doWeirdSwitch();
            weirdSwitchOK = false;
        } catch (Exception e) {
            weirdSwitchOK = true;
        }

        if (!weirdSwitchOK)
            throw new RuntimeException("Weird Switch failed.");

        boolean isBad = false;
        try {
            doDefaultSwitch();
            defaultSwitchOK = false;
        } catch (Exception e) {
            defaultSwitchOK = true;
        }

        if (!defaultSwitchOK)
            throw new RuntimeException("Default Switch Failed");

        return 42;
    }
}
