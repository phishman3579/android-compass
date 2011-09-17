package com.jwetherell.compass.data;


/**
 * Abstract class which should be used to set global data.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public abstract class GlobalData {
    private static int bearing = 0;

    public static void setBearing(int bearing) {
        GlobalData.bearing = bearing;
    }
    public static int getBearing() {
        return bearing;
    }
}
