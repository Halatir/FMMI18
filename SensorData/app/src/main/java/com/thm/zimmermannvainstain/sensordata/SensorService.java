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

import java.util.ArrayList;

public class SensorService extends Service implements SensorEventListener {

    public static SensorService singleton;

    private  SensorManager mSensorManager;
    public boolean ready = false;
    Context context;

    private float[] Acc = {0.0f,0.0f,0.0f};
    //TODO not locked correctly
    public long AccTimeStamp = 0;
    private float[] Gyr = {0.0f,0.0f,0.0f};

    public boolean logging = false;
    private static int bufferAmount = 1000;
    private ArrayList AccLog = new ArrayList(bufferAmount);
    private ArrayList GyrLog = new ArrayList(bufferAmount);

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        if(singleton==null){
            singleton = this;
        }else{
            stopSelf();
        }

        context= getApplicationContext();

        mSensorManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
        Sensor m_Accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor m_Gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor m_Barometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        HandlerThread m_SensorThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
        m_SensorThread.start();
        Handler mSensorHandler = new Handler(m_SensorThread.getLooper());
        mSensorManager.registerListener(this, m_Accelerometer, 8300, mSensorHandler);//120 sample in one second
        mSensorManager.registerListener(this, m_Gyroscope,8300, mSensorHandler);
        mSensorManager.registerListener(this,m_Barometer,8300,mSensorHandler);
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
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String log="";

        //event.timestamp does not return utc time, but time since last systemboot. it is easyer to get current time when called.
        long l= System.currentTimeMillis();
        if(logging) {
            //If Values are not in the same format, we need to put the String constructer inside the getType
            log = Long.toString(l) + "," + Float.toString(event.values[0]) + "," +
                    Float.toString(event.values[1]) + "," + Float.toString(event.values[2]) + "," + Float.toString(event.accuracy) + "\n\r";
        }
        if(event.sensor.getType()==1){//Accelerometer
            AccTimeStamp = l;
            setAcc(event.values);
            if(logging){
                AccLog.add(log);
                if(AccLog.size()==bufferAmount){
                    String data = AccLog.toString();
                    LogService.WriteDataToFile(this, data, "accelerometer");
                    AccLog.clear();
                }
            }
        }
        else if(event.sensor.getType()==4){//Gyroscope
            setGyr(event.values);
            if(logging){
                GyrLog.add(log);
                if(GyrLog.size()==bufferAmount){
                    String data = GyrLog.toString();
                    LogService.WriteDataToFile(this, data, "gyroscope");
                    GyrLog.clear();
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //TODO find out if this is the right way to prevent the objects from beeing accesed by different Threads
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
}
