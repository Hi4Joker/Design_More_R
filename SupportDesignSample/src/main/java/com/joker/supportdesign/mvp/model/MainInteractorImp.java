package com.joker.supportdesign.mvp.model;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import com.joker.supportdesign.mvp.domain.ForecastEntity;
import com.joker.supportdesign.mvp.domain.WeatherEntity;
import com.joker.supportdesign.mvp.domain.event.LocationEvent;
import com.joker.supportdesign.mvp.domain.event.WeatherEvent;
import com.joker.supportdesign.mvp.domain.event.WeatherStateEvent;
import com.joker.supportdesign.ui.WeatherLayout;
import com.joker.supportdesign.util.EventBusInstance;
import de.greenrobot.event.EventBus;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Joker on 2015/7/6.
 */
public class MainInteractorImp implements MainInteractor {

  private static final String TAG = MainInteractorImp.class.getSimpleName();
  public static final String KEY_CURRENT_WEATHER = "key_current_weather";
  public static final String KEY_FORECAST_WEATHERS = "key_forecast_weathers";
  private static final long LOCATION_TIMEOUT_SECONDS = 8;

  private EventBus eventBus;

  private LocationService locationService;
  private WeatherService weatherService;

  public MainInteractorImp() {

    MainInteractorImp.this.eventBus = EventBusInstance.getDefault();
  }

  @Override public void requestLocation(final LocationManager locationManager) {

    /*发送event至MainPresenterImp*/
    eventBus.post(new LocationEvent<>(Observable.defer(new Func0<Observable<Location>>() {
      @Override public Observable<Location> call() {

        locationService = new LocationService(locationManager);

        return locationService.fetchLocation();
      }
    }).timeout(LOCATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)));
  }

  @Override public void getWeatherData(final Observable<? extends Location> observable) {

    final Observable<HashMap<String, ForecastEntity>> mapObservable =
        Observable.defer(new Func0<Observable<HashMap<String, ForecastEntity>>>() {
          @Override public Observable<HashMap<String, ForecastEntity>> call() {

            return observable.flatMap(
                new Func1<Location, Observable<HashMap<String, ForecastEntity>>>() {
                  @Override
                  public Observable<HashMap<String, ForecastEntity>> call(final Location location) {

                    Log.e(TAG, "获取天气信息ing");

                    eventBus.post(new WeatherStateEvent(WeatherLayout.FETCH_WEATHER_ING));

                    weatherService = new WeatherService();

                    //openweathermap.org/img/w/10n.png
                    final double longitude = location.getLongitude();
                    final double latitude = location.getLatitude();

                    return Observable.zip(

                        /*根据经纬度获取当日和包括当日在内的未来两天天气*/
                        weatherService.fetchCurrentWeather(longitude, latitude),
                        weatherService.fetchWeatherForecasts(longitude, latitude),

                        /*并发请求，并返回结果后，统一处理*/
                        new Func2<WeatherEntity, List<ForecastEntity>, HashMap<String, ForecastEntity>>() {

                          @Override public HashMap<String, ForecastEntity> call(
                              final WeatherEntity currentWeather,
                              final List<ForecastEntity> forecastWeathers) {

                            HashMap<String, ForecastEntity> weatherData = new HashMap<>();

                            WeatherEntity forecastEntity = new WeatherEntity();
                            forecastEntity.setList(forecastWeathers);

                            weatherData.put(KEY_CURRENT_WEATHER, currentWeather);
                            weatherData.put(KEY_FORECAST_WEATHERS, forecastEntity);

                            return weatherData;
                          }
                        });
                  }
                });
          }
        });

    /*发送event至MainPresenterImp*/
    eventBus.post(new WeatherEvent<>(mapObservable));
  }

  @Override public void stopRequestLocation() {

    Log.e(TAG, "stopRequestLocation");
    Log.e(TAG, "" + locationService.stopFetchLocation());
  }

  @Override public void onWeatherDetach() {

    locationService = null;
    weatherService = null;
  }
}
