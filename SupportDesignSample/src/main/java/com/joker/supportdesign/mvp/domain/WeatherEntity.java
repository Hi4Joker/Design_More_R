package com.joker.supportdesign.mvp.domain;

import java.util.List;

/**
 * Created by Joker on 2015/7/7.
 */
public class WeatherEntity extends ForecastEntity {
  private float currTemperature;

  private List<ForecastEntity> list;

  public WeatherEntity() {
  }

  public float getCurrTemperature() {
    return currTemperature;
  }

  public void setCurrTemperature(float currTemperature) {
    this.currTemperature = currTemperature;
  }

  public List<ForecastEntity> getList() {
    return list;
  }

  public void setList(List<ForecastEntity> list) {
    this.list = list;
  }
}
