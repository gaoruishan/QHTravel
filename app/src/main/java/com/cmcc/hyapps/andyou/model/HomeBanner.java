package com.cmcc.hyapps.andyou.model;
/**
 * Created by Administrator on 2015/5/19.
 */
public class HomeBanner {
//        "id": 1,
//        "image_url": "http://selftravel-image.qiniudn.com/www.bmp",
//        "stype": 1,
//        "action": "22"

    //H5 景区 视频 攻略 (H5，视频 暂无)
    public static final int SCENIC = 0;
    public static final int VIDEO = 1;
    public static final int H5 = 2;
    public static final int STRATEGY = 3;
    public int id;
    public String image_url;
    public int stype;
    public String action;

    public class HomeBannerLists extends ResultList<HomeBanner> {

    }
}
