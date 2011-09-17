package com.jwetherell.compass;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.jwetherell.compass.data.GlobalData;

import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


/**
 * This class extends Activity and processes sensor data and location data.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class SensorsActivity extends Activity implements SensorEventListener, LocationListener {
    private static final Logger logger = Logger.getLogger(SensorsActivity.class.getSimpleName());    
    private static final AtomicBoolean computing = new AtomicBoolean(false); 

    private static final int MIN_TIME = 30*1000;
    private static final int MIN_DISTANCE = 10;

    private static final float grav[] = new float[3]; //Gravity (a.k.a accelerometer data)
    private static final float mag[] = new float[3]; //Magnetic 
    private static final float R[] = new float[9]; //Rotation matrix in Android format
    private static final float I[] = new float[9]; //Inclination matrix
    private static final float orientation[] = new float[3]; //yaw, pitch, roll
    
    private static int bearingIdx = 0;
    private static final float[] bearingArray = new float[3];

    private static SensorManager sensorMgr = null;
    private static List<Sensor> sensors = null;
    private static Sensor sensorGrav = null;
    private static Sensor sensorMag = null;
    
    private static LocationManager locationMgr = null;
    private static Location currentLocation = null;

    private static int bearing = 0;
    private static float floatBearing = 0;
    private static float floatSmoothedBearing = 0;
    
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

            locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

            try {
                /*defaulting to our place*/
                Location hardFix = new Location("ATL");
                hardFix.setLatitude(39.931261);
                hardFix.setLongitude(-75.051267);
                hardFix.setAltitude(1);

                try {
                    Location gps=locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location network=locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(gps!=null)
                    	currentLocation=(gps);
                    else if (network!=null)
                    	currentLocation=(network);
                    else
                    	currentLocation=(hardFix);
                } catch (Exception ex2) {
                    currentLocation=(hardFix);
                }
            } catch (Exception ex) {
                logger.info("Exception: "+ex);
            }
        } catch (Exception ex1) {
            try {
                if (sensorMgr != null) {
                    sensorMgr.unregisterListener(this, sensorGrav);
                    sensorMgr.unregisterListener(this, sensorMag);
                    sensorMgr = null;
                }
                if (locationMgr != null) {
                    locationMgr.removeUpdates(this);
                    locationMgr = null;
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

            try {
                locationMgr.removeUpdates(this);
            } catch (Exception ex) {
                logger.info("Exception: "+ex);
            }
            locationMgr = null;
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
        SensorManager.getRotationMatrix(R, I, grav, mag);
        SensorManager.getOrientation(R, orientation);
        floatBearing = orientation[0];
        
        int smoothCnt = 0;
        float smooth = 0f;
        for (int i = 0; i < bearingArray.length; i++) {
        	smooth += bearingArray[i];
        	smoothCnt++;
        }
        floatSmoothedBearing = (smoothCnt>0)?(smooth/smoothCnt):0f;

        if (bearingIdx == bearingArray.length) bearingIdx = 0;
        bearingArray[bearingIdx] = floatBearing;
        bearingIdx++;

        bearing = (int)Math.toDegrees(floatSmoothedBearing); //degrees east of true north (180 to -180)
        if (bearing<0) bearing+=360; //adjust to 0-360
        
        //Compensate for the difference between true north and magnetic north
        GeomagneticField gmf = new GeomagneticField((float) currentLocation.getLatitude(), 
                                                    (float) currentLocation.getLongitude(),
                                                    (float) currentLocation.getAltitude(), 
                                                    System.currentTimeMillis());
        floatBearing+=gmf.getDeclination();
        
        GlobalData.setBearing(bearing);

        computing.set(false);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE) {
            logger.info("Compass data unreliable");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation=(location);
    }

	@Override
	public void onProviderDisabled(String provider) {
		//Ignore
	}

	@Override
	public void onProviderEnabled(String provider) {
		//Ignore
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		//Ignore
	}
}
