package ca.cmetcalfe.locationshare;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private final static int PERMISSION_REQUEST = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button gpsButton;
    private TextView progressTitle;
    private ProgressBar progressBar;
    private TextView detailsText;

    private Button shareButton;
    private Button copyButton;
    private Button viewButton;

    private LocationManager locManager;
    private Location lastLocation;

    private final LocationListener locListener = new LocationListener() {
        public void onLocationChanged(Location loc) {
            updateLocation(loc);
        }

        public void onProviderEnabled(String provider) {
            updateLocation();
        }

        public void onProviderDisabled(String provider) {
            updateLocation();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    // ----------------------------------------------------
    // Android Lifecycle
    // ----------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        setTitle(R.string.app_name);

        // Display area
        gpsButton = findViewById(R.id.gpsButton);
        progressTitle = findViewById(R.id.progressTitle);
        progressBar = findViewById(R.id.progressBar);
        detailsText = findViewById(R.id.detailsText);

        // Button area
        shareButton = findViewById(R.id.shareButton);
        copyButton = findViewById(R.id.copyButton);
        viewButton = findViewById(R.id.viewButton);

        // Set default values for preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            locManager.removeUpdates(locListener);
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to stop listening for location updates", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startRequestingLocation();
        updateLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST &&
                grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRequestingLocation();
        } else {
            Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ----------------------------------------------------
    // UI
    // ----------------------------------------------------
    private void updateLocation() {
        // Trigger a UI update without changing the location
        updateLocation(lastLocation);
    }

    private void updateLocation(Location location) {
        boolean locationEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean waitingForLocation = locationEnabled && !validLocation(location);
        boolean haveLocation = locationEnabled && !waitingForLocation;

        // Update display area
        gpsButton.setVisibility(locationEnabled ? View.GONE : View.VISIBLE);
        progressTitle.setVisibility(waitingForLocation ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(waitingForLocation ? View.VISIBLE : View.GONE);
        detailsText.setVisibility(haveLocation ? View.VISIBLE : View.GONE);

        // Update buttons
        shareButton.setEnabled(haveLocation);
        copyButton.setEnabled(haveLocation);
        viewButton.setEnabled(haveLocation);

        if (haveLocation) {
            String newline = System.getProperty("line.separator");
            detailsText.setText(String.format("%s: %s%s%s: %s (%s)%s%s: %s (%s)",
                    getString(R.string.accuracy), getAccuracy(location), newline,
                    getString(R.string.latitude), getLatitude(location), getDMSLatitude(location), newline,
                    getString(R.string.longitude), getLongitude(location), getDMSLongitude(location)));

            lastLocation = location;
        }
    }

    // ----------------------------------------------------
    // DialogInterface Listeners
    // ----------------------------------------------------
    private class onClickShareListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int i) {
            shareLocationText(formatLocation(lastLocation, getResources().getStringArray(R.array.link_options)[i]));
        }
    }

    private class onClickCopyListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int i) {
            copyLocationText(formatLocation(lastLocation, getResources().getStringArray(R.array.link_options)[i]));
        }
    }

    //-----------------------------------------------------
    // Menu related methods
    //-----------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intentSettingsActivity = new Intent(this, SettingsActivity.class);
            this.startActivity(intentSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ----------------------------------------------------
    // Actions
    // ----------------------------------------------------
    public void shareLocation(View view) {
        if (!validLocation(lastLocation)) {
            return;
        }

        String linkChoice = PreferenceManager.getDefaultSharedPreferences(this).getString("prefLinkType", "");

        if (linkChoice.equals(getResources().getString(R.string.always_ask))) {
            new Builder(this).setTitle(R.string.choose_link)
                    .setCancelable(true)
                    .setItems(R.array.link_names, new onClickShareListener())
                    .create()
                    .show();
        } else {
            shareLocationText(formatLocation(lastLocation, linkChoice));
        }
    }

    public void copyLocation(View view) {
        if (!validLocation(lastLocation)) {
            return;
        }

        String linkChoice = PreferenceManager.getDefaultSharedPreferences(this).getString("prefLinkType", "");

        if (linkChoice.equals(getResources().getString(R.string.always_ask))) {
            new Builder(this).setTitle(R.string.choose_link)
                    .setCancelable(true)
                    .setItems(R.array.link_names, new onClickCopyListener())
                    .create()
                    .show();
        } else {
            copyLocationText(formatLocation(lastLocation, linkChoice));
        }
    }

    public void viewLocation(View view) {
        if (!validLocation(lastLocation)) {
            return;
        }

        String uri = formatLocation(lastLocation, "geo:{0},{1}?q={0},{1}");

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(Intent.createChooser(intent, getString(R.string.view_location_via)));
    }

    public void openLocationSettings(View view) {
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    // ----------------------------------------------------
    // Helper functions
    // ----------------------------------------------------
    public void shareLocationText(String string) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, string);
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, getString(R.string.share_location_via)));
    }

    public void copyLocationText(String string) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(getString(R.string.app_name), string);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), R.string.copied, Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Failed to get the clipboard service");
            Toast.makeText(getApplicationContext(), R.string.clipboard_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void startRequestingLocation() {
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);
            return;
        }

        // GPS enabled and have permission - start requesting location updates
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean validLocation(Location location) {
        if (location == null) {
            return false;
        }

        // Location must be from less than 30 seconds ago to be considered valid
        if (Build.VERSION.SDK_INT < 17) {
            return System.currentTimeMillis() - location.getTime() < 30e3;
        } else {
            return SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos() < 30e9;
        }
    }

    private String getAccuracy(Location location) {
        float accuracy = location.getAccuracy();
        if (accuracy < 0.01) {
            return "?";
        } else if (accuracy > 99) {
            return "99+";
        } else {
            return String.format(Locale.US, "%2.0fm", accuracy);
        }
    }

    private String getLatitude(Location location) {
        return String.format(Locale.US, "%2.5f", location.getLatitude());
    }

    private String getDMSLatitude(Location location) {
        double val = location.getLatitude();
        return String.format(Locale.US, "%.0f° %2.0f′ %2.3f″ %s",
                Math.floor(Math.abs(val)),
                Math.floor(Math.abs(val * 60) % 60),
                (Math.abs(val) * 3600) % 60,
                val > 0 ? "N" : "S"
        );
    }

    private String getDMSLongitude(Location location) {
        double val = location.getLongitude();
        return String.format(Locale.US, "%.0f° %2.0f′ %2.3f″ %s",
                Math.floor(Math.abs(val)),
                Math.floor(Math.abs(val * 60) % 60),
                (Math.abs(val) * 3600) % 60,
                val > 0 ? "E" : "W"
        );
    }

    private String getLongitude(Location location) {
        return String.format(Locale.US, "%3.5f", location.getLongitude());
    }

    private String formatLocation(Location location, String format) {
        return MessageFormat.format(format,
                getLatitude(location), getLongitude(location));
    }
}
