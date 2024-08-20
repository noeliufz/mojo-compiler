/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package Translate;

import java.util.List;
import Translate.Temp.*;

/**
 * Intermediate code trees (IR)
 */
public interface Tree {
    default Exp[] kids() { return new Exp[] {}; }
    public sealed interface Exp extends Tree {
        default Exp build(Exp[] kids) { return this; }

        /**
         * Binary operations: apply operator to results of evaluating
         * first left and then right.
         */
        public sealed interface BINOP extends Exp {
            public record ADD (Exp left, Exp right) implements BINOP {}
            public record AND (Exp left, Exp right) implements BINOP {}
            public record DIV (Exp left, Exp right) implements BINOP {}
            public record DIVU(Exp left, Exp right) implements BINOP {}
            public record MOD (Exp left, Exp right) implements BINOP {}
            public record MODU(Exp left, Exp right) implements BINOP {}
            public record MUL (Exp left, Exp right) implements BINOP {}
            public record OR  (Exp left, Exp right) implements BINOP {}
            public record SLL (Exp left, Exp right) implements BINOP {}
            public record SRA (Exp left, Exp right) implements BINOP {}
            public record SRL (Exp left, Exp right) implements BINOP {}
            public record SUB (Exp left, Exp right) implements BINOP {}
            public record XOR (Exp left, Exp right) implements BINOP {}
            public record NOR (Exp left, Exp right) implements BINOP {}
            public Exp left();
            public Exp right();
            default String op() {
                return switch (this) {
                case ADD  x -> "ADD";
                case AND  x -> "AND";
                case DIV  x -> "DIV";
                case DIVU x -> "DIVU";
                case MOD  x -> "MOD";
                case MODU x -> "MODU";
                case MUL  x -> "MUL";
                case OR   x -> "OR";
                case SLL  x -> "SLL";
                case SRA  x -> "SRA";
                case SRL  x -> "SRL";
                case SUB  x -> "SUB";
                case XOR  x -> "XOR";
                case NOR  x -> "NOR";
                };
            }
            default Exp[] kids() { return new Exp[] { left(), right() }; }
            default Exp build(Exp[] kids) {
                return switch (this) {
                case ADD  x -> new ADD (kids[0], kids[1]);
                case AND  x -> new AND (kids[0], kids[1]);
                case DIV  x -> new DIV (kids[0], kids[1]);
                case DIVU x -> new DIVU(kids[0], kids[1]);
                case MOD  x -> new MOD (kids[0], kids[1]);
                case MODU x -> new MODU(kids[0], kids[1]);
                case MUL  x -> new MUL (kids[0], kids[1]);
                case OR   x -> new OR  (kids[0], kids[1]);
                case SLL  x -> new SLL (kids[0], kids[1]);
                case SRA  x -> new SRA (kids[0], kids[1]);
                case SRL  x -> new SRL (kids[0], kids[1]);
                case SUB  x -> new SUB (kids[0], kids[1]);
                case XOR  x -> new XOR (kids[0], kids[1]);
                case NOR  x -> new NOR (kids[0], kids[1]);
                };
            }
        }
        /**
         * A procedure call: evaluate func to obtain address of subroutine
         * then each of the arguments in order.
         */
        public record CALL(Exp func, Exp link, Exp[] args) implements Exp
        {
            public Exp[] kids() {
                Exp[] kids = new Exp[args.length + 2];
                kids[0] = func;
                kids[1] = link;
                System.arraycopy(args, 0, kids, 2, args.length);
                return kids;
            }
            public Exp build(Exp[] kids) {
                Exp[] args = new Exp[kids.length - 2];
                System.arraycopy(kids, 2, args, 0, args.length);
                return new CALL(kids[0], kids[1], args);
            }
        }
        /**
         * A constant integer.
         */
        public record CONST(long value) implements Exp {}

