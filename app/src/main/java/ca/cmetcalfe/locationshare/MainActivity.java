package ca.cmetcalfe.locationshare;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import java.text.MessageFormat;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button gpsButton;
    private TextView progressTitle;
    private ProgressBar progressBar;
    private TextView detailsText;

    private LocationManager locManager;
    private Location lastLocation;

    private LocationListener locListener = new LocationListener(){
        public void onLocationChanged(Location loc){
            updateLocation(loc);
        }
        public void onProviderEnabled(String provider) {
            updateLocation();
        }
        public void onProviderDisabled(String provider) {
            updateLocation();
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    // ----------------------------------------------------
    // Android Lifecycle
    // ----------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsButton = (Button)findViewById(R.id.gpsButton);
        progressTitle = (TextView)findViewById(R.id.progressTitle);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        detailsText = (TextView)findViewById(R.id.detailsText);
        locManager = (LocationManager)getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onStop(){
        super.onStop();
        locManager.removeUpdates(locListener);
    }

    @Override
    protected void onResume(){
        super.onResume();
        updateLocation();
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
        }
    }

    // ----------------------------------------------------
    // UI
    // ----------------------------------------------------
    private void updateLocation(){
        // Trigger a UI update without changing the location
        updateLocation(lastLocation);
    }

    private void updateLocation(Location location){
        boolean locationEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean waitingForLocation = locationEnabled && !validLocation(location);
        boolean haveLocation = locationEnabled && !waitingForLocation;

        gpsButton.setVisibility(locationEnabled ? View.GONE : View.VISIBLE);
        progressTitle.setVisibility(waitingForLocation ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(waitingForLocation ? View.VISIBLE : View.GONE);
        detailsText.setVisibility(haveLocation ? View.VISIBLE : View.GONE);

        if (haveLocation){
            String newline = System.getProperty("line.separator");
            detailsText.setText(
                    getString(R.string.accuracy) + ": " + getAccuracy(location) + newline +
                            getString(R.string.latitude) + ": " + getLongitude(location) + newline +
                            getString(R.string.longitude) + ": " + getLatitude(location));

            lastLocation = location;
        }
    }

    // ----------------------------------------------------
    // Actions
    // ----------------------------------------------------
    public void shareLocation (View view){
        if (!validLocation(lastLocation)){
            return;
        }

        String link = getLocationLink(lastLocation);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, link);
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, getString(R.string.share_location_via)));
    }

    public void copyLocation(View view){
        if (!validLocation(lastLocation)){
            return;
        }

        String text = getLocationLink(lastLocation);

        Object clipService = getSystemService(Context.CLIPBOARD_SERVICE);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
            @SuppressWarnings("deprecation")
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager)clipService;
            clipboard.setText(text);
        } else {
            ClipboardManager clipboard = (ClipboardManager)clipService;
            ClipData clip = ClipData.newPlainText(getString(R.string.app_name), text);
            clipboard.setPrimaryClip(clip);
        }

        Toast.makeText(getApplicationContext(), R.string.copied, Toast.LENGTH_SHORT).show();
    }

    public void viewLocation (View view){
        if (!validLocation(lastLocation)){
            return;
        }

        String uri = getLocationURI(lastLocation);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(Intent.createChooser(intent, getString(R.string.view_location_via)));
    }

    public void enableGps(View view){
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    // ----------------------------------------------------
    // Helper functions
    // ----------------------------------------------------
    private boolean validLocation(Location location){
        if (location == null){
            return false;
        }

        // Location must be from less than 30 seconds ago to be considered valid
        if (Build.VERSION.SDK_INT < 17){
            return System.currentTimeMillis() - location.getTime() < 30e3;
        }
        else{
            return SystemClock.elapsedRealtime() - location.getElapsedRealtimeNanos() < 30e9;
        }

    }

    private String getLocationLink(Location location){
        return MessageFormat.format("https://maps.google.com/?q={0},{1}",
                getLatitude(location), getLongitude(location));
    }

    private String getLocationURI(Location location){
        return MessageFormat.format("geo:{0},{1}?q={0},{1}",
                getLatitude(location), getLongitude(location));
    }

    private String getAccuracy(Location location){
        float accuracy = location.getAccuracy();
        if (accuracy < 0.01) {
            return "?";
        } else if (accuracy > 99) {
            return "99+";
        } else {
            return String.format(Locale.US, "%2.0fm", accuracy);
        }
    }

    private String getLatitude(Location location){
        return String.format(Locale.US, "%2.6f", location.getLatitude());
    }

    private String getLongitude(Location location){
        return String.format(Locale.US, "%3.6f", location.getLongitude());
    }
}