package com.joker.supportdesign.mvp.domain.event;

import android.location.Location;
import com.joker.supportdesign.mvp.domain.ForecastEntity;
import java.util.HashMap;
import rx.Observable;

/**
 * Created by Joker on 2015/7/6.
 */
public class WeatherEvent<T extends HashMap<String, ForecastEntity>> {

  private Observable<T> observable;

  public WeatherEvent(Observable<T> observable) {
    this.observable = observable;
  }

  public Observable<T> getWeatherObservable() {
    return observable;
  }
}
