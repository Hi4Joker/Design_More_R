package com.joker.supportdesign.mvp.model;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import rx.Observable;
import rx.Subscriber;

public class LocationService {

  private static final String TAG = LocationService.class.getSimpleName();

  private final LocationManager locationManager;
  private LocationListener locationListener = null;

  public LocationService(LocationManager locationManager) {
    this.locationManager = locationManager;
  }

  public Observable<Location> fetchLocation() {

    return Observable.create(new Observable.OnSubscribe<Location>() {

      @Override public void call(final Subscriber<? super Location> subscriber) {

           /* MyLocation myLocation = new MyLocation();
              myLocation.getLocation(locationManager, new MyLocation.LocationResult() {
                @Override public void gotLocation(Location location) {

                  subscriber.onNext(location);
                  subscriber.onCompleted();
                }
              });*/

        if (subscriber.isUnsubscribed()) {
          Log.e(TAG, "android.os.Process.myPid():" + android.os.Process.myPid());

          Log.e(TAG, "获取位置信息ing");

          locationListener = new LocationListener() {
            public void onLocationChanged(final Location location) {

              Log.e(TAG,
                  "longitude:" + location.getLongitude() + "latitude:" + location.getLatitude());
              Log.e(TAG, locationListener.toString());

              subscriber.onNext(location);
              subscriber.onCompleted();

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
          final String locationProvider = locationManager.getBestProvider(locationCriteria, true);

          Looper.prepare();

          locationManager.requestSingleUpdate(locationProvider, locationListener,
              Looper.myLooper());

          Looper.loop();
        }
      }
    });
  }

  public boolean stopFetchLocation() {
    try {
      locationManager.removeUpdates(locationListener);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}