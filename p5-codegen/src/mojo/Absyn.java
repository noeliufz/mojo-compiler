/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */

package mojo;

import java.math.BigInteger;
import java.util.*;

import mojo.parse.*;

public abstract class Absyn {
    private static void usage() {
        throw new java.lang.Error("Usage: java mojo.Absyn <source>");
    }
    public static void main (String[] args) {
        if (args.length != 1) usage();
        java.io.File file = new java.io.File(args[0]);
        try {
            print(new Parser(Integer.BYTES, file).Unit());
        } catch (java.io.FileNotFoundException |
                 ParseException |
                 TokenMgrError e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } finally {
            System.err.flush();
        }
    }

    public static Token ID(String s) {
        return Token.newToken(ParserConstants.ID, s);
    }

    public static abstract class Node {
        public final Token token;
        Node(Token token) {
            this.token = token;
        }
    }

    public static abstract sealed class Stmt extends Node {
        public Stmt(Token t) {
            super(t);
        }

        /**
         * AssignSt = Expr "=" Expr ";".
         */
        public static final class Assign extends Stmt {
            public final Expr lhs, rhs;
            public Assign(Token t, Expr lhs, Expr rhs) {
                super(t);
                this.lhs = lhs;
                this.rhs = rhs;
            }
        }
        /**
         * CallSt = Expr "(" [ Actual { "," Actual } ] ")" ";".
         */
        public static final class Call extends Stmt {
            public final Expr expr;
            public Call(Token t, Expr expr) {
                super(t);
                this.expr = expr;
            }
        }
        /**
         * BreakSt = break ";".
         */
        public static final class Break extends Stmt {
            public Break(Token t) {
                super(t);
            }
        }
        /**
         * ForSt = for Id "=" Expr to Expr [ "by" Expr ] loop Stmt.
         */
        public static final class For extends Stmt {
            public final Token id;
            public Expr from, to, by;
            public final Stmt stmt;
            public For(Token t, Token id, Expr from, Expr to, Expr by, Stmt stmt)
            {
                super(t);
                this.id = id;
                this.from = from;
                this.to = to;
                if (by == null) by = new Expr.Int(t, BigInteger.ONE);
                this.by = by;
                this.stmt = stmt;
            }
            public Value.Variable var;
            public Scope scope;
        }
        /**
         * IfSt = if Expr then Stmt [ else Stmt ].
         */
        public static final class If extends Stmt {
            public final Expr expr;
            public final Stmt thenStmt, elseStmt;
            public If(Token t, Expr expr, Stmt thenStmt, Stmt elseStmt) {
                super(t);
                this.expr = expr;
                this.thenStmt = thenStmt;
                this.elseStmt = elseStmt;
            }
        }
        /**
         * LoopSt = [ while Expr ] loop Stmt [ until Expr ";" ]
         */
        public static final class Loop extends Stmt {
            public final Stmt stmt;
            public final Expr whileExpr, untilExpr;
            public Loop(Token t, Expr whileExpr, Stmt stmt, Expr untilExpr) {
                super(t);
                this.whileExpr = whileExpr;
                this.stmt = stmt;
                this.untilExpr = untilExpr;
            }
        }
        /**
         * ReturnSt = return [ Expr ] ";".
         */
        public static final class Return extends Stmt {
            public final Expr expr;
            public Return(Token t, Expr expr) {
                super(t);
                this.expr = expr;
            }
        }
        /**
         * Block = "{" { Decl } { Stmt } "}".
         */
        public static final class Block extends Stmt {
            public final List<Value> decls;
            public final List<Stmt> body;
            public Block(Token t, List<Value> decls, List<Stmt> body) {
                super(t);
                if (decls == null)
                    decls = new LinkedList<Value>();
                this.decls = decls;
                if (body == null)
                    body = new LinkedList<Stmt>();
                this.body = body;
            }
            public Scope scope;
        }
    }

    public static abstract sealed class Type extends Node {
        public int checkDepth = -1;
        public Type(Token t) {
            super(t);
        }
        public int size = 0;    // bytes
        public int align = 0;   // bytes
        public int bits = 0;
        public Value.Tipe tipe;
        private EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        enum Flag { CHECKED, ERRORED }
        public boolean checked() { return flags.contains(Flag.CHECKED); }
        public boolean errored() { return flags.contains(Flag.ERRORED); }
        public void checked(boolean b) { assert b; flags.add(Flag.CHECKED); }
        public void errored(boolean b) { assert b; flags.add(Flag.ERRORED); }
        public String uid;

        /**
         * ArrayType = "[" [ Expr ] "]" Type.
         */
        public static final class Array extends Type {
            public final Expr number; // null => open array
            public Type element;
            public Array(Token t, Expr number, Type element) {
                super(t);
                this.number = number;
                this.element = element;
            }
        }

