package org.misera.android.cavenav;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

public class Map {
	
	private Bitmap pic;
    private ArrayList<Point> markers;

	public Map(Bitmap pic) {
		this.pic = pic;
		this.markers = new ArrayList<Point>();
	}
	
	public Canvas draw(Canvas canvas, Matrix m, float scale) {
		// draw pic
		canvas.drawBitmap(pic, m, null);
		
		// draw markers
		Paint paint = new Paint();
		paint.setColor(Color.YELLOW);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		for (Point p : markers) {
			float[] coords = {p.x, p.y};
			m.mapPoints(coords);
			canvas.drawCircle(coords[0], coords[1], 5*scale, paint);
		}
		return canvas;
	}
	
	public void addMarker(int mapPosX, int mapPosY) {
		this.markers.add(new Point(mapPosX, mapPosY));
	}

	public void clearMarkers() {
		this.markers.clear();
	}
		
}
