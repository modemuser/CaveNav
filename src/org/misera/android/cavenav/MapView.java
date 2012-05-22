package org.misera.android.cavenav;

import java.util.ArrayList;

import android.content.*;
import android.graphics.*;
import android.hardware.*;
import android.os.*;
import android.util.*;
import android.view.*;

import org.misera.android.cavenav.graph.AStar;
import org.misera.android.cavenav.graph.Edge;
import org.misera.android.cavenav.graph.Edge.Direction;
import org.misera.android.cavenav.graph.Graph;
import org.misera.android.cavenav.graph.Vertex;

public class MapView extends View {
	
	private Map map;
	private MapScreen ms;
    private ScaleGestureDetector mScaleDetector;
	private boolean scaling = false;
	private float mLastTouchY;
	private float mLastTouchX;
	private Display mDisplay;

	private RayCastRendererView rayCastRenderer;	
	private boolean hasRayCaster = false;
	
	private boolean clickStepping = false;
	private boolean followEdges = false;
	private boolean allowRotation;
	private Graph graph;        
	private Vertex selectedVertex;
	ArrayList<Edge> route = new ArrayList<Edge>();
	private double routeLength = 0;
	
	private Mode mode = Mode.NORMAL;
	public enum Mode {
		NORMAL, WAYPOINT, GRAPH, POI
	}

	public MapView(Context context, Bitmap pic, Graph graph) {
		super(context);

		this.map = new Map(pic);
		this.graph = graph;
		
		Rect screen = new Rect();
		screen.bottom = this.getHeight();
		screen.right = this.getWidth();
		ms = new MapScreen(screen);
	    
	    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_UI);
		
		this.setOnClickListener(clickListener);
	    
	    this.setOnLongClickListener(longClickListener);
	    