        /**
         * EnumType = enum "{" [ IdList ] "}".
         */
        public static final class Enum extends Type {
            public final List<Token> elements;
            public final int number;
            public Enum(Token t, List<Token> elements) {
                super(t);
                if (elements == null)
                    elements = new LinkedList<Token>();
                this.elements = elements;
                this.number = elements.size();
            }
            protected Enum(Token t, int number) {
                super(t);
                this.elements = new LinkedList<Token>();
                this.number = number;
            }
            public Scope scope;
        }

        /**
         * TypeName = Id.
         */
        public static final class Named extends Type {
            public final String name;
            public Named(Token id) {
                super(id);
                this.name = id.image;
            }
            public Type type;
            public Value value;
        }

        /**
         * RecordType = "(" Fields ")".
         */
        public static final class Record extends Type {
            public final List<Value.Field> fields;
            public Record(Token t, List<Value.Field> fields) {
                super(t);
                if (fields == null)
                    fields = new LinkedList<Value.Field>();
                this.fields = fields;
            }
            public Scope fieldScope;
        }

        /**
         * ObjectType = [ TypeName | ObjectType ] "object" "{" Members "}".
         */
        public static final class Object extends Type {
            public Type parent;
            public final List<Value.Field> fields;
            public final List<Value.Method> methods, overrides;
            public Object(Token t, Type parent,
                          List<Value.Field> fields,
                          List<Value.Method> methods,
                          List<Value.Method> overrides) {
                super(t);
                if (parent == null)
                    parent = Type.Root.T;
                this.parent = parent;
                if (fields == null)
                    fields = new LinkedList<Value.Field>();
                this.fields = fields;
                if (methods == null)
                    methods = new LinkedList<Value.Method>();
                this.methods = methods;
                if (overrides == null)
                    overrides = new LinkedList<Value.Method>();
                this.overrides = overrides;
            }
            public Scope fieldScope, methodScope;
            public int fieldOffset = -1, fieldSize = -1, fieldAlign = -1;
            public int methodOffset = -1, methodSize = 0, overrideSize = 0;
        }

        /**
         * Signature = "(" Formals ")" [ ":" Type ]
         */
        public static abstract sealed class Proc extends Type {
            public final List<Value.Formal> formals;
            public Type result;
            public Proc(Token t, List<Value.Formal> formals, Type result) {
                super(t);
                if (formals == null)
                    formals = new LinkedList<Value.Formal>();
                this.formals = formals;
                this.result = result;
                this.minArgs = 0;
                this.maxArgs = Integer.MAX_VALUE;
            }
            public Proc(int minArgs, int maxArgs) {
                super(null);
                this.formals = null;
                this.result = null;
                this.minArgs = minArgs;
                this.maxArgs = maxArgs;
            }
            public Scope scope;

            public final int minArgs, maxArgs;
            public final boolean keywords = false;

            /**
             * Methods for handling calls to user-defined procedures and builtins
             * FIRST, LAST, ORD, VAL, NUMBER, NEW
             */
            public static final class User extends Proc {
                public User(Token t, List<Value.Formal> formals, Type result) {
                    super(t, formals, result);
                }
            }
            public static final class First extends Proc {
                private First() {
                    super(1, 1);
                }
                static final First T = new First();
                static void init() {
                    Scope.Insert(new Value.Procedure(ID("First"), T, null));
                    T.checked(true);
                }
            }
            public static final class Last extends Proc {
                private Last() {
                    super(1, 1);
                }
                static final Last T = new Last();
                static void init() {
                    Scope.Insert(new Value.Procedure(ID("Last"), T, null));
                    T.checked(true);
                }
            }
            public static final class New extends Proc {
                private New() {
                    super(1, Integer.MAX_VALUE);
                }
                static final New T = new New();
                static void init() {
                    Scope.Insert(new Value.Procedure(ID("New"), T, null));
                    T.checked(true);
                }
            }
            public static final class Ord extends Proc {
                private Ord() {
                    super(1, 1);
                }
                static final Ord T = new Ord();
                static void init() {
                    Scope.Insert(new Value.Procedure(ID("Ord"), T, null));
                    T.checked(true);
                }
            }
            public static final class Val extends Proc {
                private Val() {
                    super(2, 2);
                }
                static final Val T = new Val();
                static void init() {
                    Scope.Insert(new Value.Procedure(ID("Val"), T, null));
                    T.checked(true);
                }
            }
            public static final class Number extends Proc {
                private Number() {
                    super(1, 1);
                }
                static final Number T = new Number();
                static void init() {
                    Scope.Insert(new Value.Procedure(ID("Number"), T, null));
                    T.checked(true);
                }
            }
            public static final class Loophole extends Proc {
                private Loophole() {
                    super(2, 2);
                }
                static final Loophole T = new Loophole();
                static void init() {
                    Scope.Insert(new Value.Procedure(ID("Loophole"), T, null));
                    T.checked(true);
                }
            }
            public static final class BitSize extends Proc {
                private BitSize() {
                    super(1, 1);
                }
                static final BitSize T = new BitSize();
                static void init() {
                    Scope.Insert(new Value.Procedure(ID("BitSize"), T, null));
                    T.checked(true);
                }
            }
            public static final class ByteSize extends Proc {
                private ByteSize() {
                    super(1, 1);
                }
                static final ByteSize T = new ByteSize();
                static void init() {
                    Scope.Insert(new Value.Procedure(ID("ByteSize"), T, null));
                    T.checked(true);
                }
            }
        }

