package com.example.bkzhou.bitmapcache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by bkzhou on 16-3-4.
 */
public class ImageFileCache {
  private static final String TAG = "ImageFileCache";
  private static final String CACHDIR = "ImgCach";
  private static final String WHOLESALE_CONV = ".cach";

  private static final int MB = 1024 * 1024;
  private static final int CACHE_SIZE = 10;
  private static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 10;
  private Context context;

  public ImageFileCache(Context context) {
    this.context  = context;
//    removeCache(getDirectory());
  }

  public Bitmap getImage(final String url) {
    final String path = getDirectory() + "/" + convertUrlToFileName(url);
    File file = new File(path);
    if (file.exists()) {
      Bitmap bmp = BitmapFactory.decodeFile(path);
      if (bmp == null) {
        file.delete();
      } else {
        updateFileTime(path);
        return bmp;
      }
    }
    return null;
  }

  public void saveBitmap(Bitmap bm, String url) {
    if (bm == null) {
      return;
    }
    if (FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
      return;
    }
    String filename = convertUrlToFileName(url);
    String dir = getDirectory();
    Log.d(TAG, dir);
    File dirFile = new File(dir);
    if (!dirFile.exists()) {

      Log.d(TAG, dirFile.mkdirs() + "");
    }//第一次创建
    Log.d(TAG, dirFile.exists() + "");
    File file = new File(dir + "/" + filename);
    Log.d(TAG, filename);

    try {
      file.createNewFile();
      OutputStream outputStream = new FileOutputStream(file);
      bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
      outputStream.flush();
      outputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
      Log.d(TAG, e.toString());
    }
  }

  /**
   * 修改文件的最后修改时间 *
   */
  public void updateFileTime(String path) {
    File file = new File(path);
    long newModifiedTime = System.currentTimeMillis();
    file.setLastModified(newModifiedTime);
  }

  /**
   * 将url转成文件名 *
   */
  private String convertUrlToFileName(String url) {
    String[] strs = url.split("/");
    return strs[strs.length - 1] + WHOLESALE_CONV;
  }

  private boolean removeCache(String dirPath) {
    File dir = new File(dirPath);
    File[] files = dir.listFiles();
    if (files == null) {
      return true;
    }
    if (!android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      return false;
    }
    int dirSize = 0;
    for (int i = 0; i < files.length; i++) {
      if (files[i].getName().contains(WHOLESALE_CONV)) {
        dirSize += files[i].length();
      }
    }
    if (dirSize > CACHE_SIZE * MB || FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
      int removeFactor = (int) ((0.4 * files.length) + 1);
      Arrays.sort(files, new FileLastModifSort());
      for (int i = 0; i < removeFactor; i++) {
        if (files[i].getName().contains(WHOLESALE_CONV)) {
          files[i].delete();
        }
      }
    }
    if (freeSpaceOnSd() <= CACHE_SIZE) {
      return false;
    }
    return true;
  }

  /**
   * 取缓存目录
   */
  private String getDirectory() {
    String dir = getSDPath() + "/" + CACHDIR;
    return dir;
  }

  private String getSDPath() {
    File sdDir = null;
    boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    if (sdCardExist) {
      sdDir = Environment.getExternalStorageDirectory();
    }
//    if (sdDir != null) {
//      return sdDir.toString();
//    } else {
      return context.getCacheDir().toString();
//    }
  }

  private int freeSpaceOnSd() {
    StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
    double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize()) / MB;
    return (int) sdFreeMB;
  }

  /**
   * 根据文件的最后修改时间进行排序
   */
  private class FileLastModifSort implements Comparator<File> {
    public int compare(File arg0, File arg1) {
      if (arg0.lastModified() > arg1.lastModified()) {
        return 1;
      } else if (arg0.lastModified() == arg1.lastModified()) {
        return 0;
      } else {
        return -1;
      }
    }
  }

}
