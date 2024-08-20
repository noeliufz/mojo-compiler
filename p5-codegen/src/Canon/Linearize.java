/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package Canon;

import java.util.*;
import Translate.Temp;
import Translate.Tree.Exp;
import Translate.Tree.Exp.*;
import Translate.Tree.Stm;
import Translate.Tree.Stm.*;

public class Linearize implements java.util.function.Function<Stm,List<Stm>> {
    List<Stm> stms = new LinkedList<Stm>();
    public List<Stm> apply(Stm s) {
        if (s != null) visit(s);
        return stms;
    }
        
    static boolean commute(ArrayList<Stm> a, Exp b) {
        return a.isEmpty() || b instanceof NAME || b instanceof CONST;
    }
    
    void visit(Stm stm) {
        switch (stm) {
        case SEQ s -> {
            visit(s.left());
            visit(s.right());
        }
        case MOVE s when s.dst() instanceof TEMP t && s.src() instanceof CALL c -> {
            Exp[] kids = c.kids();
            visit(Arrays.asList(kids), stms);
            stms.add(new MOVE(t, c.build(kids)));
        }
        case MOVE s when s.dst() instanceof ESEQ e -> {
            visit(e.stm());
            visit(new MOVE(e.exp(), s.src()));
        }
        default -> {
            Exp[] kids = stm.kids();
            visit(Arrays.asList(kids), stms);
            stms.add(stm.build(kids));
        }
        }
    }

    public Exp visit(Exp exp) {
        switch(exp) {
        case ESEQ e -> {
            visit(e.stm());
            return visit(e.exp());
        }
        default -> {
            Exp[] kids = exp.kids();
            visit(Arrays.asList(kids), stms);
            return exp.build(kids);
        }
        }
    }

    private void visit(List<Exp> exps, List<Stm> l) {
        if (exps.isEmpty()) return;
        Exp a = exps.get(0);
        if (a instanceof CALL) {
            Temp t = new Temp();
            Exp e = new ESEQ(new MOVE(new TEMP(t), a), new TEMP(t));
            exps.set(0, e);
            visit(exps, stms);
        } else {
            Exp aa = visit(a);
            ArrayList<Stm> bb = new ArrayList<Stm>();
            visit(exps.subList(1, exps.size()), bb);
            if (commute(bb, aa)) {
                stms.addAll(bb);
                exps.set(0, aa);
            } else {
                Temp t = new Temp();
                l.add(new MOVE(new TEMP(t), aa));
                l.addAll(bb);
                exps.set(0, new TEMP(t));
            }
        }
    }
}
