package com.joker.app.rx;

import android.util.Log;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import java.util.concurrent.TimeUnit;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by Joker on 2015/8/8.
 */
public class LoginService {

  public static final long TIMEOUT = 6 * 1000;
  private static final String WEB_SERVICE_BASE_URL = "YOUR BASE URL";
  private static final String TAG = "LaunchActivity";
  private final LoginRetrofit loginRetrofit;

  public LoginService() {
    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override public void intercept(RequestInterceptor.RequestFacade request) {
        request.addHeader("Accept", "application/json");
      }
    };

    Gson gson =
        new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(ResultEntity.class, new DateTypeAdapter())
            .create();

    RestAdapter restAdapter = new RestAdapter.Builder()
        .setEndpoint(WEB_SERVICE_BASE_URL)
        .setRequestInterceptor(requestInterceptor)
        .setConverter(new GsonConverter(gson))
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .build();

    loginRetrofit = restAdapter.create(LoginRetrofit.class);
  }

  public Observable<ResultEntity> login(UserInfoEntity userInfoEntity) {

    ResultEntity resultEntity = new ResultEntity();

    Log.e(TAG, "模拟网络请求");
    try {
      Thread.sleep(3 * 1000);
    } catch (InterruptedException e) {
      /*模拟请求网络  request network*/
    }

    return resultEntity.valueObservable(1, "some data").doOnNext(new Action1<ResultEntity>() {
      @Override public void call(ResultEntity resultEntity) {

        /*cacheInMemory or disk */
        Log.e(TAG, "缓存网络请求：code：" + resultEntity.code);
        Log.e(TAG, "缓存网络请求：message：" + resultEntity.message);

        try {
          Thread.sleep(1 * 1000);
        } catch (InterruptedException e) {
          /*模拟缓存 cache where you want*/
        }
      }
    }).timeout(TIMEOUT, TimeUnit.MILLISECONDS);

    //真实的网络请求
    /*return loginRetrofit.fetchLogin(userInfoEntity.userName, userInfoEntity.passWord)
        .retry(new Func2<Integer, Throwable, Boolean>() {
          @Override public Boolean call(Integer integer, Throwable throwable) {

            if (throwable instanceof RetrofitError && integer <= 2) {*//*重试三次*//*
              return true;
            }
            return false;
          }
        });*/
  }
}
