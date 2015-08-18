
package com.cmcc.hyapps.andyou.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/**
 * @author kuloud
 */
public class FileUtils {

    // TODO: improve this
    public static String SDPATH = Environment.getExternalStorageDirectory().getPath();

    public static final String FILE_ROOT = "QHtravel";

    private static final String DIR_IMAGE = "image";

    private static final long LOW_STORAGE_THRESHOLD = 1024 * 1024 * 10;

    public static String getExternalRootDir() {
        File rootFile = new File(Environment.getExternalStorageDirectory(), FILE_ROOT);
        if (!rootFile.exists()) {
            rootFile.mkdir();
        }

        return rootFile.getAbsolutePath();
    }

    public static String getExternalImageDir() {
        File imageDir = new File(getExternalRootDir(), DIR_IMAGE);
        if (!imageDir.exists()) {
            imageDir.mkdir();
        }

        return imageDir.getAbsolutePath();
    }

    public static String saveBitmap(Bitmap bm, String picName) {
        FileOutputStream out = null;
        try {
            String imageDir = getExternalImageDir();
            File f = new File(imageDir, picName + ".JPEG");
            if (f.exists()) {
                f.delete();
            }
            out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 75, out);
            out.flush();
            return f.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static void cacheBitmap(Context context, Bitmap bm, String picName) {
        if (bm == null) {
            return;
        }
        FileOutputStream out = null;
        try {
            File f = new File(getCachePath(context, picName));
            if (f.exists()) {
                f.delete();
            }
            out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 75, out);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean fileCached(Context context, String fileName) {
        File f = new File(getCachePath(context, fileName));
        return f.exists();
    }

    public static String getCachePath(Context context, String fileName) {
        String name = fileName.replaceAll(" ", "_");
        return AppUtils.getAppSdRootPath() + name + ".JPEG";
    }

    public static void cleanCacheBitmap(Context context, String picName) {
        File f = new File(context.getCacheDir(), picName + ".JPEG");
        if (f.exists()) {
            f.delete();
        }
    }

    public static File createSDDir(String dirName) throws IOException {
        File dir = new File(SDPATH + dirName);
        Log.d(dir.getAbsolutePath());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            dir.mkdir();
        }
        return dir;
    }

    public static void delFile(String fileName) {
        File file = new File(SDPATH + fileName);
        if (file.isFile()) {
            file.delete();
        }
        file.exists();
    }

    public static void deleteDir() {
        File dir = new File(SDPATH);
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }

        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                deleteDir();
            }
        }
        dir.delete();
    }

    public static boolean isFileExist(String path) {
        File f = new File(path);
        return f.exists();
    }

    public static boolean isSdCardWrittenable() {

        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public static long getAvailableStorage() {

        String storageDirectory = null;
        storageDirectory = Environment.getExternalStorageDirectory().toString();

        try {
            StatFs stat = new StatFs(storageDirectory);
            long avaliableSize = ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize());
            return avaliableSize;
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    public static boolean checkAvailableStorage() {

        if (getAvailableStorage() < LOW_STORAGE_THRESHOLD) {
            return false;
        }

        return true;
    }

    public static boolean isSDCardPresent() {

        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static void prepareRootDir() throws IOException {

        File file = new File(FILE_ROOT);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
    }

    public static Bitmap getLocalBitmap(String url) {

        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String size(long size) {

        if (size / (1024 * 1024) > 0) {
            float tmpSize = (float) (size) / (float) (1024 * 1024);
            DecimalFormat df = new DecimalFormat("#.##");
            return "" + df.format(tmpSize) + "MB";
        } else if (size / 1024 > 0) {
            return "" + (size / (1024)) + "KB";
        } else {
            return "" + size + "B";
        }
    }

    public static void installAPK(Context context, final String url) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        String fileName = FILE_ROOT + NetUtils.getFileNameFromUrl(url);
        intent.setDataAndType(Uri.fromFile(new File(fileName)),
                "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClassName("com.android.packageinstaller",
                "com.android.packageinstaller.PackageInstallerActivity");
        context.startActivity(intent);
    }

    public static boolean delete(File path) {

        boolean result = true;
        if (path.exists()) {
            if (path.isDirectory()) {
                for (File child : path.listFiles()) {
                    result &= delete(child);
                }
                result &= path.delete(); // Delete empty directory.
            }
            if (path.isFile()) {
                result &= path.delete();
            }
            if (!result) {
                Log.e("Delete failed;");
            }
            return result;
        } else {
            Log.e("File does not exist.");
            return false;
        }
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String readFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        fin.close();
        return ret;
    }

    public static void stringToFile(String filename, String string) throws IOException {
        FileWriter out = new FileWriter(filename);
        try {
            out.write(string);
        } finally {
            out.close();
        }
    }
}
