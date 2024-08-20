/* Copyright (C) 1997-2023, Antony L Hosking.
 * All rights reserved.  */

package mojo;

import mojo.parse.Token;

/**
 * A class for reporting warnings and errors.
 */
public abstract class Error {
    enum Level {
        WARN("warning: "), // warnings
        ERROR("error: "); // errors
        final String label;
        private int count = 0;
        Level(String l) { label = l; }
    }

    static StringBuffer spare = null;
    public static void Msg(Token t, String msg) {
        StringBuffer wr = Header(Level.ERROR);
        Out(wr, msg);
        Trailer(t, wr);
    }
    public static void Int(Token t, int n, String msg) {
        StringBuffer wr = Header(Level.ERROR);
        Out(wr, msg);
        Out(wr, " (");
        Out(wr, String.valueOf(n));
        Out(wr, ")");
        Trailer(t, wr);
    }
    public static void ID(Token id, String msg) {
        StringBuffer wr = Header(Level.ERROR);
        Out(wr, msg);
        Out(wr, " (");
        Out(wr, id.image);
        Out(wr, ")");
        Trailer(id, wr);
    }
    public static void Txt(Token t, String id, String msg) {
        StringBuffer wr = Header(Level.ERROR);
        Out(wr, msg);
        Out(wr, ": ");
        Out(wr, id);
        Trailer(t, wr);
    }
    public static void Warn(Token t, String msg) {
        StringBuffer wr = Header(Level.WARN);
        Out(wr, msg);
        Trailer(t, wr);
    }
    public static void WarnID(Token t, String msg) {
        StringBuffer wr = Header(Level.WARN);
        Out(wr, msg);
        Out(wr, " (");
        Out(wr, t.image);
        Out(wr, ")");
        Trailer(t, wr);
    }

    private static StringBuffer Header(Level level) {
        StringBuffer wr;
        if (spare != null) {
            wr = spare;
            spare = null;
        } else {
            wr = new StringBuffer();
        }
        level.count++;
        Out(wr, level.label);
        return wr;
    }
    private static void Trailer(Token t, StringBuffer wr) {
        System.err.print(" line ");
        System.err.print(t.beginLine);
        System.err.print(", column ");
        System.err.print(t.beginColumn);
        System.err.print(": ");
        System.err.println(wr);
        wr.setLength(0);
        spare = wr;
    }
    private static void Out(StringBuffer wr, String t) {
        if (t != null) wr.append(t);
    }
    public static int nErrors() {
        return Level.ERROR.count;
    }
    public static int nWarnings() {
        return Level.WARN.count;
    }
}
