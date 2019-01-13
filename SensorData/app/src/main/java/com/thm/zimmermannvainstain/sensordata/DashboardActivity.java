package com.thm.zimmermannvainstain.sensordata;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Pair;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class DashboardActivity extends AppCompatActivity {
    
    private Activity thisA;
    private Intent Sensorintent;
    private TextView tview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        thisA=this;
        ClickListeners();

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS},1);

        Sensorintent = new Intent(this, SensorService.class);
        startService(Sensorintent);

        updateTime();

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(Sensorintent);
            }
        });
    }

    Runnable updater;
    void updateTime() {
        tview=(TextView) findViewById(R.id.textView);
        final Handler timerHandler = new Handler();

        updater = new Runnable() {
            @Override
            public void run() {
                if(SensorService.singleton!=null&& SensorService.singleton.ready){
                    float[] f = SensorService.singleton.getAcc();
                    tview.setText(Float.toString(f[0]));
                }else{
                    tview.setText("SensorService not Active");
                }
                timerHandler.postDelayed(updater,1000);
            }
        };
        timerHandler.post(updater);
    }

    private void ClickListeners(){
        findViewById(R.id.gps).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(thisA,
                        Pair.create(findViewById(R.id.textView), "testText"),
                        Pair.create(findViewById(R.id.imageView), "robot"));

                Intent intent = new Intent(thisA, gps_large_Activity.class);
                startActivity(intent, options.toBundle());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
