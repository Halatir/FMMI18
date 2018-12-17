package com.thm.zimmermannvainstain.sensordata;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

public class SensorService extends Service implements SensorEventListener {

    public static SensorService singleton;

    private HandlerThread mSensorThread;
    private Handler mSensorHandler;
    private  SensorManager mSensorManager;
    private  Sensor mAccelerometer;
    public boolean ready = false;
    Context context;

    private float[] Acc = {0.0f,0.0f,0.0f};

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        if(singleton==null){
            singleton = this;
        }else{
            stopSelf();
        }

        context= getApplicationContext();
        //sharedPreferenceManager = new SharedPreferenceManagerToReplace(context);

        mSensorManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
        mSensorThread.start();
        mSensorHandler = new Handler(mSensorThread.getLooper()) ;//Blocks until looper is prepared, which is fairly quick
        mSensorManager.registerListener(this, mAccelerometer, 100, mSensorHandler);
        ready=true;
        return super.onStartCommand(intent, flags, startId);

    }

    public SensorService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy(){
        if(mSensorManager!=null)
        mSensorManager.unregisterListener(this);
        Log.d("SensorData","Service has been destroyed");
    }


    public void setAcc(float[] f){
        synchronized(Acc){
            Acc=f;
        }
    }
    public float[] getAcc(){
        synchronized(Acc){
            return Acc;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==1)
            setAcc(event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
