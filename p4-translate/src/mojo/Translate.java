/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */

package mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

import Translate.*;
import Translate.Tree.Stm;
import Translate.Tree.Stm.*;
import Translate.Tree.Exp;
import Translate.Tree.Exp.*;
import Translate.Temp.Label;

import interp.Interpreter;
import mojo.parse.*;
import static mojo.Absyn.*;

public class Translate extends Semant {
    private static void usage() {
        throw new java.lang.Error("Usage: java mojo.Translate "
            + "[ -target= Mips|PPCDarwin|PPCLinux|x64|interp ] <source>.mj");
    }

    final List<Frag> frags = new LinkedList<Frag>();
    final Frame target, badPtr, badSub;
    Translate(Frame target) {
        super(target.wordSize);
        this.target = target;
        this.badPtr = target.newFrame("badPtr");
        this.badSub = target.newFrame("badSub");
    }

    public static void main(String[] args) {
        if (args.length < 1) usage();
        boolean main = false, trace = false;
        Frame target = new x64.Frame("");
        if (args.length > 1)
            for (String arg : args) {
                if (arg.equals("-main"))
                    main = true;
                else if (arg.equals("-trace"))
                    trace = true;
                else if (arg.equals("-target=interp"))
                    target = new interp.Frame();
                else if (arg.startsWith("-"))
                    usage();
            }
        File file = new File(args[args.length - 1]);
        Type.init();
        try {
            Value.Unit unit = new Parser(target.wordSize, file).Unit();
            if (Error.nErrors() > 0) return;
            Translate semant = new Translate(target);
            semant.Check(unit);
            if (Error.nErrors() > 0) return;
            semant.Compile(unit, main);
            if (Error.nErrors() > 0) return;
            if (main && target instanceof interp.Frame t) {
                Interpreter i = new Interpreter(t.dataLayout(), semant.frags, trace);
                Frag.Proc proc = null;
                for (Frag f: semant.frags)
                    if (f instanceof Frag.Proc p
                        && p.frame.name.toString().equals("main"))
                        proc = p;
                assert proc != null;
                System.exit(i.run(proc));
            } else {
                PrintWriter out = new PrintWriter(System.out);
                for (Frag f : semant.frags) {
                    out.println(f);
                    out.flush();
                }
            }
        } catch (FileNotFoundException |
                 ParseException |
                 TokenMgrError e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } finally {
            System.err.flush();
        }
    }

    ProcBody currentBody;
    Frame currentFrame;
    Label returnLabel;
    Value.Variable returnResult;
    final Map<ProcBody,Frame> frames = new HashMap<ProcBody,Frame>();
    void Compile(final Value.Unit m, boolean main) {
        Frame f = target.newFrame(m.name);
        Frame old = frames.put(m.body, f);
        assert old == null;
        Scope zz = Scope.Push(m.localScope);

        for (Type t : m.types) Compile(t);

        EnterScope(m.localScope);

        // generate any internal procedures
        ProcBody.EmitAll(EmitDecl, EmitBody);

        Scope.Pop(zz);

        if (!main) return;

        // generate code for main
        {
            Frame frame = target.newFrame("main");
            frame.isGlobal = true;
            Tree.Stm stm = EXP(CALL(NAME(f.name)));
            stm = SEQ(stm, MOVE(currentFrame.RV(), CONST(0)));
            frags.add(new Frag.Proc(stm, frame));
        }
        {
            Label msg = stringLabel("Attempt to use a null pointer");
            Tree.Stm stm = SEQ(EXP(CALL(badPtr.external("puts"), NAME(msg))),
                               EXP(CALL(badPtr.external("exit"), CONST(1))));
            frags.add(new Frag.Proc(stm, badPtr));
        }
        {
            Label msg = stringLabel("Subscript out of bounds");
            Tree.Stm stm = SEQ(EXP(CALL(badSub.external("puts"), NAME(msg))),
                               EXP(CALL(badSub.external("exit"), CONST(1))));
            frags.add(new Frag.Proc(stm, badSub));
        }
    }

    Consumer<Value> EmitDecl = (Value value) -> {
        switch (value) {
        case Value.Procedure v -> {
            currentBody = v.body;
            currentFrame = frames.get(v.body);
            Declare(v);
        }
        case Value.Unit v -> {
            currentBody = v.body;
            currentFrame = frames.get(v.body);
        }
        default -> { assert false; }
        }
    };

    Consumer<Value> EmitBody = (Value value) -> {
        switch (value) {
        case Value.Procedure v -> {
            returnLabel = new Label();
            returnResult = v.result;
            currentBody = v.body;
            currentFrame = frames.get(v.body);
            Tree.Stm stm = null;
            Scope zz = Scope.Push(v.localScope);
            EnterScope(v.localScope);
            stm = SEQ(stm, InitValues(v.localScope));
            stm = SEQ(stm, Compile(v.block, null));
            stm = SEQ(stm, LABEL(returnLabel));
            Scope.Pop(zz);
            frags.add(new Frag.Proc(stm, currentFrame));
        }
        case Value.Unit v -> {
            returnLabel = null;
            returnResult = null;
            currentBody = v.body;
            currentFrame = frames.get(v.body);
            // generate my initialisation procedure
            Tree.Stm stm = null;
            stm = SEQ(stm, InitValues(v.localScope));
            // perform the main body
            stm = SEQ(stm, Compile(v.stmt, null));
            frags.add(new Frag.Proc(stm, currentFrame));
        }
        default -> { assert false; }
        }
    };


    Tree.Stm InitValues(Scope scope) {
        if (scope == null) return null;
        Tree.Stm stm = null;
        for (Value v : Scope.ToList(scope)) stm = SEQ(stm, LangInit(v));
        for (Value v : Scope.ToList(scope)) stm = SEQ(stm, UserInit(v));
        return stm;
    }

    /* Tree-building helper methods */
    Tree.Exp.MEM MEM(Tree.Exp exp, Tree.Exp.CONST offset, int size, boolean signed) {
        return new Tree.Exp.MEM(exp, offset, size, signed);
    }
    Tree.Exp.MEM MEM(Tree.Exp exp, Tree.Exp.CONST offset, int size) {
        return MEM(exp, offset, size, false);
    }
    Tree.Exp.MEM MEM(Tree.Exp exp, Tree.Exp.CONST offset) {
        return MEM(exp, offset, wordSize, false);
    }
    Tree.Exp.MEM MEM(Tree.Exp exp) {
        return MEM(exp, CONST(0));
    }
    Tree.Exp TEMP(Temp temp) {
        return new Tree.Exp.TEMP(temp);
    }
    Tree.Exp ESEQ(Tree.Stm stm, Tree.Exp exp) {
        return (stm == null) ? exp : new Tree.Exp.ESEQ(stm, exp);
    }
    Tree.Exp NAME(Label label) {
        return new Tree.Exp.NAME(label);
    }
    Tree.Exp.CONST CONST(long value) {
        return new Tree.Exp.CONST(value);
    }
    Tree.Exp.CONST CONST(BigInteger value) {
        assert value.bitLength() < wordSize * 8;
        return CONST(value.longValue());
    }
    Tree.Exp CALL(Tree.Exp f, Tree.Exp l, Collection<Tree.Exp> args) {
        if (args.size() > currentFrame.maxArgsOut)
            currentFrame.maxArgsOut = args.size();
        assert f != null;
        return new Tree.Exp.CALL(f, l, args.toArray(new Tree.Exp[args.size()]));
    }
    Tree.Exp CALL(Tree.Exp f, Tree.Exp...a) {
        if (a.length > currentFrame.maxArgsOut)
            currentFrame.maxArgsOut = a.length;
        assert f != null;
        return new Tree.Exp.CALL(f, CONST(0), a);
    }

    Tree.Exp.CONST ADD(Tree.Exp.CONST l, Tree.Exp.CONST r) {
        return CONST(l.value() + r.value());
    }
    Tree.Exp ADD(Tree.Exp l, Tree.Exp r) {
        if (l instanceof Tree.Exp.CONST(long c) && c == 0) return r;
        if (r instanceof Tree.Exp.CONST(long c) && c == 0) return l;
        return new Tree.Exp.BINOP.ADD(l, r);
    }

    Tree.Exp AND(Tree.Exp l, Tree.Exp r) {
        return new Tree.Exp.BINOP.AND(l, r);
    }

    Tree.Exp DIV(Tree.Exp l, Tree.Exp r) {
        return new Tree.Exp.BINOP.DIV(l, r);
    }

    Tree.Exp MOD(Tree.Exp l, Tree.Exp r) {
        return new Tree.Exp.BINOP.MOD(l, r);
    }

    Tree.Exp.CONST MUL(Tree.Exp.CONST l, Tree.Exp.CONST r) {
        return CONST(l.value() * r.value());
    }
    Tree.Exp MUL(Tree.Exp l, Tree.Exp r) {
        if (l instanceof Tree.Exp.CONST(long c) && c == 1) return r;
        if (r instanceof Tree.Exp.CONST(long c) && c == 1) return l;
        return new Tree.Exp.BINOP.MUL(l, r);
    }

    Tree.Exp OR(Tree.Exp l, Tree.Exp r) {
        return new Tree.Exp.BINOP.OR(l, r);
    }

    Tree.Exp SLL(Tree.Exp l, Tree.Exp r) {
        return new Tree.Exp.BINOP.SLL(l, r);
    }

    Tree.Exp SRA(Tree.Exp l, Tree.Exp r) {
        return new Tree.Exp.BINOP.SRA(l, r);
    }

    Tree.Exp SRL(Tree.Exp l, Tree.Exp r) {
        return new Tree.Exp.BINOP.SRL(l, r);
    }

    Tree.Exp SUB(Tree.Exp l, Tree.Exp r) {
        return new Tree.Exp.BINOP.SUB(l, r);
    }