        /**
         * RefType = "ref" Type.
         */
        public static final class Ref extends Type {
            public Type target;
            public Ref(Token t, Type target) {
                super(t);
                this.target = target;
            }
        }

        /**
         * SubrangeType = "[" ConstExpr ".." ConstExpr "]"
         */
        public static final class Subrange extends Type {
            public Expr minExp, maxExp; // null => builtin
            public Type base; // null => unsealed
            BigInteger min;
            BigInteger max;
            public Subrange(Token t, Expr min, Expr max) {
                super(t);
                this.minExp = min;
                this.maxExp = max;
            }
        }

        /*
         * Builtin types
         */

        /**
         * A type for errors.
         */
        public static final class Err extends Type {
            public static final Err T = new Err();
            private Err() { super(ID("_error_")); }
        }

        /**
         * int
         */
        public static final class Int extends Type {
            public static final Int T = new Int();
            private Int() { super(ID("int")); }
            static void init() {
                Scope.Insert(new Value.Tipe(ID("int"), T));
            }
        }

        /**
         * bool
         */
        public abstract static class Bool {
            public static final Enum T = new Enum(ID("bool"), 2);
            public static final Value FALSE = new Value.EnumElt(ID("false"), BigInteger.ZERO, T);
            public static final Value TRUE = new Value.EnumElt(ID("true"), BigInteger.ONE, T);
            public static final Expr False = new Expr.Enum(null, T, BigInteger.ZERO);
            public static final Expr True = new Expr.Enum(null, T, BigInteger.ONE);
            static void init () {
                T.scope = Scope.PushNewClosed();
                Scope.Insert(FALSE);
                Scope.Insert(TRUE);
                Scope.PopNew();
                Scope.Insert(new Value.Tipe(ID("bool"), T));
                Scope.Insert(new Value.Const(ID("false"), T, False));
                Scope.Insert(new Value.Const(ID("true"), T, True));
            }
        }

        /**
         * byte
         */
        public abstract static class Byte {
            static BigInteger min = BigInteger.valueOf(-(1<<7));
            static BigInteger max = BigInteger.valueOf((1<<7)-1);
            public static final Subrange T
                = new Subrange(ID("byte"),
                               new Expr.Int(ID("min"), min),
                               new Expr.Int(ID("max"), max));
            static void init() {
                Scope.Insert(new Value.Tipe(ID("byte"), T));
            }
        }

        /**
         * char
         */
        public abstract static class Char {
            public static final Enum T = new Enum(ID("char"),
                                                  Character.MAX_CODE_POINT + 1);
            static void init () {
                T.scope = Scope.PushNewClosed();
                Scope.PopNew();
                Scope.Insert(new Value.Tipe(ID("char"), T));
            }
        }

        /**
         * Null
         */
        public abstract static class Null {
            public static final Ref T = new Ref(ID("Null"), null);
            public static final Expr Nil = new Expr.Address(ID("null"), BigInteger.ZERO);
            static void init () {
                Scope.Insert(new Value.Tipe(ID("Null"), T));
                Scope.Insert(new Value.Const(ID("null"), T, Nil));
            }
        }

        /**
         * Refany
         */
        public abstract static class Refany {
            public static final Ref T  = new Ref(ID("Refany"), null);
            static void init () {
                Scope.Insert(new Value.Tipe(ID("Refany"), T));
            }
        }

        /**
         * Address
         */
        public abstract static class Addr {
            public static final Ref T = new Ref(ID("Address"), null);
            static void init() {
                Scope.Insert(new Value.Tipe(ID("Address"), T));
            }
        }

        /**
         * Root
         */
        public abstract static class Root {
            public static final Object T
                = new Object(ID("Root"), null, null, null, null);
            static void init() {
                Scope.Insert(new Value.Tipe(ID("Root"), T));
            }
        }

        /**
         * Text
         */
        public abstract static class Text {
            // opaque ref type: implementation is ref array of byte
            public static final Ref T = new Ref(ID("Text"), null);
            static void init () {
                Scope.Insert(new Value.Tipe(ID("Text"), T));
            }
        }

        static void init() {
            Int.init();
            Bool.init();
            Byte.init();
            Char.init();
            Null.init();
            Refany.init();
            Addr.init();
            Root.init();
            Text.init();
            Proc.First.init();
            Proc.Last.init();
            Proc.Ord.init();
            Proc.Val.init();
            Proc.New.init();
            Proc.Number.init();
            Proc.Loophole.init();
            Proc.BitSize.init();
            Proc.ByteSize.init();
        }
    }

