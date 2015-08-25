package com.joker.supportdesign.mvp.domain;

import com.google.gson.annotations.SerializedName;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import rx.Observable;

/**
 * Created by Joker on 2015/7/6.
 */
public class BaseSimpleWeather {

  @SerializedName("cod") protected int httpCode;

  /*{
      "id": 800,
      "main": "Clear",
      "description": "Sky is Clear",
      "icon": "01d"
    }*/
  public class BaseWeather {
    private String main;
    private String description;
    private String icon;

    public String getMain() {
      return main;
    }

    public String getDescription() {
      return description;
    }

    public String getIcon() {
      return icon;
    }
  }

  public Observable getWeatherObservable() {
    if (httpCode == HttpStatus.SC_OK) {
      return Observable.just(this);
    } else {
      return Observable.error(new HttpException("There was a problem fetching the weather data."));
    }
  }
}