    Tree.Exp XOR(Tree.Exp l, Tree.Exp r) {
        return new Tree.Exp.BINOP.XOR(l, r);
    }

    Tree.Exp NOR(Tree.Exp l, Tree.Exp r) {
        return new Tree.Exp.BINOP.NOR(l, r);
    }

    Tree.Stm SEQ(Tree.Stm l, Tree.Stm r) {
        if (l == null)
            return r;
        if (r == null)
            return l;
        return new Tree.Stm.SEQ(l, r);
    }

    Tree.Stm SEQ(Tree.Stm... a) {
        Tree.Stm stm = null;
        for (Tree.Stm s : a)
            stm = SEQ(stm, s);
        return stm;
    }

    Tree.Stm LABEL(Label label) {
        return new Tree.Stm.LABEL(label);
    }

    Tree.Stm JUMP(Label target) {
        return target == null ? null : new Tree.Stm.JUMP(target);
    }

    Tree.Stm JUMP(Tree.Exp exp, Label[] targets) {
        return new Tree.Stm.JUMP(exp, targets);
    }

    Tree.Stm MOVE(Tree.Exp d, Tree.Exp s) {
        return new Tree.Stm.MOVE(d, s);
    }

    Tree.Stm EXP(Tree.Exp exp) {
        return new Tree.Stm.EXP(exp);
    }

    Tree.Stm BEQ(Tree.Exp l, Tree.Exp r, Label t, Label f) {
        return new Tree.Stm.CJUMP.BEQ(l, r, t, f);
    }

    Tree.Stm BGE(Tree.Exp l, Tree.Exp r, Label t, Label f) {
        return new Tree.Stm.CJUMP.BGE(l, r, t, f);
    }

    Tree.Stm BGT(Tree.Exp l, Tree.Exp r, Label t, Label f) {
        return new Tree.Stm.CJUMP.BGT(l, r, t, f);
    }

    Tree.Stm BLE(Tree.Exp l, Tree.Exp r, Label t, Label f) {
        return new Tree.Stm.CJUMP.BLE(l, r, t, f);
    }

    Tree.Stm BLT(Tree.Exp l, Tree.Exp r, Label t, Label f) {
        return new Tree.Stm.CJUMP.BLT(l, r, t, f);
    }

    Tree.Stm BNE(Tree.Exp l, Tree.Exp r, Label t, Label f) {
        return new Tree.Stm.CJUMP.BNE(l, r, t, f);
    }

    /**
     * Generate declarations for all the values in a scope.
     * Generate procedure declarations after non-procedure declarations.
     */
    void EnterScope(Scope scope) {
        for (Value v : Scope.ToList(scope))
            if (v instanceof Value.Procedure) /* skip */; else Declare(v);
        for (Value v : Scope.ToList(scope))
            if (v instanceof Value.Procedure) Declare(v); else /* skip */;
    }

    /**
     * Every variable declaration has an associated access.
     * This map keeps track of them.
     */
    final Map<Value.Variable,Frame.Access> accesses
        = new HashMap<Value.Variable,Frame.Access>();

    /**
     * Generate declaration for v.
     */
    void Declare(Value value) {
        if (value == null) return;
        if (value.declared()) return;
        value.declared(true);
        switch (value) {
        case Value.Const v -> { if (v.exported()) Compile(TypeOf(v)); }
        case Value.Tipe v -> { Compile(v.value); }
        case Value.Procedure v -> {
            Compile(v.sig);
            // try to compile the imported type first

            if (v.body == null) {
                // it's not a local procedure
                ImportProc(v);
                return;
            }
            Frame frame = target.newFrame(GlobalName(v),
                                          v.body.parent != null,
                                          v.body.children != null);
            Frame f = frames.put(v.body, frame);
            assert f == null;
            currentBody = v.body;
            currentFrame = frame;
            Scope zz = Scope.Push(v.localScope);
            EnterScope(v.localScope);
            Scope.Pop(zz);
        }
        case Value.Variable v -> {
            int size = v.size;
            Compile(TypeOf(v));
            if (v.indirect()) size = wordSize;
            // declare the actual variable
            if (v.external()) {
                // external
            } else if (v.imported()) {
                // imported
            } else if (v.global()) {
                // global
                Label l = Temp.getLabel(GlobalName(v));
                frags.add(new Frag.Data(target.record(l, size)));
                if (v.initZero()) v.initDone(true);
            } else {
                Temp t = null;
                Frame.Access a;
                if (!v.up_level() && !v.needsAddress())
                    t = new Temp(GlobalName(v));
                if (v.formal == null) {
                    // simple local variable
                    if (t == null)
                        a = currentFrame.allocLocal(size);
                    else
                        a = currentFrame.allocLocal(t);
                } else if (v.indirect()) {
                    // formal passed by reference => param is an address
                    if (t == null)
                        a = currentFrame.allocFormal(wordSize);
                    else
                        a = currentFrame.allocFormal(t);
                } else {
                    // simple parameter
                    if (t == null)
                        a = currentFrame.allocFormal(size);
                    else
                        a = currentFrame.allocFormal(t);
                }
                accesses.put(v, a);
            }
        }
        default -> {}
        }
    }

    Void ImportProc(Value.Procedure v) {
        if (v.localScope != null) {
            Scope zz = Scope.Push(v.localScope);
            EnterScope(v.localScope);
            Scope.Pop(zz);
        } else {
            DeclareFormals(v);
        }
        return null;
    }
    Void DeclareFormals(Value.Procedure p) {
        // declare types for each of the formals
        for (Value v : Scope.ToList(p.sig.scope)) {
            Value.Formal f = (Value.Formal)v;
            Compile(TypeOf(f));
            Compile(f.refType);
        }
        return null;
    }

    /**
     * Generate language-defined initialization code for v.
     */
    Tree.Stm LangInit(Value value) {
        if (value == null) return null;
        if (value.compiled()) return null;
        assert value.checked();
        value.compiled(true);
        return switch (value) {
        case Value.Field v -> {
            Compile(v.type);
            yield null;
        }
        case Value.Formal v -> {
            Compile(v.type);
            Compile(v.refType);
            yield null;
        }
        case Value.Method v -> {
            Compile(v.sig);
            yield null;
        }
        case Value.Variable v -> {
            Tree.Stm stm = null;
            if (v.imported() || v.external()) {
                v.initDone(true);
            } else if (v.formal != null) {
                if (v.indirect() && v.formal.refType != null)
                    stm = CopyOpenArray(v, v.formal.refType);
                // formal parameters don't need any further initialization
                v.initDone(true);
            } else if (v.indirect() && !v.global()) {
                // WITH variable bound to a designator
                v.initDone(true);
            }

            if (v.initDone()) yield stm;

            // initialize the value
            if (v.init != null && !v.up_level() && !v.imported()) {
                // variable has a user specified init value and
                // isn't referenced by any nested procedures =>
                // try to avoid the language defined init and wait
                // until we get to the user defined
                // initialization.
                v.initPending(true);
            } else {
                stm = InitValue(LoadLValue(v), v.type);
            }
            yield stm;
        }
        default -> null;
        };
    }

    Tree.Stm CopyOpenArray(Value.Variable v, Type ref) {
        Error.ID(v.token, "open array passed by value unimplemented");
        return null;
    }

    /**
     * Generate code to load v.
     */
    Tree.Exp Load(Expr e, Value value) {
        if (value == null) return null;
        assert value.checked();
        return switch (value) {
        case Value.Formal v -> {
            Error.ID(v.token, "formal has no default value");
            yield null;
        }
        case Value.Const v -> Compile(v.expr);
        case Value.Procedure v -> {
            if (!(v.sig instanceof Type.Proc.User))
                Error.Txt(e.token, e.token.image, "builtin operation is not a procedure");
            Declare(v);
            if (v.external()) yield target.external(v.extName);
            yield NAME(Temp.getLabel(GlobalName(v)));
        }
        case Value.Variable v -> LoadLValue(v);
        default -> { assert false; yield null; }
        };
    }

    Tree.Exp LoadLValue(Value.Variable v) {
        Declare(v);
        if (v.initPending()) ForceInit(v);
        Frame.Access a = accesses.get(v);
        Tree.Exp exp;
        if (a == null) {
            assert v.global();
            if (v.external())
                exp = target.external(v.extName);
            else
                exp = NAME(Temp.getLabel(GlobalName(v)));
            if (v.indirect()) exp = MEM(exp);
            exp = MEM(exp, CONST(0), v.size, IsSigned(v.type));
        } else {
            Tree.Exp fp = currentFrame.FP();
            ProcBody home = v.proc;
            for (ProcBody body = currentBody; body != home; body = body.parent)
                fp = frames.get(body).link().exp(fp);
            exp = a.exp(fp);
            if (v.indirect())
                exp = MEM(exp, CONST(0), v.size, IsSigned(v.type));
            else if (exp instanceof Tree.Exp.MEM m)
                exp = MEM(m.exp(), m.offset(), v.size, IsSigned(v.type));
        }
        return exp;
    }

    Tree.Stm ForceInit(Value.Variable v) {
        v.initPending(false);
        return InitValue(LoadLValue(v), v.type);
    }

