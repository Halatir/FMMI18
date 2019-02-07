package com.thm.zimmermannvainstain.sensordata;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;

import static com.thm.zimmermannvainstain.sensordata.SensorService.singleton;

public class LocationService extends Service implements LocationListener {

    public static LocationService singleton;

    private LocationManager locationManager;
    public boolean ready = false;

    Context context;

    private double[] GPS = {0.0d,0.0d,50.0d};
    public float speed =0.0f;
    public float maxSpeed =0.0f;
    public float avgSpeed =0.0f;
    public float distance =0.0f;
    public boolean logging = false;
    private double[] lastDistGPS = {0.0d,0.0d,0.0d};

    private static int bufferAmount = 20;
    private ArrayList gpsLog = new ArrayList(bufferAmount);
    private ArrayList speeds = new ArrayList();

    public int onStartCommand(Intent intent,int flags, int startId) {
        if (singleton == null) {
            singleton = this;
        } else {
            stopSelf();
        }
        context= getApplicationContext();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        HandlerThread mLocationThread = new HandlerThread("Location thread", Thread.MAX_PRIORITY);
        mLocationThread.start();
        Looper looper = mLocationThread.getLooper();

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            //TODO: Inform User about not granded Permission, and Ask Again
        }else{
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, this, looper);
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0, this, looper);
        }
        ready = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        if(locationManager!=null)
        locationManager.removeUpdates(this);
        Log.d("SensorData","Service has been destroyed");
    }

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //TODO Check if there is no way computing the GPS at same Time as reading
    public void setGPS(double[] f){
        synchronized(GPS){
            GPS=f;
        }
    }
    public double[] getGPS(){
        synchronized(GPS){
            return GPS;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double[] d = new double [3];
        d[0] = location.getLatitude();
        d[1] = location.getLongitude();
        d[2] = location.getAccuracy();

        setGPS(d);

        if(d[2]<30 && lastDistGPS[0]!=0.0d){
            float[] dist = new float[1];
            Location.distanceBetween(
                    lastDistGPS[0],lastDistGPS[1],
                    d[0], d[1], dist);
            lastDistGPS = d;
            distance = distance + dist[0];
        }else if(d[2]<30){
            lastDistGPS = d;
        }
        if(logging){
            String log = Long.toString(location.getTime()) +"," + Double.toString(location.getLatitude()) + "," +
                    Double.toString(location.getLongitude()) + "," + Double.toString(location.getAccuracy()) + "," + location.getProvider() + "\n\r" ;
            gpsLog.add(log);
            if(gpsLog.size()==bufferAmount){
                String data = gpsLog.toString();
                LogService.WriteDataToFile(this, data, "location");
                gpsLog.clear();
            }
        }
        if(location.hasSpeed()){
            speed = location.getSpeed()*3.6f;
            speeds.add(speed);
            float f =0;
            for(int i =0;i<speeds.size();i++){
                f+=(float) speeds.get(i);
            }
            avgSpeed = f/speeds.size();
            if(speed>maxSpeed)
                maxSpeed=speed;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
