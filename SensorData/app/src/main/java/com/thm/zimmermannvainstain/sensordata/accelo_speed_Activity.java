package com.thm.zimmermannvainstain.sensordata;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
public class accelo_speed_Activity extends AppCompatActivity {

    final Handler timerHandler = new Handler();
    private LineGraphSeries<DataPoint> mSeries;
    private LineGraphSeries<DataPoint> mSeriesY;
    private LineGraphSeries<DataPoint> mSeriesZ;

    private float maxAcceleration = 0.0f;
    private String maxTimestamp = "00.00.0000";

    private DrawerLayout mDrawerLayout;
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        String msg = menuItem.toString() + " was clicked";
                        Log.i("menuitem", msg);

                        return true;
                    }
                });

    }

    //TODO Refactor OnPause OnResume
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

    private String getDate(long time) {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String reportDate = df.format(time);
        return reportDate;
    }

    private void createGraph(){
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(-15);
        graph.getViewport().setMaxX(15);
        graph.getViewport().setMinY(-20);
        graph.getViewport().setMaxY(20);
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

    @Override
    public void onPause(){
        kill=true;
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