    /**
     * Generate language-defined initialization value for a variable of type t.
     */
    Tree.Stm InitValue(Tree.Exp lvalue, Type type) {
        type = Check(type);
        return switch(type) {
        case Type.Named t -> {
            if (t.type == null) Resolve(t);
            yield InitValue(lvalue, t.type);
        }
        case Type.Array t -> {
            if (IsOpenArray(t) == null) yield InitFixed(lvalue, t);
            yield InitOpen(lvalue, t);
        }
        case Type.Err t -> { assert false; yield null; }
        case Type.Subrange t -> {
            if (t.min.compareTo(BigInteger.ZERO) > 0)
                yield MOVE(lvalue, CONST(t.min));
            if (t.max.compareTo(BigInteger.ZERO) < 0)
                yield MOVE(lvalue, CONST(t.max));
            yield MOVE(lvalue, CONST(0));
        }
        case Type.Record t -> {
            // grab the record's lvalue
            Tree.Exp.MEM a = (Tree.Exp.MEM)lvalue;
            Tree.Stm stm = null;
            if (Force(a) instanceof ESEQ(Stm s, MEM e)) {
                stm = SEQ(stm, s);
                a = e;
            }
            for (Value.Field f: t.fields) {
                if (f.expr == null) {
                    stm = SEQ(stm,
                              InitValue(MEM(a.exp(),
                                            ADD(a.offset(),
                                                CONST(f.offset)),
                                            f.type.size,
                                            IsSigned(f.type)),
                                        f.type));
                } else {
                    assert false;
                }
            }
            yield stm;
        }
        case Type.Int t -> MOVE(lvalue, CONST(0));
        case Type.Enum t -> MOVE(lvalue, CONST(0));
        case Type.Ref t -> MOVE(lvalue, CONST(0));
        case Type.Proc t -> MOVE(lvalue, CONST(0));
        case Type.Object t -> MOVE(lvalue, CONST(0));
        };
    }
    Tree.Stm InitFixed(Tree.Exp lvalue, Type.Array t) {
        // TODO: generate code to initialize each element of the fixed
        // array having the given lvalue and type, using InitValue for
        // each element.
        Tree.Stm stm = null;
        // index is temp var to save current index
        Temp index = new Temp();
        Temp total_size = new Temp();
        Label tl = new Label();
        Label fl = new Label();

        // Check whether the index already exceed the total size of the array
        stm = SEQ(
                MOVE(TEMP(total_size), CONST(t.size / t.element.size)),
                MOVE(TEMP(index), CONST(0)),
                BGE(TEMP(index), TEMP(total_size), fl, tl),
                LABEL(tl)
        );

        // If the element type instance of Int, Enum, Ref, Proc, Object
        // Use init value
        Type type = Check(t.element);
        if (type instanceof Type.Int ||
            type instanceof Type.Enum ||
            type instanceof Type.Ref ||
            type instanceof Type.Proc ||
            type instanceof Type.Object) {
            // offset = lvalue.exp + index * element size + lvalue.offset
            MEM newLvalue = MEM(
                    ADD(
                            ((MEM) lvalue).exp(),
                            MUL(
                                    TEMP(index),
                                    CONST(t.element.size))),
                    ((MEM) lvalue).offset(), t.element.size, IsSigned(t.element)
            );
            stm = SEQ(stm, InitValue(newLvalue, t.element));
        }
        else {
            Temp t2 = new Temp();
            // For type which cannot be simply assigned to 0. Create a temp label
            // and call the init value
            MEM newLvalue = new MEM(TEMP(t2), ((MEM) lvalue).offset(), ((MEM) lvalue).size(), ((MEM) lvalue).signed());
            var src = InitValue(newLvalue, t.element);
            stm = SEQ(
                    stm,
                    MOVE(
                            TEMP(t2),
                            ADD(
                                   ((MEM) lvalue).exp(),
                                    MUL(
                                            TEMP(index), CONST(t.element.size)))),
                    src
            );
        }
        
        // Increment the index by 1 and check whether the value exceeds the total size
        // If true, the whole init process has done; if false, loop the process
        stm = SEQ(
                stm,
                MOVE(TEMP(index), ADD(TEMP(index), CONST(1))),
                BLT(TEMP(index), TEMP(total_size), tl ,fl),
                LABEL(fl)
        );
        return stm;
    }
    Tree.Stm InitOpen(Tree.Exp lvalue, Type.Array t) {
        // TODO: extra credit
        return InitValue(lvalue, t.element);
    }

    /**
     * Generate user-defined initialization code for v.
     */
    Tree.Stm UserInit(Value value) {
        if (value == null) return null;
        return switch (value) {
        case Value.Variable v -> {
            Tree.Stm stm = null;
            if (v.init != null && !v.initDone() && !v.imported()) {
                v.initPending(false);
                stm = EmitAssign(LoadLValue(v), v.type, v.init);
            }
            v.initDone(true);
            yield stm;
        }
        default -> null;
        };
    }

    /**
     * Compile a type t.
     */
    final Map<Type,Type> compiled = new HashMap<Type,Type>();
    {
        compiled.put(Check(Type.Bool.T),   Type.Bool.T);
        compiled.put(Check(Type.Char.T),   Type.Char.T);
        compiled.put(Check(Type.Int.T),    Type.Int.T);
        compiled.put(Check(Type.Null.T),   Type.Null.T);
        compiled.put(Check(Type.Root.T),   Type.Root.T);
        compiled.put(Check(Type.Refany.T), Type.Refany.T);
        compiled.put(Check(Type.Addr.T),   Type.Addr.T);
        compiled.put(Check(Type.Text.T),   Type.Text.T);
        compiled.put(Check(Type.Err.T),    Type.Err.T);
    }
    void Compile(Type type) {
        if (type == null) return;
        Type u = Check(type);
        if (compiled.put(u, u) != null) return;
        switch (type) {
        case Type.Named t -> {
            if (t.type == null) Resolve(t);
            if (t.type != null) Compile(t.type);
        }
        case Type.Object t -> {
            Compile(t.parent);
            GetOffsets(t);
            for (Value.Field f : t.fields) Compile(f.type);
            for (Value.Method m : t.methods) Compile(m.sig);
            for (Value.Method o : t.overrides) Compile(o.sig);
            Label[] defaults = new Label[(t.methodOffset + t.methodSize) / wordSize];
            GenMethods(t, defaults);
            String vtable = target.vtable(Temp.getLabel(GlobalUID(t)),
                                          Arrays.asList(defaults));
            frags.add(new Frag.Data(vtable));
        }
        case Type.Array t -> Compile(t.element);
        case Type.Proc t -> {
            Compile(t.result);
            for (Value v : Scope.ToList(t.scope)) Compile(TypeOf(v));
        }
        case Type.Ref t -> Compile(t.target);
        case Type.Record t -> {
            for (Value.Field f: t.fields) Compile(f.type);
        }
        case Type.Subrange t -> Compile(t.base);
        default -> {}
        }
    }
    void GenMethods(Type.Object t, Label[] defaults) {
        if (t == null) return;

        // TODO: Fill in method defaults (first calling recursively
        // for super type), by iterating over t's method scope.  Use
        // PrimaryMethodDeclaration from Semant to determine method
        // offsets.  Use Temp.getLabel(GlobalName(m.value)) for each
        // method m. If m.value is null use badPtr.name.

        if (t.parent != null) {
            GenMethods((Type.Object) t.parent, defaults);
        }

        var list = Scope.ToList(t.methodScope);

        for (var method: list) {
            var m = (Value.Method) method;
            var original = (Value.Method) PrimaryMethodDeclaration(t, m);
            Label l;
            if (m.value == null){
                l = badPtr.name;
            } else {
                l = Temp.getLabel(GlobalName(m.value));
            }
            defaults[(original.offset + original.parent.methodOffset) / wordSize] = l;
        }


        for (int i = 0; i < defaults.length; i++) {
            if (defaults[i] == null) {
                defaults[i] = badPtr.name;
            }
        }

    }

