package com.app.designmore.retrofit.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * Created by Joker on 2015/9/9.
 */
public class Province {

  @Expose @SerializedName("areaId") private String provinceId;
  @Expose @SerializedName("areaName") private String provinceName;
  @Expose @SerializedName("cities") private ArrayList<City> cities = new ArrayList<>();

  public String getProvinceId() {
    return provinceId;
  }

  public String getProvinceName() {
    return provinceName;
  }

  public ArrayList<City> getCities() {
    return cities;
  }

  public static class City {
    @Expose @SerializedName("areaId") public String cityId;
    @Expose @SerializedName("areaName") public String cityName;

    @Override public String toString() {
      return "City{" +
          "cityId='" + cityId + '\'' +
          ", cityName='" + cityName + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "Province{" +
        "provinceId='" + provinceId + '\'' +
        ", provinceName='" + provinceName + '\'' +
        ", cities=" + cities +
        '}';
  }
}

