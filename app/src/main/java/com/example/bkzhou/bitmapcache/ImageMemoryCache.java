package com.example.bkzhou.bitmapcache;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bkzhou on 16-3-4.
 */
public class ImageMemoryCache {
  private static final String TAG = "ImageMemoryCach";
  private static final int SOFT_CACHE_SIZE = 15;
  private static LruCache<String,Bitmap> mLruCache;
  private static LinkedHashMap<String,SoftReference<Bitmap>> mSoftCache;
  public ImageMemoryCache(Context context){
    int memClass = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    Log.d(TAG,memClass+"");
    int cacheSize = 1024*1024*memClass /4;
    mLruCache = new LruCache<String, Bitmap>(cacheSize){
      @Override
      protected int sizeOf(String key, Bitmap value) {
        if(value != null)
          return value.getRowBytes()*value.getHeight();
        else
          return 0;
      }

      @Override
      protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);
        if (oldValue != null)
          mSoftCache.put(key,new SoftReference<Bitmap>(oldValue));
      }
    };
    mSoftCache = new LinkedHashMap<String,SoftReference<Bitmap>>(SOFT_CACHE_SIZE,0.75f,true){
      private static final long serialVersionUID = 6040103833179403725L;
      @Override
      protected boolean removeEldestEntry(Entry<String, SoftReference<Bitmap>> eldest) {
        if(size()>SOFT_CACHE_SIZE){
          return true;
        }
        return false;
      }
    };

  }
  public Bitmap getBitmapFromCache(String url){
    Bitmap bitmap ;
    synchronized (mLruCache){
      bitmap = mLruCache.get(url);
      Log.d(TAG,"getBitmapFromCache "+url+bitmap);
      if (bitmap != null){
        mLruCache.remove(url);
        mLruCache.put(url,bitmap);
        return bitmap;
      }
    }
    synchronized (mSoftCache){
      SoftReference<Bitmap> bitmapSoftReference = mSoftCache.get(url);
      if(bitmapSoftReference != null){
        bitmap = bitmapSoftReference.get();
        if (bitmap != null){
          mLruCache.put(url,bitmap);
          mSoftCache.remove(url);
          return bitmap;
        }else {
          mSoftCache.remove(url);
        }
      }
    }
    return null;
  }
  public void addBitmapToCache(String url ,Bitmap bitmap){
    if(bitmap != null){
      Log.d(TAG,"addBitmapToCache "+url);
      mLruCache.put(url,bitmap);
    }

  }
  public void clearCache() {
    mSoftCache.clear();
  }

}
