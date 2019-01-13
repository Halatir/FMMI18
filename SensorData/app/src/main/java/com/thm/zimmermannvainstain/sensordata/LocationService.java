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

import static com.thm.zimmermannvainstain.sensordata.SensorService.singleton;

public class LocationService extends Service implements LocationListener {

    public static LocationService singleton;

    private LocationManager locationManager;
    public boolean ready = false;

    Context context;

    public int onStartCommand(Intent intent,int flags, int startId) {
        if (singleton == null) {
            singleton = this;
        } else {
            stopSelf();
        }
        context= getApplicationContext();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        HandlerThread mLocationThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
        mLocationThread.start();
        Looper looper = mLocationThread.getLooper();

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            //TODO: Inform User about not granded Permission, and Ask Again
        }else{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, this, looper);
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

    @Override
    public void onLocationChanged(Location location) {

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
