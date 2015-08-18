
package com.cmcc.hyapps.andyou.data;

import android.content.Context;
import android.graphics.Bitmap;

import com.cmcc.hyapps.andyou.util.FileUtils;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

public class ImageLoaderManager {
    private ImageLoader mImageLoader;
    private static ImageLoaderManager sInstance = new ImageLoaderManager();
    private String mCacheDir;

    private ImageLoaderManager() {
    }

    public static ImageLoaderManager getInstance() {
        return sInstance;
    }

    public ImageLoader getLoader() {
        return mImageLoader;
    }

    public void init(Context context) {
        //保存位置
        File file = new File(FileUtils.getExternalImageDir(), ".photoCache");
        if (file.exists()) {
            file.delete();
        }
        file.mkdirs();
        mCacheDir = file.getAbsolutePath();
        File cacheDir = StorageUtils.getOwnCacheDirectory(context,
                mCacheDir);
        //图片设置
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .considerExifParams(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .diskCache(new UnlimitedDiscCache(cacheDir))
                .memoryCache(new WeakMemoryCache());
        ImageLoaderConfiguration config = builder.build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(config);
    }

}
