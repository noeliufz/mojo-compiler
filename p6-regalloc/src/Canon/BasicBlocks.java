/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package Canon;

import java.util.*;
import Translate.Temp.*;
import Translate.Tree.Stm;
import Translate.Tree.Stm.*;

public class BasicBlocks implements java.util.function.Supplier<List<List<Stm>>> {
    private final Label done;
    private final List<List<Stm>> blocks = new LinkedList<List<Stm>>();

    public Label done() {
        return done;
    };

    public List<List<Stm>> get() {
        return blocks;
    }
    
    private List<Stm> newBlock() {
        List<Stm> block = new LinkedList<Stm>();
        blocks.add(block);
        return block;
    }

    public BasicBlocks(List<Stm> stms) {
        List<Stm> block = null;
        for (Stm s : stms) {
            if (s instanceof LABEL) {
                if (block != null)
                    block.add(new JUMP(((LABEL) s).label()));
                block = newBlock();
                block.add(s);
            } else {
                if (block == null) {
                    block = newBlock();
                    block.add(new LABEL(new Label()));
                }
                block.add(s);
                if (s instanceof JUMP || s instanceof CJUMP)
                    block = null;
            }
        }
        if (block == null) {
            block = newBlock();
            done = new Label();
            block.add(new LABEL(done));
        } else {
            Stm s = block.getFirst();
            done = ((LABEL) s).label();
        }
    }
}
