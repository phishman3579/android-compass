package com.jwetherell.compass.data;


/**
 * Abstract class which should be used to set global data.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public abstract class GlobalData {
	private static final Object lock = new Object();
    private static int bearing = 0;

    public static void setBearing(int bearing) {
    	synchronized (lock) {
    		GlobalData.bearing = bearing;
    	}
    }
    public static int getBearing() {
    	synchronized (lock) {
    		return bearing;
    	}
    }
}
