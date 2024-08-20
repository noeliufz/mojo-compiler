/* Copyright (C) 2020-2024, Antony L Hosking and Ben L. Titzer.
 * All rights reserved.  */
package interp;

import java.util.*;

import Translate.Temp;
import Translate.Temp.*;
import Translate.Tree.*;
import Translate.Tree.Exp.*;

/**
 * A frame for the Tree interpreter (tint).
 */
public class Frame extends Translate.Frame {
    public Translate.Frame newFrame(String name, boolean hasParent, boolean hasChildren) {
        Frame f = new Frame(globalPrefix, name, layout);
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

    public static final int wordSize = 4;
    static final Temp RA = new Temp("%ra"); // return value register
    static final Temp FL = new Temp("%fl"); // frame link register
    static final Temp SP = new Temp("%sp"); // stack pointer
    static final Temp FP = new Temp("%fp"); // callee-save (frame pointer)

    // Register lists: must not overlap and must include every register that
    // might show up in code
    static final Temp[]
    // registers dedicated to special purposes
        specialRegs = { SP, FP, FL },
    // registers to pass outgoing arguments
	argRegs = { RA },
    // registers that a callee must preserve for its caller
	calleeSaves = { },
    // registers that a callee may use without preserving
	callerSaves = { };

    static Temp[] callDefs = new Temp[argRegs.length + callerSaves.length];
    static {
        int i = 0;
        for (Temp t : argRegs)
            callDefs[i++] = t;
        for (Temp t : callerSaves)
            callDefs[i++] = t;
    }

    private final String globalPrefix;
    private final Interpreter.DataLayout layout;
    private final ArrayList<Access> actuals = new ArrayList<Access>();
    private Access link;
    private Label badPtr;
    private Label badSub;

    public Frame() {
        super(null, wordSize);
        globalPrefix = "";
        layout = new Interpreter.DataLayout();
    }

    public Frame(String prefix) {
        super(null, wordSize);
        globalPrefix = prefix;
	layout = new Interpreter.DataLayout();
    }

    private Frame(String prefix, String n, Interpreter.DataLayout l) {
        super(Temp.getLabel(n), wordSize);
        globalPrefix = prefix;
	layout = l;
    }

    public Access link() { return link; }
    public Interpreter.DataLayout dataLayout() { return layout; }
    public ArrayList<Access> actuals() { return actuals; }

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

    public Access allocFormal(int size) {
        Access formal = allocLocal(size);
        formals.add(formal);
        return formal;
    }
    public Access allocFormal(Temp t) {
        Access formal = allocLocal(t);
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
    public int frameSize() {
	return localSize;
    }

    private static Temp[] registers = {
	SP, FP,
    };

    public Temp[] registers() {
        return registers;
    }

    @Override
    public Exp FP() {
        return new TEMP(FP);
    }

    public Exp RV() {
        return new TEMP(RA);
    }

    public Exp external(String s) {
        return new NAME(Temp.getLabel(globalPrefix + s));
    }

    public String string(Label lab, String string) {
	return layout.addString(lab, string);
    }

    public String dope(Label lab, Label payload, int... dims) {
        return layout.addDope(lab, payload, dims);
    }

    public String record(Label lab, int size) {
	return layout.addRecord(lab, size);
    }

    public String vtable(Label lab, Collection<Label> values) {
	return layout.addVtable(lab, values);
    }

    public String switchtable(Label lab,
                              int[] values,
                              Label[] labels) {
	throw new Error("unimplemented");
    }

    public Label badPtr() {
        if (badPtr == null) badPtr = Temp.getLabel("badPtr");
        return badPtr;
    }

    public Label badSub() {
        if (badSub == null) badSub = Temp.getLabel("badSub");
        return badSub;
    }

    public CodeGen codegen() { return null; };
    public void procEntryExit1(List<Stm> body) {};
    public void procEntryExit2(List<Assem.Instr> insns) {};
    public void procEntryExit3(List<Assem.Instr> insns, Temp.Map map) {};
}
