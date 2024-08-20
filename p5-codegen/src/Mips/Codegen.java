/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package Mips;

import java.math.BigInteger;
import java.util.*;
import Assem.Instr;
import Translate.Temp;
import Translate.Tree;
import Translate.Temp.*;
import Translate.Tree.*;
import Translate.Tree.Exp.*;
import Translate.Tree.Stm.*;

public class Codegen implements Frame.CodeGen {
    private final Frame frame;
    public Codegen(Frame f) {
        frame = f;
    }

    private List<Instr> insns;
    public List<Instr> apply(List<Stm> stms) {
        insns = new LinkedList<Instr>();
        for (Stm stm : stms) visit(stm);
        return insns;
    }

    static Instr MOVE(Temp d, Temp s) {
        return new Instr.MOVE("\tmove `d0 `s0", d, s);
    }

    private static Temp[] T(Temp... a) {
        return a;
    }

    static Instr OPER(String a, Temp[] d, Temp[] s, Label... j) {
        return new Instr.OPER("\t" + a, d, s, j);
    }

    private void visit(Stm stm) {
        switch (stm) {
        // MOVE(MEM(NAME, CONST16), Exp)
        case MOVE(MEM(NAME(Label l), CONST(long o), int size, boolean signed, boolean struct), Exp r)
            when CONST16(o) -> {
            insns.add(OPER(STORE(size) + " `s0 " + l + "+" + o, T(), T(visit(r))));
        }
        // MOVE(MEM(FP, CONST16), Exp)
        case MOVE(MEM(TEMP(Temp t), CONST(long o), int size, boolean signed, boolean struct), Exp r)
            when t == Frame.FP && CONST16(o) -> {
            insns.add(OPER(STORE(size) + " `s1 " + o + "+" + frame.name + ".framesize(`s0)", T(), T(Frame.SP, visit(r))));
        }
        // MOVE(MEM(Exp, CONST16), Exp)
        case MOVE(MEM(Exp l, CONST(long o), int size, boolean signed, boolean struct), Exp r)
            when CONST16(o) -> {
            insns.add(OPER(STORE(size) + " `s1 " + o + "(`s0)", T(), T(visit(l), visit(r))));
        }
        // MOVE(MEM(Exp, CONST 0), Exp)
        case MOVE(MEM(Exp l, CONST(long o), int size, boolean signed, boolean struct), Exp r)
            when o == 0 -> {
            insns.add(OPER(STORE(size) + " `s1 (`s0)", T(), T(visit(l), visit(r))));
        }
        // MOVE(MEM(Exp, CONST), Exp)
        case MOVE(MEM(Exp l, CONST o, int size, boolean signed, boolean struct), Exp r) -> {
            Temp t = new Temp();
            insns.add(OPER("add `d0 `s0 `s1", T(t), T(visit(l), visit(o))));
            insns.add(OPER(STORE(size) + " `s1 (`s0)", T(), T(t, visit(r))));
        }
        // MOVE(Exp, Exp)
        case MOVE(Exp l, Exp r) -> {
            insns.add(MOVE(visit(l), visit(r)));
        }

        // EXP(Exp)
        case EXP(Exp e) -> visit(e);

        // JUMP(NAME)
        case JUMP(NAME e, Label[] targets) -> {
            insns.add(OPER("b `j0", T(), T(), targets));
        }
        // JUMP(Exp)
        case JUMP(Exp e, Label[] targets) -> {
            insns.add(OPER("jr `s0", T(), T(visit(e)), targets));
        }

        // CJUMP(Exp, CONST16)
        case CJUMP.BEQ(Exp l, CONST(long r), Label t, Label f)
            when CONST16(r) -> CJUMP("beq", l, r, t, f);
        case CJUMP.BNE(Exp l, CONST(long r), Label t, Label f)
            when CONST16(r) -> CJUMP("bne", l, r, t, f);
        case CJUMP.BLT(Exp l, CONST(long r), Label t, Label f)
            when CONST16(r) -> CJUMP("blt", l, r, t, f);
        case CJUMP.BGT(Exp l, CONST(long r), Label t, Label f)
            when CONST16(r) -> CJUMP("bgt", l, r, t, f);
        case CJUMP.BLE(Exp l, CONST(long r), Label t, Label f)
            when CONST16(r) -> CJUMP("ble", l, r, t, f);
        case CJUMP.BGE(Exp l, CONST(long r), Label t, Label f)
            when CONST16(r) -> CJUMP("bge", l, r, t, f);

        // CJUMP(CONST16, Exp)
        case CJUMP.BEQ(CONST(long l), Exp r, Label t, Label f)
            when CONST16(l) -> CJUMP("beq", r, l, t, f);
        case CJUMP.BNE(CONST(long l), Exp r, Label t, Label f)
            when CONST16(l) -> CJUMP("bne", r, l, t, f);
        case CJUMP.BLT(CONST(long l), Exp r, Label t, Label f)
            when CONST16(l) -> CJUMP("bgt", r, l, t, f);
        case CJUMP.BGT(CONST(long l), Exp r, Label t, Label f)
            when CONST16(l) -> CJUMP("blt", r, l, t, f);
        case CJUMP.BLE(CONST(long l), Exp r, Label t, Label f)
            when CONST16(l) -> CJUMP("bge", r, l, t, f);
        case CJUMP.BGE(CONST(long l), Exp r, Label t, Label f)
            when CONST16(l) -> CJUMP("ble", r, l, t, f);

        // CJUMP(Exp, Exp)
        case CJUMP.BEQ(Exp l, Exp r, Label t, Label f) -> CJUMP("beq", l, r, t, f);
        case CJUMP.BNE(Exp l, Exp r, Label t, Label f) -> CJUMP("bne", l, r, t, f);
        case CJUMP.BLT(Exp l, Exp r, Label t, Label f) -> CJUMP("blt", l, r, t, f);
        case CJUMP.BGT(Exp l, Exp r, Label t, Label f) -> CJUMP("bgt", l, r, t, f);
        case CJUMP.BLE(Exp l, Exp r, Label t, Label f) -> CJUMP("ble", l, r, t, f);
        case CJUMP.BGE(Exp l, Exp r, Label t, Label f) -> CJUMP("bge", l, r, t, f);

        // LABEL
        case LABEL(Label l) -> {
            insns.add(new Instr.LABEL(l.toString() + ":", l));
        }

        case SEQ s -> throw new Error("canonical trees should have no SEQ");
        }
    }