    /**
     * Decl = "const" { ConstDecl ";" }
     *      | "type" { TypeDecl ";" }
     *      | "var" { VariableDecl ";" }
     *      | "proc" ProcDecl.
     */
    public static abstract sealed class Value extends Node {
        public final String name;
        public String extName;
        public Scope scope;
        public int checkDepth = -1;
        /**
         * @param id the symbol (Id) being declared
         */
        public Value(Token id) {
            super(id);
            this.name = id.image;
        }

        private EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        private enum Flag {
            CHECKED, READONLY, EXTERNAL, UPLEVEL, ERROR, DECLARED, INITED, COMPILED,
            IMPORTED, EXPORTED, EXPORTABLE
        }
        public boolean checked() { return flags.contains(Flag.CHECKED); }
        public boolean readonly() { return flags.contains(Flag.READONLY); }
        public boolean external() { return flags.contains(Flag.EXTERNAL); }
        public boolean up_level() { return flags.contains(Flag.UPLEVEL); }
        public boolean error() { return flags.contains(Flag.ERROR); }
        public boolean declared() { return flags.contains(Flag.DECLARED); }
        public boolean inited() { return flags.contains(Flag.INITED); }
        public boolean compiled() { return flags.contains(Flag.COMPILED); }
        public boolean imported() { return flags.contains(Flag.IMPORTED); }
        public boolean exported() { return flags.contains(Flag.EXPORTED); }
        public boolean exportable() { return flags.contains(Flag.EXPORTABLE); }

        public void checked(boolean b) { assert b; flags.add(Flag.CHECKED); }
        public void readonly(boolean b) { assert b; flags.add(Flag.READONLY); }
        public void external(boolean b) { assert b; flags.add(Flag.EXTERNAL); }
        public void up_level(boolean b) { assert b; flags.add(Flag.UPLEVEL); }
        public void error(boolean b) { assert b; flags.add(Flag.ERROR); }
        public void declared(boolean b) { assert b; flags.add(Flag.DECLARED); }
        public void compiled(boolean b) { assert b; flags.add(Flag.COMPILED); }
        public void imported(boolean b) { if (b) flags.add(Flag.IMPORTED); else flags.remove(Flag.IMPORTED); }
        public void exported(boolean b) { if (b) flags.add(Flag.EXPORTED); else flags.remove(Flag.EXPORTED); }
        public void exportable(boolean b) { assert b; flags.add(Flag.EXPORTABLE); }

        /**
         * ConstDecl = Id [ ":" Type ] "=" Expr.
         */
        public static final class Const extends Value {
            public Type type;
            public Expr expr;
            public Const(Token id, Type type, Expr expr) {
                super(id);
                this.type = type;
                this.expr = expr;
                readonly(true);
            }
        }

        /**
         * EnumType = "{" [ IdList ] "}".
         */
        public static final class EnumElt extends Value {
            public final BigInteger value;
            public final Type parent;
            public EnumElt(Token id, BigInteger value, Type parent) {
                super(id);
                this.value = value;
                this.parent = parent;
                readonly(true);
            }
        }

        /**
         * Field = IdList ":" Type.
         */
        public static final class Field extends Value {
            public Type type;
            public final Type parent;
            public final Expr expr;
            public Field(Token id, Type parent, Type type) {
                super(id);
                this.parent = parent;
                this.type = type;
                this.expr = null;
            }
            public int offset = 0;
        }
        /**
         * Formal = [ "val" | "var" ] IdList ":" Type.
         */
        public static final class Formal extends Value {
            public enum Mode { VALUE, VAR, READONLY }
            public final Mode mode;
            public Type type;
            public Formal(Token id, Mode mode, Type type) {
                super(id);
                this.mode = mode;
                this.type = type;
                if (mode == Mode.READONLY) readonly(true);
            }
            public Type refType = null;
        }
        /**
         * Method = Id Signature [ "=" Expr ].
         */
        public static final class Method extends Value {
            public final boolean override;
            public final Type.Object parent;
            public Type.Proc sig;
            public final Expr expr;
            public Method(Token id, Type.Object parent, Type.Proc sig, Expr expr) {
                super(id);
                this.parent = parent;
                this.sig = sig;
                override = sig == null;
                this.expr = expr;
                readonly(true);
            }
            public int offset = 0;
            public Value value;
        }

        /**
         * Unit = { Decl } [ Block ].
         */
        public static final class Unit extends Value {
            public final List<Value> decls;
            public final Stmt stmt;
            public Unit(Token id, List<Value> decls, Stmt stmt) {
                super(id);
                this.decls = decls;
                this.stmt = stmt;
                readonly(true);
            }

            private static Unit current = null;
            public static Unit Current() { return current; }
            public static Unit Switch(Unit newUnit) {
                Unit oldUnit = current;
                current = newUnit;
                return oldUnit;
            }
            public static boolean IsInterface() {
                return current == null || current.stmt == null;
            }

