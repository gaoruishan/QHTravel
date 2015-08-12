package com.cmcc.hyapps.andyou.model;

/**
 * Created by Administrator on 2015/6/17.
 */
public class Apk {
    public String version;
    public String apk_file;
    public int size;
    public int force;

    public class QHApkList extends ResultList<Apk> {
    }

}
