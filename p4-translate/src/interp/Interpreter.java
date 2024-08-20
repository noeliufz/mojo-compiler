/* Copyright (C) 2024, Antony L Hosking and Ben L. Titzer.
 * All rights reserved.  */
package interp;

import Translate.Frag;
import Translate.Temp;
import Translate.Temp.Label;
import Translate.Tree;
import Translate.Tree.Stm;
import Translate.Tree.Exp;
import Translate.Tree.Stm.*;
import Translate.Tree.Exp.*;
import java.util.*;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

/**
 * An interpreter capable of running programs expressed as Tree code.
 */
public class Interpreter {
    private HashMap<Label, LinearTreeCode> procedures = new HashMap<Label, LinearTreeCode>();

    private final DataLayout layout;
    private final boolean trace;
    private static final int RESERVED_PAGE = 4096;
    private static final int MEMORY_WORDS = 256 * 1024;
    private static final int MAX_ALLOCATION = MEMORY_WORDS * Frame.wordSize;

    private static final int EXTERNAL_NEW = -1;
    private static final int EXTERNAL_PUTCHAR = -2;
    private static final int EXTERNAL_GETCHAR = -3;
    private static final int EXTERNAL_BADPTR = -4;
    private static final int EXTERNAL_BADSUB = -5;
    private static final int EXTERNAL_MALLOC = -6;
    private static final int EXTERNAL_EXIT = -7;
    private static final int EXTERNAL_MEMCPY = -8;
    private static final int EXTERNAL_MEMMOVE = -9;
    private static final int EXTERNAL_PUTS = -10;
    private static final int FIRST_CODE_ADDRESS = -11;

    private static Integer ZERO = 0;

    // Dynamic state of the machine.
    private int sp;  // stack pointer register
    private int fp;  // frame pointer register
    private int ra;  // return value register
    private int fl;  // frame link register
    private ByteBuffer memory; // memory, including statically allocated data, heap, and stack
    private int brk; // end of heap memory, used for bump-pointer malloc
    private Activation topOfStack;
    private String exception = null;

    public Interpreter(DataLayout layout, List<Frag> frags, boolean trace) {
	this.layout = layout;
	this.trace = trace;
	// Translate all procedures and give them addresses, if they don't already have one.
	for (Frag f : frags) {
	    if (f instanceof Frag.Proc) {
		Frag.Proc p = (Frag.Proc)f;
		LinearTreeCode lc = linearize(p);
		if (trace) dumpCode(lc);
		lc.address = layout.getCodeAddr(p.frame.name);
		procedures.put(p.frame.name, lc);
	    }
	}
    }

    private Evaluator eval;
    public int run(Frag.Proc p) {
	// initialize memory
	memory = ByteBuffer.allocate(MEMORY_WORDS * Frame.wordSize);
	layout.init(memory);
	// Pass arguments
	sp = fp = memory.capacity();
	pushFrame(procedures.get(p.frame.name), 0, new Tree.Exp[]{});
	eval = new Evaluator();
	while (true) {
	    if (exception != null) throw new RuntimeException(exception);
	    if (topOfStack == null) return ra;  // end of program
	    if (topOfStack.pc >= topOfStack.lc.stms.size()) {
		popFrame();
		continue;
	    }
	    Stm s = topOfStack.lc.stms.get(topOfStack.pc++);
	    eval.accept(s);
	}
    }

