package com.thm.zimmermannvainstain.sensordata;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Locale;

public class gps_large_Activity extends AppCompatActivity {

    final Handler timerHandler = new Handler();
    Runnable updater;
    private boolean kill = false;
    private DrawerLayout mDrawerLayout;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_large_);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        activity = this;

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
                        Intent intent = null;
                        switch(menuItem.getItemId()){
                            case R.id.dashboard:
                                finish();
                                break;
                            case R.id.speed:
                                intent = new Intent(activity, gps_large_Activity.class);
                                startActivity(intent);
                                break;
                            case R.id.acceleration:
                                intent = new Intent(activity, accelo_speed_Activity.class);
                                startActivity(intent);
                                break;
                        }
                        return true;
                    }
                });
    }

    void Update() {

        updater = new Runnable() {
            private TextView lat=(TextView) findViewById(R.id.lat);
            private TextView longi=(TextView) findViewById(R.id.longi);

            private TextView max = (TextView) findViewById(R.id.max);
            private TextView avg = (TextView) findViewById(R.id.avg);
            private TextView current = (TextView) findViewById(R.id.aktuell);

            private TextView distance = (TextView) findViewById(R.id.distance);

            @SuppressLint("SetTextI18n")//remove to find all texts unable to translate
            @Override
            public void run() {

                if(LocationService.singleton!=null && LocationService.singleton.ready){
                    double[] d =LocationService.singleton.getGPS();
                    String s="";
                    s= String.format(Locale.getDefault(),"%31.12f",d[0]);
                    lat.setText("latitude: " + s);
                    s= String.format(Locale.getDefault(),"%31.12f",d[1]);
                    longi.setText("longitude: "+ s);
                    max.setText(Float.toString(LocationService.singleton.maxSpeed)+" km/h");
                    avg.setText(Float.toString(LocationService.singleton.avgSpeed)+" km/h");
                    current.setText(Float.toString(LocationService.singleton.speed)+" km/h");
                    distance.setText("zurückgelegte Strecke: " +Float.toString(LocationService.singleton.distance) +" m");
                }else{
                    lat.setText("LocationService not Active");
                }
                if(!kill)
                    timerHandler.postDelayed(updater,64);//15 frames right now
            }
        };
        timerHandler.post(updater);
    }

    @Override
    public void onDestroy(){
        kill=true;
        timerHandler.removeCallbacks(updater);
        LocationService.singleton.logging=false;
        Log.d("GPSActivity","Activity has been destroyed");
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

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
