/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package Canon;

import java.util.*;
import Translate.Temp.*;
import Translate.Tree.Stm;
import Translate.Tree.Exp;
import Translate.Tree.Stm.*;

public class TraceSchedule implements java.util.function.Supplier<List<Stm>> {
    private final List<Stm> stms = new LinkedList<Stm>();
    private final HashMap<Label, List<Stm>> map = new HashMap<Label, List<Stm>>();

    private JUMP trace(List<Stm> l) {
        for (;;) {
            Stm s = l.getFirst();
            if (s instanceof LABEL) {
                stms.addAll(l.subList(0, l.size() - 1));
                s = l.getLast();
                if (s instanceof JUMP j) {
                    if (j.targets().length == 1) {
                        Label target = j.targets()[0];
                        l = map.remove(target);
                        if (l != null)
                            continue;
                        if (target == blocks.done() && map.isEmpty())
                            return null;
                    }
                    return j;
                }
                if (s instanceof CJUMP j) {
                    // Try to follow by false target
                    l = map.remove(j.iffalse());
                    if (l != null) {
                        stms.add(j);
                        continue;
                    }
                    // else try to follow by true target
                    l = map.remove(j.iftrue());
                    if (l != null) {
                        stms.add(switch (j) {
                            case CJUMP.BEQ(Exp ll, Exp r, Label t, Label f) -> new CJUMP.BNE(ll, r, f, t);
                            case CJUMP.BNE(Exp ll, Exp r, Label t, Label f) -> new CJUMP.BEQ(ll, r, f, t);
                            case CJUMP.BGE(Exp ll, Exp r, Label t, Label f) -> new CJUMP.BLT(ll, r, f, t);
                            case CJUMP.BLE(Exp ll, Exp r, Label t, Label f) -> new CJUMP.BGT(ll, r, f, t);
                            case CJUMP.BGT(Exp ll, Exp r, Label t, Label f) -> new CJUMP.BLE(ll, r, f, t);
                            case CJUMP.BLT(Exp ll, Exp r, Label t, Label f) -> new CJUMP.BGE(ll, r, f, t);
                            });
                        continue;
                    }

                    // else add bridging jump
                    if (j.iffalse() == blocks.done() && map.isEmpty()) {
                        stms.add(j);
                        return null;
                    }
                    Label f = new Label();
                    stms.add(switch(j) {
                        case CJUMP.BEQ(Exp ll, Exp rr, Label tt, Label ff) -> new CJUMP.BEQ(ll, rr, tt, f);
                        case CJUMP.BNE(Exp ll, Exp rr, Label tt, Label ff) -> new CJUMP.BNE(ll, rr, tt, f);
                        case CJUMP.BGE(Exp ll, Exp rr, Label tt, Label ff) -> new CJUMP.BGE(ll, rr, tt, f);
                        case CJUMP.BLE(Exp ll, Exp rr, Label tt, Label ff) -> new CJUMP.BLE(ll, rr, tt, f);
                        case CJUMP.BGT(Exp ll, Exp rr, Label tt, Label ff) -> new CJUMP.BGT(ll, rr, tt, f);
                        case CJUMP.BLT(Exp ll, Exp rr, Label tt, Label ff) -> new CJUMP.BLT(ll, rr, tt, f);
                        });
                    stms.add(new LABEL(f));
                    return new JUMP(j.iffalse());
                }
            }
            throw new Error("Bad basic block in TraceSchedule");
        }
    }

    private final BasicBlocks blocks;
    
    public TraceSchedule(BasicBlocks b) {
        blocks = b;
        for (List<Stm> block : blocks.get()) {
            LABEL lab = (LABEL)block.getFirst();
            map.put(lab.label(), block);
        }
        List<Stm> last = map.remove(blocks.done());
        JUMP j = null;
        for (List<Stm> block : blocks.get()) {
            LABEL lab = (LABEL)block.getFirst();
            List<Stm> l = map.remove(lab.label());
            if (l != null) {
                if (j != null)
                    stms.add(j);
                j = trace(l);
            }
        }
        if (j != null)
            stms.add(j);
        stms.addAll(last);
    }

    public List<Stm> get() {
        return stms;
    }
}

