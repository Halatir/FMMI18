package com.thm.zimmermannvainstain.sensordata;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DashboardActivity extends AppCompatActivity {
    
    private Activity thisA;
    private Intent Sensorintent;
    private TextView tview;
    private DrawerLayout mDrawerLayout;

    private ImageView mImageViewCompass;

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
    void updateTime() {
        tview=(TextView) findViewById(R.id.textView);
        final Handler timerHandler = new Handler();

        //float [] mAcceletationValues = new float[3];
        float [] mRotationMatrix = new float[9];
        float mLastDirectionInDegrees = 0f;

        updater = new Runnable() {
            Context context =getApplicationContext();
            SensorManager mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
            @Override
            public void run() {
                if(SensorService.singleton!=null&& SensorService.singleton.ready){
                    float[] f = SensorService.singleton.getAcc();
                    tview.setText(Float.toString(f[0]));
                    float [] mGravityValues = SensorService.singleton.getMag();
                }else{
                    tview.setText("SensorService not Active");
                }
                timerHandler.postDelayed(updater,1000);

                boolean success = SensorService.singleton.getRotMat();
                if(success) {
                    float [] orientationValues = new float[3];
                    //SensorService.singleton.mSensorManager.getOrientation()
                }
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
    /*
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
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
