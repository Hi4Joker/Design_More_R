package com.joker.supportdesign.mvp.domain;

/**
 * Created by joker on 2015/7/6.
 */
public class ForecastEntity implements Cloneable {

  protected float minTemperature;
  protected float maxTemperature;
  protected long date;
  protected String locationName;
  protected String description;
  protected String iconUrl;

  public ForecastEntity() {
  }

  public float getMinTemperature() {
    return minTemperature;
  }

  public void setMinTemperature(float minTemperature) {
    this.minTemperature = minTemperature;
  }

  public float getMaxTemperature() {
    return maxTemperature;
  }

  public void setMaxTemperature(float maxTemperature) {
    this.maxTemperature = maxTemperature;
  }

  public long getDate() {
    return date;
  }

  public void setDate(long date) {
    this.date = date;
  }

  public String getLocationName() {
    return locationName;
  }

  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  public ForecastEntity newInstance() {

    try {
      return (ForecastEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {

    ForecastEntity forecastEntity = (ForecastEntity) super.clone();

    return forecastEntity;
  }
}
