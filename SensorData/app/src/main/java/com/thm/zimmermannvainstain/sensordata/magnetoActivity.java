package com.thm.zimmermannvainstain.sensordata;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class magnetoActivity extends AppCompatActivity {

    private Activity activity;
    private DrawerLayout mDrawerLayout;
    final Handler timerHandler = new Handler();
    private ImageView imageViewCompass;
    private boolean voice =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magneto);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        activity = this;

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SensorService.singleton.logging) {
                    SensorService.singleton.stopLogging();
                    LocationService.singleton.stopLogging();
                    fab.setImageResource(android.R.drawable.ic_menu_edit);
                } else {
                    SensorService.singleton.logging = true;
                    LocationService.singleton.logging = true;
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });
        if(SensorService.singleton.ready && SensorService.singleton.logging){
            fab.setImageResource(android.R.drawable.ic_media_pause);
        }else{
            fab.setImageResource(android.R.drawable.ic_menu_edit);
        }
        imageViewCompass=(ImageView)findViewById(R.id.compass);
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
                                intent = new Intent(activity, DashboardActivity.class);
                                break;
                            case R.id.gps:
                                intent = new Intent(activity, gpsActivity.class);
                                break;
                            case R.id.acceleration:
                                intent = new Intent(activity, accelo_speed_Activity.class);
                                break;
                            case R.id.gyro:
                                intent = new Intent(activity, gyroscope_Activity.class);
                                break;
                            case R.id.magneto:
                                //intent = new Intent(activity, magnetoActivity.class);
                                break;
                            case R.id.pressure:
                                intent = new Intent(activity, pressureActivity.class);
                                break;

                        }
                        if(intent != null) {
                            startActivity(intent);
                        } else {
                            Log.e("intent", "no Intent available");
                        }

                        return true;
                    }
                });
    }
    Runnable updater;
    private boolean kill = false;

    void Update() {

        updater = new Runnable() {


            private ImageView trafficMag = (ImageView) findViewById(R.id.magTraffic);

            private TextView magX = (TextView) findViewById(R.id.magX);
            private TextView magY = (TextView) findViewById(R.id.magY);
            private TextView magZ = (TextView) findViewById(R.id.magZ);

            private boolean once =false;

            @SuppressLint("SetTextI18n")//remove to find all texts unable to translate
            @Override
            public void run() {

                if (SensorService.singleton != null && SensorService.singleton.ready) {

                    RotateAnimation rotateAnimation = SensorService.singleton.getRotation();
                    imageViewCompass.setAnimation(rotateAnimation);

                    float[] magneto = SensorService.singleton.getMag();
                    magX.setText(" " + Float.toString(magneto[0]));
                    magY.setText(" " + Float.toString(magneto[1]));
                    magZ.setText(" " + Float.toString(magneto[2]));
                    makeTrafficLight((int) magneto[3], trafficMag);
                }
                if (!kill)
                    timerHandler.postDelayed(updater, 64);//15 frames right now
            }
        };

        timerHandler.post(updater);
    }

    private void makeTrafficLight(int a, ImageView image) {
        switch (a) {
            case 1:
                image.setImageResource(R.drawable.dot_red);
                break;
            case 2:
                image.setImageResource(R.drawable.dot_orange);
                break;
            case 3:
                image.setImageResource(R.drawable.dot_grun);
                break;
        }
    }
    private int position = 9;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        TextView tv = new TextView(this);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(0, WRAP_CONTENT);

        if(requestCode == 123 && resultCode == RESULT_OK){
            if(position==9){
                View last = findViewById(R.id.compass);
                layoutParams.leftToLeft = last.getId();
                layoutParams.topToBottom = last.getId();
            }else{
                layoutParams.leftToLeft = position-1;
                layoutParams.topToBottom = position-1;
            }
            layoutParams.rightMargin=0;
            layoutParams.topMargin = 8;
            tv.setLayoutParams(layoutParams);
            long millis = System.currentTimeMillis();
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            tv.setText(result.get(0)+" "+getDate(millis));

            String allVoice = "\n\r"+result.get(0)+","+Long.toString(millis) + ",gps";
            LogService.WriteDataToFile(this,allVoice,"VoiceNodes");

            ViewGroup layout = (ViewGroup) findViewById(R.id.root);
            layout.addView(tv,position);

            position++;
        }
    }
    private String getDate(long time) {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String reportDate = df.format(time);
        return reportDate;
    }

    @Override
    public void onPause(){
        if(!voice){
            kill=true;}

        super.onPause();
    }
    @Override
    public void onResume(){
        if (!voice) {
            kill=false;
            Update();}
        voice=false;
        super.onResume();
    }

    @Override
    public void onDestroy(){
        kill=true;
        timerHandler.removeCallbacks(updater);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_voice:
                voice=true;
                Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please Speak");
                startActivityForResult(voiceIntent, 123);
        }
        return super.onOptionsItemSelected(item);
    }

}
