package com.joker.supportdesign.mvp.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * Created by Joker on 2015/7/6.
 */
public class CurrentWeather extends BaseSimpleWeather {

  @SerializedName("name") public String locationName;
  @SerializedName("dt") public long timestamp;

  public ArrayList<BaseWeather> weather;

  public Main main;

  public class Main {
    @SerializedName("temp") public float currTemperature;
    @SerializedName("temp_min") public float minTemperature;
    @SerializedName("temp_max") public float maxTemperature;
  }
}