    private void CJUMP(String op, Exp l, long r, Label t, Label f) {
        insns.add(OPER(op + " `s0 " + r + " `j0", T(), T(visit(l)), t, f));
    }
    private void CJUMP(String op, Exp l, Exp r, Label t, Label f) {
        insns.add(OPER(op + " `s0 `s1 `j0", T(), T(visit(l), visit(r)), t, f));
    }

    private Temp visit(Exp exp) {
        return switch (exp) {

        // CONST(0)
        case CONST(long c) when c == 0 -> Frame.ZERO;
        // CONST
        case CONST(long c) -> {
            Temp d0 = new Temp();
            insns.add(OPER("li `d0 " + c, T(d0), T()));
            yield d0;
        }

        // NAME
        case NAME(Label label) -> {
            Temp d0 = new Temp();
            insns.add(OPER("la `d0 " + label, T(d0), T()));
            yield d0;
        }

        // FP
        case TEMP(Temp t) when t == Frame.FP -> {
            Temp d0 = new Temp();
            insns.add(OPER("addu `d0 `s0 " + frame.name + ".framesize", T(d0), T(Frame.SP)));
            yield d0;
        }
        // TEMP
        case TEMP(Temp t) -> t;

        // ADD(FP, CONST16)
        case BINOP.ADD(TEMP(Temp t), CONST(long o)) when t == Frame.FP && CONST16(o) -> {
            Temp d0 = new Temp();
            insns.add(OPER("addu `d0 `s0 " + o + "+" + frame.name + ".framesize", T(d0), T(Frame.SP)));
            yield d0;
        }
        // ADD(CONST16, FP)
        case BINOP.ADD(CONST(long o), TEMP(Temp t)) when t == Frame.FP && CONST16(o) -> {
            Temp d0 = new Temp();
            insns.add(OPER("addu `d0 `s0 " + o + "+" + frame.name + ".framesize", T(d0), T(Frame.SP)));
            yield d0;
        }

        // BINOP(Exp, CONST2^k)
        case BINOP.MUL (Exp l, CONST(long r)) when shift(r) != 0 -> BINOP("sll", l, shift(r));
        case BINOP.DIV (Exp l, CONST(long r)) when shift(r) != 0 -> BINOP("sra", l, shift(r));
        case BINOP.DIVU(Exp l, CONST(long r)) when shift(r) != 0 -> BINOP("srl", l, shift(r));

        // BINOP(CONST2^k, Exp)
        case BINOP.MUL (CONST(long l), Exp r) when shift(l) != 0 -> BINOP("sll", r, shift(l));

        // BINOP(Exp, CONST16)
        case BINOP.ADD (Exp l, CONST(long r)) when CONST16(r) -> BINOP("addu", l, r);
        case BINOP.AND (Exp l, CONST(long r)) when CONST16(r) -> BINOP("and",  l, r);
        case BINOP.DIV (Exp l, CONST(long r)) when CONST16(r) -> BINOP("div",  l, r);
        case BINOP.DIVU(Exp l, CONST(long r)) when CONST16(r) -> BINOP("divu", l, r);
        case BINOP.MOD (Exp l, CONST(long r)) when CONST16(r) -> BINOP("rem",  l, r);
        case BINOP.MODU(Exp l, CONST(long r)) when CONST16(r) -> BINOP("remu", l, r);
        case BINOP.MUL (Exp l, CONST(long r)) when CONST16(r) -> BINOP("mul",  l, r);
        case BINOP.NOR (Exp l, CONST(long r)) when CONST16(r) -> BINOP("nor",  l, r);
        case BINOP.OR  (Exp l, CONST(long r)) when CONST16(r) -> BINOP("or",   l, r);
        case BINOP.SLL (Exp l, CONST(long r)) when CONST16(r) -> BINOP("sll",  l, r);
        case BINOP.SRA (Exp l, CONST(long r)) when CONST16(r) -> BINOP("sra",  l, r);
        case BINOP.SRL (Exp l, CONST(long r)) when CONST16(r) -> BINOP("srl",  l, r);
        case BINOP.SUB (Exp l, CONST(long r)) when CONST16(r) -> BINOP("subu", l, r);
        case BINOP.XOR (Exp l, CONST(long r)) when CONST16(r) -> BINOP("xor",  l, r);

        // BINOP(CONST16, Exp)
        case BINOP.ADD (CONST(long l), Exp r) when CONST16(l) -> BINOP("addu", r, l);
        case BINOP.AND (CONST(long l), Exp r) when CONST16(l) -> BINOP("and",  r, l);
        case BINOP.MUL (CONST(long l), Exp r) when CONST16(l) -> BINOP("mul",  r, l);
        case BINOP.NOR (CONST(long l), Exp r) when CONST16(l) -> BINOP("nor",  r, l);
        case BINOP.OR  (CONST(long l), Exp r) when CONST16(l) -> BINOP("or",   r, l);
        case BINOP.XOR (CONST(long l), Exp r) when CONST16(l) -> BINOP("xor",  r, l);

        // BINOP(Exp, Exp)
        case BINOP.ADD (Exp l, Exp r) -> BINOP("addu", l, r);
        case BINOP.AND (Exp l, Exp r) -> BINOP("and",  l, r);
        case BINOP.DIV (Exp l, Exp r) -> BINOP("div",  l, r);
        case BINOP.DIVU(Exp l, Exp r) -> BINOP("divu", l, r);
        case BINOP.MOD (Exp l, Exp r) -> BINOP("rem",  l, r);
        case BINOP.MODU(Exp l, Exp r) -> BINOP("remu", l, r);
        case BINOP.MUL (Exp l, Exp r) -> BINOP("mul",  l, r);
        case BINOP.NOR (Exp l, Exp r) -> BINOP("nor",  l, r);
        case BINOP.OR  (Exp l, Exp r) -> BINOP("or",   l, r);
        case BINOP.SLL (Exp l, Exp r) -> BINOP("sll",  l, r);
        case BINOP.SRA (Exp l, Exp r) -> BINOP("sra",  l, r);
        case BINOP.SRL (Exp l, Exp r) -> BINOP("srl",  l, r);
        case BINOP.SUB (Exp l, Exp r) -> BINOP("subu", l, r);
        case BINOP.XOR (Exp l, Exp r) -> BINOP("xor",  l, r);

        // MEM(NAME, CONST16)
        case MEM(NAME(Label l), CONST(long o), int size, boolean signed, boolean struct)
            when CONST16(o) -> {
            Temp d0 = new Temp();
            insns.add(OPER(LOAD(size, signed) + " `d0 " + l + "+" + o, T(d0), T()));
            yield d0;
        }
        // MEM(FP, CONST16)
        case MEM(TEMP(Temp t), CONST(long o), int size, boolean signed, boolean struct)
            when t == Frame.FP && CONST16(o) -> {
            Temp d0 = new Temp();
            insns.add(OPER(LOAD(size, signed) + " `d0 " + o + "+" + frame.name + ".framesize(`s0)",
                           T(d0), T(Frame.SP)));
            yield d0;
        }
        // MEM(Exp, CONST16)
        case MEM(Exp e, CONST(long o), int size, boolean signed, boolean struct)
            when CONST16(o) -> {
            Temp d0 = new Temp();
            insns.add(OPER(LOAD(size, signed) + " `d0 " + o + "(`s0)", T(d0), T(visit(e))));
            yield d0;
        }
        // MEM(Exp, CONST 0)
        case MEM(Exp e, CONST(long o), int size, boolean signed, boolean struct)
            when o == 0 -> {
            Temp d0 = new Temp();
            insns.add(OPER(LOAD(size, signed) + " `d0 (`s0)", T(d0), T(visit(e))));
            yield d0;
        }
        // MEM(Exp, CONST)
        case MEM(Exp e, CONST o, int size, boolean signed, boolean struct) -> {
            Temp d0 = new Temp();
            insns.add(OPER("add `d0 `s0 `s1", T(d0), T(visit(e), visit(o))));
            insns.add(OPER(LOAD(size, signed) + " `d0 (`s0)", T(d0), T(d0)));
            yield d0;
        }

        case CALL(Exp func, Exp link, Exp[] args) -> {
            String op;
            LinkedList<Temp> uses = new LinkedList<Temp>();
            if (func instanceof NAME(Label label)) {
                // CALL(NAME, ...)
                op = "jal " + label;
            } else {
                // CALL(Exp, ...)
                uses.add(visit(func));
                op = "jalr `s0";
            }
            if (link instanceof CONST(long value)) {
                assert value == 0;
            } else {
                insns.add(MOVE(Frame.V0, visit(link)));
                uses.add(Frame.V0);
            }
            int argStackUsed = 0;
            int argRegUsed = 0;
            for (Tree.Exp arg: args) {
                if (arg instanceof MEM(Exp e, CONST(long offset),
                                       int size, boolean signed, boolean struct)
                    && struct) {
                    Temp s0 = visit(e);
                    for (int k = 0; k < size; k += Frame.wordSize) {
                        if (argRegUsed < Frame.argRegs.length) {
                            Temp argReg = Frame.argRegs[argRegUsed++];
                            visit(new MOVE(new TEMP(argReg),
                                           new MEM(new TEMP(s0),
                                                   new CONST(offset + k),
                                                   Frame.wordSize)));
                            uses.add(argReg);
                        } else {
                            visit(new MOVE(new MEM(new TEMP(Frame.SP),
                                                   new CONST(argStackUsed),
                                                   Frame.wordSize),
                                           new MEM(new TEMP(s0),
                                                   new CONST(offset + k),
                                                   Frame.wordSize)));
                        }
                        argStackUsed += Frame.wordSize;
                    }
                } else {
                    if (argRegUsed < Frame.argRegs.length) {
                        Temp argReg = Frame.argRegs[argRegUsed++];
                        visit(new MOVE(new TEMP(argReg), arg));
                        uses.add(argReg);
                    } else {
                        visit(new MOVE(new MEM(new TEMP(Frame.SP),
                                               new CONST(argStackUsed),
                                               Frame.wordSize),
                                       arg));
                    }
                    argStackUsed += Frame.wordSize;
                }
            }
            if (argStackUsed > frame.maxArgStackUsed)
                frame.maxArgStackUsed = argStackUsed;
            insns.add(OPER(op, Frame.callDefs, uses.toArray(T())));
            yield Frame.V0;
        }
        case ESEQ e -> throw new Error("canonical trees should have no ESEQ");
        };
    }

