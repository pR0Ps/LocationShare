package ca.cmetcalfe.locationshare;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    LocationManager mlocManager;
    LocationListener mlocListener;
    Location location;
    long lastFix;

    private Handler handler = new Handler();
    private Runnable updateUITask = new Runnable() {
        public void run() {
            updateDisplay();
            handler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener(this);
        location = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null){
            lastFix = location.getTime();
        }

        handler.post(updateUITask);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        if (this.location == null){
            return;
        }

        String link = formatLocation("http://maps.google.com/?q={0},{1}", this.location);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, link);
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_via)));
    }

    public void viewLocation (View view){
        if (this.location == null){
            return;
        }

        String uri = formatLocation("geo:{0},{1}?q={0},{1}", this.location);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.view_via)));
    }

    public void updateLocation(Location location){
        //TODO: Check if new location is 'better'
        this.location = location;
        this.lastFix = location.getTime();
        updateDisplay();
    }

    public void updateDisplay(){

        if (this.location != null){
            long timeElapsed = System.currentTimeMillis() - this.lastFix;
            ((TextView)findViewById(R.id.timeText)).setText(stringifyTime(timeElapsed/1000));

            ((TextView)findViewById(R.id.latitudeText)).setText("" + this.location.getLatitude());
            ((TextView)findViewById(R.id.longitudeText)).setText("" + this.location.getLongitude());
            ((TextView)findViewById(R.id.accuracyText)).setText(new DecimalFormat("###.00").format(this.location.getAccuracy()) + "m");
        }
        else{
            ((TextView)findViewById(R.id.latitudeText)).setText("N/A");
            ((TextView)findViewById(R.id.longitudeText)).setText("N/A");
            ((TextView)findViewById(R.id.accuracyText)).setText("N/A");
            ((TextView)findViewById(R.id.timeText)).setText("N/A");
        }

    }

    private String formatLocation(String s, Location l){
        //Hack to get around MessageFormat precision weirdness
        return MessageFormat.format(s, ""+l.getLatitude(), ""+l.getLongitude(), ""+l.hasAccuracy());
    }

    public static String stringifyTime(long seconds) {

        //int days = (int)(seconds/86400);
        //seconds -= days * 86400;
        int hours = (int)(seconds/3600);
        seconds -= hours * 3600;
        int minutes = (int)(seconds/60);
        seconds -= minutes * 60;

        //Fix negative values (rounding)
        seconds = Math.max(0, seconds);

        return MessageFormat.format("{0,number,00}:{1,number,00}:{2,number,00}", hours, minutes, seconds);
    }
}