    Tree.Stm EmitAssign(final Tree.Exp lhs, final Type tlhs, final Expr rhs) {
        Type type = Base(tlhs); // strip renaming and packing
        assert type.checked();
        assert tlhs.checked();
        return switch(type) {
        case Type.Enum t -> AssignOrdinal(lhs, tlhs, rhs);
        case Type.Int t -> AssignOrdinal(lhs, tlhs, rhs);
        case Type.Object t -> AssignReference(lhs, tlhs, rhs);
        case Type.Array t -> AssignArray((Tree.Exp.MEM)lhs, tlhs, rhs);
        case Type.Proc t -> AssignProcedure(lhs, tlhs, rhs);
        case Type.Ref t -> AssignReference(lhs, tlhs, rhs);
        case Type.Record t -> AssignRecord((Tree.Exp.MEM)lhs, tlhs, rhs);
        case Type.Subrange t -> AssignOrdinal(lhs, tlhs, rhs);
        case Type.Named t -> { assert false; yield null; }
        case Type.Err t -> null;
        };
    }
    Tree.Stm AssignOrdinal(Tree.Exp lhs, Type tlhs, Expr rhs) {
        Bounds b = GetBounds(tlhs);
        Tree.Exp exp = EmitChecks(rhs, b.min(), b.max());
        return MOVE(lhs, exp);
    }
    Tree.Stm AssignReference(Tree.Exp lhs, Type tlhs, Expr rhs) {
        Tree.Exp exp = Compile(rhs);
        return MOVE(lhs, exp);
    }
    Tree.Stm AssignProcedure(Tree.Exp lhs, Type tlhs, Expr rhs) {
        Tree.Exp exp = Compile(rhs);
        return MOVE(lhs, exp);
    }
    Tree.Stm AssignRecord(Tree.Exp.MEM lhs, Type tlhs, Expr rhs) {
        Type.Record t = Is(tlhs, Type.Record.class);
        Tree.Exp.MEM a = (Tree.Exp.MEM)Compile(rhs);
        return EXP(CALL(target.external("memcpy"),
                        ADD(lhs.exp(), lhs.offset()),
                        ADD(a.exp(), a.offset()),
                        CONST(t.size)));
    }
    Tree.Stm AssignArray(Tree.Exp.MEM lhs, Type tlhs, Expr e_rhs) {
        Tree.Stm stm = null;
        Type trhs = TypeOf(e_rhs);
        Type.Array openRHS = IsOpenArray(trhs);
        Type.Array openLHS = IsOpenArray(tlhs);
        // capture the lhs and rhs pointers
        Tree.Exp.MEM rhs = (Tree.Exp.MEM)Compile(e_rhs);
        if (openRHS != null || openLHS != null) {
            if (Force(lhs) instanceof ESEQ(Stm s, MEM e)) {
                stm = SEQ(stm, s);
                lhs = e;
            }
            if (Force(rhs) instanceof ESEQ(Stm s, MEM e)) {
                stm = SEQ(stm, s);
                rhs = e;
            }
        }
        if (openRHS != null && openLHS != null) {
            stm = SEQ(stm, GenOpenArraySizeChecks(lhs, rhs, tlhs, trhs));
            stm = SEQ(stm, GenOpenArrayCopy(lhs, rhs, tlhs, trhs));
        } else if (openRHS != null) {
            stm = SEQ(stm, GenOpenArraySizeChecks(lhs, rhs, tlhs, trhs));
            stm = SEQ(stm, EXP(CALL(target.external("memmove"),
                                    ADD(lhs.exp(), lhs.offset()),
                                    rhs,
                                    CONST(tlhs.size))));
        } else if (openLHS != null) {
            stm = SEQ(stm, GenOpenArraySizeChecks(lhs, rhs, tlhs, trhs));
            stm = SEQ(stm, EXP(CALL(target.external("memmove"),
                                    lhs,
                                    ADD(rhs, rhs.offset()),
                                    CONST(trhs.size))));
        } else {
            stm = SEQ(stm, EXP(CALL(target.external("memmove"),
                                    ADD(lhs.exp(), lhs.offset()),
                                    ADD(rhs.exp(), rhs.offset()),
                                    CONST(trhs.size))));
        }
        return stm;
    }
    Tree.Stm GenOpenArraySizeChecks(Tree.Exp.MEM lhs, Tree.Exp.MEM rhs, Type tlhs, Type trhs) {
        Tree.Stm stm = null;
        Type.Array alhs, arhs;
        int n = 0;
        Label badSub = currentFrame.badSub();
        if (badSub == null) return null;
        while ((alhs = Is(tlhs, Type.Array.class)) != null &&
               (arhs = Is(trhs, Type.Array.class)) != null) {
            Expr
                nlhs = ConstValue(alhs.number),
                nrhs = ConstValue(arhs.number);
            if (nlhs != null && nrhs != null) return stm;
            Label l = new Label();
            if (nlhs != null) {
                stm = SEQ(stm, BNE(
                        Compile(nlhs),
                        MEM(rhs.exp(), ADD(rhs.offset(), CONST((n+1)*wordSize))),
                        badSub, l), LABEL(l));
            } else if (nrhs != null) {
                stm = SEQ(stm, BNE(
                        MEM(lhs.exp(), ADD(lhs.offset(), CONST((n+1)*wordSize))),
                        Compile(nrhs),
                        badSub, l), LABEL(l));
            } else {
                stm = SEQ(stm, BNE(
                        MEM(lhs.exp(), ADD(lhs.offset(), CONST((n+1)*wordSize))),
                        MEM(rhs.exp(), ADD(rhs.offset(), CONST((n+1)*wordSize))),
                        badSub, l), LABEL(l));
            }
            ++n;
            tlhs = alhs.element;
            trhs = arhs.element;
        }
        return stm;
    }
    Tree.Stm GenOpenArrayCopy(Tree.Exp.MEM lhs, Tree.Exp.MEM rhs, Type tlhs, Type trhs) {
        assert rhs.exp() instanceof Tree.Exp.TEMP;
        assert lhs.exp() instanceof Tree.Exp.TEMP;
        Tree.Stm stm = null;
        int lhsDepth = OpenDepth(tlhs);
        int rhsDepth = OpenDepth(trhs);
        assert lhsDepth > 0 && rhsDepth > 0;
        int depth = Math.min(lhsDepth, rhsDepth);
        Temp max = new Temp();
        stm = SEQ(stm, MOVE(TEMP(max),
                            MEM(rhs.exp(), ADD(rhs.offset(), CONST(wordSize)))));
        for (int i = 1; i < depth; i++) {
            stm = SEQ(stm, MOVE(TEMP(max),
                                MUL(TEMP(max),
                                    MEM(rhs.exp(),
                                        ADD(rhs.offset(),
                                            CONST((i+1)*wordSize))))));
        }
        Type eltType = OpenType((lhsDepth < rhsDepth) ? tlhs : trhs);
        return SEQ(stm,
                   EXP(CALL(target.external("memmove"), lhs, rhs,
                            MUL(TEMP(max),
                                CONST(eltType.size)))));
    }

    Tree.Stm Compile(Stmt stmt, Label loopExit) {
        return switch (stmt) {
        case null-> null;
        case Stmt.Assign s -> EmitAssign(Compile(s.lhs), TypeOf(s.lhs), s.rhs);
        case Stmt.Call s -> EXP(Compile(s.expr));
        case Stmt.Break s -> new Tree.Stm.JUMP(loopExit); // TODO
        case Stmt.For s -> {
            Tree.Stm stm = null;
            Expr step, limit, from;
            BigInteger step_val = BigInteger.ZERO;
            BigInteger limit_val = BigInteger.ZERO;
            BigInteger from_val = BigInteger.ZERO;
            Temp t_index, t_to, t_by;

            t_index = new Temp();
            from = ConstValue(s.from);
            if (from == null) {
                stm = SEQ(stm, MOVE(TEMP(t_index), Compile(s.from)));
            } else {
                if (from instanceof Expr.Int)
                    from_val = ((Expr.Int)from).value;
                else if (from instanceof Expr.Enum)
                    from_val = ((Expr.Enum)from).value;
                stm = SEQ(stm, MOVE(TEMP(t_index), CONST(from_val)));
            }
            t_to = new Temp();
            limit = ConstValue(s.to);
            if (limit == null) {
                stm = SEQ(stm, MOVE(TEMP(t_to), Compile(s.to)));
            } else {
                if (limit instanceof Expr.Int)
                    limit_val = ((Expr.Int)limit).value;
                else if (limit instanceof Expr.Enum)
                    limit_val = ((Expr.Enum)limit).value;
                stm = SEQ(stm, MOVE(TEMP(t_to), CONST(limit_val)));
            }
            t_by = new Temp();
            step = ConstValue(s.by);
            if (step == null) {
                stm = SEQ(stm, MOVE(TEMP(t_by), Compile(s.by)));
            } else {
                if (step instanceof Expr.Int)
                    step_val = ((Expr.Int)step).value;
                else if (step instanceof Expr.Enum)
                    step_val = ((Expr.Enum)step).value;
                stm = SEQ(stm, MOVE(TEMP(t_by), Compile(step)));
            }
            Label l_top = new Label();
            Label l_test = new Label();
            Label l_exit = new Label();

            Scope zz = Scope.Push(s.scope);
            EnterScope(s.scope);
            stm = SEQ(stm, InitValues(s.scope));

            if (from == null || limit == null || step == null) {
                // we don't know all three values
                stm = SEQ(stm, JUMP(l_test));
            } else if (step_val.signum() >= 0 && from_val.compareTo(limit_val) <= 0) {
                // we know we'll execute the loop at least once
            } else if (step_val.signum() <= 0 && from_val.compareTo(limit_val) >= 0) {
                // we know we'll execute the loop at least once
            } else {
                // we won't execute the loop
                stm = SEQ(stm, JUMP(l_test));
            }
            stm = SEQ(stm, LABEL(l_top));

            // make the user's variable equal to the counter
            stm = SEQ(stm, MOVE(LoadLValue(s.var), TEMP(t_index)));

            stm = SEQ(stm, Compile(s.stmt, l_exit));

            // increment the counter
            stm = SEQ(stm, MOVE(TEMP(t_index), ADD(TEMP(t_index), TEMP(t_by))));

            // generate the loop test
            stm = SEQ(stm, LABEL(l_test));
            if (step != null) {
                // constant step value
                if (step_val.signum() >= 0)
                    stm = SEQ(stm, BLE(TEMP(t_index), TEMP(t_to), l_top, l_exit));
                else
                    stm = SEQ(stm, BGE(TEMP(t_index), TEMP(t_to), l_top, l_exit));
            } else {
                // variable step value
                Label l_up = new Label();
                Label l_down = new Label();

                stm = SEQ(stm, BLT(TEMP(t_by), CONST(0), l_down, l_up));
                stm = SEQ(stm, LABEL(l_up));
                stm = SEQ(stm, BLE(TEMP(t_index), TEMP(t_to), l_top, l_exit));
                stm = SEQ(stm, LABEL(l_down));
                stm = SEQ(stm, BGE(TEMP(t_index), TEMP(t_to), l_top, l_exit));
            }
            Scope.Pop(zz);
            yield SEQ(stm, LABEL(l_exit));
        }
        case Stmt.If s -> {
            Label t = new Label();
            if (s.elseStmt == null) {
                Label f = new Label();
                yield
                    SEQ(CompileBranch(s.expr, t, f),
                        LABEL(t),
                        Compile(s.thenStmt, loopExit),
                        LABEL(f));
            } else {
                // TODO
                Label e = new Label();
                Label f = new Label();
                yield SEQ(CompileBranch(s.expr, t, e),
                        LABEL(t),
                        Compile(s.thenStmt, loopExit),
                        new Tree.Stm.JUMP(f),
                        LABEL(e),
                        Compile(s.elseStmt, loopExit),
                        LABEL(f)
                );

            }
        }
        case Stmt.Loop s -> {
            // TODO
            Label j = new Label();
            Label l = new Label();
            Label f = new Label();
            List<Tree.Stm> statements = new ArrayList<>();
            statements.add(LABEL(j));
            statements.add(CompileBranch(s.whileExpr, l, f));

            statements.add(LABEL(l));
            statements.add(Compile(s.stmt, f));

            statements.add(CompileBranch(s.untilExpr, f, j));

            if (s.untilExpr == null) {
                statements.add(new Tree.Stm.JUMP(j));
            }
            statements.add(LABEL(f));

            yield SEQ(statements.toArray(new Tree.Stm[0]));
        }
        case Stmt.Return s -> {
            if (s.expr == null)
                yield JUMP(returnLabel);
            if (returnResult != null) {
                Tree.Exp.MEM mem = (Tree.Exp.MEM)LoadLValue(returnResult);
                Temp t = new Temp();
                yield SEQ(MOVE(TEMP(t), ADD(mem.exp(), mem.offset())),
                          EmitAssign(MEM(TEMP(t)), TypeOf(returnResult),
                                     s.expr),
                          MOVE(currentFrame.RV(), TEMP(t)),
                          JUMP(returnLabel));
            }
            yield SEQ(MOVE(currentFrame.RV(), Compile(s.expr)),
                      JUMP(returnLabel));
        }
        case Stmt.Block s -> {
            Tree.Stm stm = null;
            if (s.scope != null) {
                Scope zz = Scope.Push(s.scope);
                EnterScope(s.scope);
                stm = SEQ(stm, InitValues(s.scope));
                for (Stmt x : s.body)
                    stm = SEQ(stm, Compile(x, loopExit));
                Scope.Pop(zz);
            } else {
                for (Stmt x : s.body)
                    stm = SEQ(stm, Compile(x, loopExit));
            }
            yield stm;
        }
        };
    }

