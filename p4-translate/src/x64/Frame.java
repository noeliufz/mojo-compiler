/* Copyright (C) 1997-2012, Antony L Hosking.
 * All rights reserved.  */
package x64;

import java.math.BigInteger;
import java.util.*;

import Translate.Temp;
import Translate.Temp.*;
import Translate.Tree.*;
import Translate.Tree.Exp.*;
import Translate.Tree.Stm.*;

public class Frame extends Translate.Frame {
    public Translate.Frame newFrame(String name, boolean hasParent, boolean hasChildren) {
        Frame f = new Frame(globalPrefix, name);
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

    static final int wordSize = 8;
    private final String globalPrefix;

    Access link;
    public Access link() { return link; }

    public Frame(String prefix) {
        super(null, wordSize);
        globalPrefix = prefix;
    }

    private Frame(String prefix, String n) {
        super(Temp.getLabel(n), wordSize);
        globalPrefix = prefix;
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

    private List<Access> actuals = new LinkedList<Access>();

    private int formalSize = 0;
    private int numFormals = 0;
    public Access allocFormal(int size) {
        Access formal;
        int i = numFormals++;
        if (size <= wordSize && i < argRegs.length) {
            formal = allocLocal(size);
            actuals.add(new InReg(argRegs[i]));
        } else {
            // accounting for saved %rip and %rbp
            actuals.add(formal = new InFrame(formalSize + 16, size));
            formalSize += (size + wordSize - 1) / wordSize * wordSize;
        }
        formals.add(formal);
        return formal;
    }
    public Access allocFormal(Temp t) {
        Access formal;
        int i = numFormals++;
        if (i < argRegs.length) {
            actuals.add(new InReg(argRegs[i]));
            formal = new InReg(t);
        } else {
            // accounting for saved %rip and %rbp
            actuals.add(formal = new InFrame(formalSize + 16, wordSize));
            formalSize += wordSize;
        }
        formals.add(formal);
        return formal;
    }

    private int localSize = 0;
    public Access allocLocal(int size) {
        localSize += (size + wordSize - 1) / wordSize * wordSize;
        return new InFrame(-localSize, size);
    }
    public Access allocLocal(Temp t) {
        return new InReg(t);
    }

    static final Temp RAX = new Temp("%rax"); // return1
    static final Temp RBX = new Temp("%rbx"); // callee-saved
    static final Temp RCX = new Temp("%rcx"); // argument4
    static final Temp RDX = new Temp("%rdx"); // argument3/return2
    static final Temp RSP = new Temp("%rsp"); // stack pointer
    static final Temp RBP = new Temp("%rbp"); // callee-save (frame pointer)
    static final Temp RSI = new Temp("%rsi"); // argument2
    static final Temp RDI = new Temp("%rdi"); // argument1
    static final Temp R8 = new Temp("%r8"); // argument5
    static final Temp R9 = new Temp("%r9"); // argument6
    static final Temp R10 = new Temp("%r10"); // caller-saved (static chain)
    static final Temp R11 = new Temp("%r11"); // caller-saved
    static final Temp R12 = new Temp("%r12"); // callee-saved
    static final Temp R13 = new Temp("%r13");
    static final Temp R14 = new Temp("%r14");
    static final Temp R15 = new Temp("%r15");

    // Register lists: must not overlap and must include every register that
    // might show up in code
    static final Temp[]
            // registers dedicated to special purposes
            specialRegs = { RSP, RBP },
            // registers to pass outgoing arguments
            argRegs = { RDI, RSI, RDX, RCX, R8, R9 },
            // registers that a callee must preserve for its caller
            calleeSaves = { RBX, R12, R13, R14, R15 },
            // registers that a callee may use without preserving
            callerSaves = { RAX, R10, R11 };

    private static Temp[] registers = {
        RAX, RBX, RCX, RDX, RSP, RBP, RSI, RDI,
         R8, R9,  R10, R11, R12, R13, R14, R15,
    };

    public Temp[] registers() {
        return registers;
    }

    @Override
    public Exp FP() {
        return new TEMP(RBP);
    }

    private Temp RV = null;
    public Exp RV() {
        if (RV == null)
            RV = RAX;
        return new TEMP(RAX);
    }

    public Translate.Tree.Exp external(String s) {
        return new Translate.Tree.Exp.NAME(Temp.getLabel(globalPrefix + s));
    }

    public String string(Label lab, String s) {
        String result = "\t.data\n\t.balign 8\n" + lab + ":";
        for (byte b : s.getBytes()) result += "\n\t.byte " + b;
        result += "\n\t.byte 0";
        return result.toString();
    }

    public String record(Label lab, int size) {
        return "\t.data\n\t.balign 8\n" + lab + ":\n\t.zero " + size;
    }

    public String dope(Label lab, Label payload, int... dims) {
        String result = "\t.data\n\t.balign 8\n" + lab + ":";
        result += "\n\t.quad " + payload;
        for (int d : dims) result += "\n\t.quad " + d;
        return result;
    }

    public String vtable(Label lab, Collection<Label> values) {
        String result = "\t.data\n\t.balign 8\n" + lab + ":";
        for (Label l : values) {
            result += "\n\t.quad ";
            result += (l == null) ? "0" : l;
        }
        return result;
    }

    public String switchtable(Label lab,
                              int[] values,
                              Label[] labels) {
        String result = "\t.data\n\t.balign 8\n" + lab + ":";
        for (int i = 0; i < values.length; i++) {
            result += "\n\t.quad " + values[i];
            result += "\n\t.quad " + labels[i];
        }
        return result.toString();
    }

    private Label badPtr;
    public Label badPtr() {
        if (badPtr == null) badPtr = Temp.getLabel(name + ".badPtr");
        return badPtr;
    }

    private Label badSub;
    public Label badSub() {
        if (badSub == null) badSub = Temp.getLabel(name + ".badSub");
        return badSub;
    }

    // Registers defined by a call
    static Temp[] callDefs = new Temp[argRegs.length + callerSaves.length];
    static {
        int i = 0;
        for (Temp t : argRegs)
            callDefs[i++] = t;
        for (Temp t : callerSaves)
            callDefs[i++] = t;
    }

    private void saveArgs(Iterator<Access> formals, Iterator<Access> actuals, List<Stm> body) {
        if (!formals.hasNext() || !actuals.hasNext())
            return;
        Access formal = formals.next();
        Access actual = actuals.next();
        saveArgs(formals, actuals, body);
        if (formal != actual)
            body.addFirst(new MOVE(formal.exp(FP()), actual.exp(FP())));
    }
}
