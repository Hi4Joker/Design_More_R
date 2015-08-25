package com.joker.supportdesign.mvp.viewInterface;

import com.joker.supportdesign.mvp.domain.CurrentWeather;
import com.joker.supportdesign.mvp.domain.ForecastWeather;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Joker on 2015/7/6.
 */
public interface OpenWeatherService {

  @GET("/weather?units=metric") Observable<CurrentWeather> fetchCurrentWeather(
      @Query("lon") double longitude, @Query("lat") double latitude);

  @GET("/forecast/daily?units=metric&cnt=3") Observable<ForecastWeather> fetchWeatherForecasts(
      @Query("lon") double longitude, @Query("lat") double latitude);
}
