/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package RegAlloc;

import java.util.*;
import Translate.Temp;

import javax.sql.RowSetReader;

public final class Color {
  final InterferenceGraph ig;
  final int K;
  /**
   * The colors available to the allocator.
   */
  final LinkedList<Temp> colors = new LinkedList<Temp>();
  /**
   * Precolored nodes corresponding to the machine registers.
   */
  final LinkedHashSet<Node> precolored = new LinkedHashSet<Node>();

  /**
   * Return the spilled registers from this round of allocation.
   */
  public Temp[] spills() {
    Temp[] spills = null;
    int spillCount = spilledNodes.size();
    if (spillCount > 0) {
      spills = new Temp[spilledNodes.size()];
      int i = 0;
      for (Node n : spilledNodes)
        spills[i++] = n.temp;
    }
    return spills;
  }

  // Node worklists, sets and stacks
  final LinkedHashSet<Node> simplifyWorklist; // low-degree non-move-related nodes
  final LinkedHashSet<Node> freezeWorklist; // low-degree move-related nodes
  final LinkedHashSet<Node> coalescedNodes; // registers that have been coalesced
  final LinkedList<Node> initial; // temporary registers, not precolored and not yet processed
  final LinkedList<Node> spillWorklist; // high-degree nodes
  final LinkedList<Node> spilledNodes; // nodes marked for spilling during this round
  final LinkedList<Node> coloredNodes; // nodes successfully colored
  final LinkedList<Node> selectStack; // stack containing temporaries removed from the graph
  {
    initial = new LinkedList<Node>();
    simplifyWorklist = new LinkedHashSet<Node>();
    freezeWorklist = new LinkedHashSet<Node>();
    spillWorklist = new LinkedList<Node>();
    spilledNodes = new LinkedList<Node>();
    coalescedNodes = new LinkedHashSet<Node>();
    coloredNodes = new LinkedList<Node>();
    selectStack = new LinkedList<Node>();
  }

  /*
   * Move sets. There are five sets of move instructions, and every move is in
   * exactly one of these sets (after Build through end of Color).
   */

  /**
   * Moves enabled for possible coalescing.
   */
  final LinkedList<Move> worklistMoves = new LinkedList<Move>();
  /**
   * Moves that have been coalesced.
   */
  final LinkedHashSet<Move> coalescedMoves = new LinkedHashSet<Move>();
  /**
   * Moves whose source and target interfere.
   */
  final LinkedHashSet<Move> constrainedMoves = new LinkedHashSet<Move>();
  /**
   * Moves no longer considered for coalescing.
   */
  final LinkedHashSet<Move> frozenMoves = new LinkedHashSet<Move>();
  /**
   * Moves not yet ready for coalescing.
   */
  final LinkedHashSet<Move> activeMoves = new LinkedHashSet<Move>();

  /*
   * Other data structures.
   */

  /**
   * Current degree of each node.
   */
  final LinkedHashMap<Node, Integer> degree = new LinkedHashMap<Node, Integer>();
  /**
   * Moves associated with each node.
   */
  final LinkedHashMap<Node, LinkedList<Move>> moveList = new LinkedHashMap<Node, LinkedList<Move>>();
  /**
   * When a move (u,v) has been coalesced, and v put in coalescedNodes, then
   * alias(v) = u
   */
  final LinkedHashMap<Node, Node> alias = new LinkedHashMap<Node, Node>();

  LinkedList<Move> MoveList(Node n) {
    LinkedList<Move> moves = moveList.get(n);
    if (moves == null) {
      moves = new LinkedList<Move>();
      moveList.put(n, moves);
    }
    return moves;
  }

