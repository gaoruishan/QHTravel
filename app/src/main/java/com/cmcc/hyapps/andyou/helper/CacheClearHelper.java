package com.cmcc.hyapps.andyou.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;

import com.cmcc.hyapps.andyou.utils.ToastUtils;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2015/7ß/6.
 *
 * 1.功能相当于,点击了 应用程序信息 里面的 清楚缓存按钮，而非 清除数据
 *
 * 2.功能相当于,删除了/data/data/packageName/cache 文件夹里面所有的东西
 *
 * 3.需要权限 <uses-permission android:name="android.permission.CLEAR_APP_CACHE" />
 */

public class CacheClearHelper {

    public static void clearCache(final Context context) {

        try {
            PackageManager packageManager = context.getPackageManager();
            Method localMethod = packageManager.getClass().getMethod("freeStorageAndNotify", Long.TYPE,
                    IPackageDataObserver.class);
            Long localLong = Long.valueOf(getEnvironmentSize() - 1L);
            Object[] arrayOfObject = new Object[2];
            arrayOfObject[0] = localLong;
            localMethod.invoke(packageManager, localLong, new IPackageDataObserver.Stub() {

                @Override
                public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.show(context,"清除缓存成功");
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * * 方法：getDataDirectory()：返回 File ，获取 Android 数据目录。
     * 方法：getDownloadCacheDirectory() 返回 File ，获取 Android 下载/缓存内容目录。
     * 方法：getExternalStorageDirectory()：返回 File ，获取外部存储目录即 SDCard
     * 方法：getExternalStoragePublicDirectory(String type)：返回 File ，取一个高端的公用的外部存储器目录来摆放某些类型的文件
     * 方法：getExternalStorageState()：返回 File ，获取外部存储设备的当前状态
     * 方法：getRootDirectory()：返回 File ，获取 Android 的根目录
     * @return
     */
    private static long getEnvironmentSize() {
        File localFile = Environment.getDataDirectory();
        long l1;
        if (localFile == null)
            l1 = 0L;
        while (true) {
            String str = localFile.getPath();
            StatFs localStatFs = new StatFs(str);
            long l2 = localStatFs.getBlockSize();
            l1 = localStatFs.getBlockCount() * l2;
            return l1;
        }

    }
}
