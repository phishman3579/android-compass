package com.jwetherell.compass.ui.objects;

import java.util.logging.Logger;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


/**
 * This abstract class provides many methods paint objects on a given Canvas.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public abstract class PaintableObject {
    private static final Logger logger = Logger.getLogger(PaintableObject.class.getSimpleName());
    private static final boolean DEBUG = false;
    
    private static Paint paint = null;

    public PaintableObject() {
        if (paint==null) {
            paint = new Paint();
            paint.setTextSize(16);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
        }
    }

    public abstract float getWidth();

    public abstract float getHeight();

    public abstract void paint(Canvas canvas);
    
    public void setFill(boolean fill) {
        if (fill)
            paint.setStyle(Paint.Style.FILL);
        else
            paint.setStyle(Paint.Style.STROKE);
    }

    public void setColor(int c) {
        paint.setColor(c);
    }

    public void setStrokeWidth(float w) {
        paint.setStrokeWidth(w);
    }

    public float getTextWidth(String txt) {
        return paint.measureText(txt);
    }

    public float getTextAsc() {
        return -paint.ascent();
    }

    public float getTextDesc() {
        return paint.descent();
    }

    public void setFontSize(float size) {
        paint.setTextSize(size);
    }

    public void paintRect(Canvas canvas, float x, float y, float width, float height) {
    	if (canvas==null) return;
    	
        if (DEBUG) logger.severe("paintRect: x="+x+" y="+y+" width="+(x + width)+" height="+(y + height)+" paint="+paint.toString());
        canvas.drawRect(x, y, x + width, y + height, paint);
    }

    public void paintText(Canvas canvas, float x, float y, String text) {
    	if (canvas==null && text==null) return;
    	
        if (DEBUG) logger.severe("paintText: x="+x+" y="+y+" text="+text);
        canvas.drawText(text, x, y, paint);
    }

    public void paintObj(	Canvas canvas, PaintableObject obj, 
    						float x, float y, 
    						float rotation, float scale) 
    {
    	if (canvas==null || obj==null) return;
    	
        if (DEBUG) logger.severe("paintObj: x="+x+" y="+y+" rotation="+rotation+" scale="+scale);
        canvas.save();
        canvas.translate(x + obj.getWidth() / 2, y + obj.getHeight() / 2);
        canvas.rotate(rotation);
        canvas.scale(scale, scale);
        canvas.translate(-(obj.getWidth() / 2), -(obj.getHeight() / 2));
        obj.paint(canvas);
        canvas.restore();
    }
}
