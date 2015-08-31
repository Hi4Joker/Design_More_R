package com.makeinfo.flowerpi.API;

import com.makeinfo.flowerpi.model.gitmodel;

import java.util.Map;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.QueryMap;

public interface gitapi {
  @GET("/users/{user}") void getFeed(@Path("user") String user, Callback<gitmodel> response);

  @Headers("Accept: application/json") @GET("/users/{user}") void call(@Path("user") String user,
      @QueryMap Map<String, String> map, Callback<gitmodel> response);
}