  void Build() {

    for (Move m : ig.moves()) {
      MoveList(m.src).add(m);
      MoveList(m.dst).add(m);
      if (!worklistMoves.contains(m))
        worklistMoves.add(m);
    }

    // for (Node n : ig.nodes()) {
    // for (Node adj : ig.succs(n)) {
    // AddEdge(n, adj);
    // }
    // }

    for (Move m : ig.moves()) {
      if (!this.moveList.containsKey(m.src)) {
        this.moveList.put(m.src, new LinkedList<>());
      }
      this.moveList.get(m.src).add(m);

      if (!this.moveList.containsKey(m.dst)) {
        this.moveList.put(m.dst, new LinkedList<>());
      }
      this.moveList.get(m.dst).add(m);

      if (!worklistMoves.contains(m))
        this.worklistMoves.add(m);
    }
    // // TODO
  }

  void AddEdge(Node u, Node v) {
    if (u != v && !ig.adj(u, v)) {
      if (!precolored.contains(u)) {
        ig.addEdge(u, v);
        degree.put(u, Degree(u) + 1);
      }
      if (!precolored.contains(v)) {
        ig.addEdge(v, u);
        degree.put(v, Degree(v) + 1);
      }
    }
  }

  void MakeWorkList() {
    // TODO
    for (Node n : this.initial) {
      if (Degree(n) >= this.K) {
        SetAdd(this.spillWorklist, n);
      } else if (MoveRelated(n)) {
        SetAdd(this.freezeWorklist, n);
      } else {
        SetAdd(this.simplifyWorklist, n);
      }
    }
    // clear
    this.initial.clear();
  }

  /*
   * Adjacency changes as nodes are selected and coalesced, corresponding to
   * removing them from the interference graph.
   */
  LinkedHashSet<Node> Adjacent(Node n) {
    LinkedHashSet<Node> adj = new LinkedHashSet<Node>(ig.succs(n));
    adj.removeAll(selectStack);
    adj.removeAll(coalescedNodes);
    return adj;
  }

  /*
   * A nodes moves change as adjacency changes, and include only those moves
   * still active (not frozen) but not ready for coalescing, or moves
   * available for coalescing.
   */
  LinkedHashSet<Move> NodeMoves(Node n) {
    LinkedHashSet<Move> moves = new LinkedHashSet<Move>();
    moves.addAll(activeMoves);
    moves.addAll(worklistMoves);
    moves.retainAll(MoveList(n));
    return moves;
  }

  /*
   * A node is move-related if any of its moves are still active
   */
  boolean MoveRelated(Node n) {
    return !NodeMoves(n).isEmpty();
  }

  private <T> LinkedHashSet<T> union(final T singleton,
      final LinkedHashSet<T> set) {
    final LinkedHashSet<T> newSet = LinkedHashSet.newLinkedHashSet(1 + set.size());
    newSet.add(singleton);
    newSet.addAll(set);
    return newSet;
  }

  private <T> LinkedHashSet<T> union(final Collection<T> setA,
      final Collection<T> setB) {
    final LinkedHashSet<T> newSet = LinkedHashSet.newLinkedHashSet(
        (setA == null ? 0 : setA.size())
            + (setB == null ? 0 : +setB.size()));
    if (setA != null)
      newSet.addAll(setA);
    if (setB != null)
      newSet.addAll(setB);
    return newSet;
  }

  void DecrementDegree(Node m) {
    var d = Degree(m);
    degree.put(m, d - 1);
    if (d == K) {
      EnableMoves(union(m, Adjacent(m)));
      SetRem(spillWorklist, m);
      if (MoveRelated(m)) {
        SetAdd(freezeWorklist, m);
      } else {
        SetAdd(simplifyWorklist, m);
      }
    }
  }

  void EnableMoves(LinkedHashSet<Node> nodes) {
    for (Node n : nodes) {
      for (Move m : NodeMoves(n)) {
        if (this.activeMoves.contains(m)) {
          SetRem(activeMoves, m);
          SetAdd(worklistMoves, m);
        }
      }
    }
  }

