package com.joker.supportdesign.mvp.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * Created by Joker on 2015/7/6.
 */
public class ForecastWeather extends BaseSimpleWeather {

  public ArrayList<ForecastDataEnvelope> list;

  public Location city;

  public class Location {
    public String name;
  }

  public class ForecastDataEnvelope {
    @SerializedName("dt") public long date;
    @SerializedName("temp") public Temperature temperature;
    public ArrayList<BaseWeather> weather;

    public class Temperature {
      @SerializedName("min") public float minTemperature;
      @SerializedName("max") public float maxTemperature;
    }
  }
}
