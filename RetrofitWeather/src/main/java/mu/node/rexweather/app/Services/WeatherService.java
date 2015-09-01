package mu.node.rexweather.app.Services;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.apache.http.HttpException;

import java.util.ArrayList;
import java.util.List;

import mu.node.rexweather.app.Models.CurrentWeather;
import mu.node.rexweather.app.Models.WeatherForecast;
import org.apache.http.HttpStatus;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.appengine.UrlFetchClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

public class WeatherService {
  // We are implementing against version 2.5 of the Open Weather Map web service.

  private static final long LOCATION_TIMEOUT_SECONDS = 10;
  private static final String WEB_SERVICE_BASE_URL = "http://api.openweathermap.org/data/2.5";
  private final OpenWeatherMapWebService mWebService;

  public WeatherService() {
    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override public void intercept(RequestInterceptor.RequestFacade request) {
        request.addHeader("Accept", "application/json");
      }
    };

    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(WEB_SERVICE_BASE_URL)
        .setRequestInterceptor(requestInterceptor)
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .setClient(new UrlFetchClient())
        .setConverter(new GsonConverter(new Gson()))
        .build();

    mWebService = restAdapter.create(OpenWeatherMapWebService.class);
  }

  public Observable<CurrentWeather> fetchCurrentWeather(final double longitude,
      final double latitude) {

    Observable<CurrentWeather> observable =

        Observable.defer(new Func0<Observable<CurrentWeatherDataEnvelope>>() {
          @Override public Observable<CurrentWeatherDataEnvelope> call() {

            return mWebService.fetchCurrentWeather(longitude, latitude,
                new Callback<CurrentWeatherDataEnvelope>() {
                  @Override
                  public void success(CurrentWeatherDataEnvelope currentWeatherDataEnvelope,
                      Response response) {
                  }

                  @Override public void failure(RetrofitError error) {

                  }
                });
          }
        })
            .flatMap(
                new Func1<CurrentWeatherDataEnvelope, Observable<? extends CurrentWeatherDataEnvelope>>() {

                  // Error out if the request was not successful.
                  @Override public Observable<? extends CurrentWeatherDataEnvelope> call(
                      final CurrentWeatherDataEnvelope data) {
                    Log.e("Joker", data.toString());
                    return data.filterWebServiceErrors();
                  }
                })
            .map(new Func1<CurrentWeatherDataEnvelope, CurrentWeather>() {

              // Parse the result and build a CurrentWeather object.
              @Override public CurrentWeather call(final CurrentWeatherDataEnvelope data) {

                return new CurrentWeather(data.locationName, data.timestamp,
                    data.weather.get(0).description, data.main.temp, data.main.temp_min,
                    data.main.temp_max);
              }
            });

    return observable;
  }

  public Observable<List<WeatherForecast>> fetchWeatherForecasts(final double longitude,
      final double latitude) {

    return mWebService.fetchWeatherForecasts(longitude, latitude)
        .flatMap(
            new Func1<WeatherForecastListDataEnvelope, Observable<? extends WeatherForecastListDataEnvelope>>() {

              // Error out if the request was not successful.
              @Override public Observable<? extends WeatherForecastListDataEnvelope> call(
                  final WeatherForecastListDataEnvelope listData) {

                return listData.filterWebServiceErrors();
              }
            })
        .map(new Func1<WeatherForecastListDataEnvelope, List<WeatherForecast>>() {

          // Parse the result and build a list of WeatherForecast objects.
          @Override public List<WeatherForecast> call(
              final WeatherForecastListDataEnvelope Envelope) {

            final ArrayList<WeatherForecast> weatherForecasts = new ArrayList<>();

            for (WeatherForecastListDataEnvelope.ForecastDataEnvelope data : Envelope.list) {

              final WeatherForecast weatherForecast =
                  new WeatherForecast(Envelope.city.name, data.timestamp,
                      data.weather.get(0).description, data.temp.min, data.temp.max);
              weatherForecasts.add(weatherForecast);
            }

            return weatherForecasts;
          }
        });
  }

  private interface OpenWeatherMapWebService {
    @GET("/weather?units=metric") Observable<CurrentWeatherDataEnvelope> fetchCurrentWeather(
        @Query("lon") double longitude, @Query("lat") double latitude,
        Callback<CurrentWeatherDataEnvelope> call);

    @GET("/forecast/daily?units=metric&cnt=7")
    Observable<WeatherForecastListDataEnvelope> fetchWeatherForecasts(
        @Query("lon") double longitude, @Query("lat") double latitude);

    @GET("/forecast/daily?units=metric&cnt=7") Response call(@Query("lon") double longitude,
        @Query("lat") double latitude);
  }

  /**
   * Base class for results returned by the weather web service.
   */
  private class WeatherDataEnvelope {
    @SerializedName("cod") private int httpCode;

    class Weather {
      public String description;
    }

    /**
     * The web service always returns a HTTP header code of 200 and communicates errors
     * through a 'cod' field in the JSON payload of the response body.
     */
    public Observable filterWebServiceErrors() {
      if (httpCode == HttpStatus.SC_OK) {
        return Observable.just(this);
      } else {
        return Observable.error(
            new HttpException("There was a problem fetching the weather data."));
      }
    }
  }

  /**
   * Data structure for current weather results returned by the web service.
   */
  private class CurrentWeatherDataEnvelope extends WeatherDataEnvelope {
    @SerializedName("name") public String locationName;
    @SerializedName("dt") public long timestamp;
    public ArrayList<Weather> weather;
    public Main main;

    class Main {
      public float temp;
      public float temp_min;
      public float temp_max;
    }
  }

  /**
   * Data structure for weather forecast results returned by the web service.
   */
  private class WeatherForecastListDataEnvelope extends WeatherDataEnvelope {
    public ArrayList<ForecastDataEnvelope> list;
    public Location city;

    class Location {
      public String name;
    }

    class ForecastDataEnvelope {
      @SerializedName("dt") public long timestamp;
      public Temperature temp;
      public ArrayList<Weather> weather;

      class Temperature {
        public float min;
        public float max;
      }
    }
  }
}