    Tree.Exp EmitChecks(Expr e, BigInteger min, BigInteger max) {
        Expr x = ConstValue(e);
        if (x != null) e = x;
        Tree.Exp exp = Compile(e);
        Label err = currentFrame.badSub();
        if (err == null) return exp;
        Bounds b = GetBounds(e);
        if (b.min().compareTo(min) < 0 && b.max().compareTo(max) > 0) {
            Temp t = new Temp();
            Label l1 = new Label();
            Label l2 = new Label();
            return ESEQ(SEQ(
                    MOVE(TEMP(t), exp),
                    BLT(TEMP(t), CONST(min), err, l1),
                    LABEL(l1),
                    BGT(TEMP(t), CONST(max), err, l2),
                    LABEL(l2)),
                    TEMP(t));
        } else if (b.min().compareTo(min) < 0) {
            if (b.max().compareTo(min) < 0)
                Error.Warn(e.token, "value out of range");
            Temp t = new Temp();
            Label l = new Label();
            return ESEQ(SEQ(
                    MOVE(TEMP(t), exp),
                    BLT(TEMP(t), CONST(min), err, l),
                    LABEL(l)),
                    TEMP(t));
        } else if (b.max().compareTo(max) > 0) {
            if (b.min().compareTo(max) > 0)
                Error.Warn(e.token, "value out of range");
            Temp t = new Temp();
            Label l = new Label();
            return ESEQ(SEQ(
                    MOVE(TEMP(t), exp),
                    BGT(TEMP(t), CONST(max), err, l),
                    LABEL(l)),
                    TEMP(t));
        } else {
            return exp;
        }
    }

    final HashMap<Expr,Temp> passObject = new HashMap<Expr,Temp>();

