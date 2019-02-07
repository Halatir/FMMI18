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
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import java.util.ArrayList;

public class SensorService extends Service implements SensorEventListener {

    public static SensorService singleton;

    private  SensorManager mSensorManager;
    public boolean ready = false;
    Context context;

    private float[] Acc = {0.0f,0.0f,0.0f,0.0f};
    private float[] Acc_o_g = {0.0f,0.0f,0.0f};
    private float[] Gyr = {0.0f,0.0f,0.0f};
    private float[] pressure = {0.0f,0.0f,0.0f};
    private float[] geo = new float[3];
    private float[] lowMag = new float[3];
    private float[] lowAcc = new float[3];

    private float lastDirectionInDegrees = 0f;
    private RotateAnimation rotateAnimation;
    private float smoothingFactor = 0.1f;

    //TODO not locked correctly
    public long AccTimeStamp = 0;
    private float light =0.0f;

    public boolean logging = false;
    private static int bufferAmount = 1000;
    private ArrayList AccLog = new ArrayList(bufferAmount);
    private ArrayList GyrLog = new ArrayList(bufferAmount);
    private ArrayList BaroLog = new ArrayList(bufferAmount);
    private ArrayList LightLog = new ArrayList(bufferAmount);
    private ArrayList MagLog = new ArrayList(bufferAmount);

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        if(singleton==null){
            singleton = this;
        }else{
            stopSelf();
        }

        context= getApplicationContext();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        Sensor m_Accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor m_Gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor m_Barometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        Sensor m_Accelo_ohne_g = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor m_light = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Sensor m_magneto = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        HandlerThread m_SensorThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
        m_SensorThread.start();
        Handler mSensorHandler = new Handler(m_SensorThread.getLooper());
        mSensorManager.registerListener(this, m_Accelerometer, 8300, mSensorHandler);//120 sample in one second
        mSensorManager.registerListener(this, m_Gyroscope,8300, mSensorHandler);
        mSensorManager.registerListener(this,m_Barometer,8300,mSensorHandler);
        mSensorManager.registerListener(this, m_Accelo_ohne_g,8300,mSensorHandler);
        mSensorManager.registerListener(this, m_light, 8300, mSensorHandler);
        mSensorManager.registerListener(this,m_magneto,8300,mSensorHandler);
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

        //event.timestamp does not return utc time, but time since last systemboot. it is easyer to get current time when called. TODO: Berechne UTC von event.timestamp
        long l= System.currentTimeMillis();
        int i = event.sensor.getType();

        switch(i){
            case Sensor.TYPE_MAGNETIC_FIELD:
                if(logging){
                    log = Long.toString(l) + "," + Float.toString(event.values[0]) + "," +
                            Float.toString(event.values[1]) + "," + Float.toString(event.values[2]) + "," + Float.toString(event.accuracy) + "\n\r";
                    MagLog.add(log);
                    if(MagLog.size()==bufferAmount){
                        String data = MagLog.toString();
                        LogService.WriteDataToFile(this, data, "magnetic_field");
                        MagLog.clear();
                    }
                }
                float[] magneto = {event.values[0], event.values[1],event.values[2],event.accuracy};
                setMag(magneto);
                lowMag = applyLowPassFilter(event.values.clone(), lowMag);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                if(logging){
                    log = Long.toString(l) + "," + Float.toString(event.values[0]) + "," +
                            Float.toString(event.values[1]) + "," + Float.toString(event.values[2]) + "," + Float.toString(event.accuracy) + "\n\r";
                    AccLog.add(log);
                    if(AccLog.size()==bufferAmount){
                        String data = AccLog.toString();
                        LogService.WriteDataToFile(this, data, "accelerometer");
                        AccLog.clear();
                    }
                }
                float[] f = {event.values[0], event.values[1],event.values[2],event.accuracy};
                AccTimeStamp = l;
                setAcc(f);
                lowAcc = applyLowPassFilter(event.values.clone(),lowAcc);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                setAcc_o_g((event.values));
                break;
            case Sensor.TYPE_GYROSCOPE:
                setGyr(event.values);
                if(logging){
                    log = Long.toString(l) + "," + Float.toString(event.values[0]) + "," +
                            Float.toString(event.values[1]) + "," + Float.toString(event.values[2]) + "," + Float.toString(event.accuracy) + "\n\r";
                    GyrLog.add(log);
                    if(GyrLog.size()==bufferAmount){
                        String data = GyrLog.toString();
                        LogService.WriteDataToFile(this, data, "gyroscope");
                        GyrLog.clear();
                    }
                }
                break;
            case Sensor.TYPE_PRESSURE:
                if(logging){
                    log = Long.toString(l) + "," + Float.toString(event.values[0]) + "," + Float.toString(event.accuracy) + "\n\r";
                    BaroLog.add(log);
                    if(BaroLog.size()==bufferAmount){
                        String data = BaroLog.toString();
                        LogService.WriteDataToFile(this,data,"barometer");
                        BaroLog.clear();
                    }
                }
                float height = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,event.values[0]);
                float[] press = {event.values[0],height,event.accuracy};
                setPress(press);

                break;
            case Sensor.TYPE_LIGHT:
                if(logging){
                    log = Long.toString(l) + "," + Float.toString(event.values[0])+ "," + Float.toString(event.accuracy)+"\n\r";
                    LightLog.add(log);
                    if(LightLog.size()==bufferAmount){
                        String data = LightLog.toString();
                        LogService.WriteDataToFile(this,data,"light");
                        LightLog.clear();
                    }
                    light = event.values[0];
                }
                break;
        }
        float R[] = new float[9];
        float I[] = new float[9];
        boolean success = SensorManager.getRotationMatrix(R, I, lowAcc, lowMag);
        if(success) {
            float[] orientationValues = new float[3];
            SensorManager.getOrientation(R,orientationValues);
            float newDirectionInDegrees = (float) Math.toDegrees(
                    -orientationValues[0]);

            rotateAnimation = new
                    RotateAnimation(
                    lastDirectionInDegrees, newDirectionInDegrees,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(50);
            rotateAnimation.setFillAfter(true);

            lastDirectionInDegrees = newDirectionInDegrees;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //TODO find out if this is the right way to prevent the objects from beeing accesed by different Threads
    public void setAcc(float[] f){
        synchronized (Acc){
            Acc=f;
        }
    }
    public float[] getAcc(){
        synchronized(Acc){
            return Acc;
        }
    }
    public void setAcc_o_g(float[] f){
        synchronized(Acc_o_g){
            Acc_o_g=f;
        }
    }
    public float[] getAcc_o_g(){
        synchronized(Acc_o_g){
            return Acc_o_g;
        }
    }

    public void setPress(float[] f){
        synchronized(pressure){
            pressure=f;
        }
    }
    public float[] getPress(){
        synchronized(pressure){
            return pressure;
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
        synchronized (geo){
            geo = f;
        }
    }

    public float[] getMag(){
        synchronized (geo){
            return geo;
        }
    }

    public RotateAnimation getRotation(){
        return rotateAnimation;
    }

    //https://stackoverflow.com/questions/27846604/how-to-get-smooth-orientation-data-in-android
    private float[] applyLowPassFilter(float[] input, float[] output) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + smoothingFactor * (input[i] - output[i]);
        }
        return output;
    }
}