  void EnableMoves(Node n) {
    LinkedHashSet<Node> set = LinkedHashSet.newLinkedHashSet(1);
    set.add(n);
    EnableMoves(set);
  }

  void Simplify() {
    // TODO
    Node n = simplifyWorklist.removeFirst();
    this.selectStack.push(n);
    for (var m : Adjacent(n)) {
      DecrementDegree(m);
    }
  }

  int Degree(Node n) {
    Integer d = degree.get(n);
    if (d == null)
      return 0;
    return d;
  }

  void AddWorkList(Node u) {
    if (!precolored.contains(u) && !MoveRelated(u) && Degree(u) < K) {
      SetRem(freezeWorklist, u);
      // if (Degree(u) < K)
      SetAdd(simplifyWorklist, u);
    }
  }

  boolean OK(Node t, Node r) {
    return Degree(t) < K || precolored.contains(t) || ig.adj(t, r);
  }

  Node GetAlias(Node n) {
    if (coalescedMoves.contains(n)) {
      return GetAlias(n);
    }
    return n;
  }

  boolean Conservative(LinkedHashSet<Node> nodes) {
    int k = 0;
    for (Node n : nodes) {
      if (Degree(n) >= K) {
        k = k + 1;
      }
    }
    return k < K;
  }

  void Combine(Node u, Node v) {
    if (freezeWorklist.contains(v)) {
      SetRem(freezeWorklist, v);
    } else {
      SetRem(simplifyWorklist, v);
    }
    SetAdd(coalescedNodes, v);
    alias.put(v, u);
    moveList.get(u).addAll(moveList.get(v));
    EnableMoves(v);
    for (Node t : Adjacent(v)) {
      AddEdge(t, u);
      DecrementDegree(t);
    }
    if (Degree(u) >= K && freezeWorklist.contains(u)) {
      SetRem(freezeWorklist, u);
      SetAdd(spillWorklist, u);
    }

  }

  boolean AllOK(Node u, Node v) {
    for (Node t : Adjacent(v)) {
      if (!OK(t, u))
        return false;
    }
    return true;
  }

  void Coalesce() {
    // TODO
    Move m = worklistMoves.getFirst();
    Node x = GetAlias(m.src);
    Node y = GetAlias(m.dst);
    Node u, v;
    if (precolored.contains(y)) {
      u = y;
      v = x;
    } else {
      u = x;
      v = y;
    }
    SetRem(worklistMoves, m);
    if (u.equals(v)) {
      SetAdd(coalescedMoves, m);
      AddWorkList(u);
    } else if (precolored.contains(v) || ig.adj(u, v)) {
      SetAdd(constrainedMoves, m);
      AddWorkList(u);
      AddWorkList(v);
    } else if ((precolored.contains(u) && AllOK(u, v))
        || (!precolored.contains(u) && Conservative(union(Adjacent(u), Adjacent(v))))) {
      SetAdd(coalescedMoves, m);
      Combine(u, v);
      AddWorkList(u);
    } else {
      SetAdd(activeMoves, m);
    }
  }

  void Freeze() {
    // TODO
    Node u = freezeWorklist.removeFirst();
    SetAdd(simplifyWorklist, u);
    FreezeMoves(u);
  }

  void FreezeMoves(Node u) {
    for (Move m : NodeMoves(u)) {
      Node x = m.src;
      Node y = m.dst;
      Node v;
      if (GetAlias(y).equals(GetAlias(u))) {
        v = GetAlias(x);
      } else {
        v = GetAlias(y);
      }
//      SetRem(activeMoves, m);
//      SetAdd(frozenMoves, m);
//      if (freezeWorklist.contains(v) && NodeMoves(v).isEmpty()) {
//        SetRem(freezeWorklist, v);
//        SetAdd(simplifyWorklist, v);
//      }
      if (activeMoves.contains(m)) {
        SetRem(activeMoves,m);
      } else {
        SetRem(worklistMoves,m);
      }
      SetAdd(frozenMoves, m);
      if (NodeMoves(v).isEmpty() && Degree(v) <K)
      {
        SetRem(freezeWorklist, v);
        SetAdd(simplifyWorklist, v);}
    }
  }

