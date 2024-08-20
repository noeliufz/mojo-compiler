/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package x64;

import java.math.BigInteger;
import java.util.*;

import Assem.Instr;
import Translate.Temp;
import Translate.Tree;
import Translate.Temp.*;
import Translate.Tree.*;
import Translate.Tree.Exp.*;
import Translate.Tree.Exp.BINOP.*;
import Translate.Tree.Stm.*;

public final class Codegen implements Frame.CodeGen {
  final Frame frame;

  Codegen(Frame f) {
    frame = f;
  }

  private List<Instr> insns;

  public List<Instr> apply(List<Stm> stms) {
    insns = new LinkedList<Instr>();
    for (Stm stm : stms)
      visit(stm);
    return insns;
  }

  static Instr MOVE(Temp d, Temp s) {
    return new Instr.MOVE("\tmovq `s0,`d0", d, s);
  }

  private static Temp[] T(Temp... a) {
    return a;
  }

  static Instr OPER(String a, Temp[] d, Temp[] s, Label... j) {
    return new Instr.OPER("\t" + a, d, s, j);
  }

  private void visit(Stm stm) {
    switch (stm) {
      case MOVE(MEM(Exp l, CONST(long o), int size, boolean signed, boolean struct), Exp r) -> {
        // TODO
        Temp base = visit(l);
        Temp value = visit(r);
        String offsetBase = o + "(`s0)";
        insns.add(OPER("movq `s1," + offsetBase, T(), T(base, value)));
      }
      case MOVE(Exp l, Exp r) -> {
        // TODO
        // if (r instanceof Exp.CONST) {
        // long value = ((Exp.CONST) r).value();
        // if (value == 0) {
        // Temp left = visit(l);
        // insns.add(OPER("xorq `d0,`d0", T(left), T()));
        // } else {
        // insns.add(OPER("movq $" + value + ",`d0", T(visit(l)), T()));
        // }
        // break;
        // }
        insns.add(MOVE(visit(l), visit(r)));
      }
      case JUMP(Exp exp, Label[] targets) -> {
        // TODO
        insns.add(OPER("jmp `j0", T(), T(), targets));
      }
      case CJUMP j -> { // TODO
        Temp left = visit(j.left());
        Temp right = visit(j.right());
        Label trueLabel = j.iftrue();
        Label falseLabel = j.iffalse();

        insns.add(OPER("cmpq `s1,`s0", T(), T(left, right)));

        String jumpInstr = switch (j.op()) {
          case "BEQ" -> "je";
          case "BNE" -> "jne";
          case "BLT" -> "jl";
          case "BLE" -> "jle";
          case "BGT" -> "jg";
          case "BGE" -> "jge";
          default -> throw new IllegalArgumentException("Unsupported relop");
        };

        insns.add(OPER(jumpInstr + " " + trueLabel, T(), T(), new Label[] { trueLabel, falseLabel }));
      }
      case LABEL(Label l) -> {
        // TODO
        insns.add(new Instr.LABEL(l.toString() + ":", l));
      }
      case EXP(Exp e) -> visit(e);
      case SEQ s -> throw new Error("canonical trees should have no SEQ");
    }
  }

