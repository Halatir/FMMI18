package com.thm.zimmermannvainstain.sensordata;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class accelo_speed_Activity extends AppCompatActivity {

    final Handler timerHandler = new Handler();
    private LineGraphSeries<DataPoint> mSeries;
    private LineGraphSeries<DataPoint> mSeriesY;
    private LineGraphSeries<DataPoint> mSeriesZ;
    private Activity activity;

    private float maxAcceleration = 0.0f;
    private String maxTimestamp = "00.00.0000";

    private DrawerLayout mDrawerLayout;
    private boolean voice =false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelo_speed_);
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
        Update();
        createGraph();

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
                                //intent = new Intent(activity, accelo_speed_Activity.class);
                                break;
                            case R.id.gyro:
                                intent = new Intent(activity, gyroscope_Activity.class);
                                break;
                            case R.id.magneto:
                                intent = new Intent(activity, magnetoActivity.class);
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
    private boolean kill=false;
    void Update() {

        updater = new Runnable() {
            private int counter =0;
            private TextView accXg = (TextView) findViewById(R.id.mitgX);
            private TextView accYg = (TextView) findViewById(R.id.mitgY);
            private TextView accZg = (TextView) findViewById(R.id.mitgZ);

            private TextView accX = (TextView) findViewById(R.id.ohnegX);
            private TextView accY = (TextView) findViewById(R.id.ohnegY);
            private TextView accZ = (TextView) findViewById(R.id.ohnegZ);
            private TextView max = (TextView) findViewById(R.id.hochst);

            private ImageView traffic = (ImageView) findViewById(R.id.accTraffic);

            @SuppressLint("SetTextI18n")//remove to find all texts unable to translate
            @Override
            public void run() {

                if(SensorService.singleton!=null&& SensorService.singleton.ready){
                    float[] f = SensorService.singleton.getAcc_o_g();
                    accX.setText(" " + Float.toString(f[0]));
                    accY.setText(" " + Float.toString(f[1]));
                    accZ.setText(" " + Float.toString(f[2]));

                    f = SensorService.singleton.getAcc();
                    accXg.setText(" " +Float.toString(f[0]));
                    accYg.setText(" " +Float.toString(f[1]));
                    accZg.setText(" " +Float.toString(f[2]));

                    mSeries.appendData(new DataPoint(counter,f[0]),true,1000);
                    mSeriesY.appendData(new DataPoint(counter,f[1]),true,1000);
                    mSeriesZ.appendData(new DataPoint(counter,f[2]),true,1000);
                    counter++;
                    if(maxAcceleration<f[0]){maxAcceleration = f[0]; maxTimestamp = getDate(SensorService.singleton.AccTimeStamp);}
                    if(maxAcceleration<f[1]){maxAcceleration = f[1]; maxTimestamp = getDate(SensorService.singleton.AccTimeStamp);}
                    if(maxAcceleration<f[2]){maxAcceleration = f[2]; maxTimestamp = getDate(SensorService.singleton.AccTimeStamp);}
                    max.setText("Höchstbeschleunigung: "+ maxAcceleration + " am "+ maxTimestamp);

                    switch((int)f[3]){
                        case 1:
                            traffic.setImageResource(R.drawable.dot_red);
                            break;
                        case 2:
                            traffic.setImageResource(R.drawable.dot_orange);
                            break;
                        case 3:
                            traffic.setImageResource(R.drawable.dot_grun);
                            break;
                    }


                }else{

                }
                if(!kill)
                    timerHandler.postDelayed(updater,100);//10 Frames a second
            }
        };
        timerHandler.post(updater);
    }


    private void createGraph(){
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(-15);
        graph.getViewport().setMaxX(15);
        graph.getViewport().setMinY(-5);
        graph.getViewport().setMaxY(11);
        graph.getViewport().setYAxisBoundsStatus(Viewport.AxisBoundsStatus.FIX);
        mSeries = new LineGraphSeries<>();
        mSeriesY = new LineGraphSeries<>();
        mSeriesZ = new LineGraphSeries<>();
        graph.addSeries(mSeries);
        graph.addSeries(mSeriesY);
        graph.addSeries(mSeriesZ);


        //graph.getGridLabelRenderer().setNumVerticalLabels(5);

        mSeries.setTitle("X-Achse");
        mSeries.setColor(Color.GREEN);
        mSeries.setDrawDataPoints(true);
        mSeries.setDataPointsRadius(10);

        mSeriesY.setTitle("Y-Achse");
        mSeriesY.setColor(Color.RED);
        mSeriesY.setDrawDataPoints(true);
        mSeriesY.setDataPointsRadius(10);

        mSeriesZ.setTitle("Z-Achse");
        mSeriesZ.setColor(Color.BLUE);
        mSeriesZ.setDrawDataPoints(true);
        mSeriesZ.setDataPointsRadius(10);
    }
    private int position = 15;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        TextView tv = new TextView(this);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(0, WRAP_CONTENT);

        if(requestCode == 123 && resultCode == RESULT_OK){
            if(position==15){
                View last = findViewById(R.id.hochst);
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
