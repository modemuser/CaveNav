package org.misera.android.cavenav;

import android.content.*;
import android.graphics.*;
import android.hardware.*;
import android.os.*;
import android.util.*;
import android.view.*;

import java.util.*;

import org.misera.android.cavenav.graph.Edge;
import org.misera.android.cavenav.graph.Graph;
import org.misera.android.cavenav.graph.Vertex;

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
	private float heading = 0.f;
	private float prevAngle = 0.f;
	private Display mDisplay;

	private RayCastRendererView rayCastRenderer;	
	private boolean hasRayCaster = false;
	
	private boolean clickStepping = false;
	private boolean showPaths = false;
	private Graph graph;

	public MapView(Context context, Bitmap pic, Graph graph) {
		super(context);
		this.pic = pic;
		this.graph = graph;
		
		screen.bottom = this.getHeight();
		screen.right = this.getWidth();
	    
	    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
		
		this.setOnClickListener(clickListener);
	    
	    this.setOnLongClickListener(longClickListener);
	    
	    WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    mDisplay = mWindowManager.getDefaultDisplay();
	    
	    // 0deg = 0; 90deg CW = 1; 180deg = 2; 90deg CCW = 3 
	    Log.d("ORIENTATION_TEST", "getOrientation(): " + mDisplay.getOrientation());
	}  
	

	protected void onCreate(Bundle savedValues) {
	}
	
	public void setRayCaster(RayCastRendererView r){
		this.rayCastRenderer = r;
		hasRayCaster = true;
	}
	
	public void toggleClickStepping() {
		this.clickStepping = !this.clickStepping;
	}
	
	public void togglePaths() {
		this.showPaths = !this.showPaths ;
		invalidate();
	}
	
    public void clearMarkers() {
		markers.clear();
		invalidate();
    }
    
	
	@Override
    public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	    canvas.save();
	    
	    Matrix m = new Matrix();
	    
		screen.bottom = this.getHeight();
		screen.right = this.getWidth();

    	int centerX = screen.right/2;
    	int centerY = screen.bottom/2;

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
	    canvas.drawCircle(centerX, centerY, 5, paint);
	    // debug overlay
	    String debug = String.format("angle: %.2f, zoom: %.3f (x,y): (%.1f,%.1f) ", angle, mScaleFactor, mPosX, mPosY);
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
			canvas.drawCircle(coords[0], coords[1], mScaleFactor, paint);
		}
		if (showPaths) {
			// vertices
			paint.setColor(Color.GREEN);
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			for (Integer key : graph.vertices.keySet()) {
				Vertex v = graph.vertices.get(key);
				float[] coords = {v.x, v.y};
				m.mapPoints(coords);
				canvas.drawCircle(coords[0], coords[1], mScaleFactor, paint);
			}
			// edges
			for (Integer key : graph.edges.keySet()) {
				Edge e = graph.edges.get(key);
				Vertex startVertex = e.startVertex;
				float[] start = {startVertex.x, startVertex.y};
				m.mapPoints(start);
				Vertex endVertex = e.endVertex;
				float[] end = {endVertex.x, endVertex.y};
				m.mapPoints(end);
				canvas.drawLine(start[0], start[1], end[0], end[1], paint);
			}
		}
		
		if(hasRayCaster){
			RayCaster rayCaster = rayCastRenderer.rayCaster;
			
			rayCaster.playerPos[0] = (int) Math.floor(mPosX + centerX);
			rayCaster.playerPos[1] = (int) Math.floor(mPosY + centerY);
			rayCaster.viewingAngle =  angle + 90;
			
			rayCastRenderer.invalidate();
			
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
	
	
	private float[] screenToMapCoords(float angle, float xScreen,float yScreen){

		float x = xScreen;
		float y = yScreen;

		// Convert x,y to polar coords

		double r = Math.sqrt(x*x + y*y);
		double theta = Math.atan2(y,x);

		// Rotate by angle
		double rotation = angle * Math.PI / 180;
		theta += rotation;

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
		return super.onTouchEvent(ev);
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
	
	private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
        	float _heading = event.values[0];
        	// make the heading compatible to the transform matrix:
        	// 1. invert sign of heading to turn the other way
        	// 2. take into account screen orientation
        	float angleNew = -_heading - 90*mDisplay.getOrientation();
        	// to smooth the rotation, only rotate if angle changes significantly
        	if (Math.abs(angle - angleNew) > 0.3) {
        		angle = angleNew;
        		heading = _heading;
                invalidate();
        	}
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    
    private ArrayList<Point> markers = new ArrayList<Point>();
    
    private OnLongClickListener longClickListener = new OnLongClickListener() {
        public boolean onLongClick(View v) {
			float[] coords = screenToMapCoords(angle, mLastTouchX, mLastTouchY);
        	Vertex vertex = graph.nearestVertex((int)coords[0], (int)coords[1]);
    		markers.add(new Point((int)coords[0], (int)coords[1]));
        	if (vertex != null) {
        		//markers.add(new Point(vertex.x, vertex.y));
        		Log.d("CaveNav", vertex.toString());
        	}
			return false;
        }
    };
    	
	private OnClickListener clickListener = new OnClickListener(){
		public void onClick(View v){
			if (clickStepping) {
				float stepLength = 0.75f;
				float pixelLength = 0.5f;
				
				float dx = 0;
				float dy = -(stepLength / pixelLength);
				
				float[] transformed = mapToScreenCoords(angle, dx,dy);
				mPosX += transformed[0];
				mPosY += transformed[1];
				
				float centerX = screen.right/2;
				float centerY = screen.bottom/2;
				
				markers.add(new Point((int) (mPosX + centerX), (int) (mPosY + centerY)));
				//mPosX -= dx;
				Log.i("clickAdvance", "PosX = " + mPosX);
				invalidate();
			}
			//return false;
		}
	};
}
