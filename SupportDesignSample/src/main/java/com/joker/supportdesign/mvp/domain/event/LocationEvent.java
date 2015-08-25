package com.joker.supportdesign.mvp.domain.event;

import android.location.Location;
import rx.Observable;

/**
 * Created by Joker on 2015/7/6.
 */
public class LocationEvent<T extends Location> {

  private Observable<T> observable;

  public LocationEvent() {
  }

  public LocationEvent(Observable<T> observable) {
    this.observable = observable;
  }

  public Observable<T> getLocationObservable() {
    return observable;
  }
}
