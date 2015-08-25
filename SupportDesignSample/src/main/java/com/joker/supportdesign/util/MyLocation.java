package com.joker.supportdesign.util;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Joker on 2015/7/7.
 */
public class MyLocation {

  Timer timer1;
  LocationManager locationManager;
  LocationResult locationResult;
  boolean gps_enabled = false;
  boolean network_enabled = false;

  public boolean getLocation(LocationManager lm, LocationResult result) {

    this.locationManager = lm;
    //I use LocationResult callback class to pass location value from MyLocation to user code.
    locationResult = result;

    //exceptions will be thrown if provider is not permitted.
    try {
      gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    } catch (Exception ex) {
    }
    try {
      network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    } catch (Exception ex) {
    }

    //don't start listeners if no provider is enabled
    if (!gps_enabled && !network_enabled) return false;

    if (gps_enabled) {
      lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
    }
    if (network_enabled) {
      lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
    }
    timer1 = new Timer();
    timer1.schedule(new GetLastLocation(), 20000);
    return true;
  }

  LocationListener locationListenerGps = new LocationListener() {
    public void onLocationChanged(Location location) {
      timer1.cancel();
      locationResult.gotLocation(location);
      locationManager.removeUpdates(this);
      locationManager.removeUpdates(locationListenerNetwork);
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
  };

  LocationListener locationListenerNetwork = new LocationListener() {
    public void onLocationChanged(Location location) {
      timer1.cancel();
      locationResult.gotLocation(location);
      locationManager.removeUpdates(this);
      locationManager.removeUpdates(locationListenerGps);
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
  };

  class GetLastLocation extends TimerTask {
    @Override public void run() {
      locationManager.removeUpdates(locationListenerGps);
      locationManager.removeUpdates(locationListenerNetwork);

      Location net_loc = null, gps_loc = null;
      if (gps_enabled) gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      if (network_enabled) {
        net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
      }

      //if there are both values use the latest one
      if (gps_loc != null && net_loc != null) {
        if (gps_loc.getTime() > net_loc.getTime()) {
          locationResult.gotLocation(gps_loc);
        } else {
          locationResult.gotLocation(net_loc);
        }
        return;
      }

      if (gps_loc != null) {
        locationResult.gotLocation(gps_loc);
        return;
      }
      if (net_loc != null) {
        locationResult.gotLocation(net_loc);
        return;
      }
      locationResult.gotLocation(null);
    }
  }

  public static abstract class LocationResult {
    public abstract void gotLocation(Location location);
  }
}


