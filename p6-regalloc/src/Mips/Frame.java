/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package Mips;

import java.util.*;

import Translate.Temp;
import Translate.Temp.*;
import Translate.Tree.*;
import Translate.Tree.Exp.*;
import Translate.Tree.Stm.*;

public class Frame extends Translate.Frame {
    public Translate.Frame newFrame(String name, boolean hasParent, boolean hasChildren) {
        Frame f = new Frame(name);
        if (hasParent) {
            if (hasChildren)
                // inner frames must access my link in memory
                f.link = f.allocLocal(wordSize);
            else
                // no inner frames so my link can be in a register
                f.link = f.allocLocal(new Temp("_link"));
        } else
            f.link = null;
        return f;
    }

    Access link;
    public Access link() { return link; }

    static final int wordSize = 4;

    public Frame() {
        super(null, wordSize);
    }

    private Frame(String n) {
        super(Temp.getLabel(n), wordSize);
    }

    public static class InFrame extends Access {
        public final int offset;
        public final int size;
        protected InFrame(int o, int s) {
            offset = o;
            size = s;
        }
        public Exp exp(Exp fp) {
            return new MEM(fp, new Exp.CONST(offset), size);
        }
        public String toString() {
            return "FP+" + offset;
        }
    }
    public static class InReg extends Access {
        public final Temp temp;
        protected InReg(Temp t) {
            temp = t;
        }
        public Exp exp(Exp fp) {
            return new TEMP(temp);
        }
        public String toString() {
            return temp.toString();
        }
    }

    private int formalSize = 0;
    public Access allocFormal(int size) {
        size = (size + wordSize - 1) / wordSize * wordSize;
        Access formal = new InFrame(formalSize, size);
        formals.add(formal);
        formalSize += size;
        return formal;
    }
    public Access allocFormal(Temp t) {
        Access formal = new InReg(t);
        formals.add(formal);
        formalSize += wordSize;
        return formal;
    }

    private int localSize = 0;
    public Access allocLocal(int size) {
        size = (size + wordSize - 1) / wordSize * wordSize;
        localSize += size;
        return new InFrame(-localSize, size);
    }
    public Access allocLocal(Temp t) {
        return new InReg(t);
    }

    static final Temp ZERO = new Temp("$0"); // zero reg
    static final Temp AT = new Temp("$at"); // reserved for assembler
    static final Temp V0 = new Temp("$v0"); // function result
    static final Temp V1 = new Temp("$v1"); // second function result
    static final Temp A0 = new Temp("$a0"); // argument1
    static final Temp A1 = new Temp("$a1"); // argument2
    static final Temp A2 = new Temp("$a2"); // argument3
    static final Temp A3 = new Temp("$a3"); // argument4
    static final Temp T0 = new Temp("$t0"); // caller-saved
    static final Temp T1 = new Temp("$t1");
    static final Temp T2 = new Temp("$t2");
    static final Temp T3 = new Temp("$t3");
    static final Temp T4 = new Temp("$t4");
    static final Temp T5 = new Temp("$t5");
    static final Temp T6 = new Temp("$t6");
    static final Temp T7 = new Temp("$t7");
    static final Temp S0 = new Temp("$s0"); // callee-saved
    static final Temp S1 = new Temp("$s1");
    static final Temp S2 = new Temp("$s2");
    static final Temp S3 = new Temp("$s3");
    static final Temp S4 = new Temp("$s4");
    static final Temp S5 = new Temp("$s5");
    static final Temp S6 = new Temp("$s6");
    static final Temp S7 = new Temp("$s7");
    static final Temp T8 = new Temp("$t8"); // caller-saved
    static final Temp T9 = new Temp("$t9");
    static final Temp K0 = new Temp("$k0"); // reserved for OS kernel
    static final Temp K1 = new Temp("$k1"); // reserved for OS kernel
    static final Temp GP = new Temp("$gp"); // pointer to global area
    static final Temp SP = new Temp("$sp"); // stack pointer
    static final Temp FP = new Temp("$fp"); // callee-save (frame pointer)
    static final Temp RA = new Temp("$ra"); // return address

    // Register lists: must not overlap and must include every register that
    // might show up in code
    static final Temp[]
    // registers dedicated to special purposes
            specialRegs = { ZERO, AT, K0, K1, GP, SP },
            // registers to pass outgoing arguments
            argRegs = { A0, A1, A2, A3 },
            // registers that a callee must preserve for its caller
            calleeSaves = { S0, S1, S2, S3, S4, S5, S6, S7, FP, RA },
            // registers that a callee may use without preserving
            callerSaves = { T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, V0, V1 };

    private static Temp[] registers =
        new Temp[callerSaves.length + calleeSaves.length + argRegs.length
            + specialRegs.length];
    static {
        int i = 0;
        for (Temp t : callerSaves)
            registers[i++] = t;
        for (Temp t : calleeSaves)
            registers[i++] = t;
        for (Temp t : argRegs)
            registers[i++] = t;
        for (Temp t : specialRegs)
            registers[i++] = t;
    }

    public Temp[] registers() {
        return registers;
    }

    @Override
    public Exp FP() {
        return new TEMP(FP);
    }

    private Temp RV = null;
    public Exp RV() {
        if (RV == null)
            RV = V0;
        return new TEMP(RV);
    }

