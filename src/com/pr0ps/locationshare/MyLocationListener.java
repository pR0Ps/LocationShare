package com.pr0ps.locationshare;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MyLocationListener implements LocationListener {
	
	LocationShare main;
	
	public MyLocationListener(LocationShare main){
		this.main = main;
	}

	@Override
	public void onLocationChanged(Location location) {
		main.updateLocation(location);
		return;
		/*
		float meters = location.getAccuracy();
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		
		Log.e(null, "Long: " + lon);
		Log.e(null, "Lat: " + lat);
		Log.e(null, "Accuracy: " + meters);
		*/
	}

	@Override
	public void onProviderDisabled(String provider) {}
	@Override
	public void onProviderEnabled(String provider) {}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}