  public Temp visit(Exp exp) {
    return switch (exp) {
      case CONST(long c) -> {
        // TODO
        Temp t = new Temp();
        // for clearing the register (use const 0), adopt `xorq` to make it faster
        // and save 4 bytes space to store a const number compared with `mov`
        if (c == 0) {
          insns.add(OPER("xorq `d0,`d0", T(t), T()));
          yield t;
        }
        // if the constant is 32 bit (4 bytes) use movq is ok.
        // as movq will read in a 32 bit number and expand it to 64 bits
        // Or, we will use movabsq to read in directly a 64 bit number
        if (CONST32(c)) {
          insns.add(OPER("movq $" + c + ",`d0", T(t), T()));
        } else {
          insns.add(OPER("movabsq $" + c + ",`d0", T(t), T()));
        }
        yield t;
      }
      case NAME(Label label) -> {
        // TODO
        Temp t = new Temp();
        insns.add(OPER("leaq " + label + "(%rip),`d0", T(t), T()));
        yield t;
      }
      case TEMP(Temp t) -> t;
      case BINOP b -> {
        // TODO
        String operator = switch (b.op()) {
          case "ADD" -> "addq";
          case "AND" -> "and";
          case "MUL" -> "imulq";
          case "OR" -> "or";
          case "SLL" -> "sal";
          case "SRA" -> "sar";
          case "SRL" -> "shr";
          case "SUB" -> "subq";
          case "XOR" -> "xor";
          case "NOR" -> "nor";
          default -> "";
        };

        if (operator == "nor") {
          Temp d0 = new Temp();
          insns.add(MOVE(d0, visit(b.left())));
          insns.add(OPER("or `s0,`d0", T(d0), T(visit(b.right()), d0)));
          insns.add(OPER("not `s0,`d0", T(d0), T(visit(b.right()), d0)));
          yield d0;
        }

        if (operator != "") {
          // if (b.right() instanceof Exp.CONST) {
          // yield BINOP(operator, b.left(), ((Exp.CONST) b.right()).value());
          // }
          yield BINOP(operator, b.left(), b.right());
        }

        // division and mod operations
        boolean signed;
        switch (b.op()) {
          case "DIV" -> {
            operator = "idivq ";
            signed = true;
          }
          case "DIVU" -> {
            operator = "divq ";
            signed = false;
          }
          case "MOD" -> {
            operator = "idivq ";
            signed = true;
          }
          case "MODU" -> {
            operator = "divq ";
            signed = false;
          }
          default -> {
            throw new Error();
          }
        }

        Temp t = new Temp();
        Temp left = visit(b.left());
        Temp right = visit(b.right());
        // move divident to RAX
        insns.add(OPER("movq `s0,`d0", T(frame.RAX), T(left)));
        // check signed to user different operator
        if (signed) {
          // if signed, use cqto to expand signed bit
          insns.add(OPER("cqto", T(), T()));
        } else {
          // if not signed, clear the RDX register
          insns.add(OPER("xorq `d0,`d0", T(frame.RDX), T()));
        }

        // operate the division
        insns.add(OPER(operator + "`s0", T(t, frame.RAX, frame.RDX), T(right, frame.RAX, frame.RDX)));

        if (b.op() == "MOD" || b.op() == "MODU") {
          // move the remainder from RDX
          insns.add(OPER("movq `s0,`d0", T(t), T(frame.RDX)));
        } else {
          // move the quotient from RAX
          insns.add(OPER("movq `s0,`d0", T(t), T(frame.RAX)));
        }
        yield t;

      }
      case MEM(Exp e, CONST(long o), int size, boolean signed, boolean struct) -> {
        // TODO
        Temp t = new Temp();
        Temp base = visit(e);
        String offsetBase = o + "(`s0)";
        String command = "mov";
        // if the size is 8 (quad word) use directly the 'movq'
        if (size == 8) {
          ;
        } else {
          // else choose different command to expand
          command = signed ? (command + "s") : (command + "z");
          command = command + switch (size) {
            case 1 -> {
              yield "b";
            }
            case 2 -> {
              yield "w";
            }
            case 4 -> {
              yield "l";
            }
            default -> {
              throw new Error();
            }
          };
        }
        command = command + "q ";
        insns.add(OPER(command + offsetBase + ",`d0", T(t), T(base)));
        yield t;
      }
      case CALL(Exp func, Exp link, Exp[] args) -> {
        String op;
        LinkedList<Temp> uses = new LinkedList<Temp>();
        if (func instanceof NAME(Label label)) {
          // CALL(NAME, ...)
          op = "call " + label;
        } else {
          // CALL(Exp, ...)
          uses.add(visit(func));
          op = "call *`s0";
        }
        if (link instanceof CONST(long c)) {
          assert c == 0;
        } else {
          insns.add(MOVE(Frame.R10, visit(link)));
          uses.add(Frame.R10);
        }
        int argStackUsed = 0;
        int argRegUsed = 0;
        for (Tree.Exp arg : args) {
          if (arg instanceof MEM(Exp e, CONST(long offset), int size, boolean signed, boolean struct)
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
                visit(new MOVE(new MEM(new TEMP(Frame.RSP),
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
              visit(new MOVE(new MEM(new TEMP(Frame.RSP),
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
        yield Frame.RAX;
      }
      case ESEQ e -> throw new Error("canonical trees should have no ESEQ");
    };
  }

  private Temp BINOP(String op, Exp l, long r) {
    assert CONST32(r);
    Temp s0 = visit(l);
    Temp d0 = new Temp();
    insns.add(MOVE(d0, s0));
    insns.add(OPER(op + " $" + r + ",`d0", T(d0), T(d0)));
    return d0;
  }

  private Temp BINOP(String op, Exp l, Exp r) {
    Temp s0 = visit(l);
    Temp s1 = visit(r);
    Temp d0 = new Temp();
    insns.add(MOVE(d0, s0));
    insns.add(OPER(op + " `s0,`d0", T(d0), T(s1, d0)));
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

  static boolean CONST32(long i) {
    return BigInteger.valueOf(i).bitLength() < 32;
  }
}