    public class Evaluator implements java.util.function.Consumer<Tree.Stm> {
        public void accept(Stm stm) {
            switch (stm) {
            case SEQ n -> throw new Error();
            case LABEL n -> { /* skip */ }
            case JUMP n -> {
                if (trace) print("JUMP(" + n.targets()[0] + ")");
                doGoto(n.targets()[0]);
            }
            case MOVE n -> {
                if (n.dst() instanceof MEM) {
                    MEM m = (MEM)n.dst();
                    int addr = eval(m.exp());
                    int val = eval(n.src());
                    int size = m.size();
                    long offset = m.offset().value();
                    int o = (int)offset;
                    assert o == offset;
                    if (trace) {
                        StringBuilder buf = new StringBuilder();
                        buf.append("MOVE(MEM(");
                        buf.append(addr);
                        buf.append(" + " + o);
                        buf.append("," + size);
                        buf.append("), ");
                        buf.append(val);
                        buf.append(")");
                        print(buf.toString());
                    }
                    addr = checkMemoryAddr(addr, o, size);
                    switch (size) {
                    case Byte.BYTES -> memory.put(addr, (byte)val);
                    case Short.BYTES -> memory.putShort(addr, (short)val);
                    case Integer.BYTES -> memory.putInt(addr, (int)val);
                    default -> { assert false; }
                    }
                    if (trace) {
                        StringBuilder buf = new StringBuilder();
                        buf.append("  memory[" + addr + "] <-- " + val);
                        print(buf.toString());
                    }
                } else if (n.dst() instanceof TEMP) {
                    TEMP m = (TEMP)n.dst();
                    Activation f = topOfStack;
                    int val = eval(n.src());
                    f.returnTemp = m.temp();
                    if (trace) {
                        StringBuilder buf = new StringBuilder();
                        buf.append("MOVE(TEMP(");
                        buf.append(m.temp());
                        buf.append("), ");
                        buf.append(val);
                        buf.append(")");
                        print(buf.toString());
                    }
                    f.writeTemp(m.temp(), val);
                    if (trace) {
                        StringBuilder buf = new StringBuilder();
                        buf.append("  " + m.temp() + " <-- " + val);
                        print(buf.toString());
                    }
                } else
                    throwError("MOVE() to invalid left-hand-side");
            }
            case EXP n -> eval(n.exp());
            case CJUMP n -> {
                int l = eval(n.left());
                int r = eval(n.right());
                boolean cond = switch (n) {
                case CJUMP.BEQ b -> l == r;
                case CJUMP.BNE b -> l != r;
                case CJUMP.BGE b -> l >= r;
                case CJUMP.BLE b -> l <= r;
                case CJUMP.BLT b -> l < r;
                case CJUMP.BGT b -> l > r;
                };
                Label target = cond ? n.iftrue() : n.iffalse();
                if (trace) print(n.op() + "(" + cond + ") => " + target);
                doGoto(target);
            }
            }
        }
        public int eval(Exp exp) {
            switch (exp) {
            case MEM n -> {
                int addr = eval(n.exp());
                int size = n.size();
                long offset = n.offset().value();
                int o = (int)offset;
                assert o == offset;
                if (trace) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("MEM(");
                    buf.append(addr);
                    buf.append(" + ");
                    buf.append(o);
                    buf.append("," + size);
                    buf.append(")");
                    print(buf.toString());
                }
                addr = checkMemoryAddr(addr, o, size);
                int result;
                switch (n.size()) {
                case Byte.BYTES -> {
                    result = memory.get(addr);
                    if (!n.signed()) result &= 0xFF;
                }
                case Short.BYTES -> {
                    result = memory.getShort(addr);
                    if (!n.signed()) result &= 0xFFFF;
                }
                case Integer.BYTES -> {
                    result = memory.getInt(addr);
                    if (!n.signed()) result &= 0xFFFFFFFF;
                }
                default -> { assert false; result = 0; }
                };
                if (trace) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("  " + result + " <-- memory[" + addr + "]");
                    print(buf.toString());
                }
                return result;
            }
            case TEMP n -> {
                Integer val = topOfStack.readTemp(n.temp());
                if (trace) {
                    print("TEMP(" + n.temp() + ")");
                    print("  " + val + " <-- " + n.temp());
                }
                if (val != null) return val;
                throwError("Temp(" + n.temp() + ") has no value in this frame");
                return 0;
            }
            case ESEQ n -> throw new Error();
            case NAME n -> {
                Integer addr = layout.lookup(n.label());
                if (trace) print("NAME(" + n.label() + ") = " + addr);
                if (addr != null) return addr;
                throwError("NAME(" + n.label() + ") does not have an address");
                return 0;
            }
            case CONST n -> {
                if (trace) print("CONST(" + n.value() + ")");
                return (int)n.value();
            }
            case CALL n -> {
                topOfStack.returnTemp = null;
                int func = eval(n.func());
                int link = eval(n.link());

                if (func > FIRST_CODE_ADDRESS) {
                    int[] args = new int[n.args().length];
                    for (int i = 0; i < args.length; i++) args[i] = eval(n.args()[i]);
                    if (trace) {
                        StringBuilder buf = new StringBuilder();
                        buf.append("CALL(func:");
                        buf.append(func);
                        buf.append(", link:");
                        buf.append(link);
                        buf.append(", args:");
                        appendArgs(buf, args);
                        buf.append(")");
                        print(buf.toString());
                    }
                    return switch (func) {
                    case EXTERNAL_NEW     -> callNew(args);
                    case EXTERNAL_PUTCHAR -> callPutchar(args);
                    case EXTERNAL_GETCHAR -> callGetchar(args);
                    case EXTERNAL_MALLOC  -> callMalloc(args);
                    case EXTERNAL_EXIT    -> callExit(args);
                    case EXTERNAL_MEMCPY  -> callMemcpy(args);
                    case EXTERNAL_MEMMOVE -> callMemmove(args);
                    case EXTERNAL_PUTS    -> callPuts(args);
                    default -> throw new Error("unimplemented");
                    };
                }

                if (trace) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("CALL(func:");
                    buf.append(func);
                    buf.append(", link:");
                    buf.append(link);
                    buf.append(")");
                    print(buf.toString());
                }
                LinearTreeCode lc = addrToCode(func);
                if (lc == null) {
                    throwError("CALL() of invalid function address: " + func);
                    return 0;
                }
                pushFrame(lc, link, n.args());
                return 0;
            }
            case BINOP n -> {
                int l = eval(n.left());
                int r = eval(n.right());
                int val = switch (n) {
                case BINOP.ADD b -> l + r;
                case BINOP.AND b -> l & r;
                case BINOP.DIV b -> l / checkDivisor(r);
                case BINOP.DIVU b ->
                (int)((((long)l) & 0xFFFFFFFF) / (((long)checkDivisor(r)) & 0xFFFFFFFF));
                case BINOP.MOD b -> l % checkDivisor(r);
                case BINOP.MODU b ->
                (int)((((long)l) & 0xFFFFFFFF) % (((long)checkDivisor(r)) & 0xFFFFFFFF));
                case BINOP.MUL b -> l * r;
                case BINOP.OR b ->  l | r;
                case BINOP.SLL b -> l << r;
                case BINOP.SRA b -> l >> r;
                case BINOP.SRL b -> l >>> r;
                case BINOP.SUB b -> l - r;
                case BINOP.XOR b -> l ^ r;
                case BINOP.NOR b -> ~(l | r);
                };
                if (trace) print(n.op() + "(" + l + ", " + r + ") = " + val);
                return val;
            }
            }
        }

	void doGoto(Label l) {
	    Integer offset = topOfStack.lc.labels.get(l);
	    if (offset == null) {
		offset = layout.getCodeAddr(l);
		if (offset != null) {
		    if (offset == EXTERNAL_BADPTR) {
			doException("!NullPointerException");
			return;
		    }
		    if (offset == EXTERNAL_BADSUB) {
			doException("!ArrayIndexOutOfBoundsException");
			return;
		    }
		}
		throwError("Label " + l + " not found in this procedure");
	    }
	    else topOfStack.pc = offset;
	}
	int checkDivisor(int val) {
	    if (val == 0) {
		throwError("divide by 0");
		return 1;
	    }
	    return val;
	}
    }

    void doException(String ex) {
	if (exception == null) exception = ex;
    }

    void throwError(String msg) {
        System.out.println();
        System.out.println(msg);
        System.out.println("Current sp=" + this.sp + " fp=" + this.fp);
        System.out.println("Error occurred");
        while (topOfStack != null) {
            System.out.println("\tat " + topOfStack.lc.proc.frame.name.toString());
            popFrame();
        }
        System.out.println();
	    throw new Error(msg);  // TODO: construct a stacktrace
    }

    int checkMemoryAddr(int addr, int offset, int size) {
        addr += offset;
        if ((addr % size) != 0) {
	    throwError("memory[" + addr + "] misaligned");
	    return 0;
	}
	if (addr < RESERVED_PAGE || addr >= memory.capacity()) {
	    throwError("memory[" + addr + "] out of bounds");
	    return 0;
	}
	return addr;
    }

    int callNew(int[] args) {
	if (args.length < 2) throwError("new() requires at least two arguments");
	int size = args[0];
	int hword = args[1];
	if (trace) {
	    print("=> new(size=" + size + ", hword="+hword+")");
	}
	if (size < 0) throwError("new() with negative size");
	if (size > MAX_ALLOCATION) throwError("Out of memory");
	// Allocate an additional word for the header
	int address = layout.allocBytes(size + Frame.wordSize, this.sp);
	if (address < 0) throwError("Out of memory");
        memory.putInt(address, hword);
	// Return a pointer to one word beyond the header
	return address + Frame.wordSize;
    }

    int callMalloc(int[] args) {
	if (args.length < 1) throwError("malloc() requires at least one argument");
	int size = args[0];
	if (trace) {
	    print("=> malloc(size=" + size + ")");
	}
	if (size < 0) throwError("malloc() with negative size");
	if (size > MAX_ALLOCATION) throwError("Out of memory");
	int address = layout.allocBytes(size, this.sp);
	if (address < 0) throwError("Out of memory");
	return address;
    }

    int callExit(int[] args) {
	if (args.length < 1) throwError("exit() requires at least one argument");
	int status = args[0];
	if (trace) {
	    print("=> exit(status=" + status + ")");
	}
        System.out.println(status);
        System.exit(status);
        return status;
    }

    int callPutchar(int[] args) {
	if (args.length < 1) throwError("putchar() requires at least one argument");
        int c = args[0];
	if (trace) {
	    print("=> putchar(c=" + c + ")");
	}
        System.out.write(c);
	return 0;
    }

    int callGetchar(int[] args) {
        if (trace) {
	    print("=> getchar");
	}
        try {
            return System.in.read();
        } catch (java.io.IOException e) {
            throw new Error(e.getMessage());
        }
    }

    int callMemcpy(int[] args) {
	if (args.length < 3) throwError("memcpy() requires at least three arguments");
        int dst = args[0];
        int src = args[1];
        int len = args[2];
	if (trace) {
	    print("=> memcpy(dst=" + dst + ", src=" + src + ", len=" + len + ")");
	}
        if (len < 0) throwError("memcpy() with negative length");
        if (len == 0) return dst;
        for (int i = len / Frame.wordSize; --i >= 0; ) {
            memory.putInt(checkMemoryAddr(dst, 0, Frame.wordSize),
            memory.getInt(checkMemoryAddr(src, 0, Frame.wordSize)));
            dst += Frame.wordSize;
            src += Frame.wordSize;
        }
        return args[0];
    }

    int callMemmove(int[] args) {
	if (args.length < 3) throwError("memmove() requires at least three arguments");
        int dst = args[0];
        int src = args[1];
        int len = args[2];
	if (trace) {
	    print("=> memmove(dst=" + dst + ", src=" + src + ", len=" + len + ")");
	}
        if (len < 0) throwError("memmove() with negative length");
        if (len == 0) return dst;
        checkMemoryAddr(dst + len - Frame.wordSize, 0, Frame.wordSize);
        checkMemoryAddr(src + len - Frame.wordSize, 0, Frame.wordSize);
        if (memory.hasArray()) {
            byte[] array = memory.array();
            int arrayOffset = memory.arrayOffset();
            System.arraycopy(array, arrayOffset + src,
                             array, arrayOffset + dst,
                             len);
        } else {
            byte[] bytes = new byte[len];
            memory.get(bytes, src, len);
            memory.put(bytes, dst, len);
        }
        return args[0];
    }

    int callPuts(int[] args) {
        if (args.length < 1) throwError("puts() requires at least one argument");
        int a = args[0];
        if (trace) {
            print("=> puts(a=" + a + ")");
        }
        byte[] array = memory.array();
        int arrayOffset = memory.arrayOffset();
        for (int i = arrayOffset + a; i < array.length; i++)
            if (array[i] == 0) {
                String s = new String(array, arrayOffset + a, i - (arrayOffset + a));
                System.out.println(s);
                return 0;
            }
        return -1;
    }

    void print(String s) {
	for (Activation a = topOfStack; a != null; a = a.caller) {
	    System.out.print("  ");
	}
	System.out.println(s);
    }

    void appendArgs(StringBuilder buf, int[] args) {
        boolean first = true;
	for (int i : args) {
            if (first) first = false;
            else buf.append(", ");
	    buf.append(i);
	}
    }

    void pushFrame(LinearTreeCode lc, int link, Tree.Exp[] args) {
	// Print the call first
	Frame frame = (Frame)lc.proc.frame;
	int frameSize = frame.frameSize();
	if (frameSize < 0) frameSize = 0;

        // Push a new activation
	Activation callee = new Activation();
	callee.caller = topOfStack;
	callee.lc = lc;
	callee.caller_fp = this.fp;
	callee.pc = 0;

	// Pass arguments
	Iterator<Translate.Frame.Access> formals = frame.formals.iterator();
	for (int i = 0; formals.hasNext(); i++) {
            Tree.Exp arg = i >= args.length ? new CONST(0) : args[i];
            callee.writeAccess(formals.next(), arg);
        }
        Translate.Frame.Access l = frame.link();
        if (l != null)
            callee.writeAccess(frame.link(), new CONST(link));

	this.fp = this.sp;
	this.sp -= frameSize * Frame.wordSize;
	topOfStack = callee;

	if (trace) {
	    StringBuilder buf = new StringBuilder();
	    buf.append("=> ");
	    buf.append(lc.proc.frame.name);
	    buf.append(", frameSize=");
	    buf.append(frameSize);
	    buf.append(", sp=");
	    buf.append(this.sp);
	    buf.append(", fp=");
	    buf.append(this.fp);
	    print(buf.toString());
	}
    }

    void popFrame() {
	if (topOfStack == null) return;
	this.sp = this.fp;
	this.fp = topOfStack.caller_fp;
	topOfStack = topOfStack.caller;
	if (topOfStack != null) {
	    if (topOfStack.returnTemp != null) {
		topOfStack.writeTemp(topOfStack.returnTemp, this.ra);
	    }
	}
	if (trace) {
	    StringBuilder buf = new StringBuilder();
	    buf.append("<=  sp=");
	    buf.append(this.sp);
	    buf.append(", fp=");
	    buf.append(this.fp);
	    print(buf.toString());
	}
    }

    private LinearTreeCode addrToCode(int addr) {
	Label l = layout.addrToLabel(addr);
	if (trace) print("  addrToCode(" + addr + ") = " + l);
	if (l == null) return null;
	return procedures.get(l);
    }

    // An activation of a procedure.
    private class Activation {
	Activation caller;
	HashMap<Temp, Integer> tempMap = new HashMap<Temp, Integer>(); // values of temps
	LinearTreeCode lc;
	Temp returnTemp; // return temporary
	int caller_fp;  // caller's frame pointer
	int pc;  // offset into linear tree code for return address

	Integer readTemp(Temp t) {
	    String name = t.toString();
	    if (name.equals("%fp")) return Interpreter.this.fp;
	    if (name.equals("%ra")) return Interpreter.this.ra;
	    if (name.equals("%sp")) return Interpreter.this.sp;
	    if (name.equals("%fl")) return Interpreter.this.fl;
	    return tempMap.get(t);
	}
	void writeTemp(Temp t, int val) {
	    String name = t.toString();
	    if (name.equals("%fp")) Interpreter.this.fp = val;
	    else if (name.equals("%ra")) Interpreter.this.ra = val;
	    else if (name.equals("%sp")) Interpreter.this.sp = val;
	    else if (name.equals("%fl")) Interpreter.this.fl = val;
	    else tempMap.put(t, val);
	}
        void writeAccess(Translate.Frame.Access formal, Tree.Exp actual) {
            Evaluator eval = Interpreter.this.eval;
            if (formal instanceof Frame.InFrame af) {
                if (actual instanceof MEM(Exp e, CONST(long offset), int size, boolean signed)) {
                    assert af.size == size;
                    Temp t = new Temp();
                    Interpreter.this.eval.accept(new MOVE(new TEMP(t), e));
                    for (int k = 0; k < size; k += Frame.wordSize) {
                        eval.accept(new MOVE(new MEM(new TEMP(Frame.SP),
                                                     new CONST(af.offset + k),
                                                     Frame.wordSize),
                                             new MEM(new TEMP(t),
                                                     new CONST(offset + k),
                                                     Frame.wordSize)));
                    }
                } else {
                    eval.accept(new MOVE(new MEM(new TEMP(Frame.SP),
                                                 new CONST(af.offset),
                                                 Frame.wordSize), actual));
                }
            } else if (formal instanceof Frame.InReg ar) {
                writeTemp(ar.temp, eval.eval(actual));
            } else assert false;
	}
    }

    /**
     * The static mapping of labels to addresses in memory, including what initialized data
     * goes where. Procedures are also given (negative) addresses in memory as needed, though
     * their code is not stored in memory.
     */
    public static class DataLayout {
	// Initialized data loaded into memory.
	private static class InitData {
	    final int address;
	    final ByteBuffer data;
	    InitData(int address, ByteBuffer data) {
		this.address = address;
		this.data = data;
	    }
	}

	private int address = RESERVED_PAGE * Frame.wordSize;  // don't allocate addresses on first page
	private int codeaddress = FIRST_CODE_ADDRESS;  // give code labels negative addresses
	private final List<InitData> inits = new LinkedList<InitData>();
	private final HashMap<String, Integer> map = new HashMap<String, Integer>();
	private final HashMap<Integer, Label> reverseMap = new HashMap<Integer, Label>();

	public DataLayout() {
	    map.put("new", EXTERNAL_NEW);
	    map.put("putchar", EXTERNAL_PUTCHAR);
	    map.put("getchar", EXTERNAL_GETCHAR);
            map.put("puts", EXTERNAL_PUTS);
	    map.put("badPtr", EXTERNAL_BADPTR);
	    map.put("badSub", EXTERNAL_BADSUB);
            map.put("malloc", EXTERNAL_MALLOC);
            map.put("memcpy", EXTERNAL_MEMCPY);
            map.put("memmove", EXTERNAL_MEMMOVE);
            map.put("exit", EXTERNAL_EXIT);
	}

	public String addString(Label lab, String string) {
	    byte[] bytes = string.getBytes();
            // pad by one byte and round up
	    int words = ((bytes.length + 1) + Frame.wordSize - 1) / Frame.wordSize;
            int address = alloc(lab, words);
            ByteBuffer data = ByteBuffer.allocate(words * Frame.wordSize);
            // nul-terminated
            data.put(bytes).put((byte)0);
	    inits.add(new InitData(address, data));
	    return "@" + address + ":" + lab + ":string=" + string;
	}

	public String addRecord(Label lab, int size) {
            int words = (size + Frame.wordSize - 1) / Frame.wordSize;
	    int address = alloc(lab, words);
	    return "@" + address + ":" + lab + ":r[" + words + "]";
	}

        public String addDope(Label lab, Label payload, int... dims) {
            int words = dims.length + 1;
            int address = alloc(lab, words);
            ByteBuffer data = ByteBuffer.allocate(words * Frame.wordSize);
            data.putInt(lookup(payload));
            for (int d : dims) data.putInt(d);
            inits.add(new InitData(address, data));
            return "@" + address + ":" + lab + ":dope[" + words + "]";
        }

	public String addVtable(Label lab, Collection<Label> values) {
	    int words = values.size();
	    int address = alloc(lab, words);
            ByteBuffer data = ByteBuffer.allocate(words * Frame.wordSize);
            for (Label l : values) data.putInt(getCodeAddr(l));
	    inits.add(new InitData(address, data));
	    return "@" + address + ":" + lab + ":vtable[" + words + "]";
	}

	void init(ByteBuffer memory) {
	    for (InitData d : inits)
                memory.position(d.address).put(d.data.position(0));
	}

        int getCodeAddr(Label l) {
	    Integer addr = map.get(l.toString());
	    if (addr == null) {
		addr = codeaddress--;
		map.put(l.toString(), addr);
		reverseMap.put(addr, l);
	    }
	    return addr;
	}

	Label addrToLabel(int addr) {
	    return reverseMap.get(addr);
	}

	Integer lookup(Label l) {
	    return map.get(l.toString());
	}

	private int alloc(Label l, int words) {
	    int result = address;
	    address += (words * Frame.wordSize);
	    map.put(l.toString(), result);
	    reverseMap.put(result, l);
	    return result;
	}

	int allocBytes(int bytes, int heapEnd) {
	    int words = (bytes + Frame.wordSize - 1) / Frame.wordSize;
	    int result = address;
	    int end = address + words * Frame.wordSize;
	    if (end > heapEnd) return -1;
	    address = end;
	    return result;
	}
    }

    /**
     * It's easier to interpret the code if it's been linearized into a list
     * of statements that can be indexed by an integer.
     */
    static class LinearTreeCode {
	Frag.Proc proc;
	int address;
	List<Tree.Stm> stms = new ArrayList<Tree.Stm>();
	HashMap<Label, Integer> labels = new HashMap<Label, Integer>();
    }

    static void dumpCode(LinearTreeCode lc) {
	PrintWriter out = new PrintWriter(System.out);
	out.println(lc.proc.frame.name + " (linearized):");
	int i = 0;
	for (Tree.Stm s : lc.stms) {
	    out.print(i + ": \n");
	    new Tree.Print(out).accept(s);
	    i++;
	}
	out.println();
	out.flush();
    }

    static LinearTreeCode linearize(Frag.Proc p) {
	LinearTreeCode lc = new LinearTreeCode();
	lc.proc = p;
        lc.stms = new Canon.Linearize().apply(p.body);
	for (int i = 0; i < lc.stms.size(); i++) {
	    Tree.Stm s = lc.stms.get(i);
	    if (s instanceof LABEL) {
		LABEL l = (LABEL)s;
		lc.labels.put(l.label(), i);
	    }
	}
	return lc;
    }
}
