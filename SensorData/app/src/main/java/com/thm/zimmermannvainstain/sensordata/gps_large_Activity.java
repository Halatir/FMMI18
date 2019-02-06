package com.thm.zimmermannvainstain.sensordata;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Locale;

public class gps_large_Activity extends AppCompatActivity {

    final Handler timerHandler = new Handler();
    Runnable updater;
    private boolean kill = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_large_);

        Update();
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
}
