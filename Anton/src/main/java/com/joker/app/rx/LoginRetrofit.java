package com.joker.app.rx;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Joker on 2015/8/8.
 */
public interface LoginRetrofit {

  /**
   * e.g. /login?userName=name?passWord=password
   */
  @GET("/login") Observable<ResultEntity> fetchLogin(@Query("userName") String userName,
      @Query("passWord") String passWord);
}
