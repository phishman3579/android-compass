package com.jwetherell.compass.data;


/**
 * Abstract class which should be used to set global data.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public abstract class GlobalData {
	private static final Object lock = new Object();
    private static float bearing = 0;

    /**
     * Set the bearing.
     * @param bearing int representing the current bearing.
     */
    public static void setBearing(float bearing) {
    	synchronized (lock) {
    		GlobalData.bearing = bearing;
    	}
    }
    
    /**
     * Get the bearing.
     * @return int representing the bearing.
     */
    public static float getBearing() {
    	synchronized (lock) {
    		return bearing;
    	}
    }
}
