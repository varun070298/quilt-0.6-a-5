/* LabeledNOP */

package org.quilt.bytecode;

import org.apache.bcel.generic.*;

/**
 * An extension of the NOP which provides a label. To ease debugging.
 */

public class LabeledNOP extends NOP {
    private String label = null;

    public LabeledNOP(String pos) {
        label = pos;
    }

    public String toString(boolean verbose) {
        if (verbose) {
            return super.toString(true) + "\t{" + label + "}";
        } else {
            return super.toString(false);
        }
    }
}
