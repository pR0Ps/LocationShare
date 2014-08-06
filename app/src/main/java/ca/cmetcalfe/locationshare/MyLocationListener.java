package ca.cmetcalfe.locationshare;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class MyLocationListener implements LocationListener {

    MainActivity main;

    public MyLocationListener(MainActivity main){
        this.main = main;
    }

    @Override
    public void onLocationChanged(Location location) {
        main.updateLocation(location);
    }

    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}
