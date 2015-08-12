
package com.cmcc.hyapps.andyou.model;


import java.util.ArrayList;
import java.util.List;

public class ResultList<T> {
    public int count;
    public String next;
    public String previous;
    public List<T> results = new ArrayList<T>();

}