    private Temp BINOP(String op, Exp l, long r) {
        Temp d0 = new Temp();
        insns.add(OPER(op + " `d0 `s0 " + r, T(d0), T(visit(l))));
        return d0;
    }
    private Temp BINOP(String op, Exp l, Exp r) {
        Temp d0 = new Temp();
        insns.add(OPER(op + " `d0 `s0 `s1", T(d0), T(visit(l), visit(r))));
        return d0;
    }

    static int shift(long i) {
        int shift = 0;
        if ((i > 1) && ((i & (i - 1)) == 0)) {
            while (i > 1) {
                shift += 1;
                i >>= 1;
            }
        }
        return shift;
    }

    static boolean CONST16(long i) {
        return BigInteger.valueOf(i).bitLength() < 16;
    }

    String LOAD(int size, boolean signed) {
        if (signed) {
            switch (size) {
            case 1: return "lb";
            case 2: return "lh";
            case 4: return "lw";
            }
        } else {
            switch (size) {
            case 1: return "lbu";
            case 2: return "lhu";
            case 4: return "lw";
            }
        }
        throw new Error();
    }
    String STORE(int size) {
        switch (size) {
        case 1: return "sb";
        case 2: return "sh";
        case 4: return "sw";
        }
        throw new Error();
    }        
}
