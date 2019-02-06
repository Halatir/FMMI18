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

    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagnetometer;

    private HandlerThread mSensorThread;
    private Handler mSensorHandler;
    public  SensorManager mSensorManager;

    public boolean ready = false;
    Context context;

    private float[] Acc = {0.0f,0.0f,0.0f};
    private float[] Gyr = {0.0f,0.0f,0.0f};
    private float[] Mag = new float[3];
    private boolean RotMat;


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
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
        mSensorThread.start();
        mSensorHandler = new Handler(mSensorThread.getLooper()) ;//Blocks until looper is prepared, which is fairly quick
        mSensorManager.registerListener(this, mAccelerometer, 100, mSensorHandler);
        mSensorManager.registerListener(this, mGyroscope,100,mSensorHandler);
        mSensorManager.registerListener(this, mMagnetometer,100,mSensorHandler);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] mAccelerationValues = new float [3];
        float[] mGravityValues = new float [3];
        float[] mRotationMatrix=new float[9];


        if(event.sensor.getType()==1){//Accelerometer
            mAccelerationValues = event.values;
            setAcc(mAccelerationValues);
        }
        else if(event.sensor.getType()==4){//Gyroscope
            setGyr(event.values);
        }
        else if(event.sensor.getType()== Sensor.TYPE_MAGNETIC_FIELD){
            mGravityValues = event.values;
            setMag(mGravityValues);
        }
        RotMat = SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerationValues, mGravityValues);
        setRotMat(RotMat);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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

    public float[] getGyr(){
        synchronized (Gyr){
            return Gyr;
        }
    }

    public void setGyr(float[] f){
        synchronized (Gyr){
            Gyr = f;
        }
    }

    public void setMag(float[] f){
        synchronized (Mag){
            Mag = f;
        }
    }

    public float[] getMag(){
        synchronized (Mag){
            return Mag;
        }
    }

    public void setRotMat(boolean bool){
        RotMat = bool;
    }

    public boolean getRotMat(){
        return RotMat;
    }
}