	    WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    mDisplay = mWindowManager.getDefaultDisplay();
	    
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
		this.map.clearWaypoints();
		this.route.clear();
		routeLength = 0;
		invalidate();
    }
    
    public void setMode(Mode mode) {
    	this.mode = mode;
    	switch (mode) {
    		case GRAPH: {
    			allowRotation = false;
    			ms.setAngle(-90 * mDisplay.getOrientation());
    			break;
    		}
    		default: {
    			allowRotation = true;
    		}
    	}
    	invalidate();
    }

	public void route() {
		if (map.waypoints.size() > 1) {
			this.route.clear();
			routeLength = 0;
			for (int i=0; i<map.waypoints.size()-1; i++) {
				graph.clearReferrences();
				AStar aStar = new AStar(graph);
				Vertex start = map.waypoints.get(i);
				Vertex goal = map.waypoints.get(i+1);
				route.addAll(aStar.getRoute(start, goal));
			}
			Edge prevEdge = null;
			for (Edge e : route) {
				if(prevEdge != null){
					double angle = prevEdge.angle(e);
					Log.i("MapView", "Angle: " + angle);
					
				}
				prevEdge = e;
				
				routeLength  += e.length;
			}
			invalidate();
		}
	}
    
   
    
	
	@Override
    public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	    canvas.save();
	    
	    if (followEdges) {
	    	centerOnNearestEdge();
	    }
	    ms.updateMapToScreenMatrix(this.getWidth(), this.getHeight());
	    int[] screenCenter = ms.getScreenCenter();
	    float[] position = ms.getPosition();

        
        map.draw(canvas, ms.getMapToScreenMatrix(), ms.getScale());
		
        // bullseye at center
	    Paint paint = new Paint();
	    paint.setColor(Color.RED);
	    canvas.drawCircle(screenCenter[0], screenCenter[1], 1, paint);
	    paint.setStyle(Paint.Style.STROKE);
	    canvas.drawCircle(screenCenter[0], screenCenter[1], 3, paint);
	    canvas.drawCircle(screenCenter[0], screenCenter[1], 5, paint);
	    
	    // debug overlay
	    String debug = String.format("angle: %.2f, zoom: %.3f (x,y): (%.1f,%.1f), route length: %.1fm", ms.getAngle(), ms.getScale(), position[0], position[1], routeLength/2);
	    if(scaling){
			debug += "[SCALING] ";
		}
		canvas.drawText(debug, 10, 10, paint);
		
		// draw graph
		if (mode == Mode.GRAPH) {
			// vertices
			paint.setColor(Color.rgb(0, 127, 0));
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			for (Integer key : graph.vertices.keySet()) {
				Vertex v = graph.vertices.get(key);
				float[] coords = ms.mapToScreenCoords(v.x, v.y);
				canvas.drawCircle(coords[0], coords[1], ms.getScale(), paint);
			}
			// edges
			for (Integer key : graph.edges.keySet()) {
				Edge e = graph.edges.get(key);
				Vertex startVertex = e.startVertex;
				float[] start = ms.mapToScreenCoords(startVertex.x, startVertex.y);
				Vertex endVertex = e.endVertex;
				float[] end = ms.mapToScreenCoords(endVertex.x, endVertex.y);
				canvas.drawLine(start[0], start[1], end[0], end[1], paint);
			}
		}
		
		// route
		Edge prevEdge = null;
		boolean above = true;
		int i = 1;
		

		paint.setColor(Color.YELLOW);
		paint.setTextSize(10+ms.getScale());
		for (Edge e : route) {

			
			Vertex startVertex = e.startVertex;
			float[] start = ms.mapToScreenCoords(startVertex.x, startVertex.y);
			Vertex endVertex = e.endVertex;
			float[] end = ms.mapToScreenCoords(endVertex.x, endVertex.y);
			canvas.drawLine(start[0], start[1], end[0], end[1], paint);
			
			// only show directions when resolution shown > 1 meter per pixel
			if (ms.getScale() > 1 / map.pixelLength) {
				double distance = Math.round(e.length) * map.pixelLength;
				float[] midLine = { start[0] + (end[0] - start[0]) / 2, start[1] + (end[1] - start[1]) / 2};
				canvas.drawText(distance + "m", midLine[0] + 10, midLine[1], paint);
				
				// For routes with more than 2 markers, results are useless
				//if(map.markers.size() == 2){
					if(prevEdge != null){
						double angle = prevEdge.angle(e);
						Log.i("MapView", "Angle: " + angle);
						Direction d = prevEdge.direction(e);
						
						if(d != Direction.STRAIGHT){
							//canvas.drawText("(" + i + ")" + Math.floor(angle) + "°", end[0], above ? end[1] - 10 : end[1] + 10, paint);
							canvas.drawText("(" + i + ")" + (d == Direction.LEFT ? "Left" : "Right") , end[0], above ? end[1] - 10 : end[1] + 10, paint);
							above = !above;
							i += 1;
						}
					}
					prevEdge = e;				
				//}
			}
		}
		
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
        	if (allowRotation) {
	        	float _heading = event.values[0];
	        	// make the heading compatible to the transform matrix:
	        	// 1. invert sign of heading to turn the other way
	        	// 2. take into account screen orientation
	        	float angleNew = -_heading - 90*mDisplay.getOrientation();
	        	// to smooth the rotation, only rotate if angle changes significantly
	        	if (Math.abs(ms.getAngle() - angleNew) > 0.3) {
	        		ms.setAngle(angleNew);
	                invalidate();
	        	}
        	} else {
        		ms.setAngle(-90 * mDisplay.getOrientation());
        	}
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    
    
    private OnLongClickListener longClickListener = new OnLongClickListener() {
		public boolean onLongClick(View v) {
			selectedVertex = null;
        	if (mode == Mode.WAYPOINT) {
				float[] coords = ms.screenToMapCoords(mLastTouchX, mLastTouchY);
	        	Vertex vertex = graph.nearestVertex((int)coords[0], (int)coords[1]);
	        	if (vertex != null) {
	        		map.addWaypoint(vertex);
	        		invalidate();
	        	}
	        	route();
        	}
        	else if (mode == Mode.GRAPH) {
        		float[] coords = ms.screenToMapCoords(mLastTouchX, mLastTouchY);
	        	Vertex vertex = graph.nearestVertex((int)coords[0], (int)coords[1]);
	        	if (vertex != null) {
	        		map.clearWaypoints();
	        		map.addWaypoint(vertex);
	        		selectedVertex = vertex;
	        		invalidate();
	        	} else {
	        		map.clearWaypoints();
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
				float dy = -(stepLength / map.pixelLength);
				
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
		for (Edge edge : graph.edges.values()) {
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
