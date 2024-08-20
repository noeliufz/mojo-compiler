/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package RegAlloc;

import java.util.*;
import Translate.Temp;
import Translate.Tree;
import Translate.Tree.Exp.*;
import Translate.Tree.Stm.*;
import Assem.Instr;

public class RegAlloc implements Temp.Map {
    FlowGraph.AssemFlowGraph cfg;
    Liveness ig;
    public Temp[] spills;
    Color color;

    public Temp get(Temp temp) {
        Temp t = ig.get(temp).color;
        if (t == null)
            t = temp;
        return t;
    }

    public RegAlloc(Translate.Frame frame, List<Instr> insns,
            java.io.PrintWriter out) {
        for (;;) {
            out.println("# Control Flow Graph:");
            cfg = new FlowGraph.AssemFlowGraph(insns);
            cfg.show(out);
            out.println("# Interference Graph:");
            ig = new Liveness(cfg, frame);
            ig.show(out);
            color = new Color(ig, frame);
            spills = color.spills();
            if (spills == null)
                break;
            out.println("# Spills:");
            for (Temp s : spills)
                out.println(s);
            out.flush();
            throw new Error("Spilling unimplemented");
        }
        out.println("# Register Allocation:");
        for (Node n : ig.nodes()) {
            out.print(n.temp);
            out.print("->");
            out.println(n.color);
        }
    }
}
