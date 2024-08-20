/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package FlowGraph;

import Assem.Instr;
import Assem.Instr.*;
import Translate.Temp;
import Translate.Temp.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;

public final class AssemFlowGraph extends FlowGraph<Instr,AssemNode> {

    public Set<Temp> def(AssemNode node) {
        return node.def;
    }

    public Set<Temp> use(AssemNode node) {
        return node.use;
    }

    /**
     * My implementation builds a flowgraph node per basic block.
     */
    final Map<Label, AssemNode> blocks = new LinkedHashMap<Label, AssemNode>();

    public AssemFlowGraph(List<Instr> insns) {
        AssemNode from = null;
        boolean seenStm = false;
        for (Instr i : insns) {
            if (i instanceof LABEL l) {
                if (from == null) {
                    from = new AssemNode(this, i);
                    seenStm = false;
                } else if (seenStm) {
                    AssemNode to = new AssemNode(this, i);
                    addEdge(from, to);
                    from = to;
                    seenStm = false;
                }
                blocks.put(l.label, from);
            } else {
                if (from == null)
                    from = new AssemNode(this, i);
                seenStm = true;
                from.instrs.add(i);
                if (i.jumps.length != 0)
                    from = null;
            }
        }
        for (AssemNode n : nodes()) {
            for (Instr i : n.instrs) {
                for (Temp u : i.use)
                    if (!n.def.contains(u))
                        n.use.add(u);
                Collections.addAll(n.def, i.def);
            }
            Instr i = n.instrs.getLast();
            for (Label l : i.jumps) {
                AssemNode to = blocks.get(l);
                if (to != null)
                    addEdge(n, to);
            }
        }
    }
}
