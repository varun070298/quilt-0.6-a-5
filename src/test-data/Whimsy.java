/* Whimsy.java */

import org.quilt.cover.stmt.StmtRegistry;

public class Whimsy implements org.quilt.cl.RunTest {
    public static int q$$q[] = new int [52] ;
    public static int q$$Ver = 0;
    private static class q$$qKlazz { };
    public static final StmtRegistry q$$qStmtReg = (StmtRegistry)
        ((org.quilt.cl.QuiltClassLoader)q$$qKlazz.class.getClassLoader()).
            getRegistry("org.quilt.cover.stmt.StmtRegistry");
    public static int q$$qID = q$$qStmtReg.registerCounts(
            "Whimsy", q$$q);
    
    public Whimsy () {
        q$$q[7] = 92;
    }

    public int runTest (int x) {
        q$$q[5]++;
        // possible index exception
        return q$$q[x]++;
    }
}
