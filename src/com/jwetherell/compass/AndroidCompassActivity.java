package com.jwetherell.compass;

import com.jwetherell.compass.data.GlobalData;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


/**
 * This class extends the SensorsActivity and is designed tie the CompassView and Sensors together.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class AndroidCompassActivity extends SensorsActivity {
	private static final String TAG = "AndroidCompassActivity";

	private static WakeLock wakeLock = null;
	
    private static TextView text = null;
    private static View compassView = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate()");

        setContentView(R.layout.main);

        text = (TextView) findViewById(R.id.text);
        compassView = findViewById(R.id.compass);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.i(TAG,"onDestroy()");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
    	super.onStart();
    	Log.i(TAG,"onStart()");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
    	super.onStop();
    	Log.i(TAG,"onStop()");
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG,"onResume()");
		
		wakeLock.acquire();
	}
    
    /**
     * {@inheritDoc}
     */
	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG,"onPause()");
		
		wakeLock.release();
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent evt) {
        super.onSensorChanged(evt);

        if (    evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER || 
                evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
        ) {
        	//Tell the compass to update it's graphics
            if (compassView!=null) compassView.postInvalidate();
        }

        //Update the direction text
        updateText(GlobalData.getBearing());
    }
    
    private static void updateText(float bearing) {
        int range = (int) (bearing / (360f / 16f)); 
        String  dirTxt = "";
        if (range == 15 || range == 0) dirTxt = "N"; 
        else if (range == 1 || range == 2) dirTxt = "NE"; 
        else if (range == 3 || range == 4) dirTxt = "E"; 
        else if (range == 5 || range == 6) dirTxt = "SE";
        else if (range == 7 || range == 8) dirTxt= "S"; 
        else if (range == 9 || range == 10) dirTxt = "SW"; 
        else if (range == 11 || range == 12) dirTxt = "W"; 
        else if (range == 13 || range == 14) dirTxt = "NW";
        text.setText(""+((int) bearing)+((char)176)+" "+dirTxt);
    }
}
