/* Copyright (C) 1997-2023, Antony L Hosking.
 * All rights reserved.  */

package mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import mojo.parse.*;
import static mojo.Absyn.*;

public class Semant {
    private static void usage() {
        throw new java.lang.Error("Usage: java mojo.Semant <source>.mj");
    }

    /**
     * The target word size (in bytes).
     */
    final int wordSize;

    /**
     * The target's minimum and maximum word values.
     */
    final BigInteger MIN_VALUE, MAX_VALUE;

    Semant(int wordSize) {
        this.wordSize = wordSize;
        if (wordSize == Integer.BYTES) {
            MIN_VALUE = BigInteger.valueOf(Integer.MIN_VALUE);
            MAX_VALUE = BigInteger.valueOf(Integer.MAX_VALUE);
        } else if (wordSize == Long.BYTES) {
            MIN_VALUE = BigInteger.valueOf(Long.MIN_VALUE);
            MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);
        } else {
            MIN_VALUE = null;
            MAX_VALUE = null;
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) usage();
        File file = new File(args[0]);
        Type.init();
        try {
            int wordSize = Integer.BYTES;
            Value.Unit unit = new Parser(wordSize, file).Unit();
            if (Error.nErrors() > 0) return;
            Semant semant = new Semant(wordSize);
            semant.Check(unit);
            semant.print(Scope.Top());
        } catch (FileNotFoundException |
                 ParseException |
                 TokenMgrError e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } finally {
            System.out.flush();
            System.err.flush();
        }
    }

    /**
     * Type-check values.
     */
    void Check(Value value) {
        Runnable visitor = () -> {
            switch (value) {
            case Value.EnumElt v -> {} // no checking needed
            case Value.Field v -> {
                // TODO: Check and set type
                // errors:
                //   - "empty field"
                //   - "fields may not be open arrays"
                v.type = Check(v.type);
                if(IsEmpty(v.type))
                    Error.ID(v.token, "empty field");
                if(IsOpenArray(v.type) != null)
                    Error.ID(v.token,"fields may not be open arrays");
            }
            case Value.Formal v -> {
                v.type = Check(TypeOf(v));
                switch(v.mode) {
                case VALUE -> {
                    if (IsOpenArray(v.type) != null) {
                        v.refType = new Type.Ref(null, v.type);
                        v.refType = Check(v.refType);
                    }
                }
                default -> {}
                }
            }
            case Value.Method v -> {
                // TODO:
                // - Check and set sig if non-null
                // - Check expr and resolve default if non-null
                // - Check value if non-null
                //   - Check value
                //   - if type Null set value null
                //   - else check value is a procedure
                //     errors:
                //       - "default is not a procedure"
                //       - "default is a nested procedure"
                //       - "default is incompatible with method type"

                if (v.sig != null)
                    v.sig = (Type.Proc) Check(v.sig);
                if (v.expr != null) {
                    Check(v.expr);
                    ResolveDefault(v);
                }
                if (v.value != null) {
                    Check(v.value);
                    // ?
                    if (v.expr.type == null)
                        v.value = null;
                    else {
                        if (Is(v.value, Value.Procedure.class) == null) {
                            Error.ID(v.token, "default is not a procedure");
                        } else {
                            if (IsNested((Value.Procedure) v.value))
                                Error.ID(v.token, "default is a nested procedure");
                            if (((Value.Procedure) v.value).sig.formals == null ||
                                    ((Value.Procedure) v.value).sig.formals.isEmpty() ||
                                !IsSubtype( v.parent,((Value.Procedure) v.value).sig.formals.get(0).type)){
                                Error.ID(v.token, "default is incompatible with method type");
                            }
                        }

                    }
                }
            }
            case Value.Procedure p -> {
                Check(p.sig);
                // NOTE: don't save the signature returned, otherwise
                // the formals will be reused by procedures with the
                // same signature.

                if (p.block == null) break;
                p.body = new ProcBody() {
                        public void accept(Consumer<Value> v) {
                            v.accept(p);
                        }
                    };

                // defer the rest to CheckBody
            }
            case Value.Variable v -> {
                v.proc = ProcBody.Top();
                v.type = Check(TypeOf(v));
                v.size = v.type.size;
                if (v.formal == null && IsOpenArray(v.type) != null)
                    Error.ID(v.token, "variable cannot be an open array");
                if (IsEmpty(v.type))
                    Error.ID(v.token, "variable has empty type");
                if (v.type != Type.Err.T && IsEqual(v.type, Type.Null.T, null))
                    Error.WarnID(v.token, "variable has type Null");
                if (Scope.OuterMost(v.scope)) v.global(true);
                v.checked(true); // allow recursions through the init expr
                if (!v.indirect() && !v.global()) {
                    if (v.formal != null && v.size > 8 * wordSize)
                        Error.WarnID(v.token, "large parameter passed by value");
                    else if (v.size > 8192)
                        Error.WarnID(v.token, "large local variable");
                } else if (v.formal != null && v.formal.refType != null)
                    Error.WarnID(v.token, "open array passed by value");
                if (IsStructured(v.type)) v.needsAddress(true);
                Check(v.formal);
                if (v.external()) {
                    if (v.init != null) {
                        Error.ID(v.token, "External variables cannot be initialized");
                        Check(v.init);
                        CheckAssign(v.token, v.type, v.init);
                    }
                } else if (v.init != null) {
                    Check(v.init);
                    CheckAssign(v.token, v.type, v.init);
                    Expr e = ConstValue(v.init);
                    if (e == null) {
                        if (Value.Unit.IsInterface()) {
                            Error.ID(v.token, "initial value is not a constant");
                        }
                        if (v.global() && v.size > 64 * wordSize) {
                            assert !v.indirect();
                            v.indirect(true);
                        }
                    } else {
                        // initialize the variable to an explicit constant
                        if (!v.indirect()) {
                            if (v.global()) {
                                //
                            } else if (IsStructured(v.type)) {
                                v.initStatic(true);
                            }
                        }
                        v.init = e;
                    }
                } else if (v.global()) {
                    // no explicit initialization is given but the var is global
                    Bounds t = GetBounds(v.type);
                    if (t.min.compareTo(t.max) <= 0)
                        // synthesize an initialization expression
                        v.init = new Expr.Int(v.token, t.min);
                    else
                        v.initDone(true);
                }
            }
            case Value.Const v -> {
                // TODO:
                // - Check expr
                // - Check and set type
                // - if nested procedure:
                //   - error: "nested procedures are not constants"
                // - if type is Err.T
                //   - error: "value is not a constant expression"
                // - else
                //   - check assignment of expr to type
                //   - compute constant from expr
                //   - if not constant
                //     - error: "value is not a constant"
                //   - else set expr to constant
                Check(v.expr);
                v.type = Check(v.expr.type);
                Value.Procedure p = IsProcedureLiteral(v.expr);
                if (IsNested(p))
                    Error.Msg(v.token, "nested procedures are not constants");
                if (v.type == Type.Err.T)
                    Error.ID(v.token, "value is not a constant expression");
                else {
                    CheckAssign(v.token, v.type, v.expr);
                    Expr e = ConstValue(v.expr);
                    if (e == null)
                        Error.ID(v.token, "value is not a constant");
                    else
                        v.expr = e;
                }
            }
            case Value.Unit t -> {
                Value.Unit save = Value.Unit.Switch(t);
                // open my private local scope
                t.localScope = Scope.PushNewModule(t.name);
                {
                    Scope.Insert(t.decls);
                    Check(t.localScope);
                    t.body = new ProcBody() {
                            public void accept(Consumer<Value> v) {
                                v.accept(t);
                            }
                        };
                    ProcBody.Push(t.body);
                    if (t.stmt != null)
                        Check(t.stmt, false, null, null);
                    ProcBody.Pop();
                }
                Scope.PopNew();
                Value.Unit.Switch(save);
            }
            case Value.Tipe v -> {
                // TODO:
                // - check and set value
                v.value = Check(v.value);
            }
            }
        };

        if (value == null) return;
        if (value.checked()) return;
        if (value.checkDepth >= 0) {
            // this node is currently being checked
            if (value.checkDepth == recursionDepth)
                IllegalRecursion(value);
            // this is a legal recursion, just return
            return;
        }
        // this node is not currently being checked
        try {
            value.checkDepth = recursionDepth;
            visitor.run();
        } finally {
            value.checkDepth = 0;
            value.checked(true);
        }
    }

    Type TypeOf(Value value) {
        Supplier<Type> visitor = () -> {
            return switch (value) {
            case Value.EnumElt v -> v.parent;
            case Value.Const v -> {
                // TODO:
                // - if type null set type to type of expr
                if (v.type == null)
                    v.type = v.expr.type;
                yield v.type;
            }
            case Value.Field v -> {
                // TODO:
                // - if type null set type to type of expr
                if(v.type == null)
                    v.type = v.expr.type;
                yield v.type;
            }
            case Value.Variable v -> {
                // TODO:
                // - If type null:
                //   - if init expr not null set type to type of expr
                //   - else if formal not null set type to type of formal
                //   - if type is null: error "variable has no type"
                if (v.type == null) {
                    if (v.init != null) {
                        v.type = TypeOf(v.init);
                    }
                    else if (v.formal != null)
                        v.type = v.formal.type;
                    if (v.type == null)
                        Error.ID(v.token, "variable has no type");
                }
                yield v.type;
            }
            case Value.Formal v -> v.type;
            case Value.Method v -> v.sig;
            case Value.Procedure v -> v.sig;
            case Value.Unit v -> null;
            case Value.Tipe v -> null;
            };
        };

        if (value == null) return Type.Err.T;
        if (inTypeOf.contains(value)) {
            IllegalRecursion(value);
            return Type.Err.T;
        }
        try {
            inTypeOf.add(value);
            return visitor.get();
        } finally {
            inTypeOf.remove(value);
        }
    }
    final HashSet<Node> inTypeOf = new HashSet<Node>();

    Expr ToExpr(Value value) {
        if (value == null) return null;
        if (inToExpr.contains(value)) {
            IllegalRecursion(value);
            return null;
        }
        try {
            inToExpr.add(value);
            return switch (value) {
            case Value.EnumElt v -> new Expr.Enum(v.token, v.parent, v.value);
            case Value.Procedure v -> new Expr.Proc(v);
            case Value.Const v -> v.expr;
            default -> null;
            };
        } finally {
            inToExpr.remove(value);
        }
    }
    final HashSet<Value> inToExpr = new HashSet<Value>();

    Type ToType(Value value) {
        if (value == null) return null;
        if (inToType.contains(value)) {
            IllegalRecursion(value);
            return null;
        }
        inToType.add(value);
        Type result = switch (value) {
        case Value.Tipe v -> v.value;
        default -> null;
        };
        inToType.remove(value);
        return result;
    }
    final HashSet<Value> inToType = new HashSet<Value>();

    /**
     * Report an illegal recursive declaration of a value
     * @param v the value in error
     */
    void IllegalRecursion(Value v) {
        if (v.error()) return;
        Error.ID(v.token, "illegal recursive declaration");
        v.error(true);
    }

    boolean IsExternal(Value v) { return v.external(); }
    boolean IsImported(Value v) { return v.imported(); }
    boolean IsWritable(Value v) { return !v.readonly(); }

    String GlobalName(Value v, boolean dots, boolean withModule) {
        return NameToPrefix(v, !dots, dots, withModule);
    }
    String GlobalName(Value v) {
        return GlobalName(v, true, true);
    }

    /**
     * Casting instance check
     */
    <R> R Is(Value v, Class<R> c) {
        if (c.isInstance(v)) return c.cast(v);
        return null;
    }

    boolean IsEqual(Value.EnumElt a, Value.EnumElt b) {
        return a.name.equals(b.name) && a.value == b.value;
    }
    boolean IsEqual(Value.Procedure a, Value.Procedure b) {
        return a == b;
    }

    /**
     * Returns "true" if the two scopes represented by "aa" and "ab"
     * have the same length and for each pair of values "a" and "b",
     * "IsEqual(a, b, x, types)" returns "true".  Otherwise, returns "false".
     */
    boolean IsFieldEqual(Scope aa, Scope bb, Assumption x, boolean types) {
        Iterator<Value> a = Scope.ToList(aa).iterator();
        Iterator<Value> b = Scope.ToList(bb).iterator();
        while (a.hasNext() && b.hasNext())
            if (!IsEqual((Value.Field)a.next(), (Value.Field)b.next(), x, types))
                return false;
        return !a.hasNext() && !b.hasNext();
    }

    /**
     * If "types" is "false", only the surface syntax (name & field index) are
     * checked.  Otherwise, the field types and default values are checked too.
     */
    boolean IsEqual(Value.Field a, Value.Field b, Assumption x, boolean types) {
        if (a == null || b == null || !a.name.equals(b.name)) return false;
        if (!types) return true;
        return IsEqual(TypeOf(a), TypeOf(b), x);
    }

    /**
     * Check that the actuals and formals match in call to a procedure.
     */
    boolean CheckArgs(
            Collection<Expr> actuals,
            Collection<Value> formals,
            Expr.Call ce) {
        Iterator<Expr> aa = actuals.iterator();
        Iterator<Value> ff = formals.iterator();
        boolean ok = true;

        while (aa.hasNext() && ff.hasNext()) {
            Expr actual = aa.next();
            Value.Formal formal = (Value.Formal)ff.next();
            if (actual != null && formal != null) {
                // we've got both a formal and an actual
                Check(actual);
                Type ta = TypeOf(actual);
                Type tf = TypeOf(formal);
                switch (formal.mode) {
                case VALUE -> {
                    if (!IsAssignable(tf, ta)) {
                        Error.Msg(actual.token, "incompatible types");
                        ok = false;
                    }
                }
                case VAR -> {
                    if (!IsDesignator(actual)) {
                        Error.Msg(actual.token, "var actual must be a designator");
                        ok = false;
                    } else if (!IsWritable(actual)) {
                        Error.Msg(actual.token, "var actual must be writable");
                        ok = false;
                    } else if (IsEqual(tf, ta, null)) {
                        NeedsAddress(actual);
                    } else if (Is(tf, Type.Array.class) != null && IsAssignable(tf, ta)) {
                        NeedsAddress(actual);
                    } else {
                        Error.Msg(actual.token, "incompatible types");
                        ok = false;
                    }
                }
                case READONLY -> {
                    if (!IsAssignable(tf, ta)) {
                        Error.Msg(actual.token, "incompatible types");
                        ok = false;
                    } else if (!IsDesignator(actual)) {
                        // we'll make a copy when it's generated
                    } else if (IsEqual(tf, ta, null)) {
                        NeedsAddress(actual);
                    } else {
                        // we'll make a copy when it's generated
                    }
                }
                }
            } else if (actual == null) {
                Error.Txt(ce.token, formal.token.image, "parameter not specified");
                ok = false;
            } else if (formal == null) {
                Error.Msg(actual.token, "too many actual parameters");
                ok = false;
            }
        }
        while (ff.hasNext()) {
            Value.Formal f = (Value.Formal)ff.next();
            Error.Txt(ce.token, f.token.image, "parameter not specified");
            ok = false;
        }
        while (aa.hasNext()) {
            Expr actual = aa.next();
            Error.Msg(actual.token, "too many actual parameters");
            Check(actual);
            ok = false;
        }
        return ok;
    }

    /**
     * Returns "true" if the two scopes represented by "aa" and "ab"
     * have the same length and for each pair of values "a" and "b",
     * "IsEqual(a, b, x, types)" returns "true".  Otherwise, returns "false".
     */
    boolean IsMethodEqual(Scope aa, Scope bb, Assumption x, boolean types) {
        Iterator<Value> a = Scope.ToList(aa).iterator();
        Iterator<Value> b = Scope.ToList(bb).iterator();
        while (a.hasNext() && b.hasNext())
            if (!IsEqual((Value.Method)a.next(), (Value.Method)b.next(), x, types))
                return false;
        return !a.hasNext() && !b.hasNext();
    }

    /**
     * If "types" is "false", only the surface syntax (name & method index) are
     * checked.  Otherwise, the method types and default values are checked too.
     */
    boolean IsEqual(Value.Method a, Value.Method b, Assumption x, boolean types) {
        if (a == null || b == null) return false;
        if (!a.name.equals(b.name)) return false;
        if (a.override != b.override) return false;
        if (!types) return true;
        // now we'll do the harder type-based checks
        ResolveDefault(a);
        ResolveDefault(b);
        return IsEqual(a.sig, b.sig, x) && a.value == b.value;
    }
    void ResolveDefault(Value.Method v) {
        if (v.value != null) return;
        if (v.expr == null) return;
        if ((v.value = IsProcedureLiteral(v.expr)) != null) return;
        Type t = TypeOf(v.expr);
        if (IsEqual(t, Type.Null.T, null)) return;
        if (Is(t, Type.Proc.class) == null)
            Error.ID(v.token, "default is not a procedure");
        else
            Error.ID(v.token, "default is not a procedure constant");
    }
    Value.Procedure IsProcedureLiteral(Expr e) {
        e = ConstValue(e);
        if (e == null) return null;
        Value v = null;
        if (e instanceof Expr.Named exp) v = Resolve(exp);
        if (e instanceof Expr.Qualify exp) v = Resolve(exp);
        if (e instanceof Expr.Proc exp) v = exp.proc;
        return Is(v, Value.Procedure.class);
    }

    void CheckBody(final Value.Procedure p) {
        if (p == null || p.block == null) return;
        ProcBody.Push(p.body);
        p.localScope = Scope.PushNewProc(p.name);
        Type result = Result(p.sig);
        // create a variable for the return result
        if (IsStructured(result)) {
            Value.Formal f
                = new Value.Formal(ID("_result_"), Value.Formal.Mode.VAR, result);
            Value.Variable v
                = new Value.Variable(f, IsOpenArray(result) != null);
            Scope.Insert(v);
            f.scope = v.scope;
            p.result = v;
        }
        // create local variables for each of the formals
        for (Value formal: Formals(p.sig)) {
            Value.Formal f = (Value.Formal)formal;
            Value.Variable v
                = new Value.Variable(f, IsOpenArray(f.type) != null);
            Scope.Insert(v);
            f.scope = v.scope; // identify names of formal and its local
        }
        p.checked(true);
        recursionDepth++;
        {
            Check(p.localScope);
            if (Check(p.block, true, result, null) == null && result != null)
                Error.WarnID(p.token, "function may not return a value");
        }
        recursionDepth--;
        Scope.PopNew();
        ProcBody.Pop();
    }
    boolean IsNested(Value.Procedure t) {
        return t != null && t.body != null && t.body.level != 0;
    }

    /**
     * Check type 't'. Return the underlying constructed type of 't' (i.e.,
     * strip renaming).
     */
    Type Check(Type type) {
        Runnable visitor = () -> {
            switch (type) {
            case Type.Enum t -> {
                if (t.scope == null) {
                    t.scope = Scope.PushNewClosed();
                    int i = 0;
                    for (Token id: t.elements)
                        Scope.Insert(new Value.EnumElt(id, BigInteger.valueOf(i++), t));
                    Scope.PopNew();
                }
                Check(t.scope);
                BigInteger max = BigInteger.valueOf(t.number - 1);
                if (max.compareTo(MAX_VALUE) > 0)
                    Error.Msg(t.token, "enumeration type too large");
                t.size = t.align = wordSize;
                t.bits = max.bitLength();
            }
            case Type.Err t -> { t.size = t.bits = 0; t.align = 1; }
            case Type.Int t -> { t.size = t.align = wordSize; t.bits = wordSize * 8; }
            case Type.Named t -> {
                Resolve(t);
                int nErrs = Error.nErrors();
                Check(t.value);
                if (nErrs == Error.nErrors())
                    // no errors yet
                    t.type = Check(t.type);
                else
                    t.type = Type.Err.T;
                t.size = t.type.size;
                t.align = t.type.align;
                t.bits = t.type.bits;
            }
            case Type.Object t -> {
                t.size = t.align = wordSize;
                t.bits = wordSize * 8;
                t.methodSize = t.methods.size() * wordSize;
                t.overrideSize = t.overrides.size() * wordSize;
                
                // TODO: build field scope and method scope
                //       (including overrides)

                t.fieldScope = Scope.PushNewClosed();
                for (Value.Field f: t.fields) {
                    Scope.Insert(f);
                }

                if (t.parent instanceof Type.Object) {
                    for (Value f : Scope.ToList(((Type.Object) t.parent).fieldScope)) {
                        Scope.Insert(f);
                    }
                }

                Scope.PopNew();

                t.methodScope= Scope.PushNewClosed();

                for (Value.Method m: t.methods) {
                    Scope.Insert(m);
                }

                for (Value.Method o: t.overrides) {
                    Scope.Insert(o);
                }



                Scope.PopNew();

                // TODO:
                // - if parent (supertype) not null
                //   - check and set parent
                //   - if parent is an object type
                //     - if parent == t
                //       - error: "illegal recursive supertype"
                //       - set parent to null
                //   - else
                //     - error: "super type must be an object type"
                //     - set parent to null
                // - check scopes have non-overlapping symbols
                //   - error: "field and method with the same name"

                if (t.parent != null) {
                    t.parent = Check(t.parent);
                    if (Is(t.parent, Type.Object.class) != null) {
                        if (t.parent == t) {
                            Error.ID(t.token, "illegal recursive supertype");
                            t.parent = null;
                        }
                    }
                    else {
                        Error.ID(t.token, "super type must be an object type");
                        t.parent = null;
                    }
                }
                for (Value m: t.methods) {
                    Value found = Scope.LookUp(t.fieldScope, m.name,true);
                    if (found != null){
                        Error.ID(found.token, "field and method with the same name");
                    }
                }

                recursionDepth++;
                {
                    t.checked(true);
                    // bind method overrides to their original declaration

                    // TODO: For each method in this type's method scope:
                    // - if method is an override
                    //   - lookup method declaration in parent
                    //   - if found then set overriding method's sig and offset
                    //     to those of overridden method
                    //   - else error: no method to override
                    // - else set and increment method offset
                    int i = 0;
                    for (Value m: Scope.ToList(t.methodScope)) {
                        if (!((Value.Method)m).override) {
                            ((Value.Method) m).offset = i;
                            i += t.size;
                        }  else {
                            // int index = ((Type.Object) t.parent).methods.lastIndexOf(m);
                            Value.Method v = null;
                            Type.Object parent = (Type.Object) t.parent;
                            while (v == null && parent != null) {
                                v= (Value.Method) (Scope.LookUp(parent.methodScope, m.name, false));
                                parent = (Type.Object) parent.parent;
                            }
                            if (v != null) {
//                                v.sig = ((Value.Method) m).sig;
//                                v.offset = ((Value.Method) m).offset;
                                ((Value.Method) m).sig = v.sig;
                                ((Value.Method) m).offset = v.offset;
                            } else {
                                Error.ID(m.token, "no method to override in supertype");
                            }
                        }
                    }


                    // check my fields and methods
                    Check(t.fieldScope);
                    Check(t.methodScope);
                }
                recursionDepth--;
                GetOffsets(t);
            }
            case Type.Array t -> {
                // TODO:
                // - check number
                // - check and set element type
                // - if number is null (open array) break
                // - if element type is open array
                //   - error: "fixed array element type cannot be an open array"
                // - compute constant n from number
                // - if n not constant
                //   - error: "fixed array length must be a constant integer expression"
                //   - break
                // - if n cannot be encoded in Integer.SIZE bits
                //   - error: "array type has too many elements"
                //   - assume n is 1
                // - if n > 0
                //   and element size > 0
                //   and n > Integer.MAX_VALUE / element size
                //   - error: "array type is too large"
                //     set size to 0
                // - else
                //   - set size to n * element size

                //   - set align to element align
                //   - set bits to size * 8


                Check(t.number);
                t.element = Check(t.element);
                if (t.number == null)
                    break;
                if (IsOpenArray(t.element) != null)
                    Error.ID(t.token, "fixed array element type cannot be an open array");

                var value = ConstValue(t.number);

                if (value == null || Is(value, Expr.Int.class) == null) {
                    Error.ID(t.token, "fixed array length must be a constant integer expression");
                    break;
                }

                var n = ((Expr.Int) value).value;

                if (n.compareTo(MAX_VALUE) > 0)
                    Error.ID(t.token, "array type has too many elements");

                if (n.compareTo(BigInteger.valueOf(0)) < 0) {
                    Error.ID(t.token, "fixed array length must be non-negative");
                }

                if (n.compareTo(new BigInteger(String.valueOf(0))) > 0  &&
                    t.element.size > 0  &&
                    n.compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE / t.element.size))) > 0) {
                        Error.ID(t.number.token, "array type is too large");
                        t.size = 0;
                }
                else {
                    t.size = n.intValue() * t.element.size;
                    t.align = t.element.align;
                    t.bits = t.size * 8;
                }




            }
            case Type.Proc t -> {
                // TODO:
                // - push and set scope
                // - insert formals
                // - pop scope
                // - recursively (inc/dec recursionDepth)
                //   - set size to wordSize
                //   - mark t checked
                //   - check scope
                //   - if result not null
                //     - check and set result
                //     - if result is open array
                //       - error: "procedures may not return open arrays"
                t.scope = Scope.PushNewClosed();
                for (Value.Formal f: t.formals) {
                    Scope.Insert(f);
                }
                Scope.PopNew();

                recursionDepth++;
                {
                    t.size = wordSize;
                    t.checked(true);
                    Check(t.scope);
                    if (t.result != null) {
                        t.result = Check(t.result);
                        if (IsOpenArray(t.result) != null)
                            Error.ID(t.token, "procedures may not return open arrays");
                    }
                }
                recursionDepth--;

            }
            case Type.Record t -> {
                // TODO:
                // - push and set field scope
                // - insert fields
                // - pop scope
                // - for each field
                //   - check field
                //   - set offset
                //   - add field type size to record size
                // - if size cannot be encoded in Integer.SIZE bits
                //   - error: "record type is too large"
                // - else set size
                t.fieldScope = Scope.PushNewClosed();
                int i = 0;
                int size = 0;
                for (Value.Field f : t.fields) {
                    Scope.Insert(f);
                }
                Scope.PopNew();

                for (Value.Field f: t.fields) {
                    Check(f);
//                    if ( f.type.align != 0 && i % f.type.align != 0)
//                        i = RoundUp(i, f.type.align);
                    f.offset = i;

                    i += f.type.size;
                    size = i;
                }

                SizeAndAlign sa = SizeAndAlign(t, t.fieldScope);
                t.align = sa.align();

                if (BigInteger.valueOf(size).compareTo(MAX_VALUE) > 0)
                    Error.Msg(t.token, "record type is too large");
                else
                    t.size = size;



            }
            case Type.Ref t -> {
                t.size = t.align = wordSize;
                t.bits = wordSize * 8;
                recursionDepth++;
                {
                    t.checked(true);
                    if (t.target != null) t.target = Check(t.target);
                }
                recursionDepth--;
            }
            case Type.Subrange t -> {
                Seal(t);
                Check(t.minExp);
                Check(t.maxExp);
                t.base = Check(t.base);
            }
            }
        };
        if (type == null) return Type.Err.T;
        if (!type.checked()) {
            if (type.checkDepth == recursionDepth) {
                IllegalRecursion(type);
            } else {
                int saveDepth = type.checkDepth;
                type.checkDepth = recursionDepth;
                visitor.run();
                type.checkDepth = saveDepth;
                type.checked(true);
            }
        }
        return Strip(type);
    }
    int recursionDepth = 0;
    // incremented/decremented every time the type checker enters/leaves one of
    // the types that's allowed to introduce recursions

    record SizeAndAlign(int size, int align){};
    SizeAndAlign SizeAndAlign(Type t, Scope fields) {
        // compute the size of the record
        int curSize, newSize = 0; // total size of the record
        int newAlign = wordSize; // minimum allowed alignment
        boolean overflow = false;

        // check the fields and set their offsets
        for (Value v: Scope.ToList(fields)) {
            Value.Field f = (Value.Field)v;
            Check(f);
            int fieldSize = f.type.size;
            int fieldAlign = f.type.align;
            if (fieldAlign > newAlign) newAlign = fieldAlign;
            curSize = newSize;
            newSize = RoundUp(curSize, fieldAlign);
            f.offset = newSize;
            newSize += fieldSize;
            if (newSize < 0) overflow = true;
        }

        curSize = newSize;
        newSize = RoundUp(curSize, newAlign);

        if (overflow) {
            Error.Msg(t.token, "record or object type is too large");
            newSize = 0;
        }

        return new SizeAndAlign(newSize, newAlign);
    }
    int RoundUp(int size, int alignment) {
        if (alignment == 0) return size;
        else return ((size + alignment - 1) / alignment) * alignment;
    }

    void Seal(Type.Subrange t) {
        if (t.base != null) return;
        Expr min = ConstValue(t.minExp);
        Type tmin;
        if (min == null) {
            Error.Msg(t.token, "subrange lower bound is not constant");
            t.min = BigInteger.ZERO;
            tmin = Type.Int.T;
        } else {
            if (min instanceof Expr.Int e) {
                t.min = e.value;
                tmin = e.type;
            } else if (min instanceof Expr.Enum e) {
                t.min = e.value;
                tmin = e.type;
            } else {
                Error.Msg(t.token, "subrange lower bound is not an ordinal value");
                t.min = BigInteger.ZERO;
                tmin = Type.Int.T;
            }
        }
        Expr max = ConstValue(t.maxExp);
        Type tmax;
        if (max == null) {
            Error.Msg(t.token, "subrange upper bound is not constant");
            t.max = t.min;
            tmax = tmin;
        } else {
            if (max instanceof Expr.Int e) {
                t.max = e.value;
                tmax = e.type;
            } else if (max instanceof Expr.Enum e) {
                t.max = e.value;
                tmax = e.type;
            } else {
                Error.Msg(t.token, "subrange upper bound is not an ordinal value");
                t.max = t.min;
                tmax = tmin;
            }
        }
        t.base = tmin;
        if (!IsEqual(tmin, tmax, null))
            Error.Msg(t.token, "subrange endpoints must be of same type");
        if (t.max.compareTo(t.min) < 0) {
            t.min = BigInteger.ZERO;
            t.max = BigInteger.valueOf(-1);
            t.size = t.align = wordSize;
            t.bits = wordSize * 8;
        } else {
            int bits;
            if (t.min.compareTo(BigInteger.ZERO) >= 0) // unsigned
                t.bits = bits = t.max.bitLength();
            else // signed
                t.bits = bits = Math.max(t.min.bitLength(), t.max.bitLength()) + 1;
            if      (bits <=  8) t.size = t.align = 1;
            else if (bits <= 16) t.size = t.align = 2;
            else if (bits <= 32) t.size = t.align = 4;
            else if (bits <= 64) t.size = t.align = 8;
            else assert false;
            assert t.size <= wordSize;
        }
    }

    /**
     * Return the constructed type of 't' (i.e., strip renaming).
     */
    Type Strip(Type t) {
        Type u = t, v = t;
        if (u == null) return null;
        for (;;) {
            if (u instanceof Type.Named x) u = Strip(x); else return u;
            if (v instanceof Type.Named y) v = Strip(y); else return v;
            if (v instanceof Type.Named z) v = Strip(z); else return v;
            if (u == v) {
                IllegalRecursion(t);
                return Type.Err.T;
            }
        }
    }
    Type Strip(Type.Named t) {
        Resolve(t);
        return t.type;
    }
    void Resolve(Type.Named p) {
        if (p.type != null) return;
        if ((p.value = Scope.LookUp(Scope.Top(), p.name, false)) == null) {
            Error.Txt(p.token, p.name, "undefined");
            p.type = Type.Err.T;
        } else if ((p.type = ToType(p.value)) != null) {
            // ok
        } else {
            Error.Txt(p.token, p.name, "name isn't bound to a type");
            p.type = Type.Err.T;
        }
    }

    void IllegalRecursion(Type t) {
        if (t.errored()) return;
        if (t instanceof Type.Named n) {
            if (n.value != null)
                IllegalRecursion(n.value);
            else
                Error.Txt(n.token, n.name, "illegal recursive type declaration");
        } else
            Error.Msg(t.token, "illegal recursive type decalaration");
        t.errored(true);
    }

    /**
     * Return the base type of 't' (strip renaming, packing & subranges)
     */
    Type Base(Type t) {
        Type u = t;
        Type v = t;
        if (u == null) return null;
        for (;;) {
            if (u instanceof Type.Named x) u = Strip(x);
            else if (u instanceof Type.Subrange x) u = Base(x);
            else return u;
            if (v instanceof Type.Named x) v = Strip(x);
            else if (v instanceof Type.Subrange x) v = Base(x);
            else return v;
            if (v instanceof Type.Named x) v = Strip(x);
            else if (v instanceof Type.Subrange x) v = Base(x);
            else return v;
            if (u == v) {
                IllegalRecursion(t);
                return Type.Err.T;
            }
        }
    }
    Type Base(Type.Subrange t) {
        Seal(t);
        if (t.base != null) return t.base;
        return TypeOf(t.minExp);
    }

    /**
     * Used to terminate recursion in type equivalence.
     */
    class Assumption {
        final Assumption prev;
        final Type a, b;

        Assumption(Assumption prev, Type a, Type b) {
            this.prev = prev;
            this.a = a;
            this.b = b;
        }
    }

    /**
     * Return true iff (a == b) given the assumptions x.
     */
    boolean IsEqual(Type ta, Type tb, Assumption x) {
        if (ta == null || tb == null) return false;
        if (ta == tb) return true;
        if (ta instanceof Type.Named) ta = Strip(ta);
        if (tb instanceof Type.Named) tb = Strip(tb);
        if (ta == tb) return true;
        if (ta == Type.Err.T || tb == Type.Err.T) return true;
        if (ta.getClass() != tb.getClass()) return false;
        for (Assumption y = x; y != null; y = y.prev) {
            if (y.a == ta) {
                if (y.b == tb) return true;
            } else if (y.a == tb) {
                if (y.b == ta) return true;
            }
        }
        Assumption xx = new Assumption(x, ta, tb);
        return switch (xx.a) {
        case Type.Enum a -> {
            Type.Enum b = (Type.Enum)xx.b;
            if (a.scope == null)
                yield b.scope == null;
            if (b.scope == null)
                yield a.scope == null;
            Iterator<Value> va = Scope.ToList(a.scope).iterator();
            Iterator<Value> vb = Scope.ToList(b.scope).iterator();
            while (va.hasNext() && vb.hasNext())
                if (!IsEqual(Is(va.next(), Value.EnumElt.class),
                             Is(vb.next(), Value.EnumElt.class)))
                    yield false;
            if (va.hasNext() || vb.hasNext())
                yield false;
            yield true;
        }
        case Type.Err a -> {
            assert a != xx.b;
            yield false;
        }
        case Type.Int a -> {
            assert a != xx.b;
            yield false;
        }
        case Type.Named a -> {
            assert a != xx.b;
            yield false;
        }
        case Type.Object a -> {
            Type.Object b = (Type.Object)xx.b;
            // check the field names and offsets
            if (!IsFieldEqual(a.fieldScope, b.fieldScope, xx, false))
                yield false;
            // check the method names and offsets
            if (!IsMethodEqual(a.methodScope, b.methodScope, xx, false))
                yield false;
            // check the super types
            if (!IsEqual(a.parent, b.parent, xx))
                yield false;
            // check the field types and default values
            if (!IsFieldEqual(a.fieldScope, b.fieldScope, xx, true))
                yield false;
            // check the method types and defaults
            if (!IsMethodEqual(a.methodScope, b.methodScope, xx, true))
                yield false;
            yield true;
        }
        case Type.Array a -> {
            Type.Array b = (Type.Array)xx.b;
            if (!IsEqual(a.element, b.element, xx)) yield false;
            if (OpenDepth(a) != OpenDepth(b)) yield false;
            yield IsEqual(ConstValue(a.number), ConstValue(b.number), xx);
        }
        case Type.Proc a -> {
            Type.Proc b = (Type.Proc)xx.b;
            if (a.result == null && b.result == null) {
                // ok
            } else if (!IsEqual(a.result, b.result, xx))
                yield false;
            yield FormalsMatch(a.scope, b.scope, true, true, xx);
        }
        case Type.Record a -> {
            Type.Record b = (Type.Record)xx.b;
            // check the field names and offsets
            if (!IsFieldEqual(a.fieldScope, b.fieldScope, xx, false))
                yield false;
            // check the field types and default values
            if (!IsFieldEqual(a.fieldScope, b.fieldScope, xx, true))
                yield false;
            yield true;
        }
        case Type.Ref a -> {
            Type.Ref b = (Type.Ref) xx.b;
            if (a.target == null && b.target == null)
                yield a == b;
            yield IsEqual(a.target, b.target, xx);
        }
        case Type.Subrange a -> {
            Type.Subrange b = (Type.Subrange) xx.b;
            Seal(a); Seal(b);
            yield a.min.compareTo(b.min) == 0
                && a.max.compareTo(b.max) == 0
                && IsEqual(a.base, b.base, x);
        }
        };
    }

    /**
     * Return true iff (value(a) == value(b)) assuming a constant global store
     * and the type equalities represented by 'x'.
     */
    boolean IsEqual(Expr ea, Expr eb, Assumption x) {
        if (ea == eb) return true;
        if (ea == null || eb == null) return false;
        return switch (ea) {
        case Expr.Prefix a -> {
            yield eb instanceof Expr.Prefix b
                && a.op == b.op
                && IsEqual(a.expr, b.expr, x);
        }
        case Expr.Infix a -> {
            yield eb instanceof Expr.Infix b
                && a.op == b.op
                && IsEqual(a.left, b.left, x)
                && IsEqual(a.right, b.right, x);
        }
        case Expr.Deref a -> {
            yield eb instanceof Expr.Deref b
                && IsEqual(a.expr, b.expr, x);
        }
        case Expr.Subscript a -> {
            yield eb instanceof Expr.Subscript b
                && IsEqual(a.expr, b.expr, x)
                && IsEqual(a.index, b.index, x);
        }
        case Expr.Address a -> {
            yield eb instanceof Expr.Address b
                && a.value.equals(b.value);
        }
        case Expr.Enum a -> {
            yield eb instanceof Expr.Enum b
                && a.value == b.value;
        }
        case Expr.Int a -> {
            yield eb instanceof Expr.Int b
                && a.value.equals(b.value);
        }
        case Expr.Call a -> false;
        case Expr.Check a -> {
            yield eb instanceof Expr.Check b
                && a.min.equals(b.min)
                && a.max.equals(b.max)
                && IsEqual(a.expr, b.expr, x);
        }
        case Expr.Method a -> {
            yield eb instanceof Expr.Method b
                && a.name.equals(b.name)
                && IsEqual(a.object, b.object, x);
        }
        case Expr.Named a -> {
            yield eb instanceof Expr.Named b
                && Resolve(a) == Resolve(b);
        }
        case Expr.Proc a -> {
            yield eb instanceof Expr.Proc b
                && IsEqual(Is(a.proc, Value.Procedure.class),
                           Is(b.proc, Value.Procedure.class));
        }
        case Expr.Qualify a -> {
            yield eb instanceof Expr.Qualify b
                && Resolve(a) == Resolve(b)
                && TypeOf(a) == TypeOf(b)
                && IsEqual(a.expr, b.expr, x);
        }
        case Expr.Text a -> {
            yield eb instanceof Expr.Text b
                && Objects.equals(a.value, b.value);
        }
        case Expr.TypeExpr a -> {
            yield eb instanceof Expr.TypeExpr b
                && IsEqual(a.value, b.value, x);
        }
        case Expr.Cast a -> {
            yield eb instanceof Expr.Cast b
                && IsEqual(a.tipe, b.tipe, x)
                && IsEqual(a.expr, b.expr, x);
        }
        };
    }

    /**
     * Returns true iff (a <: b).
     */
    boolean IsSubtype(Type ta, Type tb) {
        if (ta == null || tb == null) return false;
        if (ta == tb) return true;
        if (ta instanceof Type.Named) ta = Strip(ta);
        if (tb instanceof Type.Named) tb = Strip(tb);
        if (ta == tb) return true;
        if (ta == Type.Err.T || tb == Type.Err.T) return true;
        if (IsEqual(ta, tb, null)) return true;
        // I give up, call the methods.
        return switch (ta) {
        case Type.Subrange a -> {
            Seal(a);
            if (!IsEqual(Base(a.base), Base(tb), null)) yield false;
            Type.Subrange b = Is(tb, Type.Subrange.class);
            if (b == null) yield true;
            if (a.max.compareTo(a.min) < 0) yield true; /* a is empty */
            if (a.min.compareTo(b.min) < 0) yield false;
            if (a.max.compareTo(b.max) > 0) yield false;
            yield true;
        }
        case Type.Object a -> {
            // TODO:
            // - return true if a <: b

            Type.Object parent = (Type.Object) a.parent;
            while (parent != null) {
                if (parent == tb)
                    yield true;
                parent = (Type.Object) parent.parent;
            }


            yield false;
        }
        case Type.Array a -> {
            // TODO: An array type A is a subtype of an array type
            // B if they have the same ultimate element type, the
            // same number of dimensions, and, for each dimension,
            // either both are open (for the first m), or A is
            // fixed and B is open (for the next n dimensions),
            // or they are both fixed and have the same size (for
            // the last p dimensions).
            // 1. peel off common open dimensions
            // 2. peel off remaining fixed dimensions of A and open dimensions of B
            // 3. peel off fixed dimensions as long as number of elements are equal
            // 4. let ta and tb be element types of last dimensions


            if (ta.tipe.value != tb.tipe.value)
                yield false;
            var ta_tmp = ((Type.Array) ta).element;
            var tb_tmp = ((Type.Array) tb).element;

            while (ta_tmp != null || tb_tmp != null) {
                if (ta_tmp == null || tb_tmp == null)
                    yield false;
                if (ta_tmp.tipe.value != tb_tmp.tipe.value)
                    yield false;
                if (IsOpenArray(ta_tmp) == null && IsOpenArray(tb_tmp) == null) {
                    if (ta.size != tb.size)
                        yield false;
                } else if (IsOpenArray(ta_tmp) == null && IsOpenArray(tb_tmp) != null) {
                    // ok
                } else if (IsOpenArray(ta_tmp) != null && IsOpenArray(tb_tmp) != null) {
                    // ok
                } else {
                    yield false;
                }

                if (Is(ta_tmp, Type.Array.class) == null || Is(tb_tmp, Type.Array.class) == null)
                    yield false;

                ta_tmp = ((Type.Array) ta_tmp).element;
                tb_tmp = ((Type.Array) tb_tmp).element;
            }


            yield true;
        }
        case Type.Proc a -> {
            // TODO: A procedure type T is a subtype of another
            // procedure type U if they are the same except for
            // parameter names (see FormalsMatch).
            yield FormalsMatch(((Type.Proc) ta).scope, ((Type.Proc) tb).scope, false, false, null);
//            yield IsEqual(ta, tb, null);
        }
        case Type.Ref a -> {
            // TODO: Null <: ref T <: Refany That is, Refany contains
            // all references, and null is a member of every reference
            // type.
            if (ta == Type.Null.T)
                yield true;
            if (tb == Type.Refany.T)
                yield true;
            yield IsEqual(ta, tb, null);
        }
        default -> false;
        };
    }

    /**
     * Returns true iff (a := b) type-checks.
     */
    boolean IsAssignable(Type a, Type b) {
        if (IsEqual(a, b, null) || IsSubtype(b, a)) return true;
        if (IsOrdinal(a)) {
            // ordinal types: OK if there is a common supertype
            // and they have at least one member in common
            Type aBase = Base(a), bBase = Base(b);
            Bounds aBounds = GetBounds(a), bBounds = GetBounds(b);
            if (IsEqual(aBase, bBase, null)
                || IsEqual(aBase, Type.Char.T, null) && IsEqual(bBase, Type.Char.T, null)) {
                // check for a non-empty intersection
                BigInteger min = aBounds.min.max(bBounds.min), max = aBounds.max.min(bBounds.max);
                return min.compareTo(max) <= 0;
            }
        }
        return false;
    }

    /**
     * Returns true if the type 't' is an ordinal (integer, enumeration, subrange)
     */
    boolean IsOrdinal(Type t) {
        return switch (Check(t)) {
        case Type.Int u -> true;
        case Type.Subrange u -> true;
        case Type.Enum u -> true;
        case Type.Err u -> true;
        default -> false;
        };
    }

    /**
     * @return the number of values of the type 't'
     */
    BigInteger Number(Type type) {
        Type u = Check(type);
        return switch(u) {
        case Type.Enum t -> BigInteger.valueOf(t.number);
        case Type.Err t -> BigInteger.ZERO;
        default -> {
            BigInteger min, max;
            switch (u) {
            case Type.Subrange t -> {
                min = t.min;
                max = t.max;
            }
            case Type.Int t -> {
                min = MIN_VALUE;
                max = MAX_VALUE;
            }
            default -> {
                assert false;
                yield MAX_VALUE;
            }
            }
            BigInteger n = max.subtract(min).add(BigInteger.ONE);
            if (n.compareTo(MIN_VALUE) >= 0 && n.compareTo(MAX_VALUE) <= 0)
                yield n;
            Error.Msg(type.token, "type has too many elements");
            yield MAX_VALUE;
        }
        };
    }

    Bounds GetBounds(Type type) {
        return switch (Check(type)) {
        case Type.Subrange t -> new Bounds(t.min, t.max);
        case Type.Enum t -> {
            BigInteger min = BigInteger.ZERO;
            BigInteger max = BigInteger.valueOf(t.number - 1);
            yield new Bounds(min, max);
        }
        case Type.Int t -> new Bounds(MIN_VALUE, MAX_VALUE);
        default -> new Bounds(BigInteger.ZERO, BigInteger.valueOf(-1));
        };
    }

    boolean IsEmpty(Type type) {
        Type u = Check(type);
        return switch (u) {
        case Type.Enum t -> t.number <= 0;
        case Type.Subrange t -> t.max.compareTo(t.min) < 0;
        case Type.Array t -> IsEmpty(t.element);
        case Type.Record t -> {
            for (Value.Field f: t.fields)
                if (IsEmpty(f.type)) yield true;
            yield false;
        }
        default -> false;
        };
    }

    int count = 0;
    String GlobalUID(Type t) {
        if (t == null) return null;
        if (t.tipe != null) return GlobalName(t.tipe, true, true);
        if (t.uid == null) t.uid = "_T" + count++;
        return t.uid;
    }

    /**
     * Returns true iff the type 't' is a record, set, or array.
     */
    boolean IsStructured(Type type) {
        if (type == null) return false;
        return switch (type) {
        case Type.Array t -> true;
        case Type.Record t -> true;
        default -> false;
        };
    }

    <R extends Type> R Reduce(Type t, Class<R> c) {
        if (t == null) return null;
        if (t instanceof Type.Named) t = Strip(t);
        if (c.isInstance(t)) return c.cast(t);
        return null;
    }
    <R extends Type> R Is(Type t, Class<R> c) {
        return Reduce(t, c);
    }

    Type.Array IsOpenArray(Type t) {
        Type.Array a = Reduce(t, Type.Array.class);
        if (a == null || a.number != null) return null;
        return a;
    }
    int OpenDepth(Type t) {
        Type.Array a = Reduce(t, Type.Array.class);
        if (a == null || a.number != null ) return 0;
        return 1 + OpenDepth(a.element);
    }
    Type OpenType(Type t) {
        Type.Array a = Reduce(t, Type.Array.class);
        if (a == null || a.number != null) return t;
        return OpenType(a.element);
    }

    Value LookUp(Type.Enum p, String name) {
        if (p == null) return null;
        return Scope.LookUp(p.scope, name, true);
    }

    Value LookUp(Type.Record p, String name) {
        if (p == null) return null;
        return Scope.LookUp(p.fieldScope, name, true);
    }

    /*
     * Look up field or method with name in object type p.
     */
    Value LookUp(Type.Object p, String name) {
        Type t = p;
        for (;;) {
            t = Check(t);
            if (t == Type.Err.T) return null;
            if ((p = Is(t, Type.Object.class)) == null) return null;
            // found an object type => try it!
            if (Scope.LookUp(p.methodScope, name, true)
                instanceof Value.Method m)
                // find the first non-override declaration for this method
                return PrimaryMethodDeclaration(p, m);
            // try for a field
            if (Scope.LookUp(p.fieldScope, name, true)
                instanceof Value.Field f)
                return f;
            t = p.parent;
        }
    }

    Value.Method PrimaryMethodDeclaration(Type.Object p, Value.Method m) {
        if (p == null) return null;
        if (!m.override) {
            assert m.parent == p;
            return m;
        }
        if (inPrimLookUp.contains(p)) {
            Error.Msg(p.token, "illegal recursive supertype");
            return null;
        }
        inPrimLookUp.add(p);
        m = Is(LookUp(Is(p.parent, Type.Object.class), m.name),
               Value.Method.class);
        inPrimLookUp.remove(p);
        return m;
    }
    final HashSet<Type.Object> inPrimLookUp = new HashSet<Type.Object>();

    void GetSizes(Type.Object t) {
        if (t.fieldSize >= 0) return;
        if (t.parent == null) {
            t.fieldSize = 0;
            t.fieldAlign = wordSize;
        } else {
            SizeAndAlign sa = SizeAndAlign(t, t.fieldScope);
            t.fieldSize = RoundUp(sa.size(), wordSize);
            t.fieldAlign = sa.align();
        }
    }

    void GetOffsets(Type.Object t) {
        if (t.fieldOffset >= 0) return;
        GetSizes(t);
        if (t.parent == null) {
            t.fieldOffset = 0;
            t.methodOffset = 0;
        } else {
            Type.Object parent = Is(t.parent, Type.Object.class);
            GetOffsets(parent);
            if (parent.fieldOffset >= 0) {
                t.fieldOffset = parent.fieldOffset + parent.fieldSize;
                t.fieldOffset = RoundUp(t.fieldOffset, t.fieldAlign);
                t.methodOffset = parent.methodOffset + parent.methodSize;
            }
        }
    }

    /**
     * Convert a method signature into a procedure signature
     * @param method the method signature to convert
     * @param objType the object type
     * @return the resulting procedure type
     */
    Type MethodSigAsProcSig(Type.Proc method, Type objType) {
        if (method == null) return Type.Err.T;
        Type.Proc proc = new Type.Proc.User(method.token, null, method.result);
        // insert the "self" formal
        proc.formals.add(new Value.Formal(ID("_self_"), Value.Formal.Mode.VALUE, objType));
        // copy the remaining formals
        for (Value formal: Formals(method)) {
            Value.Formal f = (Value.Formal)formal;
            proc.formals.add(new Value.Formal(f.token, f.mode, f.type));
        }
        return proc;
    }

    boolean IsCompatible(Type.Proc proc, Type objectType, Type.Proc method) {
        if (proc == null || method == null) return false;
        if (Formals(proc).size() != Formals(method).size() + 1) return false;
        if (proc.result == null && method.result == null) {
            // ok
        } else if (IsEqual(proc.result, method.result, null)) {
            // ok
        } else return false;
        if (!FirstArgOK(proc.scope, objectType)) return false;
        if (!FormalsMatch(proc.scope, method.scope, false, false, null)) return false;
        return true;
    }
    boolean FirstArgOK(Scope s, Type t) {
        for (Value v: Scope.ToList(s)) {
            Value.Formal formal = Is(v, Value.Formal.class);
            switch (formal.mode) {
            case VALUE:
                return IsSubtype(t, formal.type);
            default:
                return false;
            }
        }
        return false;
    }
    boolean FormalsMatch(Scope a, Scope b, boolean strict, boolean useFirst, Assumption x) {
        Iterator<Value> va = Scope.ToList(a).iterator();
        Iterator<Value> vb = Scope.ToList(b).iterator();
        if (!useFirst) {
            if (!va.hasNext()) return false;
            va.next();
        }
        while (va.hasNext() && vb.hasNext()) {
            Value.Formal ia = Is(va.next(), Value.Formal.class);
            Value.Formal ib = Is(vb.next(), Value.Formal.class);
            if (ia.mode != ib.mode) return false;
            if (!IsEqual(TypeOf(ia), TypeOf(ib), x)) return false;
            if (strict)
                if (!ia.name.equals(ib.name)) return false;
        }
        return (!va.hasNext() && !vb.hasNext());
    }
    Type Result(Type.Proc p) {
        p = Reduce(p, Type.Proc.class);
        if (p == null) return Type.Err.T;
        return p.result;
    }
    Collection<Value> Formals(Type.Proc p) {
        if (p == null) return Collections.emptyList();
        return Scope.ToList(p.scope);
    }

    /**
     * Type-check statements.
     */
    Type Check(Stmt stmt, boolean returnOK, Type returnType, Stmt loop) {
        return switch (stmt) {
        case Stmt.Assign s -> {
            // TODO:
            // - check lhs and rhs
            // - if lhs is not a designator
            //   error: "left-hand side is not a designator"
            // - else if lhs is not writable
            //   error: "left-hand side is read-only"
            // - check assignment
            Check(s.lhs);
            Check(s.rhs);

            if (!IsDesignator(s.lhs))
                Error.ID(s.token, "left-hand side is not a designator");
            if (!IsWritable(s.lhs))
                Error.ID(s.token, "left-hand side is read-only");

            CheckAssign(s.token, s.lhs.type, s.rhs);

            yield null;
        }
        case Stmt.Call s -> {
            Check(s.expr);
            yield null;
        }
        case Stmt.Break s -> {
            if (loop == null)
                Error.Msg(s.token, "break not contained in a loop");
            yield null;
        }
        case Stmt.For s -> {
            Check(s.from);
            Check(s.to);
            Type tFrom = Base(TypeOf(s.from));
            Type tTo = Base(TypeOf(s.to));
            if (tFrom == Type.Err.T || tTo == Type.Err.T) {
                //  already an error
                tFrom = Type.Err.T;
                tTo = Type.Err.T;
            } else if (Is(tFrom, Type.Enum.class) != null) {
                if (!IsEqual(tFrom, tTo, null))
                    Error.Msg(s.token, "'from' and 'to' expressions are incompatible");
            } else if (tFrom == Type.Int.T && tTo == Type.Int.T) {
                // ok
            } else
                Error.Msg(s.token, "'from' and 'to' expressions must be compatible ordinals");
            Check(s.by);
            if (!IsSubtype(TypeOf(s.by), Type.Int.T))
                Error.Msg(s.token, "'by' expression must be an integer");
            // set the type of the control variable
            s.var = new Value.Variable(s, tFrom);
            s.scope = Scope.PushNewOpen();
            Scope.Insert(s.var);
            Check(s.var);
            Check(s.stmt, returnOK, returnType, s);
            Scope.PopNew();
            yield null;
        }
        case Stmt.If s -> {
            Type t = Type.Err.T;
            // TODO:
            // - check expr
            // - if type of expr not boolean
            //   - error: "if condition must be a boolean"
            // - check thenStmt
            // - if elseStmt not null check elseStmt
            // - yield null if either checks null otherwise Type.Err.T

            Check(s.expr);
            if (s.expr.type != Type.Bool.T) {
                Error.Msg(s.token, "if condition must be a boolean");
            }
            t = (Check(s.thenStmt, returnOK, returnType, loop) == null) ? null : t;
            if (s.elseStmt != null)
                t = (Check(s.elseStmt, returnOK, returnType, loop) == null) ? null : t;

            yield t;
        }
        case Stmt.Loop s -> {
            // TODO:
            // - check whileExpr
            // - if type of whileExpr is not boolean
            //   - error: "loop while condition must be a boolean"
            // - check stmt
            // - check untilEdpr
            // - if type of untilExpr is not boolean
            //   - error: "loop until condition must be a boolean"
            Check(s.whileExpr);
            if (Is(s.whileExpr, Type.Bool.class) == null)
                Error.ID(s.token, "loop while condition must be a boolean");
            Check(s.stmt, returnOK, returnType, loop);
            Check(s.untilExpr);
            if (Is(s.untilExpr, Type.Bool.class) == null)
                Error.ID(s.token, "loop until condition must be a boolean");
            yield null;
        }
        case Stmt.Return s -> {
            Check(s.expr);
            Type t = TypeOf(s.expr);
            if (!returnOK) {
                Error.Msg(s.token, "return not in a procedure");
                yield t;
            }
            Type result = returnType;
            if (s.expr == null) {
                if (result != null) {
                    Error.Msg(s.token, "missing return result");
                    t = Type.Err.T;
                }
            } else if (result == null) {
                Error.Msg(s.token, "procedure does not have a return result");
                t = null;
            } else {
                CheckAssign(s.token, result, s.expr);
                yield result;
            }
            yield t;
        }
        case Stmt.Block s -> {
            // TODO:
            // - push, build, check scope from decls if not null/empty
            // - check body
            // - pop scope if decls not null/empty
            // - yield type from checking block

            if (s.decls != null && !s.decls.isEmpty()) {
                s.scope = Scope.PushNewOpen();
                for (Value d : s.decls) {
                    Scope.Insert(d);
                }
                Check(s.scope);
            }
            Type t = null;
            for (Stmt b: s.body) {
                t = t == null ? Check(b, returnOK, returnType, null) : t;
            }

            if(s.decls != null && !s.decls.isEmpty()){
                Scope.PopNew();
            }

            yield t;
        }
        };
    }

    /**
     * Check that 'rhs' is assignable to 'tlhs'.
     */
    void CheckAssign(Token token, Type tlhs, Expr rhs) {
        Type t = Base(tlhs);
        Type trhs = TypeOf(rhs);
        tlhs = Check(tlhs);
        t = Check(t);
        Check(rhs);
        if (!IsAssignable(tlhs, trhs)) {
            if (tlhs != Type.Err.T && trhs != Type.Err.T)
                Error.Msg(token, "types are not assignable");
        } else if (IsOrdinal(tlhs)) {
            CheckOrdinal(token, tlhs, rhs);
        } else if (tlhs instanceof Type.Ref || tlhs instanceof Type.Object) {
            // ok
        } else if (tlhs instanceof Type.Proc) {
            CheckProcedure(token, rhs);
        } else {
            // ok
        }
    }
    void CheckOrdinal(Token token, Type tlhs, Expr rhs) {
        // ok, but must generate a check
        Expr constant = ConstValue(rhs);
        if (constant != null) rhs = constant;
        Bounds r = GetBounds(rhs);
        Bounds l = GetBounds(tlhs);
        if (l.min.compareTo(l.max) <= 0 && r.min.compareTo(r.max) <= 0
            && (r.min.compareTo(l.max) > 0 || r.max.compareTo(l.min) < 0))
            // non-overlapping, non-empty ranges
            Error.Warn(token, "value not assignable (range fault)");
    }
    void CheckProcedure(Token token, Expr rhs) {
        if (NeedsClosureCheck(token, rhs, true)) {
            // may generate a more detailed message
        }
    }
    boolean NeedsClosureCheck(Token token, Expr proc, boolean errors) {
        Value v = switch(proc) {
        case Expr.Named e -> Resolve(e);
        case Expr.Qualify e -> Resolve(e);
        case Expr.Proc e -> e.proc;
        default -> null;
        };
        if (v == null) return false; // non-constant, non-variable => ok
        if (IsNested(Is(v, Value.Procedure.class))) {
            if (errors) Error.Msg(token, "cannot assign nested procedures");
            return false;
        } else if (HasClosure(Is(v, Value.Variable.class))) {
            return true;
        } else return false; // non-formal, non-const => no check
    }
    boolean HasClosure(Value.Variable v) {
        return v != null && v.formal != null && HasClosure(v.formal);
    }
    boolean HasClosure(Value.Formal v) {
        return v != null && v.mode != Value.Formal.Mode.VAR
                && Is(Base(TypeOf(v)), Type.Proc.class) != null;
    }

    /**
     * Return the type of an expression 'e'
     * @param e the expression
     * @return its type or null if it has no type
     */
    Type TypeOf(Expr expr) {
        if (expr == null) return Type.Err.T;
        if (expr.type != null) return expr.type;
        return expr.type = switch (expr) {
        case Expr.Infix e when e.op == Expr.Op.Add -> {
            Type t = Check(TypeOf(e.left));
            if (IsSubtype(t, Type.Addr.T)) yield Type.Addr.T;
            yield Base(t);
        }
        case Expr.Infix e when e.op == Expr.Op.Sub -> {
            Type t = Base(TypeOf(e.left));
            if (IsSubtype(t, Type.Addr.T)
                && IsSubtype(Base(TypeOf(e.right)), Type.Addr.T))
                yield Type.Int.T;
            yield t;
        }
        case Expr.Infix e when e.op == Expr.Op.Mul -> TypeOf(e.left);
        case Expr.Infix e when e.op == Expr.Op.Div -> Base(TypeOf(e.left));
        case Expr.Prefix e when e.op == Expr.Op.Pos -> Base(TypeOf(e.expr));
        case Expr.Prefix e when e.op == Expr.Op.Neg -> Base(TypeOf(e.expr));
        case Expr.Call ce -> {
            Resolve(ce);
            if (ce.procType == null) yield Type.Err.T;
            FixArgs(ce);
            yield switch (ce.procType) {
            case Type.Proc.User p -> {
                Type t = TypeOf(ce.proc);
                if (t == Type.Err.T) yield t;
                if (t == null) t = MethodType(Is(ce.proc, Expr.Qualify.class));
                yield Result(Is(Base(t), Type.Proc.class));
            }
            case Type.Proc.First p -> {
                Expr e = ce.args[0];
                Type.Array a = Is(TypeOf(e), Type.Array.class);
                if (a != null) {
                    if (ConstValue(a.number) == null) yield Type.Int.T;
                    yield Base(TypeOf(a.number));
                }
                Type t = IsType(e);
                if (t == null) yield Type.Int.T;
                a = Is(t, Type.Array.class);
                if (a != null) {
                    if (ConstValue(a.number) == null) yield Type.Int.T;
                    yield Base(TypeOf(a.number));
                }
                yield Base(t);
            }
            case Type.Proc.Last p -> {
                Expr e = ce.args[0];
                Type.Array a = Is(TypeOf(e), Type.Array.class);
                if (a != null) {
                    if (ConstValue(a.number) == null) yield Type.Int.T;
                    yield Base(TypeOf(a.number));
                }
                Type t = IsType(e);
                if (t == null) yield Type.Int.T;
                a = Is(t, Type.Array.class);
                if (a != null) {
                    if (ConstValue(a.number) == null) yield Type.Int.T;
                    yield Base(TypeOf(a.number));
                }
                yield Base(t);
            }
            case Type.Proc.Ord p -> Type.Int.T;
            case Type.Proc.Val p -> {
                Type t = IsType(ce.args[1]);
                if (t == null) yield Type.Int.T;
                yield t;
            }
            case Type.Proc.Number p -> Type.Int.T;
            case Type.Proc.New p -> {
                Type t = IsType(ce.args[0]);
                if (t == null) yield Type.Null.T;
                if (Is(t, Type.Ref.class) != null) yield t;
                // sleazy bug!! ignore method overrides
                if (Is(t, Type.Object.class) != null) yield t;
                yield Type.Null.T;
            }
            case Type.Proc.Loophole p -> {
                Type t = IsType(ce.args[1]);
                yield t != null ? t : Type.Int.T;
            }
            };
        }
        case Expr.Check e -> TypeOf(e.expr);
        case Expr.Method e -> MethodSigAsProcSig(e.method.sig, e.object);
        case Expr.Named e -> {
            Value v = Resolve(e);
            if (inTypeOf.contains(e)) {
                IllegalRecursion(v);
                yield Type.Err.T;
            }
            try {
                inTypeOf.add(e);
                yield TypeOf(v);
            } finally {
                inTypeOf.remove(e);
            }
        }
        case Expr.Proc e -> TypeOf(e.proc);
        case Expr.Deref e -> {
            Type t = TypeOf(e.expr);
            Type.Ref ref = Is(t, Type.Ref.class);
            if (ref != null) yield ref.target;
            yield Type.Err.T;
        }
        case Expr.Qualify e -> {
            Value v = Resolve(e);
            if (inTypeOf.contains(e)) {
                IllegalRecursion(v);
                yield Type.Err.T;
            }
            try {
                inTypeOf.add(e);
                Type t = TypeOf(v);
                if (e.objType != null)
                    yield MethodSigAsProcSig(Is(t, Type.Proc.class), e.objType);
                if (Is(v, Value.Method.class) != null)
                    yield null;
                yield t;
            } finally {
                inTypeOf.remove(e);
            }
        }
        case Expr.Subscript e -> {
            Type t = Base(TypeOf(e.expr));
            if (Is(t, Type.Ref.class) != null) {
                // auto-magic dereference
                e.expr = new Expr.Deref(e.token, e.expr);
                t = Base(TypeOf(e.expr));
            }
            Type.Array a = Is(t, Type.Array.class);
            if (a != null) yield a.element;
            yield t;
        }
        case Expr.TypeExpr e -> null;
        default -> {
            assert false;
            assert expr.type != null;
            yield expr.type;
        }
        };
    }

    /**
     * Type-check an expression 'e'
     * @param e the expression to check
     */
    void Check(Expr expr) {
        Runnable visitor = () -> {
        switch (expr) {
        case Expr.Infix e -> {
            switch (e.op) {
            case GT, LT, GE, LE -> {
                assert e.type != null;
                // TODO
                Check(e.left);
                Check(e.right);
                Type ta = Base(TypeOf(e.left));
                Type tb = Base(TypeOf(e.right));
                if (ta != Type.Bool.T || tb != Type.Bool.T) {
                    e.type = BadOperands(e, ta, tb);
                }
            }
            case Add -> {
                // TODO
                Check(e.left);
                Check(e.right);
                Type ta = Base(TypeOf(e.left));
                Type tb = Base(TypeOf(e.right));
                if (ta == Type.Int.T && tb == Type.Int.T)
                    e.type = Type.Int.T;
                else
                    e.type = BadOperands(e, ta, tb);
            }
            case Sub -> {
                // TODO
                Check(e.left);
                Check(e.right);
                Type ta = Base(TypeOf(e.left));
                Type tb = Base(TypeOf(e.right));
                if (ta == Type.Int.T && tb == Type.Int.T)
                    e.type = Type.Int.T;
                else
                    e.type = BadOperands(e, ta, tb);
            }
            case Or, And -> {
                assert e.type != null;
                Check(e.left);
                Check(e.right);
                Type ta = Base(TypeOf(e.left));
                Type tb = Base(TypeOf(e.right));
                if (ta != Type.Bool.T || tb != Type.Bool.T) {
                    e.type = BadOperands(e, ta, tb);
                }
            }
            case Div -> {
                Check(e.left);
                Check(e.right);
                Type ta = Base(TypeOf(e.left));
                Type tb = Base(TypeOf(e.right));
                if (ta == Type.Int.T && tb == Type.Int.T)
                    e.type = Type.Int.T;
                else
                    e.type = BadOperands(e, ta, tb);
            }
            case Mul, Mod -> {
                // TODO
                Check(e.left);
                Check(e.right);
                Type ta = Base(TypeOf(e.left));
                Type tb = Base(TypeOf(e.right));
                if (ta == Type.Int.T && tb == Type.Int.T)
                    e.type = Type.Int.T;
                else
                    e.type = BadOperands(e, ta, tb);
            }
            case NE, EQ -> {
                assert e.type != null;
                // TODO: lhs and rhs must be assignable
                Check(e.left);
                Check(e.right);
                Type ta = Base(TypeOf(e.left));
                Type tb = Base(TypeOf(e.right));
                if ((ta.tipe != tb.tipe) || !IsAssignable(ta, tb) || !IsAssignable(tb, ta)) {
                    e.type = BadOperands(e, ta, tb);
                }
            }
            case SLL, SRA, SRL, BitAnd, BitXor, BitOr -> {
                assert e.type != null;
                Check(e.left);
                Check(e.right);
                Type ta = Base(TypeOf(e.left));
                Type tb = Base(TypeOf(e.right));
                if (ta == Type.Int.T && tb == Type.Int.T) {
                    // ok
                } else
                    e.type = BadOperands(e, ta, tb);
            }
            default -> {
                assert false;
            }
            }
        }
        case Expr.Address e -> {
            assert e.type != null;
        }
        case Expr.Call ce -> {
            // check the procedure
            int nErrs0 = Error.nErrors();
            Check(ce.proc);
            Resolve(ce);
            int nErrs1 = Error.nErrors();
            if (ce.procType == null) {
                if (nErrs0 == nErrs1 && TypeOf(ce.proc) != Type.Err.T)
                    Error.Msg(ce.token, "attempting to call a non-procedure");
                ce.type = Type.Err.T;
            }
            // check its args
            for (Expr arg : ce.args) {
                Check(arg);
                if (TypeOf(arg) == Type.Err.T)
                    ce.type = Type.Err.T;
            }
            // finally do the procedure specific checking
            if (ce.type != Type.Err.T && ce.procType != null) {
                FixArgs(ce);
                ce.type = switch (ce.procType) {
                case Type.Proc.User p -> {
                    // TODO:
                    // - check that arguments and formals of p match
                    // - yield result type of p

                    CheckArgs(new HashSet<>(List.of(ce.args)), new HashSet<>(p.formals), ce);

                    yield p.result;
                }
                case Type.Proc.First p -> {
                    Type t = TypeOf(ce.args[0]);
                    Type index;
                    if (Is(t, Type.Array.class) != null) {
                        index = Type.Int.T;
                    } else if ((t = IsType(ce.args[0])) != null) {
                        Type.Array a = Is(t, Type.Array.class);
                        if (a != null) {
                            if (a.number == null) {
                                Error.Msg(ce.token, "argument cannot be an open array type");
                                index = Type.Int.T;
                            } else index = TypeOf(a.number);
                        } else index = t;
                    } else {
                        Error.Msg(ce.token, "argument must be a type or array");
                        index = Type.Int.T;
                    }
                    if (IsOrdinal(index)) {
                        // ok
                    } else {
                        Error.Msg(ce.token, "argument must be an ordinal type, array type or array");
                    }
                    yield Base(index);
                }
                case Type.Proc.Last p -> {
                    Type t = TypeOf(ce.args[0]);
                    Type index;
                    if (Is(t, Type.Array.class) != null) {
                        index = Type.Int.T;
                    } else if ((t = IsType(ce.args[0])) != null) {
                        Type.Array a = Is(t, Type.Array.class);
                        if (a != null) {
                            if (a.number == null) {
                                Error.Msg(ce.token, "argument cannot be an open array type");
                                index = Type.Int.T;
                            } else index = TypeOf(a.number);
                        } else index = t;
                    } else {
                        Error.Msg(ce.token, "argument must be a type or array");
                        index = Type.Int.T;
                    }
                    if (IsOrdinal(index)) {
                        // ok
                    } else {
                        Error.Msg(ce.token, "argument must be an ordinal type, array type or array");
                    }
                    yield Base(index);
                }
                case Type.Proc.Ord p -> {
                    Type t = TypeOf(ce.args[0]);
                    if (!IsOrdinal(t))
                        Error.Msg(ce.token, "argument must be an ordinal");
                    yield Type.Int.T;
                }
                case Type.Proc.Val p -> {
                    Type u = TypeOf(ce.args[0]);
                    Type t = Type.Int.T;
                    if (!IsSubtype(u, Type.Int.T))
                        Error.Msg(ce.token, "first argument must be an integer");
                    if ((t = IsType(ce.args[1])) == null)
                        Error.Msg(ce.token, "second argument must be a type");
                    else if (!IsOrdinal(t))
                        Error.Msg(ce.token, "second argument must be an ordinal type");
                    else { // looks ok
                        Bounds b = GetBounds(ce.args[0]);
                        BigInteger minu = b.min;
                        BigInteger maxu = b.max;
                        Bounds tb = GetBounds(t);
                        BigInteger mint = tb.min;
                        BigInteger maxt = tb.max;
                        if (minu.compareTo(mint) < 0) {
                            // we need a lower bound check
                            if (maxu.compareTo(maxt) > 0)
                                // we also need an upper bound check
                                ce.args[0] = new Expr.Check(ce.args[0], mint, maxt);
                            else
                                ce.args[0] = new Expr.Check(ce.args[0], mint, null);
                        } else if (maxu.compareTo(maxt) > 0)
                            ce.args[0] = new Expr.Check(ce.args[0], null, maxt);
                        Check(ce.args[0]);
                    }
                    yield t;
                }
                case Type.Proc.Number p -> {
                    Type t = TypeOf(ce.args[0]);
                    Type index;
                    Type.Array a = Is(t, Type.Array.class);
                    if (a != null) {
                        index = ConstValue(a.number) == null ? Type.Int.T : TypeOf(a.number);
                    } else if ((t = IsType(ce.args[0])) != null) {
                        a = Is(t, Type.Array.class);
                        if (a != null) {
                            if (ConstValue(a.number) == null) {
                                Error.Msg(ce.token, "argument cannot be an open array type");
                                index = Type.Int.T;
                            } else index = TypeOf(a.number);
                        } else index = t;
                    } else {
                        Error.Msg(ce.token, "argument must be a type or array");
                        index = Type.Int.T;
                    }
                    if (IsOrdinal(index)) {
                        // ok
                    } else {
                        Error.Msg(ce.token, "argument must be an ordinal type, array type or array");
                    }
                    yield Type.Int.T;
                }
                case Type.Proc.New p -> {
                    Type t = IsType(ce.args[0]);
                    if (t == null) {
                        Error.Msg(ce.token, "New must be applied to a reference type");
                        t = Type.Null.T;
                    } else if (Is(t, Type.Ref.class) != null) {
                        CheckRef(t, ce);
                    } else if (Is(t, Type.Object.class) != null) {
                        Type r = CheckObject(t);
                        if (r != t) {
                            ce.args[0] = new Expr.TypeExpr(r);
                            Check(ce.args[0]);
                            t = r;
                        }
                    } else if (t != Type.Err.T)
                        Error.Msg(ce.token, "New must be applied to a reference type");
                    yield t;
                }
                case Type.Proc.Loophole p -> {
                    Type t = IsType(ce.args[1]);
                    if (t == null) {
                        Error.Msg(ce.token, "Loophole: second argument must be a type");
                        t = Type.Int.T;
                    }
                    Check(ce.args[0] = new Expr.Cast(ce.token, ce.args[0], t));
                    Error.Warn(ce.token, "Loophole: unsafe operation");
                    ce.type = t;
                    yield t;
                }
                };
            }
        }
        case Expr.Check e -> Check(e.expr);
        case Expr.Deref e -> {
            int err0 = Error.nErrors();
            Check(e.expr);
            Type tx = TypeOf(e.expr);
            int err1 = Error.nErrors();
            Type ta = Base(tx);
            Type.Ref ref;
            if ((tx == null || tx == Type.Err.T) && err0 != err1)
                // already an error, don't generate any more
                e.type = Type.Err.T;
            else if ((ref = Is(ta, Type.Ref.class)) == null) {
                Error.Msg(e.token, "cannot dereference a non-reference value");
                e.type = Type.Err.T;
            } else if (ref.target == null) {
                Error.Msg(e.token, "cannot dereference Refany or Null");
                e.type = Type.Err.T;
            } else
                e.type = ref.target;
        }
        case Expr.Prefix e -> {
            switch (e.op) {
            case Not -> {
                assert e.type != null;
                // TODO
                Check(e.expr);
                e.type = Base(TypeOf(e.expr));
                if (e.type != Type.Bool.T)
                    e.type = BadOperands(e, e.type, null);
            }
            case Address -> {
                assert e.type != null;
                Check(e.expr);
                Error.Warn(e.token, "unsafe '&'");
                if (IsDesignator(e.expr)) NeedsAddress(e.expr);
                else Error.Msg(e.expr.token, "argument must be a designator");
            }
            default -> {
                Check(e.expr);
                e.type = Base(TypeOf(e.expr));
                if (e.type == Type.Int.T) {
                    // ok
                } else
                    e.type = BadOperands(e, e.type, null);
            }
            };
        }
        case Expr.Method e -> {
            e.object = Check(e.object);
            Check(e.method);
            Check(TypeOf(e));
        }
        case Expr.Named e -> {
            Value v = Resolve(e);
            Check(v);
            e.type = TypeOf(v);
            e.value = v;
        }
        case Expr.Proc e -> {
            Check(e.proc);
            e.type = TypeOf(e.proc);
        }
        case Expr.Qualify e -> {
            Value.Field field;
            int nErrs0 = Error.nErrors();
            Check(e.expr);
            Value v = Resolve(e);
            Check(e.expr);
            int nErrs1 = Error.nErrors();
            if (v == null) {
                if (nErrs0 == nErrs1)
                    Error.ID(e.name, "unknown qualification");
                v = new Value.Variable(e.name, Type.Err.T, null);
                e.value = v;
            } else if (e.objType != null && Is(v, Value.Method.class) == null) {
                Error.ID(e.name, "doesn't name a method");
            } else if ((field = Is(v, Value.Field.class)) != null) {
                Check(field.parent);
            }
            Check(v);
            TypeOf(e); // check for cycles
            if (e.type != null)
                e.type = Check(e.type);
        }
        case Expr.Subscript e -> {
            Check(e.expr);
            Check(e.index);
            Type ta = Base(TypeOf(e.expr));
            Type tb = TypeOf(e.index);
            if (ta == null) {
                Error.Msg(e.token, "subscripted expression is not an array");
                e.type = Type.Err.T;
                break;
            }
            if (Is(ta, Type.Ref.class) != null) {
                // auto-magic dereference
                e.expr = new Expr.Deref(e.token, e.expr);
                Check(e.expr);
                ta = Base(TypeOf(e.expr));
            }
            ta = Check(ta);
            if (ta == Type.Err.T) {
                e.type = Type.Err.T;
                break;
            }
            Type.Array a = Is(ta, Type.Array.class);
            if (a == null) {
                Error.Msg(e.token, "subscripted expression is not an array");
                e.type = Type.Err.T;
                break;
            }
            e.type = a.element;
            NeedsAddress(e.expr);
            Expr number = ConstValue(a.number);
            if (number == null) {
                // a is an open array
                if (!IsSubtype(tb, Type.Int.T))
                    Error.Msg(e.token, "open arrays must be indexed by integer expressions");
            } else if (IsSubtype(tb, Base(TypeOf(a.number)))) {
                // the index value's type has a common base type with the index type
                BigInteger
                    min = BigInteger.ZERO,
                    max = ((Expr.Int)number).value.max(BigInteger.valueOf(-1));
                Bounds b = GetBounds(e.index);
                if (b.min.compareTo(min) < 0 && b.max.compareTo(max) > 0) {
                    Check(e.index = new Expr.Check(e.index, min, max));
                } else if (b.min.compareTo(min) < 0) {
                    if (b.max.compareTo(min) < 0)
                        Error.Warn(e.token, "subscript is out of range");
                    Check(e.index = new Expr.Check(e.index, min, null));
                } else if (b.max.compareTo(max) > 0) {
                    if (b.min.compareTo(max) > 0)
                        Error.Warn(e.token, "subscript is out of range");
                    Check(e.index = new Expr.Check(e.index, null, max));
                }
            } else {
                Error.Msg(e.token, "incompatible array index");
            }
        }
        case Expr.TypeExpr e -> {
            e.value = Check(e.value);
            assert e.type == null;
        }
        case Expr.Cast e -> {
            Check(e.expr);
            Type dst = e.tipe = Check(e.tipe);
            Type src = Check(TypeOf(e.expr));
            boolean srcDesignator = IsDesignator(e.expr);
            boolean srcStruct = IsStructured(src);
            boolean dstStruct = IsStructured(dst);
            Type.Array srcArray = IsOpenArray(src);
            Type.Array dstArray = IsOpenArray(dst);
            int srcAlign = src.align;
            int dstAlign = dst.align;

            // check to see that the value is legal
            if (srcArray != null)
                Error.Msg(e.token, "Loophole: first argument cannot be an open array");
            int size0 = src.bits;

            // check to see that the destination type is legal
            if (dstArray != null) {
                // open array type
                Type element = Check(dstArray.element);
                if (IsOpenArray(element) != null)
                    Error.Msg(e.token, "Loophole: multidimensional open arrays not supported");
                int size1 = element.bits;
                if (size1 <= 0 || size0 % size1 != 0)
                    Error.Msg(e.token, "Loophole: expression's size incompatible with type's");
                dstAlign = element.align;
            } else {
                // fixed size type
                int size1 = dst.bits;
                if (size0 != size1)
                    Error.Msg(e.token, "Loophole: expression's size differs from type's");
            }

            // check for alignment problems
            if (srcAlign < dstAlign || srcAlign % dstAlign != 0)
                Error.Warn(e.token, "Loophole: expression's alignnment may differ from type's");

            // classify the type of Loophole operation
            if (dstArray != null) {
                if (srcDesignator)  e.kind = Expr.Cast.Kind.DA;
                else if (srcStruct) e.kind = Expr.Cast.Kind.SA;
                else                e.kind = Expr.Cast.Kind.VA;
            }
            else if (dstStruct) {
                if (srcDesignator)  e.kind = Expr.Cast.Kind.DS;
                else if (srcStruct) e.kind = Expr.Cast.Kind.SS;
                else                e.kind = Expr.Cast.Kind.VS;
            }
            else if (srcStruct)     e.kind = Expr.Cast.Kind.SV;
            else if (srcDesignator) e.kind = Expr.Cast.Kind.DV;
            else                    e.kind = Expr.Cast.Kind.VV;

            switch (e.kind) {
            case DA, DS -> NeedsAddress(e.expr); // we're going to take nthe address of this value
            default -> { /* skip */ }
            }
        }
        default -> {
            assert expr.type != null;
        }
        }
        };
        if (expr == null) return;
        if (expr.checked) return;
        visitor.run();
        expr.checked = true;
    }
    private void CheckRef(Type t, Expr.Call ce) {
        Type r = ((Type.Ref)t).target;
        if (r == null) {
            Error.Msg(ce.token, "cannot New a variable of type refany, address, or null");
            return;
        }
        r = Check(r);
        if (IsEmpty(r))
            Error.Msg(ce.token, "cannot allocate variables of empty types");
        else if (IsOpenArray(r) != null)
            CheckOpenArray(r, ce);
        else if (ce.args.length > 1) {
            Error.Msg(ce.token, "too many arguments to New");
        }
    }
    private void CheckOpenArray (Type r, Expr.Call ce) {
        Type.Array a;
        for (int i = 1; i < ce.args.length; ++i) {
            Type x = Base(TypeOf(ce.args[i]));
            if (!IsEqual(x, Type.Int.T, null))
                Error.Int(ce.args[i].token, i, "argument must be an integer");
            else if ((a = IsOpenArray(r)) == null)
                Error.Int(ce.args[i].token, i, "too many dimensions specified");
            else r = a.element;
        }
        if (IsOpenArray(r) != null)
            Error.Msg(ce.token, "not enough dimensions specified");
    }
    private Type CheckObject(Type t) {
        t = Check(t);
        return t;
    }

    /**
     * Note an "illegal operand(s)" error for an expression
     * @param e the erroring expression
     * @param a the type of the first operand (can be null)
     * @param b the type of the second operand (can be null)
     * @return the error type
     */
    Type BadOperands(Expr e, Type a, Type b) {
        if (a != Type.Err.T && b != Type.Err.T)
            Error.Msg(e.token, "illegal operand(s) for " + e.token);
        return Type.Err.T;
    }

    /**
     * Returns null if e is not a constant, otherwise returns a simplified
     * expression that denotes e.
     * ConstValue may be called before the expression is typechecked.
     */
    Expr ConstValue(Expr expr) {
        Supplier<Expr> visitor = () -> {
            return switch (expr) {
            case Expr.Infix e when e.op == Expr.Op.Add -> {
                Expr.Int e1 = Is(ConstValue(e.left), Expr.Int.class);
                Expr.Int e2 = Is(ConstValue(e.right), Expr.Int.class);
                if (e1 == null || e2 == null) yield null;
                BigInteger result = e1.value.add(e2.value);
                if (result.bitLength() >= wordSize * 8) yield null;
                yield new Expr.Int(e.token, result);
            }
            case Expr.Infix e when e.op == Expr.Op.Sub -> {
                // TODO
                Expr.Int e1 = Is(ConstValue(e.left), Expr.Int.class);
                Expr.Int e2 = Is(ConstValue(e.right), Expr.Int.class);
                if (e1 == null || e2 == null) yield null;
                BigInteger result = e1.value.subtract(e2.value);
                if (result.bitLength() >= wordSize * 8) yield null;
                yield new Expr.Int(e.token, result);
            }
            case Expr.Infix e when e.op == Expr.Op.And -> {
                Expr e1 = ConstValue(e.left);
                Expr e2 = ConstValue(e.right);
                if (Base(TypeOf(e1)) != Type.Bool.T) yield null;
                if (Base(TypeOf(e2)) != Type.Bool.T) yield null;
                BigInteger z1 = ((Expr.Enum)e1).value;
                BigInteger z2 = ((Expr.Enum)e2).value;
                yield z1.signum() != 0 && z2.signum() != 0 ? Type.Bool.True : Type.Bool.False;
            }
            case Expr.Infix e when e.op == Expr.Op.Or -> {
                // TODO
                Expr e1 = ConstValue(e.left);
                Expr e2 = ConstValue(e.right);
                if (Base(TypeOf(e1)) != Type.Bool.T) yield null;
                if (Base(TypeOf(e2)) != Type.Bool.T) yield null;
                BigInteger z1 = ((Expr.Enum)e1).value;
                BigInteger z2 = ((Expr.Enum)e2).value;
                yield z1.signum() != 0 || z2.signum() != 0 ? Type.Bool.True : Type.Bool.False;
            }
            case Expr.Infix e when e.op.in(Expr.Op.GT,
                                           Expr.Op.LT,
                                           Expr.Op.GE,
                                           Expr.Op.LE,
                                           Expr.Op.EQ,
                                           Expr.Op.NE) -> {
                // TODO
                Expr e1 = ConstValue(e.left);
                Expr e2 = ConstValue(e.right);
                if (Base(TypeOf(e1)) != Type.Bool.T) yield null;
                if (Base(TypeOf(e2)) != Type.Bool.T) yield null;
                BigInteger z1 = ((Expr.Enum)e1).value;
                BigInteger z2 = ((Expr.Enum)e2).value;
                yield switch (e) {
                    case Expr.Infix ex when ex.op == Expr.Op.GT ->
                            z1.compareTo(z2) > 0 ? Type.Bool.True : Type.Bool.False;
                    case Expr.Infix ex when ex.op == Expr.Op.LT ->
                            z1.compareTo(z2) < 0 ? Type.Bool.True : Type.Bool.False;
                    case Expr.Infix ex when ex.op == Expr.Op.GE->
                            z1.compareTo(z2) >= 0 ? Type.Bool.True : Type.Bool.False;
                    case Expr.Infix ex when ex.op == Expr.Op.LE->
                            z1.compareTo(z2) <= 0 ? Type.Bool.True : Type.Bool.False;
                    case Expr.Infix ex when ex.op == Expr.Op.EQ->
                            z1.compareTo(z2) == 0 ? Type.Bool.True : Type.Bool.False;
                    case Expr.Infix ex when ex.op == Expr.Op.NE->
                            z1.compareTo(z2) != 0 ? Type.Bool.True : Type.Bool.False;

                    default -> throw new IllegalStateException("Unexpected value: " + e);
                };
            }
            case Expr.Infix e when e.op == Expr.Op.Div -> {
                Expr.Int e1 = Is(ConstValue(e.left), Expr.Int.class);
                Expr.Int e2 = Is(ConstValue(e.right), Expr.Int.class);
                if (e1 == null || e2 == null) yield null;
                if (e2.value.signum() == 0) {
                    Error.Msg(e.token, "attempt to divide by 0");
                    yield null;
                }
                BigInteger result = e1.value.divide(e2.value);
                if (result.bitLength() >= wordSize * 8) yield null;
                yield new Expr.Int(e.token, result);
            }
            case Expr.Infix e when e.op == Expr.Op.Mul -> {
                // TODO
                Expr.Int e1 = Is(ConstValue(e.left), Expr.Int.class);
                Expr.Int e2 = Is(ConstValue(e.right), Expr.Int.class);
                if (e1 == null || e2 == null) yield null;

                BigInteger result = e1.value.multiply(e2.value);
                if (result.bitLength() >= wordSize * 8) yield null;
                yield new Expr.Int(e.token, result);
            }
            case Expr.Infix e when e.op == Expr.Op.Mod -> {
                Expr.Int e1 = Is(ConstValue(e.left), Expr.Int.class);
                Expr.Int e2 = Is(ConstValue(e.right), Expr.Int.class);
                if (e1 == null || e2 == null) yield null;
                if (e2.value.signum() == 0) {
                    Error.Msg(e.token, "attempt to modulus by 0");
                    yield null;
                }
                BigInteger result = e1.value.remainder(e2.value);
                if (result.bitLength() >= wordSize * 8) yield null;
                yield new Expr.Int(e.token, result);
            }
            case Expr.Infix e -> {
                Expr.Int e0 = Is(ConstValue(e.left), Expr.Int.class);
                Expr.Int e1 = Is(ConstValue(e.right), Expr.Int.class);
                if (e0 == null || e1 == null) yield null;
                BigInteger result = switch (wordSize) {
                case Integer.BYTES -> {
                    int i0 = e0.value.intValue();
                    int i1 = e1.value.intValue();
                    yield switch (e.op) {
                    case SLL -> 0 <= i1 && i1 < Integer.SIZE ? BigInteger.valueOf(i0 << i1) : null;
                    case SRA -> 0 <= i1 && i1 < Integer.SIZE ? BigInteger.valueOf(i0 >> i1) : null;
                    case SRL -> 0 <= i1 && i1 < Integer.SIZE ? BigInteger.valueOf(i0 >>> i1) : null;
                    case BitAnd -> BigInteger.valueOf(i0 & i1);
                    case BitXor -> BigInteger.valueOf(i0 ^ i1);
                    case BitOr -> BigInteger.valueOf(i0 | i1);
                    default -> null;
                    };
                }
                case Long.BYTES -> {
                    long i0 = e0.value.longValue();
                    long i1 = e1.value.longValue();
                    yield switch (e.op) {
                    case SLL -> 0 <= i1 && i1 < Long.SIZE ? BigInteger.valueOf(i0 << i1) : null;
                    case SRA -> 0 <= i1 && i1 < Long.SIZE ? BigInteger.valueOf(i0 >> i1) : null;
                    case SRL -> 0 <= i1 && i1 < Long.SIZE ? BigInteger.valueOf(i0 >>> i1) : null;
                    case BitAnd -> BigInteger.valueOf(i0 & i1);
                    case BitXor -> BigInteger.valueOf(i0 ^ i1);
                    case BitOr -> BigInteger.valueOf(i0 | i1);
                    default -> null;
                    };
                }
                default -> null;
                };
                if (result == null) yield null;
                yield new Expr.Int(e.token, result);
            }
            case Expr.Address e -> e;
            case Expr.Call ce -> {
                Resolve(ce);
                if (ce.procType == null) yield null;
                yield switch (ce.procType) {
                case Type.Proc.User p -> null;
                case Type.Proc.First p -> {
                    Expr e = ce.args[0];
                    Type t = IsType(e);
                    if (t != null) yield FirstOfType(t);
                    t = TypeOf(e);
                    Type.Array a = Is(t, Type.Array.class);
                    if (a == null) yield null;
                    yield FirstOfType(a);
                }
                case Type.Proc.Last p -> {
                    Expr e = ce.args[0];
                    Type t = IsType(e);
                    if (t != null) yield LastOfType(t);
                    t = TypeOf(e);
                    Type.Array a = Is(t, Type.Array.class);
                    if (a == null) yield null;
                    yield LastOfType(a);
                }
                case Type.Proc.Number p -> {
                    Expr e = ce.args[0];
                    Type t = IsType(e);
                    if (t == null) {
                        t = TypeOf(e);
                        Type.Array a = Is(t, Type.Array.class);
                        if (a != null) yield ConstValue(a.number);
                        yield null;
                    }
                    Type.Array a = Is(t, Type.Array.class);
                    if (a != null) yield ConstValue(a.number);
                    Bounds b = GetBounds(t);
                    if (b == null) yield null;
                    if (b.min.compareTo(b.max) > 0)
                        yield new Expr.Int(e.token, BigInteger.ZERO);
                    BigInteger num = b.max.subtract(b.min).add(BigInteger.ONE);
                    if (num.compareTo(MAX_VALUE) > 0) yield null;
                    yield new Expr.Int(e.token, num);
                }
                case Type.Proc.Ord p -> {
                    yield switch (ConstValue(ce.args[0])) {
                    case Expr.Enum v -> new Expr.Int(v.token, v.value);
                    case Expr.Int v -> v;
                    default -> null;
                    };
                }
                case Type.Proc.Val p -> {
                    Expr.Int e = Is(ConstValue(ce.args[0]), Expr.Int.class);
                    if (e == null) yield null;
                    Type t = IsType(ce.args[1]);
                    if (t == null) yield null;
                    BigInteger x = e.value;
                    Bounds b = GetBounds(t);
                    if (x.compareTo(b.min) < 0 || x.compareTo(b.max) > 0) {
                        Error.Msg(e.token, "Val: value out of range");
                        yield null;
                    }
                    t = Base(t);
                    if (Is(t, Type.Enum.class) != null)
                        yield new Expr.Enum(e.token, t, x);
                    yield new Expr.Int(e.token, x);
                }
                case Type.Proc.New p -> null;
                case Type.Proc.Loophole p -> null;
                };
            }
            case Expr.Check e -> {
                Expr.Int e1 = Is(ConstValue(e.expr), Expr.Int.class);
                if (e1 == null) yield null;
                BigInteger i = e1.value;
                if (e.min != null && e.max != null) {
                    if (i.compareTo(e.min) < 0 || i.compareTo(e.max) > 0)
                        yield null;
                } else if (e.min != null) {
                    if (i.compareTo(e.min) < 0) yield null;
                } else if (e.max != null) {
                    if (i.compareTo(e.max) > 0) yield null;
                }
                yield e1;
            }
            case Expr.Prefix e when e.op == Expr.Op.Pos -> {
                Expr.Int e1 = Is(ConstValue(e.expr), Expr.Int.class);
                if (e1 == null) yield null;
                yield e1;
            }
            case Expr.Prefix e when e.op == Expr.Op.Neg -> {
                Expr.Int e1 = Is(ConstValue(e.expr), Expr.Int.class);
                if (e1 == null) yield null;
                BigInteger result = e1.value.negate();
                if (result.bitLength() >= wordSize * 8) yield null;
                yield new Expr.Int(e.token, result);
            }
            case Expr.Prefix e when e.op == Expr.Op.Not -> {
                Expr.Enum e1 = Is(ConstValue(e.expr), Expr.Enum.class);
                if (Base(TypeOf(e1)) != Type.Bool.T) yield null;
                yield e1.value.signum() == 0 ? Type.Bool.True : Type.Bool.False;
            }
            case Expr.Prefix e when e.op == Expr.Op.BitNot -> {
                Expr.Int e1 = Is(ConstValue(e.expr), Expr.Int.class);
                if (e1 == null) yield null;
                BigInteger result = switch(wordSize) {
                case Integer.BYTES -> BigInteger.valueOf(~e1.value.intValue());
                case Long.BYTES -> BigInteger.valueOf(~e1.value.longValue());
                default -> null; };
                yield new Expr.Int(e.token, result);
            }
            case Expr.Named e -> {
                Value v = Resolve(e);
                inFold.add(e);
                Expr x = ToExpr(v);
                inFold.remove(e);
                yield ConstValue(x);
            }
            case Expr.Qualify e -> {
                Value v = Resolve(e);
                if (inFold.contains(e)) {
                    IllegalRecursion(v);
                    yield null;
                }
                try {
                    inFold.add(e);
                    // evaluate the qualified expression
                    LHS lhs = new LHS();
                    lhs.kind = LHS.Kind.Expr;
                    lhs.expr = e.expr;
                    DoQualify(lhs, e.name.image);
                    // finally simplify the result to an Expr if possible
                    yield switch (lhs.kind) {
                    case Expr -> ConstValue(lhs.expr);
                    case Type -> new Expr.TypeExpr(lhs.type);
                    case Value -> ConstValue(ToExpr(lhs.value));
                    default -> null;
                    };
                } finally {
                    inFold.remove(e);
                }
            }
            case Expr.Deref e -> null;
            case Expr.Subscript e -> null;
            case Expr.Cast e -> {
                Expr x = ConstValue(e.expr);
                if (x == null) yield null;
                e.expr = x;
                yield e;
            }
            default -> expr;
            };
        };
        if (expr == null) return null;
        Expr value = visitor.get();
        if (value != expr) Check(value);
        return value;
    }
    final HashSet<Expr> inFold = new HashSet<Expr>();

    Expr FirstOfType(Type t) {
        Type.Array a = Is(t, Type.Array.class);
        if (a != null) return FirstOfType(a);
        Type base = Base(t);
        Bounds b = GetBounds(t);
        return switch (base) {
        case Type.Int p -> new Expr.Int(p.token, b.min);
        case Type.Enum p -> new Expr.Enum(t.token, t, b.min);
        default -> null;
        };
    }
    Expr FirstOfType(Type.Array a) {
        return new Expr.Int(a.token, BigInteger.ZERO);
    }

    Expr LastOfType(Type t) {
        Type.Array a = Is(t, Type.Array.class);
        if (a != null) return LastOfType(a);
        Type base = Base(t);
        Bounds b = GetBounds(t);
        return switch (base) {
        case Type.Int p -> new Expr.Int(p.token, b.max);
        case Type.Enum p -> new Expr.Enum(t.token, t, b.max);
        default -> null;
        };
    }
    Expr LastOfType(Type.Array a) {
        Expr.Int x = Is(ConstValue(a.number), Expr.Int.class);
        if (x == null) return null;
        BigInteger n = x.value.subtract(BigInteger.ONE);
        return new Expr.Int(a.token, n);
    }

    record Bounds(BigInteger min, BigInteger max) {};
    Bounds GetBounds(Expr expr) {
        if (expr == null)
            return new Bounds(BigInteger.ZERO, BigInteger.ONE.negate()) ;
        assert expr.checked;
        return switch (expr) {
        case Expr.Infix e when e.op == Expr.Op.Add -> {
            Bounds t = GetBounds(e.type);
            Bounds a = GetBounds(e.left);
            Bounds b = GetBounds(e.right);
            BigInteger sum, min = t.min, max = t.max;
            sum = a.min.add(b.min);
            if (min.compareTo(sum) < 0) min = sum;
            sum = a.max.add(b.max);
            if (sum.compareTo(max) < 0) max = sum;
            yield new Bounds(min, max);
        }
        case Expr.Infix e when e.op == Expr.Op.Sub -> {
            Bounds t = GetBounds(e.type);
            Bounds a = GetBounds(e.left);
            Bounds b = GetBounds(e.right);
            BigInteger diff, min = t.min, max = t.max;
            diff = a.min.subtract(b.max);
            if (min.compareTo(diff) < 0) min = diff;
            diff = a.max.subtract(b.min);
            if (diff.compareTo(max) < 0) max = diff;
            yield new Bounds(min, max);
        }
        case Expr.Infix e when e.op == Expr.Op.Mod -> {
            Bounds t = GetBounds(e.type);
            if (e.type != Type.Int.T) yield t;
            Bounds b = GetBounds(e.right);
            if (b.min.compareTo(BigInteger.ZERO) < 0
                || b.max.compareTo(BigInteger.ZERO) < 0)
                yield t;
            BigInteger min = BigInteger.ZERO;
            BigInteger max = b.max.subtract(BigInteger.ONE);
            if (max.compareTo(t.min) < 0 || max.compareTo(t.max) > 0)
                max = t.max;
            yield new Bounds(min, max);
        }
        case Expr.Infix e when e.op == Expr.Op.BitAnd -> {
            Bounds t = GetBounds(e.type);
            Bounds a = GetBounds(e.left);
            Bounds b = GetBounds(e.right);
            if (a.min.compareTo(BigInteger.ZERO) < 0 ||
                a.max.compareTo(BigInteger.ZERO) < 0) {
                // "a" could be 16_ffff...  => any bits from "b" can survive
                if (b.min.compareTo(BigInteger.ZERO) < 0 ||
                    b.max.compareTo(BigInteger.ZERO) < 0)
                    // too complicated
                    yield t;
                else
                    // "b" is non-negative, but "a" could be 16_ffff...
                    yield new Bounds(BigInteger.ZERO, b.max);
            } else if (b.min.compareTo(BigInteger.ZERO) < 0 ||
                       b.max.compareTo(BigInteger.ZERO) < 0) {
                // "a" is non-negative, but "b" could be 16_ffff...
                yield new Bounds(BigInteger.ZERO, a.max);
            } else {
                // both a and b are non-negative
                BigInteger min = BigInteger.ZERO; // no bits in common
                BigInteger max = switch (wordSize) {
                case Integer.BYTES -> BigInteger.valueOf(a.max.intValue() & b.max.intValue());
                case Long.BYTES -> BigInteger.valueOf(a.max.longValue() & b.max.longValue());
                default -> null;
                };
                if (max == null) yield t;
                yield new Bounds(min, max);
            }
        }
        case Expr.Address e -> new Bounds(e.value, e.value);
        case Expr.Call ce -> {
            Expr e = ConstValue(ce);
            if (e != null && e != ce) yield GetBounds(e);
            if (ce.procType == null) yield GetBounds(ce.type);
            yield switch (ce.procType) {
            case Type.Proc.Ord t -> GetBounds(ce.args[0]);
            case Type.Proc.Val t -> GetBounds(ce.args[0]);
            default -> GetBounds(ce.type);
            };
        }
        case Expr.Enum e -> new Bounds(e.value, e.value);
        case Expr.Check e -> {
            Bounds b = GetBounds(e.expr);
            BigInteger min = b.min, max = b.max;
            if (e.min != null && min.compareTo(e.min) < 0) min = e.min;
            if (e.max != null && max.compareTo(e.max) > 0) max = e.max;
            yield new Bounds(min, max);
        }
        case Expr.Named e -> {
            Value v = Resolve(e);
            if (inGetBounds.contains(e)) {
                IllegalRecursion(v);
                yield GetBounds(e.type);
            }
            inGetBounds.add(e);
            Expr x = ToExpr(v);
            inGetBounds.remove(e);
            yield x != null ? GetBounds(x) : GetBounds(e.type);
        }
        case Expr.Int e -> new Bounds(e.value, e.value);
        case Expr.Qualify e -> {
            Value v = Resolve(e);
            if (inGetBounds.contains(e)) {
                IllegalRecursion(v);
                yield GetBounds(e.type);
            }
            inGetBounds.add(e);
            Expr x = ToExpr(v);
            inGetBounds.remove(e);
            yield x != null ? GetBounds(x) : GetBounds(e.type);
        }
        default -> GetBounds(expr.type); // no bounds
        };
    }
    final HashSet<Expr> inGetBounds = new HashSet<Expr>();

    boolean IsDesignator(Expr expr) {
        if (expr == null) return true;
        assert expr.checked;
        return switch (expr) {
        case Expr.Named e -> Is(Resolve(e), Value.Variable.class) != null;
        case Expr.Qualify e -> {
            Value v = Resolve(e);
            if (e.objType != null) yield false;
            if (Is(v, Value.Field.class) != null) yield IsDesignator(e.expr);
            yield Is(v, Value.Variable.class) != null;
        }
        case Expr.Subscript e -> IsDesignator(e.expr);
        case Expr.Deref e -> true;
        case Expr.Call e when e.procType == Type.Proc.Loophole.T -> IsDesignator(e.args[0]);
        case Expr.Cast e -> IsDesignator(e.expr);
        default -> false;
        };
    }

    boolean IsWritable(Expr expr) {
        if (expr == null) return true;
        assert expr.checked;
        return switch (expr) {
        case Expr.Named e -> IsWritable(Resolve(e));
        case Expr.Qualify e -> {
            Value v = Resolve(e);
            if (e.objType != null) yield false;
            if (v instanceof Value.Field) yield IsWritable(e.expr);
            yield IsWritable(v);
        }
        case Expr.Subscript e -> IsWritable(e.expr);
        case Expr.Deref e -> true;
        case Expr.Call e when e.procType == Type.Proc.Loophole.T -> IsWritable(e.args[0]);
        case Expr.Cast e -> IsWritable(e.expr);
        default -> false;
        };
    }

    void NeedsAddress(Expr expr) {
        if (expr == null) return;
        assert expr.checked;
        switch (expr) {
        case Expr.Named e -> {
            Value v = Resolve(e);
            Value.Variable var = Is(v, Value.Variable.class);
            if (var != null)
                var.needsAddress(true);
            else
                assert false;
        }
        case Expr.Qualify e -> {
            Value v = Resolve(e);
            assert(e.objType == null);
            Value.Variable var = Is(v, Value.Variable.class);
            if (var != null)
                var.needsAddress(true);
            else if (Is(v, Value.Field.class) != null)
                NeedsAddress(e.expr);
            else
                assert false;
        }
        case Expr.Subscript e -> NeedsAddress(e.expr);
        case Expr.Deref e -> {} // ok
        case Expr.Call e when e.procType == Type.Proc.Loophole.T -> NeedsAddress(e.args[0]);
        case Expr.Cast e -> {
            switch (e.kind) {
            case NOP, DS, SS, DV, SV, VV -> NeedsAddress(e.expr);
            case DA, SA, VA, VS -> { /* ok, because we build a temporary */ }
            }
        }
        default -> { assert false; }
        }
    }

    <R> R Is(Expr e, Class<R> c) {
        if (e == null) return null;
        if (c.isInstance(e)) return c.cast(e);
        return null;
    }

    Type IsType(Expr expr) {
        if (expr == null) return null;
        return switch (expr) {
        case Expr.TypeExpr e -> e.value;
        case Expr.Named e -> ToType(Resolve(e));
        case Expr.Qualify e -> ToType(Resolve(e));
        default -> null;
        };
    }

    Value Resolve(Expr.Named e) {
        if (e.value != null) return e.value;
        Value v = Scope.LookUp(Scope.Top(), e.name, false);
        if (v == null) {
            Error.ID(e.token, "undefined");
            v = new Value.Variable(e.token, Type.Err.T, null);
        }
        return e.value = v;
    }

    Value Resolve(Expr.Qualify e) {
        if (e.value != null) return e.value;
        Type t = TypeOf(e.expr);
        if (Is(t, Type.Ref.class) != null) {
            // auto-magic dereference
            e.expr = new Expr.Deref(e.token, e.expr);
            t = TypeOf(e.expr);
        }
        assert e.value == null;
        Type base = Base(t);
        if (t == Type.Err.T) {
            // the lhs already contains an error => silently make it look like
            // everything is ok
            e.value = new Value.Variable(e.name, Type.Err.T, null);
        } else if (t == null) {
            // a module or type
            t = IsType(e.expr);
            if (t instanceof Type.Enum p)
                e.value = LookUp(p, e.name.image);
            else if (t instanceof Type.Object p) {
                e.value = LookUp(p, e.name.image);
                if (e.value != null) e.objType = p;
            } else if (e.expr instanceof Expr.Named n
                       && Resolve(n) instanceof Value.Unit m) {
                e.value = Scope.LookUp(m.localScope, m.name, true);
            }
        } else if (base instanceof Type.Record p)
            e.value = LookUp(p, e.name.image);
        else if (base instanceof Type.Object p)
            e.value = LookUp(p, e.name.image);
        return e.value;
    }

    Type MethodType(Expr.Qualify e) {
        if (e == null) return null;
        Value v = Resolve(e);
        if (Is(v, Value.Method.class) != null) return TypeOf(v);
        return null;
    }
    static class LHS {
        enum Kind {Value,Expr,Type,None}

        Kind kind;
        Value value;
        Expr expr;
        Type type;
    }
    void DoQualify(LHS lhs, String name) {
        switch (lhs.kind) {
        case None -> {
            // don't even try
        }
        case Expr -> {
            Type t;
            Value v;
            Expr e;
            if (lhs.expr == null)
                lhs.kind = LHS.Kind.None;
            else if (lhs.expr instanceof Expr.Qualify p) {
                lhs.kind = LHS.Kind.Expr;
                lhs.expr = p.expr;
                DoQualify(lhs, p.name.image);
                DoQualify(lhs, name);
            } else if ((t = IsType(lhs.expr)) != null) {
                lhs.kind = LHS.Kind.Type;
                lhs.type = t;
                DoQualify(lhs, name);
            } else if (lhs.expr instanceof Expr.Named n
                       && (v = Resolve(n)) != null) {
                lhs.kind = LHS.Kind.Value;
                lhs.value = v;
                DoQualify(lhs, name);
            } else {
                e = ConstValue(lhs.expr);
                if (e != lhs.expr) {
                    // try qualifying the constant value
                    lhs.kind = LHS.Kind.Expr;
                    lhs.expr = e;
                    DoQualify(lhs, name);
                } else {
                    lhs.kind = LHS.Kind.None;
                }
            }
        }
        case Type -> {
            Type t = Strip(lhs.type);
            if (t instanceof Type.Enum p
                && LookUp(p, name) instanceof Value.EnumElt v) {
                lhs.kind = LHS.Kind.Expr;
                lhs.expr = new Expr.Enum(v.token, v.parent, v.value);
            } else if (t instanceof Type.Object p
                && LookUp(p, name) instanceof Value.Method m) {
                lhs.kind = LHS.Kind.Expr;
                lhs.expr = new Expr.Method(p, name, m);
            } else // type that can't be qualified
                lhs.kind = LHS.Kind.None;
        }
        case Value -> {
            switch (lhs.value) {
            case Value.Const d -> {
                lhs.kind = LHS.Kind.Expr;
                lhs.expr = d.expr;
                DoQualify(lhs, name);
            }
            case Value.EnumElt d -> {
                lhs.kind = LHS.Kind.Expr;
                lhs.expr = new Expr.Enum(d.token, d.parent, d.value);
                DoQualify(lhs, name);
            }
            case Value.Unit d -> {
                lhs.kind = LHS.Kind.Value;
                lhs.value = Scope.LookUp(d.localScope, name, true);
            }
            case Value.Tipe d -> {
                lhs.kind = LHS.Kind.Type;
                lhs.type = d.value;
                DoQualify(lhs, name);
            }
            default -> {}
            }
        }
        }
    }

    void Resolve(Expr.Call e) {
        if (e.procType != null) return;
        Type t = TypeOf(e.proc);
        if (t == null)
            // we need this hack because "TypeOf(obj.method)" returns null
            // so that you can't use it as a vanilla procedure value.
            t = MethodType(Is(e.proc, Expr.Qualify.class));
        e.procType = Is(t, Type.Proc.class);
    }

    void FixArgs(Expr.Call e) {
        int minArgs = e.procType.minArgs;
        int maxArgs = e.procType.maxArgs;
        if (e.args.length < minArgs) {
            Error.Msg(e.token, "too few arguments");
            Expr[] z = new Expr[minArgs];
            System.arraycopy(e.args, 0, z, 0, e.args.length);
            e.args = z;
        } else if (maxArgs >= 0 && e.args.length > maxArgs) {
            Error.Msg(e.token, "too many arguments");
            Expr[] z = new Expr[maxArgs];
            System.arraycopy(e.args, 0, z, 0, maxArgs);
            e.args = z;
        }
    }

    /**
     * Type-check the values in a scope, including procedure bodies.
     * @param t the scope to check
     */
    void Check(Scope t) {
        for (Value v : Scope.ToList(t))
            Check(v);
        for (Value v : Scope.ToList(t)) {
            Value.Procedure p = Is(v, Value.Procedure.class);
            if (p != null) CheckBody(p);
        }
    }

    String ModuleName(Value v) {
        return v.name;
    }

    String NameToPrefix(Value v, boolean considerExternal, boolean dots, boolean withModule) {
        String dot = dots ? "." : "__";
        if (considerExternal && v.external())
            // simple external name: foo
            return v.extName;
        else if (v.exported() || v.imported() || v.scope.module) {
            // global names: foo, module.foo
            if (v.scope == null || v.scope.name == null || !withModule)
                return v.name;
            else
                return v.scope.name + dot + v.name;
        } else if (withModule || ToExpr(v) != null) {
            // procedure => fully qualified name: module.p1.p2.p
            StringBuilder result = new StringBuilder(v.name);
            for (Scope t = v.scope; t != null; t = t.parent) {
                if (t.name != null) result.insert(0, t.name + dot);
                if (!t.open) break;
                if (t.module) break;
            }
            return result.toString();
        } else {
            // variable => simple name: foo
            StringBuilder result = new StringBuilder(v.name);
            for (Scope t = v.scope; t != null; t = t.parent) {
                if (t.name != null) result.insert(0, t.name + dot);
                if (!t.open || !t.proc) break;
            }
            return result.toString();
        }
    }

    void print(Scope scope) {
        if (!scope.open) return;
        System.out.println("BEGIN " + scope);
        for (Value v: Scope.ToList(scope)) System.out.println(ToString(v));
        for (Scope s: scope.children) print(s);
        System.out.println("END " + scope);
    }

    String ToString(Type type, boolean useName) {
        if (type == null) return null;
        if (useName && type.tipe != null) return type.tipe.name;
        return switch(type) {
        case Type.Enum t -> {
            String s = "{";
            Collection<Value> values = Scope.ToList(t.scope);
            boolean first = true;
            for (Value v: values) {
                if (first) first = false; else s += ", ";
                s += ToString(v);
            }
            s += "}";
            yield s;
        }
        case Type.Err t -> t.token.image;
        case Type.Int t -> t.token.image;
        case Type.Named t -> t.name;
        case Type.Object t -> {
            String s = "";
            s += ToString(t.parent, true) + " object {";
            Collection<Value> fields = Scope.ToList(t.fieldScope);
            Collection<Value> methods = Scope.ToList(t.methodScope);
            if (fields.isEmpty() && methods.isEmpty()) {
                // skip
            } else {
                boolean first = true;
                for (Value v : fields) {
                    if (first) {
                        first = false;
                        s += String.format("%n");
                    }
                    Value.Field f = (Value.Field)v;
                    Type.Object p = (Type.Object)f.parent;
                    s += (p.fieldOffset + f.offset) + ": " + String.format(ToString(f) + "%n");
                }
                for (Value v : methods) {
                    if (first) {
                        first = false;
                        s += String.format("%n");
                    }
                    Value.Method m = (Value.Method)v;
                    int x = m.offset;
                    Value.Method p = PrimaryMethodDeclaration(m.parent, m);
                    if (p != null) x += p.parent.methodOffset;
                    s += x + ": " + String.format(ToString(m) + "%n");
                }
            }
            s += "}";
            yield s;
        }
        case Type.Array t -> {
            String s = "array ";
            s += ToString(t.number);
            s += " of ";
            s += ToString(t.element, true);
            yield s;
        }
        case Type.Proc t -> {
            String s = "(";
            if (t.scope == null) s += "...";
            else {
                boolean first = true;
                for (Value v : Scope.ToList(t.scope)) {
                    if (first) first = false; else s += "; ";
                    s += ToString(v);
                }
            }
            s += ")";
            if (t.result != null) {
                s += ":";
                s += ToString(t.result, true);
            }
            yield s;
        }
        case Type.Record t -> {
            String s = "record {";
            Collection<Value> fields = Scope.ToList(t.fieldScope);
            if (fields.isEmpty()) {
                // skip
            } else {
                boolean first = true;
                for (Value v : fields) {
                    if (first) {
                        first = false;
                        s += String.format("%n");
                    }
                    Value.Field f = (Value.Field)v;
                    s += f.offset + ": " + String.format(ToString(f) + "%n");
                }
            }
            s += "}";
            yield s;
        }
        case Type.Ref t -> "ref " + ToString(t.target, true);
        case Type.Subrange t -> {
            String s = "[";
            s += ToString(ConstValue(t.minExp));
            s += "..";
            s += ToString(ConstValue(t.maxExp));
            s += "]";
            yield s;
        }
        };
    }

    String ToString(Expr expr) {
        expr = ConstValue(expr);
        return switch (expr) {
        case null -> null;
        case Expr.Address e -> e.value.toString(16);
        case Expr.Enum e -> String.valueOf(e.value);
        case Expr.Method e -> ToString(e.object, true) + "." + e.method.name;
        case Expr.Int e -> e.value.toString();
        case Expr.Proc e -> ToString(e.proc);
        case Expr.Text e -> e.value;
        case Expr.TypeExpr e -> ToString(e.value, true);
        default -> null;
        };
    }
    String ToString(Value value) {
        return switch (value) {
        case null -> null;
        case Value.Const v ->
            GlobalName(v) + ":" + ToString(v.type, true) + "="
            + ToString(v.expr);
        case Value.EnumElt v ->
            v.name + ":" + ToString(v.parent, true) + "=" + v.value;
        case Value.Field v ->
            v.name + ":" + ToString(v.type, true);
        case Value.Formal v ->
            v.mode + " " + v.name + ":" + ToString(v.type, true);
        case Value.Method v ->
            v.name + ":" + ToString(v.sig, true) + "=" + ToString(v.value);
        case Value.Unit v -> null;
        case Value.Procedure v ->
            GlobalName(v) + ":" + ToString(v.sig, true);
        case Value.Variable v ->
            GlobalName(v) + ":" + ToString(v.type, true);
        case Value.Tipe v ->
            GlobalName(v) + "=" + ToString(v.value, false);
        };
    }
}
