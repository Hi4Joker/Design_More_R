package com.app.designmore.manager;

import android.content.Context;
import com.app.designmore.Constants;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Joker on 2015/9/1.
 */
public class OkHttpInstance {

  private static OkHttpClient singleton;

  /*public static OkHttpClient getInstance(Context context) {
    if (singleton == null) {
      synchronized (OkHttpInstance.class) {
        if (singleton == null) {
          File cacheDir = new File(context.getCacheDir(), Config.RESPONSE_CACHE);
          singleton = new OkHttpClient();
          try {
            singleton.setCache(new Cache(cacheDir, Config.RESPONSE_CACHE_SIZE));
          } catch (IOException e) {
            e.printStackTrace();
          }
          singleton.setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
          singleton.setReadTimeout(Constants.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS);
        }
      }
    }
    return singleton;
  }*/
}
