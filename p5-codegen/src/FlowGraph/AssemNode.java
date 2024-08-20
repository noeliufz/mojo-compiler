/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package FlowGraph;

import Translate.Temp;
import Assem.Instr;
import java.util.*;

public class AssemNode {
    public List<Instr> instrs = new LinkedList<Instr>();
    public Set<Temp> liveIn = new LinkedHashSet<Temp>();
    public Set<Temp> liveOut = new LinkedHashSet<Temp>();
    public Set<Temp> def = new LinkedHashSet<Temp>();
    public Set<Temp> use = new LinkedHashSet<Temp>();
    public AssemNode(AssemFlowGraph g, Instr i) {
        index = g.size();
        g.put(i, this);
    }
    private final int index;
    public String toString() { return String.valueOf(index); }
    public boolean equals(AssemNode n) { return index == n.index; }
}
