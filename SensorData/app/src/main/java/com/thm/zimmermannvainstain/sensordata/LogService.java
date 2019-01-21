package com.thm.zimmermannvainstain.sensordata;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LogService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_WRITE = "com.thm.zimmermannvainstain.sensordata.action.WriteData";

    // TODO: Rename parameters
    private static final String EXTRA_WRITEDATA = "com.thm.zimmermannvainstain.sensordata.extra.data";
    private static final String EXTRA_SENSOR = "com.thm.zimmermannvainstain.sensordata.extra.Sensor";

    public LogService() {
        super("LogService");
    }

    public static void WriteDataToFile(Context context, String data, String sensor){
        Intent intent = new Intent(context, LogService.class);
        intent.setAction(ACTION_WRITE);
        intent.putExtra(EXTRA_WRITEDATA, data);
        intent.putExtra(EXTRA_SENSOR,sensor);
        context.startService(intent);
    }

    private void handleActionWriteData(String data,String sensor) {
        // TODO: Handle action
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            String directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/SensorData";
            File dir = new File(directoryPath);
            if(!dir.exists() && !dir.isDirectory()){
                   dir.mkdirs();
            }
            File sensorFile = new File(directoryPath,sensor);
            if(!sensorFile.exists()){
                try {
                    sensorFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }try {
                FileOutputStream fOut = new FileOutputStream(sensorFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(data);
                myOutWriter.close();
                fOut.close();
                } catch (Exception e) {
                e.printStackTrace();
            }

        }else{
            //TODO Inform User, there is no Storage. Inform before starting log
            int i=0;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_WRITE.equals(action)) {
                final String data = intent.getStringExtra(EXTRA_WRITEDATA);
                final String sensor = intent.getStringExtra(EXTRA_SENSOR);
                handleActionWriteData(data,sensor);
            }
        }
    }
}
