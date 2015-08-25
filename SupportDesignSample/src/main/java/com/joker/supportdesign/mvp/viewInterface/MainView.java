package com.joker.supportdesign.mvp.viewInterface;

import com.joker.supportdesign.mvp.domain.ForecastEntity;
import com.joker.supportdesign.ui.WeatherLayout;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/7/6.
 */
public interface MainView extends MvpView {

  void setWeather(HashMap<String, ForecastEntity> weatherData);

  void onWeatherStateChange(@WeatherLayout.Status int state);
}