            public Scope localScope;
            public ProcBody body;
            public final List<Type> types = new LinkedList<Type>();
        }
        /**
         * ProcDecl = Id Signature ( Block | ";" ).
         */
        public static final class Procedure extends Value {
            public final Type.Proc sig;
            public final Stmt block;
            public Procedure(Token id, Type.Proc sig, Stmt block) {
                super(id);
                this.sig = sig;
                this.block = block;
                readonly(true);
                if (sig instanceof Type.Proc.User && block == null) {
                    external(true);
                    this.extName = id.image;
                }
            }
            public Scope localScope;
            public ProcBody body;
            public Variable result;
        }
        /**
         * TypeDecl = Id "=" Type.
         */
        public static final class Tipe extends Value {
            public Type value;
            public Tipe(Token id, Type type) {
                super(id);
                this.value = type;
                type.tipe = this;
                readonly(true);
            }
        }
        /**
         * VarDecl = IdList ( ":" Type & "=" Expr ).
         */
        public static final class Variable extends Value {
            public Type type;
            public Expr init;
            public final Value.Formal formal;
            public Variable(Token id, Type type, Expr init) {
                super(id);
                this.formal = null;
                this.type = type;
                this.init = init;
            }
            public Variable(Formal formal, boolean indirect) {
                super(formal.token);
                this.formal = formal;
                this.type = formal.type;
                this.init = null;
                if (formal.mode != Formal.Mode.VALUE) indirect(true);
                if (formal.mode == Formal.Mode.READONLY) readonly(true);
                initDone(true);
                imported(false); // in spite of module depth
                if (indirect) indirect(true);
            }
            public Variable(Stmt.For stmt, Type type) {
                super(stmt.id);
                this.formal = null;
                this.type = type;
                this.init = null;
                readonly(true);
                initDone(true);
            }

            private enum Flag { INDIRECT, ADDRESS, GLOBAL, DONE, ZERO, PENDING, STATIC }
            private EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
            public boolean indirect() { return flags.contains(Flag.INDIRECT); }
            public boolean needsAddress() { return flags.contains(Flag.ADDRESS); }
            public boolean global() { return flags.contains(Flag.GLOBAL); }
            public boolean initDone() { return flags.contains(Flag.DONE); }
            public boolean initZero() { return flags.contains(Flag.ZERO); }
            public boolean initPending() { return flags.contains(Flag.PENDING); }
            public boolean initStatic() { return flags.contains(Flag.STATIC); }
            public void indirect(boolean b) { assert b; flags.add(Flag.INDIRECT); }
            public void needsAddress(boolean b) { assert b; flags.add(Flag.ADDRESS); }
            public void global(boolean b) { assert b; flags.add(Flag.GLOBAL); }
            public void initDone(boolean b) { assert b; flags.add(Flag.DONE); }
            public void initPending(boolean b) { if (b) flags.add(Flag.PENDING); else flags.remove(Flag.PENDING); }
            public void initStatic(boolean b) { if (b) flags.add(Flag.STATIC); else flags.remove(Flag.STATIC); }

            BigInteger min, max;

            public ProcBody proc;
        }
    }

    public static abstract sealed class Expr extends Node {
        public Type type;
        public final int precedence;
        public boolean checked = false;
        public Expr(Token t, int precedence, Type type) {
            super(t);
            this.precedence = precedence;
            this.type = type;
        }

        // see https://en.cppreference.com/w/c/language/operator_precedence
        public enum Op {
            Paren(1, null, "()"),
            Brack(1, null, "[]"),
            Dot(1, null, "."),
            Deref(1, null, "^"),

            Pos(2, null, "+"),
            Neg(2, null, "-"),
            Not(2, Type.Bool.T, "!"),
            BitNot(2, Type.Int.T, "~"),
            Address(2, Type.Addr.T, "&"),

            Mul(3, null, "*"),
            Div(3, null, "/"),
            Mod(3, Type.Int.T, "%"),

            Add(4, null, "+"),
            Sub(4, null, "-"),

            SLL(5, Type.Int.T, "<<"),
            SRA(5, Type.Int.T, ">>"),
            SRL(5, Type.Int.T, ">>>"),

            LT(6, Type.Bool.T, "<"),
            GT(6, Type.Bool.T, ">"),
            LE(6, Type.Bool.T, "<="),
            GE(6, Type.Bool.T, ">="),

            EQ(7, Type.Bool.T, "=="),
            NE(7, Type.Bool.T, "!="),

            BitAnd(8, Type.Int.T, "&"),

            BitXor(9, Type.Int.T, "xor"),

            BitOr(10, Type.Int.T, "|"),

            And(11, Type.Bool.T, "&&"),

            Or(12, Type.Bool.T, "||"),

            Cond(13, null, "?:"),