  void SelectSpill() {
    // TODO
    Node node = null;
    for (Node n : spillWorklist) {
      node = n;
    }
    SetRem(spillWorklist, node);
    SetAdd(simplifyWorklist, node);
  }

  void AssignColors() {
    // TODO
    Node n;
    while (!selectStack.isEmpty() && (n = this.selectStack.pop()) != null) {
      LinkedList<Temp> okColors = new LinkedList<>(colors);
      for (Node w : ig.succs(n)) {
        if (union(coloredNodes, precolored).contains(GetAlias(w))) {
          if (okColors.contains(GetAlias(w).color))
            SetRem(okColors, GetAlias(w).color);
        }
      }
      if (okColors.isEmpty()) {
        SetAdd(spillWorklist, n);
      } else {
        SetAdd(coloredNodes, n);
        n.color = okColors.getFirst();
      }
    }
    for (Node node : coalescedNodes) {
      node.color = GetAlias(node).color;
    }
  }

  private <R> void SetRem(java.util.Collection<R> set, R e) {
    if (!set.remove(e)) {
      Error(e);
    }
  }

  private <R> void SetAdd(java.util.Collection<R> set, R e) {
    if (!set.add(e)) {
      Error(e);
    }
  }

  private <R> String Error(R e) {
    String error = "";
    if (e instanceof Node) {
      Node n = (Node) e;
      error += n.temp + "(" + Degree(n) + "):";
      if (precolored.contains(n))
        error += " precolored";
      if (initial.contains(n))
        error += " initial";
      if (simplifyWorklist.contains(n))
        error += " simplifyWorklist";
      if (freezeWorklist.contains(n))
        error += " freezeWorklist";
      if (spillWorklist.contains(n))
        error += " spillWorklist";
      if (spilledNodes.contains(n))
        error += " spilledNodes";
      if (coalescedNodes.contains(n))
        error += " coalescedNodes";
      if (coloredNodes.contains(n))
        error += " coloredNodes";
      if (selectStack.contains(n))
        error += " selectStack";
    } else if (e instanceof Move) {
      Move m = (Move) e;
      error += m.dst + "<=" + m.src + ":";
      if (coalescedMoves.contains(m))
        error += " coalescedMoves";
      if (constrainedMoves.contains(m))
        error += " constrainedMoves";
      if (frozenMoves.contains(m))
        error += " frozenMoves";
      if (worklistMoves.contains(m))
        error += " worklistMoves";
      if (activeMoves.contains(m))
        error += " activeMoves";
    }
    throw new Error(error);
  }

  public Color(InterferenceGraph ig, Translate.Frame frame) {
    this.ig = ig;
    this.K = frame.registers().length;
    for (Temp r : frame.registers()) {
      Node n = ig.get(r);
      precolored.add(n);
      colors.add(r);
    }
    for (Node n : ig.nodes())
      if (n.color == null) {
        initial.add(n);
        degree.put(n, ig.outDegree(n));
      }

    Build();
    MakeWorkList();

    do {
      if (!simplifyWorklist.isEmpty()) {
        // System.err.println("Simplify");
        Simplify();
      } else if (!worklistMoves.isEmpty()) {
        // System.err.println("Coalesce");
        Coalesce();
      } else if (!freezeWorklist.isEmpty()) {
        // System.err.println("Freeze");
        Freeze();
      } else if (!spillWorklist.isEmpty()) {
        // System.err.println("SelectSpill");
        SelectSpill();
      }
    } while (!(simplifyWorklist.isEmpty() && worklistMoves.isEmpty()
        && freezeWorklist.isEmpty() && spillWorklist.isEmpty()));
    AssignColors();
  }
}
