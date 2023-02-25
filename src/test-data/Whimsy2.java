/* Whimsy2.java */

import org.quilt.cover.stmt.StmtRegistry;

public class Whimsy2 implements org.quilt.cl.RunTest {
    
    // can be private
    public final static StmtRegistry q$$qStmtReg 
        = (StmtRegistry) 
            ((org.quilt.cl.QuiltClassLoader)org.quilt.QIC.class.getClassLoader())
                .getRegistry("org.quilt.cover.stmt.StmtRegistry");
   
    public final static int[] q$$q = new int[52];

    public final static int q$$qID = q$$qStmtReg.registerCounts("Whimsy", q$$q);

    public Whimsy2 () {
        q$$q[7] = 92;
    }
    public int runTest (int x) {
        q$$q[5]++;
        // possible index exception
        return q$$q[x]++;
    }
}
