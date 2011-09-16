package com.jwetherell.compass.ui;

import com.jwetherell.compass.common.MixState;
import com.jwetherell.compass.data.GlobalData;
import com.jwetherell.compass.ui.objects.PaintablePosition;
import com.jwetherell.compass.ui.objects.PaintableText;

import android.graphics.Canvas;
import android.graphics.Color;


/**
 * This class will visually represent a compass.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class Compass {
	private static final float PAD_X = 80;
    private static final float PAD_Y = 40;
    private static final int TEXT_COLOR = Color.rgb(255,255,255);
    private static final int TEXT_SIZE = 32;
    private static final MixState state = new MixState();

    private static PaintableText paintableText = null;
    private static PaintablePosition paintedContainer = null;

    public Compass() { }

    public void draw(Canvas canvas) {
    	if (canvas==null) return;

    	//Update the pitch and bearing using the phone's rotation matrix
        state.calcPitchBearing(GlobalData.getRotationMatrix());

        //Update the compass graphics and text based upon the new pitch and bearing
        drawCompassText(canvas);
    }

    private void drawCompassText(Canvas canvas) {
    	if (canvas==null) return;
    	
        //Direction text
        int range = (int) (state.bearing / (360f / 16f)); 
        String  dirTxt = "";
        if (range == 15 || range == 0) dirTxt = "N"; 
        else if (range == 1 || range == 2) dirTxt = "NE"; 
        else if (range == 3 || range == 4) dirTxt = "E"; 
        else if (range == 5 || range == 6) dirTxt = "SE";
        else if (range == 7 || range == 8) dirTxt= "S"; 
        else if (range == 9 || range == 10) dirTxt = "SW"; 
        else if (range == 11 || range == 12) dirTxt = "W"; 
        else if (range == 13 || range == 14) dirTxt = "NW";
        int bearing = (int) state.bearing; 
        
        radarText(canvas, ""+bearing+((char)176)+" "+dirTxt, PAD_X, PAD_Y, true);
    }
    
    private void radarText(Canvas canvas, String txt, float x, float y, boolean bg) {
    	if (canvas==null || txt==null) return;
    	
        if (paintableText==null) paintableText = new PaintableText(txt,TEXT_COLOR,TEXT_SIZE,bg);
        else paintableText.set(txt,TEXT_COLOR,TEXT_SIZE,bg);
        
        if (paintedContainer==null) paintedContainer = new PaintablePosition(paintableText,x,y,0,1);
        else paintedContainer.set(paintableText,x,y,0,1);
        
        paintedContainer.paint(canvas);
    }
}
