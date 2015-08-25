package com.joker.supportdesign.mvp.model;

import android.util.Log;
import com.joker.supportdesign.mvp.domain.CurrentWeather;
import com.joker.supportdesign.mvp.domain.ForecastEntity;
import com.joker.supportdesign.mvp.domain.ForecastWeather;
import com.joker.supportdesign.mvp.domain.WeatherEntity;
import com.joker.supportdesign.mvp.viewInterface.OpenWeatherService;
import java.util.ArrayList;
import java.util.List;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;
import rx.functions.Func1;

public class WeatherService {
  // We are implementing against version 2.5 of the Open ForecastEntity Map web service.

  private static final long LOCATION_TIMEOUT_SECONDS = 10;
  private static final String WEB_SERVICE_BASE_URL = "http://api.openweathermap.org/data/2.5";
  private static final String TAG = WeatherService.class.getSimpleName();
  private final OpenWeatherService mWebService;

  public WeatherService() {
    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override public void intercept(RequestFacade request) {
        request.addHeader("Accept", "application/json");
      }
    };

    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(WEB_SERVICE_BASE_URL)
        .setRequestInterceptor(requestInterceptor)
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .build();

    mWebService = restAdapter.create(OpenWeatherService.class);
  }

  public Observable<WeatherEntity> fetchCurrentWeather(final double longitude,
      final double latitude) {

    Observable<WeatherEntity> observable =
        Observable.defer(new Func0<Observable<CurrentWeather>>() {
          @Override public Observable<CurrentWeather> call() {

            return mWebService.fetchCurrentWeather(longitude, latitude);
          }
        }).flatMap(new Func1<CurrentWeather, Observable<? extends CurrentWeather>>() {

          // Error out if the request was not successful.
          @Override public Observable<? extends CurrentWeather> call(CurrentWeather data) {
            return data.getWeatherObservable();
          }
        }).map(new Func1<CurrentWeather, WeatherEntity>() {
          @Override public WeatherEntity call(CurrentWeather data) {

            WeatherEntity weather = new WeatherEntity();

            weather.setDate(data.timestamp);
            weather.setLocationName(data.locationName);
            weather.setDescription(data.weather.get(0).getDescription());
            weather.setIconUrl(data.weather.get(0).getIcon());
            weather.setCurrTemperature(data.main.currTemperature);
           /* weather.setMinTemperature(data.main.minTemperature);
            weather.setMaxTemperature(data.main.maxTemperature);*/

            Log.e(TAG, "当日天气,获取成功");

            return weather;
          }
        });

    return observable;
  }

  public Observable<List<ForecastEntity>> fetchWeatherForecasts(final double longitude,
      final double latitude) {

    Observable<List<ForecastEntity>> observable =

        Observable.defer(new Func0<Observable<ForecastWeather>>() {
          @Override public Observable<ForecastWeather> call() {

            return mWebService.fetchWeatherForecasts(longitude, latitude);
          }
        }).flatMap(new Func1<ForecastWeather, Observable<? extends ForecastWeather>>() {

          // Error out if the request was not successful.
          @Override public Observable<? extends ForecastWeather> call(final ForecastWeather data) {

            return data.getWeatherObservable();
          }
        }).map(new Func1<ForecastWeather, List<ForecastEntity>>() {

          // Parse the result and build a list of WeatherForecast objects.
          @Override public List<ForecastEntity> call(final ForecastWeather forecastWeather) {

            final ArrayList<ForecastEntity> forecastWeatherList = new ArrayList<>();

            ForecastEntity weather = new ForecastEntity();

            for (ForecastWeather.ForecastDataEnvelope data : forecastWeather.list) {

              ForecastEntity clone = weather.newInstance();

              clone.setDate(data.date);
              clone.setLocationName(forecastWeather.city.name);
              clone.setDescription(data.weather.get(0).getDescription());
              clone.setIconUrl(data.weather.get(0).getIcon());
              clone.setMinTemperature(data.temperature.minTemperature);
              clone.setMaxTemperature(data.temperature.maxTemperature);
              forecastWeatherList.add(clone);
            }

            Log.e(TAG, "未来三天,天气获取成功");

            return forecastWeatherList;
          }
        });

    return observable;
  }
}
