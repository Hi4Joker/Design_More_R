package mu.node.rexweather.app.Services;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Implement an Rx-style location service by wrapping the Android LocationManager and providing
 * the location result as an Observable.
 */
public class LocationService {
  private final LocationManager mLocationManager;

  public LocationService(LocationManager locationManager) {
    mLocationManager = locationManager;
  }

  public Observable<Location> getLocation() {

    Observable<String> observable = Observable.just("").map(new Func1<String, String>() {
      @Override public String call(String s) {
        return null;
      }
    }).flatMap(new Func1<String, Observable<String>>() {
      @Override public Observable<String> call(String s) {
        return null;
      }
    }).map(new Func1<String, String>() {
      @Override public String call(String o) {
        return null;
      }
    }).subscribeOn(Schedulers.immediate()).startWith(Observable.just(""));

    Observable.create(new Observable.OnSubscribe<String>() {
      @Override public void call(Subscriber<? super String> subscriber) {
        try {
          if (!subscriber.isUnsubscribed()) {
            for (int i = 1; i < 5; i++) {
              subscriber.onNext(i + "");
            }
            subscriber.onCompleted();
          }
        } catch (Exception e) {
          subscriber.onError(e);
        }
      }
    }).map(new Func1<String, Location>() {
      @Override public Location call(String s) {
        return null;
      }
    }).filter(new Func1<Location, Boolean>() {
      @Override public Boolean call(Location s) {
        return true;
      }
    }).map(new Func1<Location, String>() {
      @Override public String call(Location location) {
        return null;
      }
    }).mergeWith(Observable.just(""));

    return Observable.create(new Observable.OnSubscribe<Location>() {

      @Override public void call(final Subscriber<? super Location> subscriber) {

        final LocationListener locationListener = new LocationListener() {

          public void onLocationChanged(final Location location) {

            if (!subscriber.isUnsubscribed()) {
              subscriber.onNext(location);
              subscriber.onCompleted();
            }

            Looper.myLooper().quit();
          }

          public void onStatusChanged(String provider, int status, Bundle extras) {
          }

          public void onProviderEnabled(String provider) {
          }

          public void onProviderDisabled(String provider) {
          }
        };

        final Criteria locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        locationCriteria.setPowerRequirement(Criteria.POWER_LOW);
        final String locationProvider = mLocationManager.getBestProvider(locationCriteria, true);

        Looper.prepare();

        mLocationManager.requestSingleUpdate(locationProvider, locationListener, Looper.myLooper());

        Looper.loop();
      }
    });
  }
}