package com.pr0ps.locationshare;

import com.pr0ps.locationshare.MyLocationListener;
import com.pr0ps.locationshare.R;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class LocationShare extends Activity {
	
	LocationManager mlocManager;
	LocationListener mlocListener;
	Location location;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_share);
		
		mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    mlocListener = new MyLocationListener(this);
	    location = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location_share, menu);
		return true;
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		mlocManager.removeUpdates(mlocListener);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
	    mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
	}
	
	public void shareLocation (View view){
		
		String link = "http://maps.google.com/?q=" +
						this.location.getLatitude() + "," + 
						this.location.getLongitude();
		
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, link);
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
	}
	
	public void updateLocation(Location location){
		this.location = location;
		updateDisplay();
	}
	
	public void updateDisplay(){
		TextView tv = (TextView) findViewById(R.id.textView2);
		tv.setText("Accuracy: " + this.location.getAccuracy());
	}

}
