/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package RegAlloc;

import FlowGraph.AssemNode;
import Assem.Instr;
import Translate.Temp;

import java.util.*;

public final class Liveness extends InterferenceGraph {
    private Node newNode(Temp temp) {
        Node n = get(temp);
        if (n == null) n = new Node(this, temp);
        n.spillCost++;
        return n;
    }

    LinkedList<Move> moves = new LinkedList<Move>();

    /**
     * Return the move-related nodes for the current CFG.
     */
    public List<Move> moves() {
        return moves;
    }

    /**
     * Perform liveness analysis on the CFG `flow' for the target `frame',
     * computing the liveIn and liveOut sets for each CFG node.  The
     * constructed Liveness object is an interference graph, with nodes
     * representing temporaries and hard registers, and edges representing
     * interferences between the nodes.
     */
    public Liveness(FlowGraph.AssemFlowGraph flow, Translate.Frame frame) {
        // Create precolored nodes for registers
        Temp[] registers = frame.registers();
        for (Temp r : registers) {
            Node n = newNode(r);
            n.color = r;
        }

        // Liveness analysis
        LinkedList<AssemNode> workList = new LinkedList<AssemNode>();
        // Initialize in sets for each node in the flow graph
        for (AssemNode n : flow.nodes()) {
            Set<Temp> in = n.liveIn;
            // Create nodes for each temp
            for (Temp d : flow.def(n)) {
                newNode(d);
            }
            // initialize in on the assumption out is empty for all n
            for (Temp u : flow.use(n)) {
                newNode(u);
                in.add(u);
            }
            workList.addFirst(n);
        }

        // Compute liveIn and liveOut iteratively
        while (!workList.isEmpty()) {
            AssemNode n = workList.removeFirst();
            Set<Temp> in = n.liveIn, out = n.liveOut;

            // out = union(in[s]) for all succ s
            for (AssemNode s : flow.succs(n))
                out.addAll(s.liveIn);

            // in = use + (out - def)
            LinkedHashSet<Temp> diff = new LinkedHashSet<Temp>(out);
            diff.removeAll(flow.def(n));
            if (in.addAll(diff))
                for (AssemNode p : flow.preds(n))
                    workList.addFirst(p);
        }

        // Build interference graph
        for (AssemNode n : flow.nodes()) {
            Set<Temp> live = new LinkedHashSet<Temp>(n.liveOut);
            List<Instr> instrs = n.instrs;
            for (ListIterator<Instr> I = instrs.listIterator(instrs.size()); I.hasPrevious();) {
                Instr i = I.previous();
                Temp src = null;
                if (i instanceof Instr.MOVE m) {
                    src = m.src();
                    moves.addFirst(new Move(get(src), get(m.dst())));
                }
                for (Temp d : i.def)
                    for (Temp t : live)
                        if (t != src) {
                            Node from = get(d);
                            Node to = get(t);
                            if (from != to && !adj(from, to)) {
                                if (from.color == null)
                                    addEdge(from, to);
                                if (to.color == null)
                                    addEdge(to, from);
                            }
                        }
                for (Temp d : i.def)
                    live.remove(d);
                Collections.addAll(live, i.use);
            }
        }
    }

    public void show(java.io.PrintWriter out) {
        for (Node n : nodes()) {
            out.print(n);
            out.print(": ");
            for (Node s : succs(n)) {
                out.print(s);
                out.print(" ");
            }
            out.println();
        }
        for (Move move : moves) {
            out.print(move.dst);
            out.print(" <= ");
            out.println(move.src);
        }
        out.flush();
    }
}
