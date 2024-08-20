/* Copyright (C) 1997-2023, Antony L Hosking.
 * All rights reserved.  */

package mojo;

import java.util.*;

/**
 * The block-structured symbol table mapping symbols to values.
 */
public class Scope {
    public final Scope parent;
    public final String name;
    public final List<Scope> children = new LinkedList<Scope>();
    private final Map<String, Absyn.Value> values = new LinkedHashMap<String, Absyn.Value>();

    /**
     * lookups can see parent
     */
    public final boolean open;
    /**
     * is a procedure frame
     */
    public final boolean proc;
    /**
     * is an outer module scope
     */
    public final boolean module;
    
    public final Absyn.Value.Unit home;

    private static final List<Scope> allScopes = new LinkedList<Scope>();
    private static final Scope Initial = new Scope(true, "_BUILTIN", false, false);
    private static Scope top = Initial;

    /**
     * Create a new scope.
     * @param name the name of the scope
     * @param open scope is open (like a block) or closed (like a record)?
     * @param module module scope?
     * @param proc procedure scope?
     */
    private Scope(boolean open, String name, boolean module, boolean proc) {
        allScopes.add(this);
        parent = top;
        if (parent != null) {
            parent.children.add(this);
            if (name == null)
                name = Integer.toString(parent.children.size());
        }
        this.name = name;
        this.open = open;
        this.proc = proc;
        this.module = module;
        this.home = Absyn.Value.Unit.Current();
    }

    public static Scope PushNewModule(String name) {
        return top = new Scope(true, name, true, false);
    }

    public static Scope PushNewProc(String name) {
        return top = new Scope(true, name, false, true);
    }

    public static Scope PushNewOpen() {
        return top = new Scope(true, null, false, false);
    }

    public static Scope PushNewClosed() {
        return top = new Scope(false, null, false, false);
    }

    public static void PopNew() {
        top = top.parent;
    }

    /**
     * Reset the top of the scope stack.
     * @param t the new top
     * @return the scope to reset to (for later Pop)
     */
    public static Scope Push(Scope t) {
        Scope old = top;
        assert t != null;
        top = t;
        return old;
    }

    /**
     * Reset the top of the scope stack.
     * @param old the scope to reset to (from previous Push)
     */
    public static void Pop(Scope old) {
        assert old != null;
        top = old;
    }

    /**
     * @return the top "open" scope
     */
    public static Scope Top() {
        Scope t = top;
        while (t != null && !t.open)
            t = t.parent;
        return t;
    }

    /**
     * Test if a scope is the outermost (module/interface level) scope
     * @param t the scope to test
     * @return true if outermost, false if not
     */
    public static boolean OuterMost(Scope t) {
        return t != null && t.module;
    }

    /**
     * Look up the value denoted by a symbol in a given scope.
     * Notes when a value is referenced from an inner scope.
     * @param t the scope in which to look for the symbol
     * @param name the symbol to look up
     * @param strict look up in outer scopes if false?
     * @return the value denoted by the symbol
     */
    public static Absyn.Value LookUp(Scope t, String name, boolean strict) {
        Absyn.Value o;
        boolean up_level = false;
        for (;;) {
            if (t == null)
                return null;
            o = t.values.get(name);
            if (o != null)
                break;
            if (strict || !t.open)
                return null;
            up_level = up_level || t.proc;
            t = t.parent;
        }
        if (up_level) o.up_level(true);
        return o;
    }

    /**
     * Insert a value into the current (top) scope,
     * but only if it has not been inserted into any other scope.
     * @param o the value to insert 
     */
    public static void Insert(Absyn.Value o) {
        Scope t = top;
        // check for a reserved word
        if (t != Initial && LookUp(Initial, o.name, true) != null)
            Error.ID(o.token, "Reserved identifier redefined");
        if (t.values.get(o.name) != null)
            Error.ID(o.token, "symbol redefined");
        else
            t.values.put(o.name, o);
        if (o.scope == null) o.scope = top;
    }

    public static <T extends Absyn.Value> void Insert(List<T> values) {
        if (values == null) return;
        for (Absyn.Value v : values) Insert(v);
    }

    public static Collection<Absyn.Value> ToList(Scope t) {
        if (t == null) return Collections.emptyList();
        return t.values.values();
    }

    public String toString() {
        if (parent == null) return name;
        if (module) return name;
        if (parent.name == null) return name;
        return parent.toString() + "." + name;
    }
}
