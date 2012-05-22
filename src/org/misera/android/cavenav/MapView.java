package org.misera.android.cavenav;

import android.content.*;
import android.graphics.*;
import android.hardware.*;
import android.os.*;
import android.view.*;

import org.misera.android.cavenav.graph.Edge;
import org.misera.android.cavenav.graph.Graph;
import org.misera.android.cavenav.graph.Vertex;
import org.misera.android.cavenav.map.MapBundle;
import org.misera.android.cavenav.map.MapScreen;
import org.misera.android.cavenav.raycaster.RayCastRendererView;
import org.misera.android.cavenav.raycaster.RayCaster;

public class MapView extends View {
	
	private MapBundle mb;
	private MapScreen ms;
	
    private ScaleGestureDetector mScaleDetector;
	private boolean scaling = false;
	private float mLastTouchY;
	private float mLastTouchX;

	private RayCastRendererView rayCastRenderer;	
	private boolean hasRayCaster = false;
	
	private boolean clickStepping = false;
	private boolean followEdges = false;
	private Vertex selectedVertex;
	
	private Mode mode = Mode.NORMAL;
	public enum Mode {
		NORMAL, WAYPOINT, GRAPH, POI
	}

	public MapView(Context context, MapBundle mb) {
		super(context);

		this.mb = mb;
		
	    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_UI);
		
		this.setOnClickListener(clickListener);
	    this.setOnLongClickListener(longClickListener);
	    
	    WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    Display display = mWindowManager.getDefaultDisplay();
	    
		this.ms = new MapScreen(display);
	    ms.setDimensions(this.getWidth(), this.getHeight());
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
	
	public void toggleFollowEdges() {
		this.followEdges = !this.followEdges;
	}
		
		
    public void clear() {
		mb.route.clear();
		invalidate();
    }
    
    public void setMode(Mode mode) {
    	this.mode = mode;
    	switch (mode) {
    		case GRAPH: {
    			ms.setAllowRotation(false);
    			break;
    		}
    		default: {
    			ms.setAllowRotation(true);
    		}
    	}
    	invalidate();
    }

    

	@Override
    public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	    canvas.save();
	    
	    if (followEdges) {
	    	centerOnNearestEdge();
	    }
	    ms.setDimensions(this.getWidth(), this.getHeight());
	    ms.update();
	    int[] screenCenter = ms.getScreenCenter();
	    float[] position = ms.getPosition();

        // map
        mb.map.draw(canvas, ms);
		
        // bullseye at center
        mb.centerMarker.draw(canvas, ms);
	    	    		
		// graph
		if (mode == Mode.GRAPH) {
			mb.graph.draw(canvas, ms);
		}
		
		//route
		mb.route.draw(canvas, ms);
		
		// debug overlay
	    Paint paint = new Paint();
	    paint.setColor(Color.WHITE);
	    paint.setTextSize(10);
	    String debug = String.format("angle: %.2f, zoom: %.3f (x,y): (%.1f,%.1f)", 
	    				ms.getAngle(), ms.getScale(), position[0], position[1]);
	    if(scaling){
			debug += "[SCALING] ";
		}
		canvas.drawText(debug, 5, 10, paint);
		
		if(hasRayCaster){
			RayCaster rayCaster = rayCastRenderer.rayCaster;

			rayCaster.playerPos[0] = (int) Math.floor(position[0] + screenCenter[0]);
			rayCaster.playerPos[1] = (int) Math.floor(position[1] + screenCenter[1]);
			rayCaster.viewingAngle =  ms.getAngle() + 90;
			
			rayCastRenderer.invalidate();
			
		}
	    canvas.restore();
    }
	
	public boolean onTouchEvent(MotionEvent  ev) {
		mScaleDetector.onTouchEvent(ev);
		final int action = ev.getAction();
		switch (action) {
			case MotionEvent.ACTION_UP: {
				if(scaling){
					scaling = false;
				}
				selectedVertex = null;
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
		
					if (selectedVertex == null) {
						// Calculate the distance moved
						final float dx = (x - mLastTouchX) / ms.getScale();
						final float dy = (y - mLastTouchY) / ms.getScale();
						// Move the object
						ms.move(-dx, -dy);
					} else {
						float[] mapPosition = ms.screenToMapCoords(x,y);
						selectedVertex.x = (int) mapPosition[0];
						selectedVertex.y = (int) mapPosition[1];
					}
					
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
	        float newScale = ms.getScale() * detector.getScaleFactor();
	        // Don't let the object get too small or too large.
	        newScale = Math.max(0.1f, Math.min(newScale, 10.0f));
	        ms.setScale(newScale);
	        invalidate();
	        return true;
	    }
	}
	
	private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
        	float heading = event.values[0];
    		ms.setAngle(heading);
            invalidate();
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    
    
    private OnLongClickListener longClickListener = new OnLongClickListener() {
		public boolean onLongClick(View v) {
			selectedVertex = null;
        	if (mode == Mode.WAYPOINT) {
				float[] coords = ms.screenToMapCoords(mLastTouchX, mLastTouchY);
	        	Vertex vertex = mb.graph.nearestVertex((int)coords[0], (int)coords[1]);
	        	if (vertex != null) {
	        		mb.route.addWaypoint(vertex);
	        	}
	    		mb.route.find();
        		invalidate();
        	}
        	else if (mode == Mode.GRAPH) {
        		float[] coords = ms.screenToMapCoords(mLastTouchX, mLastTouchY);
	        	Vertex vertex = mb.graph.nearestVertex((int)coords[0], (int)coords[1]);
	        	if (vertex != null) {
	        		mb.route.clear();
	        		mb.route.addWaypoint(vertex);
	        		selectedVertex = vertex;
	        		invalidate();
	        	} else {
	        		mb.route.clear();
	        		selectedVertex = vertex;
	        		invalidate();
	        	}
        	}
			return false;
        }
    };
    	
	private OnClickListener clickListener = new OnClickListener(){
		public void onClick(View v){
			if (clickStepping) {
				float stepLength = 0.75f;
				
				float dx = 0;
				float dy = (float) -(stepLength / mb.pixelLength);
				
				ms.move(dx, dy);
				invalidate();
			}
			//return false;
		}
	};
	
	private void centerOnNearestEdge() {
		// this is the easiest but also the most inefficient way to center on an edge:
		// find closest edge E relative to center of screen C
		int[] center = ms.getScreenCenter();
		float[] c = ms.screenToMapCoords(center[0], center[1]);
		// find shortest distance from C to point P on edge E
		float[] p = null;
		double shortestDistance = Double.MAX_VALUE;
		for (Edge edge : mb.graph.edges.values()) {
			float[] closest = Graph.closestPointOnEdge(c[0], c[1], edge);
			double dist = Graph.distance(c[0], c[1], closest[0], closest[1]);
			if (dist < shortestDistance) {
				shortestDistance = dist;
				p = closest;
			}
		}
		if (p == null) {
			return;
		}
		ms.centerOnMapPosition(p[0], p[1]);
		
	}
	
}