            Assign(14, null, "="),

            Comma(15, null, ",");

            public final int precedence;
            public final Type type;
            public final String op;
            private Op(int precedence, Type type, String op) {
                this.precedence = precedence;
                this.type = type;
                this.op = op;
            }

            public boolean in (Op ... ops) {
                return Arrays.asList(ops).contains(this);
            }

            public int signA() {
                switch(this) {
                case GT: return 1;
                case GE: return 1;
                case LT: return -1;
                case LE: return -1;
                case EQ: return 0;
                case NE: return 1;
                default: assert false; return 0;
                }
            }
            public int signB() {
                switch(this) {
                case GT: return 1;
                case GE: return 0;
                case LT: return -1;
                case LE: return 0;
                case EQ: return 0;
                case NE: return -1;
                default: assert false; return 0;
                }
            }
        }

        /**
         * Expr = Expr op Expr
         */
        public static final class Infix extends Expr {
            public final Op op;
            public final Expr left, right;
            public Infix(Token t, Op op, Expr left, Expr right) {
                super(t, op.precedence, op.type);
                this.op = op;
                this.left = left;
                this.right = right;
                assert switch (op) {
                case
                    Mul, Div, Mod,
                    Add, Sub,
                    SLL, SRA, SRL,
                    LT, GT, LE, GE,
                    EQ, NE,
                    BitAnd, BitXor, BitOr, And, Or -> true;
                default -> false;
                };
            }
        }

        /**
         * Expr = op Expr
         */
        public static final class Prefix extends Expr {
            public final Op op;
            public final Expr expr;
            public Prefix(Token t, Op op, Expr expr) {
                super(t, op.precedence, op.type);
                this.expr = expr;
                this.op = op;
                assert switch (op) {
                case Pos, Neg, Not, BitNot, Address -> true;
                default -> false;
                };
            }
        }

        /**
         * Expr = Expr "^"
         */
        public static final class Deref extends Expr {
            public Expr expr;
            public Deref(Token t, Expr expr) {
                super(t, Op.Deref.precedence, null);
                this.expr = expr;
            }
        }

        /**
         * Expr = Expr "." Id.
         */
        public static final class Qualify extends Expr {
            public Expr expr; // not final to allow for auto-magic dereference
            public final Token name;
            public Qualify(Token t, Expr expr, Token name) {
                super(t, Op.Dot.precedence, null);
                this.expr = expr;
                this.name = name;
            }
            public Value value;
            public Type.Object objType;
        }

        /**
         * Expr = Expr "[" Expr "]".
         */
        public static final class Subscript extends Expr {
            public Expr expr; // not final to allow for auto-magic dereference
            public Expr index; // not final to allow for bounds check
            public Subscript(Token t, Expr expr, Expr index) {
                super(t, Op.Brack.precedence, null);
                this.expr = expr;
                this.index = index;
            }
            public int depth; // open array depth before subscripting
        }

        /**
         * Expr = Expr "(" [ Actual { "," Actual } ] ")".
         */
        public static final class Call extends Expr {
            public final Expr proc;
            public Expr[] args;
            public Call(Token t, Expr proc, List<Expr> args) {
                super(t, Op.Paren.precedence, null);
                this.proc = proc;
                this.args = args.toArray(new Expr[args.size()]);
            }
            public Type.Proc procType;
        }

        /**
         * Expr = Id.
         */
        public static final class Named extends Expr {
            public final String name;
            public Named(Token id) {
                super(id, 0, null);
                this.name = id.image;
            }
            public Value value;
        }

        /**
         * Expr = Number.
         */
        public static final class Int extends Expr {
            public final BigInteger value;
            public Int(Token t, BigInteger value) {
                super(t, 0, Type.Int.T);
                this.value = value;
                this.checked = true;
            }
        }

        public static final class Enum extends Expr {
            public final BigInteger value;
            public Enum(Token t, Type type, BigInteger value) {
                super(t, 0, type);
                this.value = value;
                this.checked = true;
            }
        }

        public static final class Address extends Expr {
            public final BigInteger value;
            public Address(Token t, BigInteger value) {
                super(t, 0,
                      value.compareTo(BigInteger.ZERO) == 0
                      ? Type.Null.T
                      : Type.Addr.T);
                this.value = value;
                this.checked = true;
            }
        }

        /**
         * Expr = TextLiteral.
         */
        public static final class Text extends Expr {
            public final String value;
            public Text(Token t, String value) {
                super(t, 0, Type.Text.T);
                this.value = value;
                this.checked = true;
            }
        }

        /**
         * Actual = Type.
         *
         * This allows us to parse a type expression where we expect to
         * find an expression.
         */
        public static final class TypeExpr extends Expr {
            public Type value;
            public TypeExpr(Type value) {
                super(value.token, 0, null);
                this.value = value;
            }
        }

