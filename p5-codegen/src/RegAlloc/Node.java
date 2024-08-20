/* Copyright (C) 1997-2024, Antony L Hosking.
 * All rights reserved.  */
package RegAlloc;

import Translate.Temp;

public class Node {
    final Temp temp;
    Temp color;
    double spillCost = 0.0;
    Node(Liveness g, Temp t) {
        temp = t;
        index = g.size();
        g.put(t, this);
    }

    private int index;
    public String toString() { return temp.toString(); }
    public boolean equals(Node n) { return index == n.index; }
}
