package com.bazz_techtronics.codefind.data;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nshidbaby on 11/7/2016.
 */

public class Cache implements Serializable
{
    private List<Tuple<String, List<Response>>> cache;

    public Cache() {
        this.cache = new ArrayList<>();
    }

    public void add(String searchKey, List<Response> entries) {
        this.cache.add(new Tuple<>(searchKey, entries));
    }

    public Integer count() {
        int cnt = 0;
        for (Tuple<String, List<Response>> tuple : cache) {
            cnt++;
        }
        return cnt;
    }

    public Boolean contains(String searchKey) {
        if (getIndex(searchKey) > -1) {
            return true;
        }
        return false;
    }

    public Integer getIndex(String searchKey) {
        int idx = 0;
        for (Tuple<String, List<Response>> tuple : cache) {
            if (tuple.getKey().equals(searchKey)){
                return idx;
            }
            idx++;
        }
        return -1;
    }

    public List<Response> getResults(String searchKey) {
        for (Tuple<String, List<Response>> tuple : cache){
            if (tuple.getKey().equals(searchKey)){
                return tuple.getObj();
            }
        }
        return null;
    }
}
