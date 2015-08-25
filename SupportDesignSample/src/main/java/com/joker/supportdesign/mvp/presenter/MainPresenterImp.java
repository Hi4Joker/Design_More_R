package com.joker.supportdesign.mvp.presenter;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import com.joker.supportdesign.mvp.domain.ForecastEntity;
import com.joker.supportdesign.mvp.domain.event.LocationEvent;
import com.joker.supportdesign.mvp.domain.event.WeatherEvent;
import com.joker.supportdesign.mvp.domain.event.WeatherStateEvent;
import com.joker.supportdesign.mvp.model.MainInteractor;
import com.joker.supportdesign.mvp.model.MainInteractorImp;
import com.joker.supportdesign.mvp.viewInterface.MainView;
import com.joker.supportdesign.ui.WeatherLayout;
import com.joker.supportdesign.util.EventBusInstance;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import org.apache.http.HttpException;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Administrator on 2015/7/6.
 */
public class MainPresenterImp implements MainPresenter<MainView> {

  private static final String TAG = MainPresenterImp.class.getSimpleName();
  private MainView mainView;
  private MainInteractor mainInteractor;
  private CompositeSubscription compositeSubscription;

  public MainPresenterImp() {
  }

  @Override public void attachView(MainView view) {
    MainPresenterImp.this.mainView = view;
    mainInteractor = new MainInteractorImp();
    EventBusInstance.getDefault().register(MainPresenterImp.this);
  }

  @Override public void requestWeatherData(LocationManager locationManager) {

    mainView.onWeatherStateChange(WeatherLayout.FETCH_LOCATION_ING);

    if (compositeSubscription == null) {
      compositeSubscription = new CompositeSubscription();
    }

    mainInteractor.requestLocation(locationManager);
  }

  @Override public void onWeatherDetach() {

    if (compositeSubscription != null && compositeSubscription.hasSubscriptions()) {
      compositeSubscription.unsubscribe();
      compositeSubscription = null;
    }
    mainInteractor.onWeatherDetach();
  }

  @Override public void detachView(boolean retainInstance) {

    EventBusInstance.getDefault().unregister(MainPresenterImp.this);
  }

  public void onEventMainThread(LocationEvent<Location> event) {

    Observable<Location> locationObservable = event.getLocationObservable();
    mainInteractor.getWeatherData(locationObservable);
  }

  //public void onEventBackgroundThread(WeatherStateEvent event) {
  public void onEventMainThread(WeatherStateEvent event) {
    mainView.onWeatherStateChange(event.getState());
  }

  public void onEventMainThread(WeatherEvent event) {

    Observable<HashMap<String, ForecastEntity>> observable = event.getWeatherObservable();

    compositeSubscription.add(observable.subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<HashMap<String, ForecastEntity>>() {

          @Override public void onStart() {
            super.onStart();
            Log.e(TAG, "Subscribe 成功订阅 Observable");
          }

          @Override public void onNext(final HashMap<String, ForecastEntity> weatherData) {

            mainView.setWeather(weatherData);
          }

          @Override public void onCompleted() {

          }

          @Override public void onError(final Throwable error) {

            if (error instanceof TimeoutException) {

              Log.e(TAG, "获取位置信息失败");
              mainInteractor.stopRequestLocation();
              mainView.onWeatherStateChange(WeatherLayout.FETCH_LOCATION_ERROR);
            } else if (error instanceof RetrofitError || error instanceof HttpException) {

              Log.e(TAG, "获取天气信息失败");
              mainView.onWeatherStateChange(WeatherLayout.FETCH_WEATHER_ERROR);
            } else {
              Log.e(TAG, error.getMessage());
              error.printStackTrace();
              throw new RuntimeException("See inner exception");
            }
          }
        }));
  }
}
