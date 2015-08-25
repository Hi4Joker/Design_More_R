package com.joker.supportdesign.mvp.domain.event;

import com.joker.supportdesign.ui.WeatherLayout;

/**
 * Created by Joker on 2015/7/8.
 */
public class WeatherStateEvent {

  @WeatherLayout.Status private int state;

  public WeatherStateEvent(@WeatherLayout.Status int state) {
    this.state = state;
  }

  @WeatherLayout.Status public int getState() {
    return state;
  }
}
