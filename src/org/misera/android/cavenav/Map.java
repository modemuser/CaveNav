package org.misera.android.cavenav;

import java.util.ArrayList;

import org.misera.android.cavenav.graph.Vertex;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

public class Map {
	
	private Bitmap pic;
    public ArrayList<Vertex> waypoints;

	public Map(Bitmap pic) {
		this.pic = pic;
		this.waypoints = new ArrayList<Vertex>();
	}
	
	public Canvas draw(Canvas canvas, Matrix m, float scale) {
		// draw pic
		canvas.drawBitmap(pic, m, null);
		
		// draw markers
		Paint paint = new Paint();
		paint.setColor(Color.YELLOW);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		for (Vertex p : waypoints) {
			float[] coords = {p.x, p.y};
			m.mapPoints(coords);
			canvas.drawCircle(coords[0], coords[1], 5*scale, paint);
		}
		return canvas;
	}
	
	public void addWaypoint(Vertex v) {
		this.waypoints.add(v);
	}

	public void clearWaypoints() {
		this.waypoints.clear();
	}
		
}
