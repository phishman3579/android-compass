package com.jwetherell.compass;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.jwetherell.compass.common.Matrix;
import com.jwetherell.compass.data.GlobalData;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;


/**
 * This class extends Activity and processes sensor data and location data.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class SensorsActivity extends Activity implements SensorEventListener {
    private static final Logger logger = Logger.getLogger(SensorsActivity.class.getSimpleName());    
    private static final AtomicBoolean computing = new AtomicBoolean(false); 
    
    private static final float RTmp[] = new float[9]; //Temporary rotation matrix in Android format
    private static final float Rot[] = new float[9]; //Final rotation matrix in Android format
    private static final float I[] = new float[9]; //Inclination matrix
    private static final float grav[] = new float[3]; //Gravity (a.k.a accelerometer data)
    private static final float mag[] = new float[3]; //Magnetic 

    private static int rHistIdx = 0;
    private static final Matrix finalR = new Matrix();
    private static final Matrix smoothR = new Matrix();
    private static final Matrix histR[] = new Matrix[10];

    private static SensorManager sensorMgr = null;
    private static List<Sensor> sensors = null;
    private static Sensor sensorGrav = null;
    private static Sensor sensorMag = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        
        try {
            sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

            sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (sensors.size() > 0) sensorGrav = sensors.get(0);

            sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if (sensors.size() > 0) sensorMag = sensors.get(0);

            sensorMgr.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_UI);
            sensorMgr.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_UI);
        } catch (Exception ex1) {
            try {
                if (sensorMgr != null) {
                    sensorMgr.unregisterListener(this, sensorGrav);
                    sensorMgr.unregisterListener(this, sensorMag);
                    sensorMgr = null;
                }
            } catch (Exception ex2) {
                logger.info("Exception: "+ex2);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            try {
                sensorMgr.unregisterListener(this, sensorGrav);
            } catch (Exception ex) {
                logger.info("Exception: "+ex);
            }
            try {
                sensorMgr.unregisterListener(this, sensorMag);
            } catch (Exception ex) {
                logger.info("Exception: "+ex);
            }
            sensorMgr = null;
        } catch (Exception ex) {
            logger.info("Exception: "+ex);
        }
    }
    
    @Override
    public void onSensorChanged(SensorEvent evt) {
    	if (!computing.compareAndSet(false, true)) return;
    	
        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            grav[0] = evt.values[0];
            grav[1] = evt.values[1];
            grav[2] = evt.values[2];
        } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mag[0] = evt.values[0];
            mag[1] = evt.values[1];
            mag[2] = evt.values[2];
        }

        //Get rotation and inclination matrices given the gravity and geomagnetic matrices
        SensorManager.getRotationMatrix(RTmp, I, grav, mag);
        
        //Translate the rotation matrices from X and -Z (landscape)
        SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, Rot);

        //Convert from float[9] to Matrix
        finalR.set(Rot[0], Rot[1], Rot[2], Rot[3], Rot[4], Rot[5], Rot[6], Rot[7], Rot[8]);

        //Start to smooth the data (catch a boundary case)
        histR[rHistIdx].set(finalR);
        rHistIdx++;
        if (rHistIdx >= histR.length) rHistIdx = 0;

        //Zero out the smoothed rotation matrix
        smoothR.set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
        
        //Add the historic data
        for (int i = 0; i < histR.length; i++) {
            smoothR.add(histR[i]);
        }
        //Smooth the historic data
        smoothR.mult(1 / (float) histR.length);

        //Set the rotation matrix (used to translate all object from lat/lon to x/y/z)
        GlobalData.setRotationMatrix(smoothR);
        
        computing.set(false);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE) {
            logger.info("Compass data unreliable");
        }
    }
}