        /**
         * An expression sequence.
         * Execute stm for side-effects then evaluate exp for result.
         */
        public record ESEQ(Stm stm, Exp exp) implements Exp
        {
            public Exp[] kids() { throw new Error("kids() not applicable to ESEQ"); }
            public Exp build(Exp[] kids) { throw new Error("build() not applicable to ESEQ"); }
        }

        /**
         * A memory access to the contents of memory at address exp+offset.
         * (where size is the allocated size of the container)
         */
        public record MEM(Exp exp, CONST offset, int size, boolean signed, boolean struct) implements Exp
        {
            public MEM(Exp exp, CONST offset, int size) { this(exp, offset, size, false, false); }
            public Exp[] kids() { return new Exp[] { exp }; }
            public Exp build(Exp[] kids) { return new MEM(kids[0], offset, size, signed, struct); }
        }
        /**
         * A symbolic constant naming a labeled location.
         */
        public record NAME(Label label) implements Exp {}

        /**
         * A temporary (one of any number of "registers").
         */
        public record TEMP(Temp temp) implements Exp {}
    }
    public sealed interface Stm extends Tree {
        default Stm build(Exp[] kids) { return this; }

        public sealed interface CJUMP extends Stm {
            public record BEQ(Exp left, Exp right, Label iftrue, Label iffalse) implements CJUMP {};
            public record BNE(Exp left, Exp right, Label iftrue, Label iffalse) implements CJUMP {};
            public record BGE(Exp left, Exp right, Label iftrue, Label iffalse) implements CJUMP {};
            public record BLE(Exp left, Exp right, Label iftrue, Label iffalse) implements CJUMP {};
            public record BGT(Exp left, Exp right, Label iftrue, Label iffalse) implements CJUMP {};
            public record BLT(Exp left, Exp right, Label iftrue, Label iffalse) implements CJUMP {};
            public Exp left();
            public Exp right();
            public Label iftrue();
            public Label iffalse();
            default String op() {
                return switch (this) {
                case BEQ x -> "BEQ";
                case BNE x -> "BNE";
                case BGE x -> "BGE";
                case BLE x -> "BLE";
                case BGT x -> "BGT";
                case BLT x -> "BLT";
                };
            }
            default Exp[] kids() { return new Exp[] { left(), right() }; }
            default CJUMP build(Exp[] kids) {
                return switch (this) {
                case BEQ x -> new BEQ(kids[0], kids[1], iftrue(), iffalse());
                case BNE x -> new BNE(kids[0], kids[1], iftrue(), iffalse());
                case BGE x -> new BGE(kids[0], kids[1], iftrue(), iffalse());
                case BLE x -> new BLE(kids[0], kids[1], iftrue(), iffalse());
                case BGT x -> new BGT(kids[0], kids[1], iftrue(), iffalse());
                case BLT x -> new BLT(kids[0], kids[1], iftrue(), iffalse());
                };
            }
        }

        /**
         * An expression statement: evaluate exp, discarding the result.
         */
        public record EXP(Exp exp) implements Stm
        {
            public Exp[] kids() { return new Exp[] { exp }; }
            public Stm build(Exp[] kids) { return new EXP(kids[0]); }
        }

        /**
         * An unconditional jump: evaluate exp to obtain target address
         * then jump to that target.
         * (targets is the complete list of possible targets.
         */
        public record JUMP(Exp exp, Label[] targets) implements Stm
        {
            public JUMP(Label target) {
                this(new Exp.NAME(target), new Label[]{target});
            }
            public Exp[] kids() { return new Exp[] { exp }; }
            public Stm build(Exp[] kids) { return new JUMP(kids[0], targets); }
        }

        /**
         * Label this code location with a symbolic name.
         */
        public record LABEL(Label label) implements Stm {}

        /**
         * A move statement: evaluate the storage destination expression dst,
         * then the source value expression src, then move the source value
         * to the addressed storage (memory/temporary).
         */
        public record MOVE(Exp dst, Exp src) implements Stm
        {
            public Exp[] kids() {
                if (dst instanceof Exp.MEM m) return new Exp[] { m.exp, src };
                return new Exp[] { src };
            }
            public Stm build(Exp[] kids) {
                if (dst instanceof Exp.MEM m)
                    return new MOVE(new Exp.MEM(kids[0], m.offset, m.size, m.signed, m.struct), kids[1]);
                return new MOVE(dst, kids[0]);
            }
        }

