package com.bazz_techtronics.codefind.data;

import java.io.Serializable;

/**
 * Created by Nshidbaby on 11/7/2016.
 */

public class Tuple<A, B> implements Serializable {
    private final A key;
    private final B obj;

    public Tuple(A key, B obj) {
        this.key = key;
        this.obj = obj;
    }

    public A getKey() { return key; }
    public B getObj() { return obj; }

    @Override
    public int hashCode() { return key.hashCode() ^ obj.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tuple)) return false;
        Tuple tuple = (Tuple) o;
        return this.key.equals(tuple.getKey()) &&
                this.obj.equals(tuple.getObj());
    }
}
