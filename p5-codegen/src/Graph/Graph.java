/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package Graph;

import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

public class Graph<K,N> {
    private Map<K, N> nodes = new LinkedHashMap<K, N>();
    private Map<N, Set<N>> succs = new LinkedHashMap<N,Set<N>>();
    private Map<N, Set<N>> preds = new LinkedHashMap<N,Set<N>>();

    public Collection<N> nodes() { return nodes.values(); }
    public Set<N> succs(N n) { return succs.get(n); }
    public Set<N> preds(N n) { return preds.get(n); }

    void check(N n) {
        if (nodes.containsValue(n)) return;
        throw new Error("Graph.addEdge using nodes from the wrong graph");
    }        

    public boolean put(K k, N n) {
        return nodes.putIfAbsent(k, n) == null
            && succs.putIfAbsent(n, new LinkedHashSet<N>()) == null
            && preds.putIfAbsent(n, new LinkedHashSet<N>()) == null;
    }
    public N get(K k) { return nodes.get(k); }

    public N remove(K k) {
        N n = nodes.remove(k);
        succs.values().stream().forEach(s -> s.remove(n));
        preds.values().stream().forEach(p -> p.remove(n));
        return n;
    }
        
    public int size() {
        return nodes.size();
    }

    public int inDegree (N n) { return preds(n).size(); }
    public int outDegree(N n) { return succs(n).size(); }
    public int degree   (N n) { return inDegree(n) + outDegree(n); }

    public boolean adj(N a, N b) {
        return succs(a).contains(b)
            || preds(a).contains(b);
    }

    public Set<N> adj(N n) {
        Set<N> result = new LinkedHashSet<N>();
        result.addAll(succs(n));
        result.addAll(preds(n));
        return result;
    }

    public boolean addEdge(N from, N to) {
        check(from); check(to);
        if (succs(from).contains(to)) return false;
        return succs(from).add(to)
            && preds(to).add(from);
    }

    public boolean removeEdge(N from, N to) {
        return succs.get(from).remove(to)
            && preds.get(to).remove(from);
    }

    /**
     * Print a human-readable dump for debugging.
     */
    public void show(java.io.PrintWriter out) {
        for (N n: nodes()) {
            out.print(n);
            out.print(":");
            for (N s: succs.get(n)) {
                out.print(" ");
                out.print(s);
            }
            out.println();
        }
        out.flush();
    }
}