    Tree.Exp Compile(Expr expr) {
        if (expr == null) return null;
        assert expr.checked;
        return switch(expr) {
        case Expr.Proc e -> Load(e, e.proc);
        case Expr.Method e -> {
            Value.Method m = e.method;
            yield MEM(NAME(Temp.getLabel(GlobalUID(m.parent))),
                      CONST(e.method.parent.methodOffset + m.offset));
        }
        case Expr.Infix e ->
            switch (e.op) {
            case Add    -> ADD(Compile(e.left), Compile(e.right));
            case Sub    -> SUB(Compile(e.left), Compile(e.right));
            case Mul    -> MUL(Compile(e.left), Compile(e.right));
            case Div    -> DIV(Compile(e.left), Compile(e.right));
            case Mod    -> MOD(Compile(e.left), Compile(e.right));
            case BitAnd -> AND(Compile(e.left), Compile(e.right));
            case BitOr  -> OR (Compile(e.left), Compile(e.right));
            case BitXor -> XOR(Compile(e.left), Compile(e.right));
            case SLL    -> SLL(Compile(e.left), Compile(e.right));
            case SRL    -> SRL(Compile(e.left), Compile(e.right));
            case SRA    -> SRA(Compile(e.left), Compile(e.right));
            // TODO: EQ, NE, GT, GE, LT, LE, Or, And
            case And -> {
                var t = new Label();
                var f = new Label();
                Temp temp = new Temp();
                // first set temp to 0;
                // if e is false, end;
                // if e is true, set temp to 1
                var stm = SEQ(
                        MOVE(TEMP(temp), CONST(0)),
                        CompileBranch(e, t, f),
                        LABEL(t),
                        MOVE(TEMP(temp), CONST(1)),
                        LABEL(f)
                );
                yield ESEQ(stm, TEMP(temp));

            }
            case Or -> {
                var t = new Label();
                var f = new Label();
                Temp temp = new Temp();
                // first set temp to 1;
                // if e is true, end;
                // if e is false, set temp to 0
                var stm = SEQ(
                        MOVE(TEMP(temp), CONST(1)),
                        CompileBranch(e, t, f),
                        LABEL(f),
                        MOVE(TEMP(temp), CONST(0)),
                        LABEL(t)
                );
                yield ESEQ(stm, TEMP(temp));
            }
            default -> {
                // TODO: EQ, NE, GT, GE, LT, LE, Or, And
                var t = new Label();
                var f = new Label();
                Temp temp = new Temp();
                // first set temp to 1;
                // if e is true, end;
                // if e is false, set temp to 0
                var stm = SEQ(
                        MOVE(TEMP(temp), CONST(0)),
                        CompileBranch(e, t, f),
                        LABEL(t),
                        MOVE(TEMP(temp), CONST(1)),
                        LABEL(f)
                );
                yield ESEQ(stm, TEMP(temp));
            }
            };
        case Expr.Prefix e -> {
            // TODO
            yield switch (e.op) {
                case Not -> {
                    var t = new Label();
                    var f = new Label();
                    Temp temp = new Temp();
                    var stm = SEQ(
                            MOVE(TEMP(temp), CONST(0)),
                            CompileBranch(e, t, f),
                            LABEL(t),
                            MOVE(TEMP(temp), CONST(1)),
                            LABEL(f)
                    );
                    yield ESEQ(stm, TEMP(temp));
                }
                case Neg -> {
                    yield SUB(CONST(0), Compile(e.expr));
                }
                default -> { yield null; }
            };
        }
        case Expr.Address e -> CONST(e.value.intValue());
        case Expr.Check e -> {
            Tree.Exp exp = Compile(e.expr);
            Label badSub = currentFrame.badSub();
            if (badSub == null) yield exp;
            if (e.min != null && e.max != null) {
                Temp t = new Temp();
                Label l1 = new Label();
                Label l2 = new Label();
                yield
                    ESEQ(SEQ(MOVE(TEMP(t), exp),
                             BLT(TEMP(t), CONST(e.min), badSub, l1),
                             LABEL(l1),
                             BGT(TEMP(t), CONST(e.max), badSub, l2),
                             LABEL(l2)),
                         TEMP(t));
            } else if (e.min != null) {
                Temp t = new Temp();
                Label l = new Label();
                yield
                    ESEQ(SEQ(MOVE(TEMP(t), exp),
                             BLT(TEMP(t), CONST(e.min), badSub, l),
                             LABEL(l)),
                         TEMP(t));
            } else if (e.max != null) {
                Temp t = new Temp();
                Label l = new Label();
                yield
                    ESEQ(SEQ(MOVE(TEMP(t), exp),
                             BGT(TEMP(t), CONST(e.max), badSub, l),
                             LABEL(l)),
                         TEMP(t));
            } else {
                yield exp;
            }
        }
        case Expr.Call ce -> {
            yield switch(ce.procType) {
            case Type.Proc.User p -> {
                Expr proc = ce.proc;
                assert ce.proc.checked;
                Type p_type = TypeOf(proc);
                if (p_type == null)
                    p_type = MethodType(Is(proc, Expr.Qualify.class));
                p_type = Base(p_type);
                // grab the formals list
                Value.Procedure p_value = IsProcedureLiteral(proc);
                Tree.Exp fp = CONST(0);
                if (p_value != null) {
                    ProcBody caller = currentBody;
                    ProcBody callee = p_value.body;
                    if (callee != null && callee.parent != null) {
                        // call to nested function
                        // TODO: pass static link
                        fp = frames.get(caller).FP();
                        for (ProcBody body = caller;
                             body != callee.parent;
                             body = body.parent)
                            fp = frames.get(body).link().exp(fp);
                    }
                }
                Tree.Exp exp = Compile(proc);
                LinkedList<Tree.Exp>args = new LinkedList<Tree.Exp>();
                Type type = Result(Is(p_type, Type.Proc.class));
                Tree.Exp.MEM result = null;
                if (IsStructured(type)) {
                    Frame.Access a = currentFrame.allocLocal(type.size);
                    result = (Tree.Exp.MEM)a.exp(currentFrame.FP());
                    args.add(ADD(result.exp(), result.offset()));
                }
                Temp t = passObject.get(proc);
                if (t != null) args.add(TEMP(t));
                int n = 0;
                for (Value f : Formals(Is(p_type, Type.Proc.class))) {
                    args.add(EmitArg(ce.proc, Is(f, Value.Formal.class),
                                     ce.args[n++]));
                }
                if (IsStructured(type))
                    yield MEM(CALL(exp, fp, args), CONST(0), type.size);
                yield CALL(exp, fp, args);
            }
            case Type.Proc.First p -> {
                Expr e = ce.args[0];
                Type t = IsType(e);
                if (t == null) t = TypeOf(e);
                Compile(t);
                Type.Array ta = Is(t, Type.Array.class);
                if (ta != null) yield CONST(0);
                assert IsOrdinal(t);
                Bounds b = GetBounds(t);
                yield CONST(b.min());
            }
            case Type.Proc.Last p -> {
                Expr e = ce.args[0];
                Type t = IsType(e);
                if (t == null) t = TypeOf(e);
                Compile(t);
                Type.Array ta = Is(t, Type.Array.class);
                if (ta != null) {
                    Expr.Int n = Is(ConstValue(ta.number), Expr.Int.class);
                    if (n == null) {
                        // open array
                        Tree.Exp.MEM mem = (Tree.Exp.MEM)Compile(e);
                        yield SUB(MEM(mem.exp(),
                                      ADD(mem.offset(),
                                          CONST(wordSize))),
                                  CONST(1));
                    }
                    yield CONST(n.value.subtract(BigInteger.ONE));
                }
                assert IsOrdinal(t);
                Bounds b = GetBounds(t);
                yield CONST(b.max());
            }
            case Type.Proc.Ord p -> Compile(ce.args[0]);
            case Type.Proc.Val p -> {
                Expr e = ce.args[1];
                Type t = IsType(e);
                if (t != null) Compile(t);
                yield Compile(ce.args[0]);
            }
            case Type.Proc.Number p -> {
                Expr e = ce.args[0];
                Type t = IsType(e);
                if (t == null) t = TypeOf(e);
                Compile(t);
                Type.Array ta = Is(t, Type.Array.class);
                if (ta != null) {
                    Expr.Int n = Is(ConstValue(ta.number), Expr.Int.class);
                    if (n == null) {
                        // open array
                        Tree.Exp.MEM mem = (Tree.Exp.MEM)Compile(e);
                        yield MEM(mem.exp(),
                                  ADD(mem.offset(),
                                      CONST(wordSize)));
                    }
                    yield CONST(n.value);
                }
                assert IsOrdinal(t);
                Bounds b = GetBounds(t);
                BigInteger min = b.min(), max = b.max();
                if (max.compareTo(min) < 0) yield CONST(0);
                BigInteger num = max.subtract(min).add(BigInteger.ONE);
                if (num.compareTo(MAX_VALUE) > 0) {
                    Error.Warn(ce.token, "result of Number too large");
                    Label badSub = currentFrame.badSub();
                    yield ESEQ(JUMP(badSub), CONST(0));
                }
                yield CONST(num);
            }
            case Type.Proc.New p -> {
                Expr e = ce.args[0];
                Type t = IsType(e);
                assert t != null;
                Compile(t);
                Type.Ref r = Is(t, Type.Ref.class);
                if (r != null) yield GenRef(t, Strip(r.target), ce);
                if (Is(t, Type.Object.class) != null) yield GenObject(t);
                Error.Msg(ce.token, "New must be applied to a variable of a reference type");
                yield null;
            }
            case Type.Proc.Loophole p -> Compile(ce.args[0]);
            case Type.Proc.BitSize p -> {
                Expr e = ce.args[0];
                Type t = IsType(e);
                if (t != null) {
                    Compile(t);
                    assert t.size > 0;
                    assert t.bits > 0;
                    yield CONST(t.bits);
                }
                t = Check(TypeOf(e));
                if (IsOpenArray(t) == null) yield CONST(t.bits);
                // open array
                Tree.Exp.MEM v = (Tree.Exp.MEM)Compile(e);
                Tree.Stm stm = null;
                if (Force(v) instanceof ESEQ(Stm s, MEM exp)) {
                    stm = SEQ(stm, s);
                    v = exp;
                }
                Temp x = new Temp();
                stm = SEQ(stm, MOVE(TEMP(x),
                                    MEM(v.exp(), ADD(v.offset(), CONST(wordSize)))));
                for (int i = 1; i < OpenDepth(t); i++)
                    stm = SEQ(stm,
                              MOVE(TEMP(x),
                                   MUL(TEMP(x),
                                       MEM(v.exp(),
                                           ADD(v.offset(),
                                               CONST((i+1) * wordSize))))));
                stm = SEQ(stm, MOVE(TEMP(x), MUL(TEMP(x), CONST(OpenType(t).size * 8))));
                yield ESEQ(stm, TEMP(x));
            }
            case Type.Proc.ByteSize p -> {
                Expr e = ce.args[0];
                Type t = IsType(e);
                if (t != null) {
                    Compile(t);
                    assert t.size > 0;
                    assert t.bits > 0;
                    yield CONST(t.size);
                }
                t = Check(TypeOf(e));
                if (IsOpenArray(t) == null) yield CONST(t.size);
                // open array
                Tree.Exp.MEM v = (Tree.Exp.MEM)Compile(e);
                Tree.Stm stm = null;
                if (Force(v) instanceof ESEQ(Stm s, MEM exp)) {
                    stm = SEQ(stm, s);
                    v = exp;
                }
                Temp x = new Temp();
                stm = SEQ(stm, MOVE(TEMP(x),
                                    MEM(v.exp(), ADD(v.offset(), CONST(wordSize)))));
                for (int i = 1; i < OpenDepth(t); i++)
                    stm = SEQ(stm,
                              MOVE(TEMP(x),
                                   MUL(TEMP(x),
                                       MEM(v.exp(),
                                           ADD(v.offset(),
                                               CONST((i+1) * wordSize))))));
                stm = SEQ(stm, MOVE(TEMP(x), MUL(TEMP(x), CONST(OpenType(t).size))));
                yield ESEQ(stm, TEMP(x));
            }
            };
        }
        case Expr.Cast e -> {
            Type u = Check(TypeOf(e.expr));
            Type t = Check(e.tipe);
            Compile(u);
            Compile(t);
            yield switch (e.kind) {
            case NOP -> Compile(e.expr);
            case DA -> BuildArray(e, t, CompileAddress(e.expr), u.size);
            case SA -> BuildArray(e, t, CompileAddress(e.expr), u.size);
            case VA -> BuildArray(e, t, GenCopy(u, Compile(e.expr)), u.size);
            case VS -> GenCopy(u, Compile(e.expr));
            case DS, SS, DV, SV, VV -> Compile(e.expr);
            };
        }
        case Expr.Deref e -> {
            Tree.Exp exp = Compile(e.expr);
            Label badPtr = currentFrame.badPtr();
            if (badPtr != null) {
                Temp t = new Temp();
                Label okPtr = new Label();
                exp = ESEQ(SEQ(MOVE(TEMP(t), exp),
                               BEQ(TEMP(t), CONST(0), badPtr, okPtr),
                               LABEL(okPtr)),
                           TEMP(t));
            }
            int size = e.type.size;
            if (size < 0) size = (OpenDepth(e.type)+1) * wordSize;
            yield MEM(exp, CONST(0), size, IsSigned(e.type));
        }
        case Expr.Qualify e -> {
            assert e.expr.checked;
            Value v = Resolve(e);
            assert v != null;
            if (e.objType != null) {
                Compile(e.objType);
                Value.Method m = (Value.Method)v;
                assert !m.override;
                yield MEM(NAME(Temp.getLabel(GlobalUID(e.objType))),
                          CONST(m.parent.methodOffset + m.offset));
            }
            if (v instanceof Value.Method m) {
                assert !m.override;
                Tree.Exp exp = Compile(e.expr);
                Temp temp;
                Tree.Stm prep;
                if (exp instanceof Tree.Exp.TEMP t) {
                    temp = t.temp();
                    prep = null;
                } else {
                    temp = new Temp();
                    prep = MOVE(TEMP(temp), exp);
                }
                passObject.put(e, temp);
                // TODO: make sure to include prep!
                // Use -wordSize instead of -8 to adapt to different platforms
                Exp mem = MEM(
                                MEM(exp, CONST(-wordSize)),
                            CONST(m.offset + m.parent.methodOffset));
                if (prep != null)
                    yield ESEQ(prep, mem);
                else
                    yield mem;
            }
            if (v instanceof Value.Field f) {
                Tree.Exp exp = Compile(e.expr);
                if (Is(f.parent, Type.Record.class) != null) {
                    Tree.Exp.MEM mem = (Tree.Exp.MEM)exp;
                    yield MEM(mem.exp(), ADD(mem.offset(), CONST(f.offset)),
                              f.type.size, IsSigned(f.type));
                }
                Type.Object p = Is(f.parent, Type.Object.class);
                if (p != null) {
                    Label badPtr = currentFrame.badPtr();
                    if (badPtr != null) {
                        Temp t = new Temp();
                        Label okPtr = new Label();
                        exp = ESEQ(SEQ(MOVE(TEMP(t), exp),
                                       BEQ(TEMP(t), CONST(0), badPtr, okPtr),
                                       LABEL(okPtr)),
                                   TEMP(t));
                    }
                    yield MEM(exp,
                              CONST(p.fieldOffset + f.offset),
                              f.type.size, IsSigned(f.type));
                }
            }
            yield Load(e, v);
        }
        case Expr.Subscript p -> {
            Type ta = Base(TypeOf(p.expr));
            Type.Array b = Is(ta, Type.Array.class); assert b != null;
            Tree.Exp.MEM a = (Tree.Exp.MEM)Compile(p.expr);
            int depth = OpenDepth(ta);
            if (depth == 0) {
                // a fixed array

                // TODO:
                //
                // - Treat constant and variable indexes differently.
                // - No need for bounds check here as already injected
                //   in semantic processing as an Expr.Check
                if (p.index instanceof Expr.Check) {
                    var value = Compile((Expr.Check)p.index);
                    yield MEM(ADD(a.exp(), MUL(value, CONST(b.element.size))), a.offset(), b.element.size, IsSigned(b.element));
                } else {
                    // new offset = old offset + index * element.size
                    var offset = new BigInteger(String.valueOf(a.offset().value()));
                    var size = (((Expr.Int) p.index).value).multiply(new BigInteger(String.valueOf(b.element.size)));
                    offset = offset.add(size);
                    yield MEM(a.exp(), CONST(offset), b.element.size, IsSigned(b.element));
                }

            } else if (depth == 1) {
                // a single dimension open array
                // TODO: extra credit
                //   - Make sure to include bounds check
                yield null;
            } else {
                // a multi dimension open array
                // TODO: extra extra credit
                //   - Make sure to include bounds check
                yield null;
            }
        }
        case Expr.Named e -> {
            Value v = Resolve(e);
            yield Load(e, v);
        }
        case Expr.Int e -> {
            if (e.value.bitLength() >= wordSize * 8) {
                Error.Msg(e.token, "value is not a constant");
                yield CONST(0);
            }
            yield CONST(e.value);
        }
        case Expr.Text e -> NAME(textLabel(e.value));
        case Expr.TypeExpr e -> { assert false; yield null; }
        case Expr.Enum e -> CONST(e.value);
        };
    }

