/* Copyright (C) 1997-2023, Antony L Hosking.
 * All rights reserved.  */

package mojo;

import mojo.Absyn.Value;

/**
 * This class records the relative level of nested procedures.
 */
public abstract class ProcBody {
    public final ProcBody parent;
    public final int level;

    private ProcBody sibling;
    public ProcBody children;

    private static ProcBody current;
    private static ProcBody head;
    private static ProcBody done;
    private static int depth = 0;

    public ProcBody() {
        this.level = depth;
        this.parent = current;
        if (current == null) {
            assert depth == 0;
            this.sibling = head;
            head = this;
        } else {
            this.sibling = current.children;
            current.children = this;
        }
    }

    /**
     * Push a procedure as a child of the current procedure.
     */
    public static void Push(ProcBody t) {
        assert t.parent == current;
        ++depth;
        current = t;
    }
    /**
     * Pops the current procedure.
     */
    public static void Pop() {
        current = current.parent;
        depth--;
    }
    /**
     * Return the current (top) procedure.
     */
    public static ProcBody Top() {
        return current;
    }
    /**
     * Schedules 't' to be written as a top-level procedure.
     */
    public static void Schedule(ProcBody t) {
        t.sibling = head;
        head = t;
    }
    /**
     * Generate all the procedure bodies.
     */
    public static <T> void EmitAll(Value.Visitor<Void> decl,
                                   Value.Visitor<Void> body) {
        // generate the declarations and bodies
        while (head != null) {
            ProcBody t = head; head = null; // grab the guys that are waiting
            t = SourceOrder(t); // put 'em in source order
            EmitDecl(t, decl); // generate their declarations
            EmitBody(t, body); // generate their bodies & build "done" list
        }
    }
    private static ProcBody SourceOrder(ProcBody t) {
        // reverse the list
        ProcBody a = t, b = null;
        while (a != null) {
            ProcBody c = a.sibling;
            a.sibling = b;
            b = a;
            a = c;
        }
        t = b;
        // recursively reorder the children
        while (t != null) {
            t.children = SourceOrder(t.children);
            t = t.sibling;
        }
        return b;
    }

    public abstract void accept(Value.Visitor<Void> v);
    private static void EmitDecl(ProcBody t, Value.Visitor<Void> v) {
        while (t != null) {
            t.accept(v);
            EmitDecl(t.children, v);
            t = t.sibling;
        }
    }
    private static void EmitBody(ProcBody t, Value.Visitor<Void> v) {
        while (t != null) {
            t.accept(v);
            EmitBody(t.children, v);
            // move to the next sibling, but leave this guy on the "done" list
            ProcBody a = t.sibling;
            t.sibling = done; done = t;
            t = a;
        }
    }
}