        public static final class Check extends Expr {
            public final Expr expr;
            public final BigInteger min, max;
            public Check(Expr expr, BigInteger min, BigInteger max) {
                super(null, 0, null);
                this.expr = expr;
                this.min = min;
                this.max = max;
            }
        }

        public static final class Proc extends Expr {
            public final Value proc;
            public Proc(Value proc) {
                super(null, 0, null);
                this.proc = proc;
            }
        }

        public static final class Method extends Expr {
            public Type object;
            public final String name;
            public final Value.Method method;
            public Method(Type.Object object, String name, Value.Method method) {
                super(null, 0, null);
                this.object = object;
                this.name = name;
                this.method = method;
            }
        }

        public static final class Cast extends Expr {
            enum Kind {
                NOP,            // code generator cannot tell the difference
                DA,             // designator -> open array
                SA,             // structure -> open array
                VA,             // value -> open array
                DS,             // designator -> structure
                SS,             // structure -> structure
                VS,             // value -> structure
                DV,             // designator -> value
                SV,             // structure -> value
                VV;             // value -> value
            };
            public Expr expr;
            public Type tipe;
            public Cast(Token t, Expr expr, Type type) {
                super(t, 0, type);
                this.expr = expr;
                this.tipe = type;
            }
            public Kind kind;
        }
    }

