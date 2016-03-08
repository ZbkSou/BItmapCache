package com.example.bkzhou.bitmapcache;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class MainActivity extends Activity implements View.OnClickListener {
  private static final String TAG = "MainActivity";
  private ImageView imageView;
  private Button button;
  private ImageMemoryCache memoryCache;
  private ImageFileCache fileCache;
  private String url = "http://f.hiphotos.baidu.com/album/w%3D2048/sign=7aa167f79f2f07085f052d00dd1cb999/472309f7905298228f794c7bd6ca7bcb0b46d4c4.jpg";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    memoryCache = new ImageMemoryCache(this);
    fileCache = new ImageFileCache(this);
    imageView = (ImageView) this.findViewById(R.id.imageview);
    button = (Button) this.findViewById(R.id.button);
    button.setOnClickListener(this);


  }

  public Bitmap getBitmap(String url) {
    Log.d(TAG, "getBitmap");
    Bitmap result = memoryCache.getBitmapFromCache(url);
    if (result == null) {//没有从内存找到从文件缓存中取
      Log.d(TAG, "从文件缓存中取");
      result = fileCache.getImage(url);
      if (result == null) {//两个缓存都没取到从网上取
        Log.d(TAG, "从网上中取");
        BitmapTask task = new BitmapTask();
        task.execute(url);
      } else {//把文件缓存图片转到内存中
        memoryCache.addBitmapToCache(url, result);
      }
    }
    return result;
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.button:
        Bitmap b = getBitmap(url);
        imageView.setImageBitmap(b);
        break;
    }
  }

  public class BitmapTask extends AsyncTask<String, Void, Bitmap> {

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      super.onPostExecute(bitmap);
      fileCache.saveBitmap(bitmap, url);
      memoryCache.addBitmapToCache(url, bitmap);
      imageView.setImageBitmap(bitmap);
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
      final HttpClient client = new HttpClient();
      final GetMethod httpGet = new GetMethod(url);
      Bitmap bitmap = null;
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      InputStream inputStream = null;
      try {
        int httpStats = client.executeMethod(httpGet);
        if (httpStats != 200) {
          return null;
        }
        inputStream = httpGet.getResponseBodyAsStream();
        int len = 0;

        byte[] responseBody = new byte[1024];
        while ((len = inputStream.read(responseBody)) != -1) {
          outputStream.write(responseBody, 0, len);
        }
        byte[] result = outputStream.toByteArray();
        bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);

      } catch (Exception e) {
        Log.d(TAG, e.toString());
      }
      return bitmap;
    }
  }
}