    public Translate.Tree.Exp external(String s) {
        return new Translate.Tree.Exp.NAME(Temp.getLabel("_" + s));
    }

    public String string(Label lab, String s) {
        String result = "\t.data\n\t.align 2\n" + lab + ":";
        for (byte b : s.getBytes()) result += "\n\t.byte " + b;
        result += "\n\t.byte 0";
        return result;
    }

    public String record(Label lab, int size) {
        String result = "\t.data\n\t.align 2\n" + lab + ":";
        while (--size >= 0) result += "\n\t.byte 0";
        return result;
    }

    public String dope(Label lab, Label payload, int... dims) {
        String result = "\t.data\n\t.align 2\n" + lab + ":";
        result += "\n\t.word " + payload;
        for (int d : dims) result += "\n\t.word " + d;
        return result;
    }

    public String vtable(Label lab, Collection<Label> values) {
        String result = "\t.data\n\t.align 2\n" + lab + ":";
        for (Label l : values) {
            result += "\n\t.word ";
            result += (l == null) ? "0" : l;
        }
        return result;
    }

    public String switchtable(Label lab,
                              int[] values,
                              Label[] labels) {
        String result = "\t.data\n\t.align 2\n" + lab + ":";
        for (int i = 0; i < values.length; i++) {
            result += "\n\t.word " + values[i];
            result += "\n\t.word " + labels[i];
        }
        return result.toString();
    }

    private static final Label badPtr = new Label();
    public Label badPtr() { return badPtr; }

    private static final Label badSub = new Label();
    public Label badSub() { return badSub; }

    // Registers defined by a call
    static Temp[] callDefs = new Temp[1 + argRegs.length + callerSaves.length];
    static {
        int i = 0;
        callDefs[i++] = RA;
        for (Temp t : argRegs)
            callDefs[i++] = t;
        for (Temp t : callerSaves)
            callDefs[i++] = t;
    }

    public void procEntryExit1(List<Stm> body) {
        int argStackUsed = 0;
        int argRegUsed = 0;
        for (Access a : formals) {
            if (a instanceof InFrame af) {
                assert argStackUsed == af.offset;
                for (int k = 0; k < af.size; k += wordSize) {
                    if (argRegUsed < argRegs.length) {
                        Temp argReg = argRegs[argRegUsed++];
                        body.addFirst(new MOVE(new MEM(new TEMP(FP),
                                                       new CONST(argStackUsed),
                                                       wordSize),
                                               new TEMP(argReg)));
                    }
                    argStackUsed += wordSize;
                }
            } else if (a instanceof InReg ar) {
                if (argRegUsed < Frame.argRegs.length) {
                    Temp argReg = argRegs[argRegUsed++];
                    body.addFirst(new MOVE(new TEMP(ar.temp),
                                           new TEMP(argReg)));
                } else {
                    body.addFirst(new MOVE(new TEMP(ar.temp),
                                           new MEM(new TEMP(FP),
                                                   new CONST(argStackUsed),
                                                   wordSize)));
                }
                argStackUsed += wordSize;
            } else assert false;
        }
        if (link != null)
            body.addFirst(new MOVE(link.exp(FP()), new TEMP(V0)));
    }

    private static Temp[] T(Temp... a) {
        return a;
    }

    private static Assem.Instr.OPER OPER(String a, Temp[] d, Temp[] s) {
        return new Assem.Instr.OPER(a, d, s);
    }

    private static Assem.Instr.OPER OPER(String a) {
        return OPER(a, T(), T());
    }

    public Codegen codegen() {
        return new Codegen(this);
    }

    // Registers live on return
    private Temp[] returnSink = specialRegs;
    public void procEntryExit2(List<Assem.Instr> insns) {
        if (RV != null) {
            returnSink = new Temp[specialRegs.length + 1];
            int i = 0;
            for (Temp t : specialRegs)
                returnSink[i++] = t;
            returnSink[i] = RV;
        }
        insns.addLast(OPER("#\treturnSink", T(), returnSink));
    }

    public int maxArgStackUsed = 0;
    public void procEntryExit3(List<Assem.Instr> insns, Temp.Map map) {
        int framesize = 0;
        framesize += maxArgStackUsed;
        framesize += localSize;

        HashSet<Temp> defs = new HashSet<Temp>();
        for (Assem.Instr insn : insns) {
            for (Temp t : insn.def)
                defs.add(map.get(t));
        }
        for (Temp t : calleeSaves)
            if (defs.contains(t))
                framesize += wordSize;
        if (framesize != 0) {
            insns.addFirst(OPER("\tsubu $sp " + name + ".framesize", T(SP), T(SP)));
            insns.addLast(OPER("\taddu $sp " + name + ".framesize", T(SP), T(SP)));
        }

        int o = -localSize;
        for (Temp t : calleeSaves)
            if (defs.contains(t)) {
                o -= wordSize;
                insns.addFirst(OPER("\tsw `s0 " + o + "($sp)", T(), T(t, SP)));
                insns.addLast(OPER("\tlw `d0 " + o + "($sp)", T(t), T(SP)));
            }

        insns.addLast(OPER("\tjr $ra", T(), returnSink));
        insns.addFirst(OPER("\t.text\n" + name + ":\n" + name
            + ".framesize=" + framesize));
        if (isGlobal)
            insns.addFirst(OPER("\t.globl " + name));
    }
}