    public static void print (Value.Unit n) {
        print(n, 0);
        System.out.flush();
    }
    private static void print (Node n, int i) {
        switch (n) {
        case Value.Unit d -> {
            for (Value v: d.decls) print(v, i);
            if (d.stmt != null) print(d.stmt, i);
        }
        case Stmt.Block s -> {
            indent(i); sayln("{");
            for (Value v: s.decls)
                print(v, i+2);
            for (Stmt stmt: s.body)
                print(stmt, i+2);
            indent(i); sayln("}");
        }
        case Value.Tipe v -> {
            indent(i); say("type " + v.token + " = ");
            print(v.value, i+2);
            sayln(";");
        }
        case Value.Variable v -> {
            indent(i); say("var " + v.token);
            if (v.type != null) {
                say(": ");
                print(v.type, i+2);
            }
            if (v.init != null) {
                say(" = ");
                print(v.init, i+2);
            }
            sayln(";");
        }
        case Value.Const v -> {
            indent(i); say("const " + v.token);
            if (v.type != null) {
                say(": ");
                print(v.type, i+2);
            }
            if (v.expr != null) {
                say(" = ");
                print(v.expr, i+2);
            }
            sayln(";");
        }
        case Value.Procedure v -> {
            indent(i); say("proc " + v.token + " ");
            printSig(v.sig, i+2);
            if (v.block != null) {
                sayln("");
                print(v.block, i);
            } else
                sayln(";");
        }
        case Type.Err t -> {
            say(t.token);
        }
        case Type.Int t -> {
            say(t.token);
        }
        case Type.Proc t -> {
            say("proc ");
            printSig(t, i);
        }
        case Value.Formal v -> {
            indent(i);
            switch (v.mode) {
            case VALUE:
                say("val ");
                break;
            case VAR:
                say("var ");
                break;
            case READONLY:
                say("readonly ");
                break;
            }
            say(v.token);
            say(": ");
            print(v.type, i+2);
            sayln(";");
        }
        case Stmt.Assign s -> {
            indent(i);
            print(s.lhs, i+2);
            say(" "); say(s.token); say(" ");
            print(s.rhs, i+2);
            sayln(";");
        }
        case Stmt.Call s -> {
            indent(i);
            print(s.expr, i+2);
            sayln(";");
        }
        case Stmt.Break s -> {
            indent(i);
            say(s.token);
            sayln(";");
        }
        case Stmt.For s -> {
            indent(i);
            say(s.token); say(" "); say(s.id);
            say(" = "); print(s.from, i+2);
            say(" to "); print(s.to, i+2);
            if (s.by != null) {
                say(" by "); print(s.by, i+2);
            }
            sayln(" loop");
            print(s.stmt, i+2);
        }
        case Stmt.If s -> {
            indent(i); say(s.token); say(" ");
            print(s.expr, i+2); sayln(" then");
            print(s.thenStmt, i+2);
            if (s.elseStmt != null) {
                indent(i); sayln("else");
                print(s.elseStmt, i+2);
            }
        }
        case Stmt.Loop s -> {
            if (s.whileExpr != null) {
                indent(i);
                say("while ");
                print(s.whileExpr, i+2);
                sayln("");
            }
            indent(i);
            sayln(s.token);
            print(s.stmt, i+2);
            if (s.untilExpr != null) {
                indent(i);
                say("until ");
                print(s.untilExpr, i+2);
                sayln(";");
            }
        }
        case Stmt.Return s -> {
            indent(i);
            say(s.token);
            if (s.expr != null) {
                say(" ");
                print(s.expr, i+2);
            }
            sayln(";");
        }
        case Type.Array t -> {
            say(t.token);
            if (t.number != null) {
                say(" ");
                print(t.number, i+2);
            }
            say(" of ");
            print(t.element, i+2);
        }
        case Type.Enum t -> {
            say(t.token);
            boolean first = true;
            for (Token tok: t.elements) {
                if (first) {
                    sayln("");
                    first = false;
                } else {
                    sayln(",");
                }
                indent(i+2); say(tok.image);
            }
            if (!first) {
                sayln("");
                indent(i);
            }
            say("}");
        }
        case Type.Subrange t -> {
            say(t.token);
            print(t.minExp, i+2);
            say("..");
            print(t.maxExp, i+2);
            say("]");
        }
        case Type.Record t -> {
            say(t.token); say(" {");
            boolean first = true;
            for (Value v: t.fields) {
                if (first) {
                    sayln("");
                    first = false;
                }
                print(v, i+2);
            }
            say("}");
        }
        case Type.Object t -> {
            if (t == Type.Root.T) {
                say(t.token);
                return;
            }
            if (t.parent != null) {
                print(t.parent, i);
                say(" ");
            }
            say(t.token);
            say(" {");
            boolean first = true;
            for (Value v: t.fields) {
                if (first) {
                    sayln("");
                    first = false;
                }
                print(v, i+2);
            }
            for (Value v: t.methods) {
                if (first) {
                    sayln("");
                    first = false;
                }
                print(v, i+2);
            }
            for (Value v: t.overrides) {
                if (first) {
                    sayln("");
                    first = false;
                }
                print(v, i+2);
            }
            say("}");
        }
        case Type.Ref t -> {
            say(t.token); say(" "); print(t.target, i+2);
        }
        case Type.Named t -> {
            say(t.name);
        }
        case Value.Field v -> {
            indent(i);
            say(v.token + ": ");
            print(v.type, i+2);
            sayln(";");
        }
        case Value.Method v ->  {
            indent(i);
            say(v.token);
            printSig(v.sig, i);
            if (v.expr != null) {
                say(" = ");
                print(v.expr, i+2);
            }
            sayln(";");
        }
        case Expr.Infix e -> {
            subexp(e.left, e, i);
            say(" "); say(e.token); say(" ");
            subexp(e.right, e, i);
        }
        case Expr.Prefix e -> {
            say(e.token);
            subexp(e.expr, e, i);
        }
        case Expr.Deref e -> {
            subexp(e.expr, e, i);
            say(e.token);
        }
        case Expr.Qualify e -> {
            subexp(e.expr, e, i);
            say(e.token);
            say(e.name);
        }
        case Expr.Subscript e -> {
            subexp(e.expr, e, i);
            say(e.token);
            print(e.index, i+2);
            say("]");
        }
        case Expr.Call e -> {
            boolean first = true;
            subexp(e.proc, e, i);
            say(e.token);
            for (Expr expr: e.args) {
                if (first) first = false; else say(", ");
                print(expr, i+2);
            }
            say(")");
        }
        case Expr.Named e -> {
            say(e.name);
        }
        case Expr.Enum e -> {
            say(e.token); say(" (* 0x" + e.value.toString(16) + " *)");
        }
        case Expr.Int e -> {
            say(e.value.toString());
        }
        case Expr.Address e -> {
            say("16_"); say(e.value.toString(16));
        }
        case Expr.Text e -> {
            say(e.token); say(" (* ");
            for (byte b : e.value.getBytes()) {
                say(String.format("%02X", b));
            }
            say(" *)");
        }
        case Expr.TypeExpr e -> {
            print(e.value, i);
        }
        case Expr.Proc e -> {
            say(e.proc.name);
        }
        case Expr.Method e -> {
            print(e.object, i);
            say("." + e.name);
        }
        case Expr.Check e -> {
            say("_rangecheck_");
            print(e.expr, i+2);
            say(", " + e.min + ", " + e.max + ")");
        }
        default -> { assert false; }
        }
    }
    private static void printSig(Type.Proc t, int i) {
        if (t == null) return;
        if (t.formals == null) {
            say("(...)");
        } else if (t.formals.isEmpty()) {
            say(t.token); say(")");
        } else {
            sayln(t.token);
            for (Value f: t.formals) print(f, i+2);
            indent(i); say(")");
        }
        if (t.result != null) {
            say(": ");
            print(t.result, i+2);
        }
    }
    private static void subexp(Expr child, Expr parent, int i) {
        if (child.precedence >= parent.precedence) say("(");
        print(child, i+2);
        if (child.precedence >= parent.precedence) say(")");
    }
    private static void indent(int d) {
        for (int i = 0; i < d; i++)
            System.out.print(' ');
    }
    private static void say(String s) {
        System.out.print(s);
    }
    private static void sayln(String s) {
        System.out.println(s);
        System.out.flush();
    }
    private static void say(Token s) {
        System.out.print(s.image);
    }
    private static void sayln(Token s) {
        System.out.println(s.image);
        System.out.flush();
    }
}
