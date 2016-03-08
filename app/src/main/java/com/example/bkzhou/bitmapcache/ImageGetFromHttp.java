package com.example.bkzhou.bitmapcache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by bkzhou on 16-3-7.
 */
public class ImageGetFromHttp {
  private static final String TAG = "ImageGetFromHttp";
  public static Bitmap downloadBitmap(String url){
    final HttpClient client = new HttpClient();
    final GetMethod httpGet = new GetMethod(url);
    Bitmap bitmap = null;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    InputStream inputStream = null;
    try{
      int httpStats = client.executeMethod(httpGet);
      if (httpStats !=200){
        return null;
      }
      inputStream = httpGet.getResponseBodyAsStream();
      int len = 0;

      byte[] responseBody = new byte[1024];
      while ((len = inputStream.read(responseBody)) != -1) {
        outputStream.write(responseBody,0,len);
      }
      byte[] result = outputStream.toByteArray();
      bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);

    }catch (Exception e){
      Log.d(TAG,e.toString());
    }
    return bitmap;
  }
}
