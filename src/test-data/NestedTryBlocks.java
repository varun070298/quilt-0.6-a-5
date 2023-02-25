// NestedTryBlocks.java

public class NestedTryBlocks implements org.quilt.cl.RunTest {

    private int i, j, k;

    public NestedTryBlocks() {}

    public int runTest (int x) {
        try {
            i = x;                              // A
            try {
                j = i;                          // B
            } catch ( Exception e1 ) {
                i = 7;                          // C
            }
            k = j;                              // D
            try {
                if (x==0)                       // E
                    throw new NullPointerException();
                i = 11;                         // E
            } catch ( Exception e2 ) {
                i = 19;                         // F
            }
            i = 2 * i;                          // G
        } catch (NullPointerException e3) {
            i = 37;                             // H
        }
        return i;                               // I
   }
}
