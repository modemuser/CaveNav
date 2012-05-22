package org.misera.android.cavenav;

import java.util.ArrayList;

import org.misera.android.cavenav.graph.AStar;
import org.misera.android.cavenav.graph.Edge;
import org.misera.android.cavenav.graph.Graph;
import org.misera.android.cavenav.graph.Vertex;
import org.misera.android.cavenav.graph.Edge.Direction;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class RouteComponent {
	
	public ArrayList<Vertex> waypoints;
	public ArrayList<Edge> path = new ArrayList<Edge>();
	public double length = 0;
	private double pixelLength;
	private Graph graph;

	public RouteComponent(GraphComponent graph, double pixelLength) {
		this.graph = graph;
		this.waypoints = new ArrayList<Vertex>();
		this.pixelLength = pixelLength;
	}
	
	public Canvas draw(Canvas canvas, MapScreen ms) {
		Paint paint = new Paint();
		paint.setColor(Color.YELLOW);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setTextSize(24);
		
		// waypoints
		for (Vertex p : waypoints) {
			float[] coords = ms.mapToScreenCoords(p.x, p.y);
			canvas.drawCircle(coords[0], coords[1], 5*ms.getScale(), paint);
		}
		
		if (waypoints.size() < 2) {
			return canvas;
		}
		
		// route length overlay
		String debug = String.format("route length: %.0fm", this.length);
		canvas.drawText(debug, 5, ms.getScreenCenter()[1]*2 - 5, paint);
		
		
		// edges with directions
		Edge prevEdge = null;
		boolean above = true;
		int i = 1;
		paint.setTextSize(10+ms.getScale());
		for (Edge e : path) {
			Vertex startVertex = e.startVertex;
			float[] start = ms.mapToScreenCoords(startVertex.x, startVertex.y);
			Vertex endVertex = e.endVertex;
			float[] end = ms.mapToScreenCoords(endVertex.x, endVertex.y);
			canvas.drawLine(start[0], start[1], end[0], end[1], paint);
			
			// only show directions when resolution shown > 1 meter per pixel
			if (ms.getScale() > 1 / pixelLength) {
				double distance = Math.round(e.length) * pixelLength;
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
		return canvas;
	}

	public void addWaypoint(Vertex v) {
		this.waypoints.add(v);
	}

	public void clear() {
		this.waypoints.clear();
		this.path.clear();
		this.length = 0;
	}
	
	public void find() {
		if (waypoints.size() > 1) {
			this.path.clear();
			length = 0;
			for (int i=0; i<waypoints.size()-1; i++) {
				graph.clearReferrences();
				AStar aStar = new AStar(graph);
				Vertex start = waypoints.get(i);
				Vertex goal = waypoints.get(i+1);
				path.addAll(aStar.getRoute(start, goal));
			}
			Edge prevEdge = null;
			for (Edge e : path) {
				if(prevEdge != null){
					double angle = prevEdge.angle(e);
					Log.i("MapView", "Angle: " + angle);
					
				}
				prevEdge = e;
				
				length  += e.length;
			}
			length *= this.pixelLength;
		}
	}
	
}
