package org.misera.android.cavenav;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class MapView extends View {

	private Bitmap pic;
    private Rect screen = new Rect();
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
	private boolean scaling = false;
	private float mLastTouchY;
	private float mLastTouchX;
	private float mPosX;
	private float mPosY;
	private float angle = 0.f;
	private float prevAngle = 0.f;
    
	public MapView(Context context, Bitmap pic, SensorManager mSensorManager) {
		super(context);
		this.pic = pic;
			
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
		screen.bottom = dm.heightPixels;
		screen.right = dm.widthPixels;
	    
	    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
	    
	    this.setOnLongClickListener(longClickListener);
	}  
	
	protected void onCreate(Bundle savedValues) {
	}
	
	@Override
    public void onDraw(Canvas canvas) {

		super.onDraw(canvas);
	    canvas.save();
		
	    Matrix m = new Matrix();
	    float centerX = screen.right/2;
	    float centerY = screen.bottom/2;

		float x = mPosX;
		float y = mPosY;

		
		/* 
			Convert the point that is C in the viewport coordinate system
			to map coordinate system and move it to (0,0)
		*/
		m.setTranslate(-(centerX + x), -(centerY + y));
		
		/* 
			Now rotate the map around point (0,0) which 
			now corresponds to the viewport center
		*/
		m.postRotate(angle, 0,0);		
		
		/* 
			Move map back so that only the (x,y) offset remains
		*/
		m.postTranslate((centerX), (centerY));

		/*
		 	Scale around the C
		 */
		m.postScale(mScaleFactor, mScaleFactor, centerX, centerY);
		
        canvas.drawBitmap(pic, m, null);
		
	    Paint paint = new Paint();
	    paint.setColor(Color.RED);
	    canvas.drawCircle(centerX, centerY, 1, paint);
	    paint.setStyle(Paint.Style.STROKE);
	    canvas.drawCircle(centerX, centerY, 3, paint);
	    // debug overlay
	    String debug = "angle: " + angle + "�, zoom: " + mScaleFactor + " (x,y): (" + mPosX + "," + mPosY + ") " ;
	    if(scaling){
			debug += "[SCALING] ";
		}
	    debug += markers.size();
		canvas.drawText(debug, 10, 10, paint);
		// markers
		paint.setColor(Color.YELLOW);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		for (Point p : markers) {
			float[] coords = {p.x, p.y};
			m.mapPoints(coords);
			canvas.drawCircle(coords[0], coords[1], 3, paint);
		}
		
	    canvas.restore();
    }
	
	private float[] mapToScreenCoords(float angle, float xMap,float yMap){
		
		float x = xMap;
		float y = yMap;
		
		// Convert x,y to polar coords

		double r = Math.sqrt(x*x + y*y);
		double theta = Math.atan2(y,x);

		// Rotate by angle
		double rotation = angle * Math.PI / 180;
		theta -= rotation;

		// Convert back to cartesian coords

		x = (float) (r * Math.cos(theta));
		y = (float) (r * Math.sin(theta));
		
		float[] returnArray = {x,y};
		return returnArray;
	}
		
	public boolean onTouchEvent(MotionEvent  ev) {
		mScaleDetector.onTouchEvent(ev);
		final int action = ev.getAction();
		switch (action) {
			case MotionEvent.ACTION_UP: {
				if(scaling){
					scaling = false;
				}
		        break;
		    }
		    case MotionEvent.ACTION_DOWN: {
				if(!scaling){
					final float x = ev.getX();
					final float y = ev.getY();

					// Remember where we started
					mLastTouchX = x;
					mLastTouchY = y;	
				}
		        break;
		    }
		        
		    case MotionEvent.ACTION_MOVE: {
				if(!scaling){
					final float x = ev.getX();
					final float y = ev.getY();
					
					

					// Calculate the distance moved
					final float dx = (x - mLastTouchX);
					final float dy = (y - mLastTouchY);

					float[] mapCoords = mapToScreenCoords(angle, dx, dy);
					
					// Move the object
					mPosX -= mapCoords[0] / mScaleFactor;
					mPosY -= mapCoords[1] / mScaleFactor;

					// Remember this touch position for the next move event
					mLastTouchX = x;
					mLastTouchY = y;

					// Invalidate to request a redraw
					invalidate();	
				}
				break;
		        
		    }
	    }
		return true;
	}
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	    @Override
	    public boolean onScale(ScaleGestureDetector detector) {
			scaling = true;
	        mScaleFactor *= detector.getScaleFactor();

	        // Don't let the object get too small or too large.
	        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

	        invalidate();
	        return true;
	    }
	}
	
	ArrayList<Float> headings = new ArrayList<Float>();
	
	private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
        	float heading = event.values[0];

        	/* averaging always makes it jump since avg({0, 360})==180
            headings.add(heading);
            if (headings.size() > 50) {
            	headings.remove(0);
            }
            float sum = 0.f;
            for (float f : headings) {
            	sum += f;
            }
            float avg = sum / headings.size();
            */
        	float angleNew = -heading + 180;
        	// to smooth the rotation, only rotate if angle changes significantly
        	if (Math.abs(angle - angleNew) > 0.3) {
        		angle = angleNew;
                invalidate();
        	}
            invalidate();

        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    
    private ArrayList<Point> markers = new ArrayList<Point>();
    
    private OnLongClickListener longClickListener = new OnLongClickListener() {
        public boolean onLongClick(View v) {
        	Point coordinates = new Point((int)mLastTouchX, (int)mLastTouchY);
        	markers.add(coordinates);
			return false;
        }
    };
}
