package com.jwetherell.compass.data;

import com.jwetherell.compass.common.Matrix;
import com.jwetherell.compass.common.MixState;


/**
 * Abstract class which should be used to set global data.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public abstract class GlobalData {
    private static final MixState state = new MixState();

    private static Matrix rotationMatrix = null;
    
	public static MixState getState() {
		return state;
	}

    public static void setRotationMatrix(Matrix rotationMatrix) {
        GlobalData.rotationMatrix = rotationMatrix;
    }
    public static Matrix getRotationMatrix() {
        return rotationMatrix;
    }
}
