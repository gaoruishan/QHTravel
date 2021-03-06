package com.cmcc.hyapps.andyou.util;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.cmcc.hyapps.andyou.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/**
 * Created by John on 2015/7/29.
 */
public class ImageUtil {
    public static void DisplayImage(String url, ImageView img)
    {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.recommand_bg)
                .showImageForEmptyUri(R.mipmap.recommand_bg)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .showImageOnFail(R.mipmap.recommand_bg)
                .considerExifParams(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoader.getInstance().displayImage(url, img,options);
    }

    public static void DisplayImage(String url, ImageView img, int loadingResource, int errorResource)
    {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(loadingResource)
                .showImageForEmptyUri(errorResource)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .showImageOnFail(errorResource)
                .considerExifParams(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoader.getInstance().displayImage(url, img, options);
    }
}
