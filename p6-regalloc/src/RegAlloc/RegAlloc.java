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

    // set spilling to true when the spill method is implemented
    public static boolean spilling = true;

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
            if (!spilling)
                throw new Error("Spilling unimplemented");
            Translate.Frame.CodeGen cg = frame.codegen();
            for (Temp s : spills) {
                Translate.Frame.Access access = frame.allocLocal(frame.wordSize);
                for (ListIterator<Instr> i = insns.listIterator(); i.hasNext();) {
                    Instr insn = i.next();
                    List<Temp> u = Arrays.asList(insn.use);
                    List<Temp> d = Arrays.asList(insn.def);
                    if (u.contains(s) && d.contains(s)) {
                        i.previous();
                        Temp t = new Temp();
                        t.spillTemp = true;
                        {
                            Tree.Stm stm = new MOVE(new TEMP(t), access.exp(frame.FP()));
                            for (Instr asm : cg.apply(Collections.singletonList(stm))) i.add(asm);
                            insn.replaceUse(s, t);
                        }
                        if (insn != i.next()) throw new Error();
                        {
                            insn.replaceDef(s, t);
                            Tree.Stm stm = new MOVE(access.exp(frame.FP()), new TEMP(t));
                            for (Instr asm : cg.apply(Collections.singletonList(stm))) i.add(asm);
                        }
                    } else if (u.contains(s)) {
                        Temp t = new Temp();
                        t.spillTemp = true;
                        Tree.Stm stm = new MOVE(new TEMP(t), access.exp(frame.FP()));
                        i.previous();
                        for (Instr asm : cg.apply(Collections.singletonList(stm))) i.add(asm);
                        insn.replaceUse(s, t);
                        if (insn != i.next())
                            throw new Error();
                    } else if (d.contains(s)) {
                        Temp t = new Temp();
                        t.spillTemp = true;
                        insn.replaceDef(s, t);
                        Tree.Stm stm = new MOVE(access.exp(frame.FP()), new TEMP(t));
                        for (Instr asm : cg.apply(Collections.singletonList(stm))) i.add(asm);
                    }
                }
            }
            out.println("# Instructions (after spilling):");
            Temp.Map map = new Temp.Map.Default();
            for (Assem.Instr i : insns)
                out.println(i.format(map));
            out.flush();
        }
        out.println("# Register Allocation:");
        for (Node n : ig.nodes()) {
            out.print(n.temp);
            out.print("->");
            out.println(n.color);
        }
    }
}
