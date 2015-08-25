package com.joker.supportdesign.mvp.presenter;

import android.content.Context;
import android.location.LocationManager;
import com.joker.supportdesign.mvp.viewInterface.MvpView;

/**
 * Created by Joker on 2015/7/6.
 */
public interface MainPresenter<T extends MvpView> extends MvpPresenter<T> {

  void requestWeatherData(LocationManager locationManager);

  void onWeatherDetach();
}