    Tree.Exp BuildArray(Expr.Cast e, Type t, Tree.Exp payload, int size) {
        Tree.Stm stm = null;
        Type elt = OpenType(t);
        int d = OpenDepth(t);
        if (d != 1)
            Error.Msg(e.token, "Loophole: multidimensional open arrays not supported");

        // allocate a dope vector
        Tree.Exp.MEM v
            = (Tree.Exp.MEM)currentFrame.allocLocal((d+1) * wordSize).exp(currentFrame.FP());
        if (Force(v) instanceof ESEQ(Stm s, MEM exp)) {
            stm = SEQ(stm, s);
            v = exp;
        }
        stm = SEQ(stm,
                  MOVE(MEM(v.exp(), v.offset()), payload),
                  MOVE(MEM(v.exp(), ADD(v.offset(), CONST(wordSize))), CONST(size / elt.size)));
        return MEM(ESEQ(stm, v.exp()), v.offset(), (d+1) * wordSize);
    }

    Tree.Exp EmitArg(Expr proc, Value.Formal f, Expr actual) {
        return switch (f.type) {
        case Type.Int t -> GenOrdinal(f, actual);
        case Type.Enum t -> GenOrdinal(f, actual);
        case Type.Subrange t -> GenOrdinal(f, actual);
        case Type.Ref t -> GenReference(f, actual);
        case Type.Object t -> GenReference(f, actual);
        case Type.Proc t -> GenProcedure(f, actual, proc);
        case Type.Record t -> GenRecord(f, actual);
        case Type.Array t -> GenArray(f, actual);
        default -> { assert false; yield null; }
        };
    }
    Tree.Exp GenOrdinal(Value.Formal f, Expr actual) {
        return switch (f.mode) {
        case VALUE -> {
            Bounds b = GetBounds(f.type);
            yield EmitChecks(actual, b.min(), b.max());
        }
        case VAR -> CompileAddress(actual);
        case READONLY -> {
            Bounds b = GetBounds(f.type);
            if (!IsEqual(f.type, TypeOf(actual), null)) {
                yield GenCopy(f.type, EmitChecks(actual, b.min(), b.max()));
            } else if (IsDesignator(actual)) {
                yield CompileAddress(actual);
            } else // non-designator, same type
                yield GenCopy(f.type, Compile(actual));
        }};
    }
    Tree.Exp GenReference(Value.Formal f, Expr actual) {
        return switch(f.mode) {
        case VALUE -> Compile(actual);
        case VAR -> CompileAddress(actual);
        case READONLY -> {
            if (!IsEqual(f.type, TypeOf(actual), null)) {
                yield GenCopy(f.type, Compile(actual));
            } else if (IsDesignator(actual)) {
                yield CompileAddress(actual);
            } else // non-designator, same type
                yield GenCopy(f.type, Compile(actual));
        }};
    }
    Tree.Exp GenProcedure(Value.Formal f, Expr actual, Expr proc) {
        return switch(f.mode) {
        case VALUE -> GenClosure(actual, proc);
        case VAR -> CompileAddress(actual);
        case READONLY ->
            IsDesignator(actual) ? CompileAddress(actual) : GenCopy(f.type, GenClosure(actual, proc));
        };
    }
    final int MarkerOffset = 0;
    final int ProcOffset = MarkerOffset + wordSize;
    final int LinkOffset = ProcOffset + wordSize;
    final int ClosureSize = LinkOffset + wordSize;
    Tree.Exp GenClosure(Expr actual, Expr proc) {
        Value.Procedure v = RequiresClosure(proc);
        if (v == null) return Compile(actual);
        // actual is a nested procedure literal passed by value
        if (IsExternalProcedure(proc)) {
            Error.Warn(actual.token, "passing nested procedure to external procedure");
        }
        // allocate space for the closure
        Frame.Access a = currentFrame.allocLocal(ClosureSize);
        Tree.Exp.MEM rhs = (Tree.Exp.MEM)a.exp(currentFrame.FP());
        Tree.Stm stm = null;
        if (Force(rhs) instanceof ESEQ(Stm s, MEM e)) {
            stm = SEQ(stm, s);
            rhs = e;
        }
        // and fill it in
        ProcBody caller = currentBody;
        ProcBody callee = v.body;
        Tree.Exp fp = CONST(0);
        if (callee != null && callee.parent != null) {
            fp = frames.get(caller).FP();
            for (ProcBody body = caller;
                 body != callee.parent;
                 body = body.parent)
                fp = frames.get(body).link().exp(fp);
        }
        return MEM(ESEQ(SEQ(stm,
                            MOVE(MEM(rhs.exp(),
                                     ADD(rhs.offset(),
                                         CONST(MarkerOffset))),
                                 CONST(-1)),
                            MOVE(MEM(rhs.exp(),
                                     ADD(rhs.offset(),
                                         CONST(ProcOffset))),
                                 Compile(proc)),
                            MOVE(MEM(rhs.exp(),
                                     ADD(rhs.offset(),
                                         CONST(LinkOffset))),
                                 fp)),
                        rhs.exp()),
                   rhs.offset(),
                   rhs.size());
    }
    Value.Procedure RequiresClosure(Expr e) {
        Value.Procedure proc = IsProcedureLiteral(e);
        if (IsNested(proc)) return proc;
        return null;
    }
    boolean IsExternalProcedure(Expr e) {
        Value.Procedure proc = IsProcedureLiteral(e);
        if (proc != null) return IsExternal(proc);
        return false;
    }
    Tree.Exp GenRecord(Value.Formal f, Expr actual) {
        switch(f.mode) {
        case VALUE: return Compile(actual);
        case VAR: return CompileAddress(actual);
        case READONLY:
            if (IsDesignator(actual))
                return CompileAddress(actual);
            else
                return Compile(actual);
        }
        assert false; return null;
    }
    Tree.Exp GenArray(Value.Formal f, Expr actual) {
        Type t = TypeOf(actual);
        switch(f.mode) {
        case VALUE: {
            Tree.Exp.MEM mem
                = ReshapeArray(f.type, t, Compile(actual));
            if (IsOpenArray(f.type) != null)
                return ADD(mem.exp(), mem.offset());
            return mem;
        }
        case VAR: {
            Tree.Exp.MEM mem
                = ReshapeArray(f.type, t, Compile(actual));
            return ADD(mem.exp(), mem.offset());
        }
        case READONLY:
            if (!IsEqual(f.type, t, null)) {
                Tree.Exp.MEM mem
                    = ReshapeArray(f.type, t, Compile(actual));
                return ADD(mem.exp(), mem.offset());
            } else if (IsDesignator(actual)) {
                return CompileAddress(actual);
            } else {
                return Compile(actual);
            }
        }
        assert false; return null;
    }
    Tree.Exp.MEM ReshapeArray(Type tlhs, Type trhs, Tree.Exp actual) {
        if (IsEqual(tlhs, trhs, null))
            return (Tree.Exp.MEM)actual;
        int d_lhs = OpenDepth(tlhs);
        int d_rhs = OpenDepth(trhs);
        if (d_lhs == d_rhs)
            return (Tree.Exp.MEM)actual;
        // capture the actual
        Tree.Exp.MEM rhs = (Tree.Exp.MEM)actual;
        Tree.Stm stm = null;
        if (Force(rhs) instanceof ESEQ(Stm s, MEM e)) {
            stm = SEQ(stm, s);
            rhs = e;
        }
        if (d_lhs > d_rhs) {
            // build a bigger dope vector
            Frame.Access a = currentFrame.allocLocal((d_lhs + 1) * wordSize);
            Tree.Exp.MEM lhs = (Tree.Exp.MEM)a.exp(currentFrame.FP());
            if (Force(lhs) instanceof ESEQ(Stm s, MEM e)) {
                stm = SEQ(stm, s);
                lhs = e;
            }
            // copy the data pointer
            stm = SEQ(stm, MOVE(MEM(lhs.exp(), lhs.offset()), d_rhs > 0 ? rhs : ADD(rhs.exp(), rhs.offset())));
            // fill in the sizes
            for (int i = 0; i < d_lhs; i++) {
                Type.Array b = Is(trhs, Type.Array.class);
                int o = (i+1) * wordSize;
                Expr nrhs = ConstValue(b.number);
                if (nrhs == null)
                    stm = SEQ(stm,
                              MOVE(MEM(lhs.exp(),
                                       ADD(lhs.offset(),
                                           CONST(o))),
                                   MEM(rhs.exp(),
                                       ADD(rhs.offset(),
                                           CONST(o)))));
                else
                    stm = SEQ(stm,
                              MOVE(MEM(lhs.exp(),
                                       ADD(lhs.offset(),
                                           CONST(o))),
                                   Compile(nrhs)));
                trhs = b.element;
            }
            // leave the result
            return MEM(ESEQ(stm, lhs.exp()), lhs.offset(), lhs.size());
        } else {
            assert d_lhs < d_rhs;
            // check some array bounds
            // don't build a smaller dope vector just reuse the existing one
            Type t = OpenType(tlhs);
            Label badSub = currentFrame.badSub();
            if (badSub != null)
                for (int i = d_lhs; i < d_rhs; i++) {
                    int o = (i+1) * wordSize;
                    Type.Array b = Is(t, Type.Array.class);
                    Expr nrhs = ConstValue(b.number);
                    assert nrhs != null;
                    Label ok = new Label();
                    stm = SEQ(stm,
                              BNE(MEM(rhs.exp(),
                                      ADD(rhs.offset(),
                                          CONST(o))),
                                  Compile(nrhs), badSub, ok),
                              LABEL(ok));
                    t = b.element;
                }
            // leave the old dope vector as the result
            if (d_lhs <= 0)
                return MEM(ESEQ(stm,
                                MEM(rhs.exp(),
                                    rhs.offset())),
                           CONST(0),
                           tlhs.size);
            return MEM(ESEQ(stm, rhs.exp()),
                       rhs.offset(),
                       (d_lhs + 1) * wordSize);
        }
    }
    Tree.Exp GenCopy(Type t, Tree.Exp exp) {
        Frame.Access a = currentFrame.allocLocal(t.size);
        Tree.Exp.MEM lhs = (Tree.Exp.MEM)a.exp(currentFrame.FP());
        Tree.Stm stm = null;
        if (Force(lhs) instanceof ESEQ(Stm s, MEM e)) {
            stm = SEQ(stm, s);
            lhs = e;
        }
        if (IsStructured(t)) {
            Tree.Exp.MEM rhs = (Tree.Exp.MEM)exp;
            return ESEQ(SEQ(stm,
                            EXP(CALL(target.external("memcpy"), ADD(lhs.exp(), lhs.offset()), ADD(rhs.exp(), rhs.offset()), CONST(t.size)))),
                        ADD(lhs.exp(), lhs.offset()));
        } else {
            return ESEQ(SEQ(stm,
                            MOVE(lhs, exp)),
                        ADD(lhs.exp(), lhs.offset()));
        }
    }
    Tree.Exp CompileAddress(Expr e) {
        Tree.Exp.MEM mem = (Tree.Exp.MEM)Compile(e);
        return ADD(mem.exp(), mem.offset());
    }
    Tree.Exp GenRef(Type t, Type r, Expr.Call ce) {
        if (IsOpenArray(r) != null)
            return GenOpenArray(r, ce);
        Type base = Base(r);
        if (Is(base, Type.Record.class) != null)
            return GenRecord(r);
        Temp temp = new Temp();
        return
            ESEQ(SEQ(MOVE(TEMP(temp),
                          CALL(target.external("malloc"), CONST(r.size))),
                     InitValue(MEM(TEMP(temp)), r)),
                 TEMP(temp));
    }
    Tree.Exp GenOpenArray(Type t, Expr.Call ce) {
        Tree.Stm stm = null;
        int n = OpenDepth(t);
        // compute the sizes
        Tree.Exp number = CONST(1);
        Temp[] sizes = new Temp[n];
        Label badSub = currentFrame.badSub();
        for (int i = 0; i < n; i++) {
            Label l = new Label();
            Temp size = sizes[i] = new Temp();
            Temp temp = new Temp();
            stm = SEQ(stm,
                      MOVE(TEMP(size), Compile(ce.args[i+1])),
                      badSub == null ? null : BLT(TEMP(size), CONST(0), badSub, l),
                      LABEL(l),
                      MOVE(TEMP(temp), MUL(TEMP(size), number)));
            number = TEMP(temp);
        }
        Temp size = new Temp();
        Temp result = new Temp();
        stm = SEQ(stm,
                  MOVE(TEMP(size), CONST((n+1) * wordSize)),
                  MOVE(TEMP(size), ADD(TEMP(size), MUL(number, CONST(OpenType(t).size)))),
                  MOVE(TEMP(result), CALL(target.external("malloc"), TEMP(size))),
                  MOVE(MEM(TEMP(result)), ADD(TEMP(result), CONST((n+1) * wordSize))));
        for (int i = 0; i < n; i++)
            stm = SEQ(stm,
                      MOVE(MEM(TEMP(result),
                               CONST((i+1) * wordSize)),
                           TEMP(sizes[i])));
        stm = SEQ(stm,
                  InitValue(MEM(TEMP(result),
                                CONST(0),
                                (n+1) * wordSize),
                            t));
        return ESEQ(stm, TEMP(result));
    }
    Tree.Exp GenRecord(Type t) {
        Type r = Base(t);
        Temp result = new Temp();
        return ESEQ(SEQ(MOVE(TEMP(result),
                             CALL(target.external("malloc"),
                                  CONST(t.size))),
                        InitValue(MEM(TEMP(result)), r)),
                    TEMP(result));
    }
    Tree.Exp GenObject(Type r) {
        Type.Object t = Is(r, Type.Object.class);
        Temp result = new Temp();
        Tree.Stm stm
            = SEQ(MOVE(TEMP(result),
                       CALL(target.external("malloc"),
                            CONST(wordSize + t.fieldOffset + t.fieldSize))),
                  MOVE(TEMP(result),
                       ADD(TEMP(result),
                           CONST(wordSize))),
                  MOVE(MEM(TEMP(result), CONST(-wordSize)),
                       NAME(Temp.getLabel(GlobalUID(t)))));
        while (t != null) {
            for (Value.Field f: t.fields)
                stm = SEQ(stm,
                          InitValue(MEM(TEMP(result),
                                        CONST(t.fieldOffset
                                              + f.offset)),
                                    f.type));
            t = Is(t.parent, Type.Object.class);
        }
        return ESEQ(stm, TEMP(result));
    }