        /**
         * A sequence statement: execute left then right.
         */
        public record SEQ(Stm left, Stm right) implements Stm
        {
            public Exp[] kids() { throw new Error("kids() not applicable to SEQ"); }
            public Stm build(Exp[] kids) { throw new Error("build() not applicable to SEQ"); }
        }
    }

    public static class Print implements java.util.function.Consumer<List<Stm>> {
        final java.io.PrintWriter out;

        public Print(java.io.PrintWriter o) {
            out = o;
        }

        private void indent(int d) {
            for (int i = 0; i < d; i++)
                out.print(' ');
        }

        private void sayln(String s) {
            out.println(s);
            out.flush();
        }

        private void say(String s) {
            out.print(s);
        }

        private void say(Object o) {
            out.print(o.toString());
        }

        public void accept(List<Stm> stms) {
            for (Stm stm : stms) {
                print(stm, 0);
                out.println();
            }
            out.flush();
        }

        public void accept(Stm stm) {
            if (stm == null) return;
            print(stm, 0);
            out.println();
            out.flush();
        }

        private void print(Stm stm, int d) {
            switch (stm) {
            case Stm.SEQ s -> {
                print(s.left, d);
                sayln(",");
                print(s.right, d);
            }
            case Stm.LABEL s -> {
                indent(d);
                say("LABEL ");
                say(s.label);
            }
            case Stm.JUMP s -> {
                indent(d);
                sayln("JUMP(");
                print(s.exp, d + 1);
                say(")");
            }
            case Stm.CJUMP s -> {
                indent(d);
                say(s.op());
                sayln("(");
                print(s.left(), d + 1);
                sayln(",");
                print(s.right(), d + 1);
                sayln(",");
                indent(d + 1);
                say(s.iftrue());
                say(", ");
                say(s.iffalse());
                say(")");
            }
            case Stm.MOVE s -> {
                indent(d);
                sayln("MOVE(");
                print(s.dst, d + 1);
                sayln(",");
                print(s.src, d + 1);
                say(")");
            }
            case Stm.EXP s -> {
                indent(d);
                sayln("EXP(");
                print(s.exp, d + 1);
                say(")");
            }
            }
        }

        public void print(Exp exp, int d) {
            switch (exp) {
            case Exp.BINOP e -> {
                indent(d);
                say(e.op());
                sayln("(");
                print(e.left(), d + 1);
                sayln(",");
                print(e.right(), d + 1);
                say(")");
            }
            case Exp.MEM e -> {
                indent(d);
                sayln("MEM(");
                print(e.exp, d + 1);
                sayln(", ");
                print(e.offset, d + 1);
                say(", ");
                say(e.size);
                say(", ");
                say(e.signed);
                say(", ");
                say(e.struct);
                say(")");
            }
            case Exp.TEMP e -> {
                indent(d);
                say("TEMP ");
                say(e.temp);
            }
            case Exp.ESEQ e -> {
                indent(d);
                sayln("ESEQ(");
                print(e.stm, d + 1);
                sayln(",");
                print(e.exp, d + 1);
                say(")");
            }
            case Exp.NAME e -> {
                indent(d);
                say("NAME ");
                say(e.label);
            }
            case Exp.CONST e -> {
                indent(d);
                say("CONST ");
                say(String.valueOf(e.value));
            }
            case Exp.CALL e -> {
                indent(d);
                sayln("CALL(");
                print(e.func, d + 1);
                sayln(",");
                print(e.link, d + 1);
                for (int i = 0; i < e.args.length; i++) {
                    sayln(",");
                    print(e.args[i], d + 1);
                }
                say(")");
            }
            }
        }
    }
}
