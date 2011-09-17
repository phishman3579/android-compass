package com.jwetherell.compass;

import java.util.concurrent.atomic.AtomicBoolean;

import com.jwetherell.compass.data.GlobalData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;


/**
 * This class extends the View class and is designed draw the compass on the View.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class CompassView extends View {
	private static final AtomicBoolean drawing = new AtomicBoolean(false);

    private static Matrix matrix = null;
    private static Bitmap bitmap = null;

    public CompassView(Context context) {
        super(context);
        
        init();
    }    
    
    public CompassView(Context context, AttributeSet attr) {
        super(context,attr);
        
        init();
    }
    
    private void init() {
        matrix = new Matrix();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.compass_icon);
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	if (canvas==null) return;

        if (!drawing.compareAndSet(false, true)) return; 

        float bearing = GlobalData.getBearing();

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        if (bitmapWidth>canvasWidth || bitmapHeight>canvasHeight) {        
            //Resize the bitmap to the size of the canvas
            bitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmapWidth*.9), (int)(bitmapHeight*.9), true);
        }
        
        int bitmapX = bitmap.getWidth()/2;
        int bitmapY = bitmap.getHeight()/2;
        
        int canvasX = canvas.getWidth()/2;
        int canvasY = canvas.getHeight()/2;
        
        int centerX = canvasX-bitmapX;
        int centerY = canvasY-bitmapY;
        
        matrix.reset();
        //Rotate the bitmap around it's center point
        matrix.setRotate(bearing, bitmapX, bitmapY);
        //Move the bitmap to the center of the canvas
        matrix.postTranslate(centerX, centerY);

        canvas.drawBitmap(bitmap, matrix, null);

	    drawing.set(false);
    }
}
