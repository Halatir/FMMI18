package com.thm.zimmermannvainstain.sensordata;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {
    
    private Activity activity;
    private Intent sensorIntent;
    private Intent gpsIntent;
    final Handler timerHandler = new Handler();

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        activity =this;

        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        sensorIntent = new Intent(this, SensorService.class);
        gpsIntent = new Intent(this, LocationService.class);
        startService(sensorIntent);
        startService(gpsIntent);

        ClickListeners();
        Update();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        String msg = menuItem.toString() + " was clicked";
                        Log.i("menuitem", msg);

                        return true;
                    }
                });
    }

    Runnable updater;
    private boolean kill = false;
    void Update() {

        updater = new Runnable() {
            private TextView lat=(TextView) findViewById(R.id.lat);
            private TextView longi=(TextView) findViewById(R.id.longi);
            private TextView accX = (TextView) findViewById(R.id.accX);
            private TextView accY = (TextView) findViewById(R.id.accY);
            private TextView accZ = (TextView) findViewById(R.id.accZ);
            @SuppressLint("SetTextI18n")//remove to find all texts unable to translate
            @Override
            public void run() {

                if(LocationService.singleton!=null && SensorService.singleton.ready){
                double[] d =LocationService.singleton.getGPS();
                String s="";
                s= String.format(Locale.getDefault(),"%31.12f",d[0]);
                lat.setText("latitude: " + s);
                s= String.format(Locale.getDefault(),"%31.12f",d[1]);
                longi.setText("longitude: "+ s);
                }else{
                lat.setText("LocationService not Active");
                }

                if(SensorService.singleton!=null&& SensorService.singleton.ready){
                    float[] f = SensorService.singleton.getAcc();
                    accX.setText("X-Achse: " +Float.toString(f[0]));
                    accY.setText("Y-Achse: " +Float.toString(f[1]));
                    accZ.setText("Z-Achse: " +Float.toString(f[2]));

                }else{
                    accX.setText("SensorService not Active");
                }
                if(!kill)
                    timerHandler.postDelayed(updater,64);//15 frames right now
            }
        };
        timerHandler.post(updater);
    }

    private void ClickListeners(){
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SensorService.singleton.logging){
                    SensorService.singleton.logging=false;
                    LocationService.singleton.logging=false;
                }else{
                    SensorService.singleton.logging=true;
                    LocationService.singleton.logging=true;
                }
            }
        });
        findViewById(R.id.gps).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                        Pair.create(findViewById(R.id.Headline), "testText"),
                        Pair.create(findViewById(R.id.imageView), "robot"));

                Intent intent = new Intent(activity, gps_large_Activity.class);
                startActivity(intent, options.toBundle());
            }
        });
        findViewById(R.id.accelerationSpeed).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                        Pair.create(findViewById(R.id.headlineAS),"head2"),
                        Pair.create(findViewById(R.id.accX),"accX"),
                Pair.create(findViewById(R.id.accY),"accY"),
                Pair.create(findViewById(R.id.accZ),"accZ"));
                Intent intent = new Intent(activity, accelo_speed_Activity.class);
                startActivity(intent,options.toBundle());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    //If MainActivity is destroyed before App is suspended ( zb. because we are in another Activty real long) our services get destrpyed.//TODO Check
    @Override
    public void onDestroy(){
        kill=true;
        timerHandler.removeCallbacks(updater);
        SensorService.singleton.logging=false;
        stopService(sensorIntent);
        stopService(gpsIntent);
        Log.d("DashboardActivity","Activity has been destroyed");
        super.onDestroy();
    }

    @Override
    public void onPause(){
        kill=true;
        timerHandler.removeCallbacks(updater);
        super.onPause();
    }
    @Override
    public void onResume(){
        kill=false;
        Update();
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    //TODO inform User, that without Permission this App is not going to work.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