    Tree.Exp Force(Tree.Exp.MEM a) {
        if (a.exp() instanceof Tree.Exp.TEMP) return null;
        else {
            Temp t = new Temp();
            return new Tree.Exp.ESEQ(MOVE(TEMP(t), a.exp()),
                                     MEM(TEMP(t), a.offset(), a.size()));
        }
    }

    Tree.Stm CompileBranch(Expr expr, final Label t, final Label f) {
        if (expr == null) return null;
        assert expr.checked;
        return switch (expr) {
        case Expr.Infix e -> {
            yield switch (e.op) {
            case EQ -> BEQ(Compile(e.left), Compile(e.right), t, f);
            case NE -> BNE(Compile(e.left), Compile(e.right), t, f);
            case GT -> BGT(Compile(e.left), Compile(e.right), t, f);
            case GE -> BGE(Compile(e.left), Compile(e.right), t, f);
            case LT -> BLT(Compile(e.left), Compile(e.right), t, f);
            case LE -> BLE(Compile(e.left), Compile(e.right), t, f);
            case Or ->  {
                Tree.Stm stm = null;

                var l = new Label();
                var left = CompileBranch(e.left, t, l);
                var right = CompileBranch(e.right, t, f);
                stm = SEQ(
                        left,
                        LABEL(l),
                        right
                );
                yield stm;
            }// TODO
            case And -> {
                Tree.Stm stm = null;

                var l = new Label();
                var left = CompileBranch(e.left, l, f);
                var right = CompileBranch(e.right, t, f);
                stm = SEQ(
                        left,
                        LABEL(l),
                        right
                );
                yield stm;

            } // TODO
            default -> { assert false; yield null; }
            };
        }
        case Expr.Prefix e -> {
            yield switch(e.op) {
            case Not -> {
                yield BEQ(Compile(e.expr), CONST(0), t, f);
            } // TODO
            default -> { assert false; yield null;
            }
            };
        }
        case Expr.Address e -> { assert false; yield null; }
        case Expr.Check e -> BNE(Compile(e), CONST(0), t, f);
        case Expr.Call e -> {
            yield switch (e.procType) {
            case Type.Proc.User v -> BEQ(Compile(e), CONST(0), f, t);
            case Type.Proc.First v -> BEQ(Compile(e), CONST(0), f, t);
            case Type.Proc.Last v -> BEQ(Compile(e), CONST(0), f, t);
            case Type.Proc.Val v -> BEQ(Compile(e), CONST(0), f, t);
            default -> { assert false; yield null; }
            };
        }
        case Expr.Qualify e -> BEQ(Compile(e), CONST(0), f, t);
        case Expr.Subscript e -> BEQ(Compile(e), CONST(0), f, t);
        case Expr.Named e -> BEQ(Compile(e), CONST(0), f, t);
        default -> { assert false; yield null; }
        };
    }

    final HashMap<String,Label> strings = new HashMap<String,Label>();
    Label stringLabel(String s) {
        Label l = strings.get(s);
        if (l != null) return l;
        l = new Label();
        strings.put(s, l);
        frags.add(new Frag.Data(target.string(l, s)));
        return l;
    }
    final HashMap<String,Label> texts = new HashMap<String,Label>();
    Label textLabel(String s) {
        Label l = texts.get(s);
        if (l != null) return l;
        Label payload = stringLabel(s);
        int length = s.getBytes().length;
        length += 1;               // nul ('\0') terminated
        l = new Label();
        texts.put(s, l);
        frags.add(new Frag.Data(target.dope(l, payload, length)));
        return l;
    }
}
