package org.misera.android.cavenav.map;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class POIComponent {
	
	private ArrayList<POI> poiList;
	
	public POIComponent() {
		this.poiList = new ArrayList<POI>();
	}
	
	public void addPOI(int posX, int posY, String text) {
		POI poi = new POI(posX, posY, text);
		this.poiList.add(poi);
	}
	
	public Canvas draw(Canvas canvas, MapScreen ms) {
		Paint paint = new Paint();
		paint.setColor(Color.CYAN);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setTextSize(10 + ms.getScale());
		for (POI poi : poiList) {
			float[] coords = ms.mapToScreenCoords(poi.x, poi.y);
			canvas.drawCircle(coords[0], coords[1], ms.getScale(), paint);
			canvas.drawText(poi.text, coords[0]+5, coords[1]+3*ms.getScale(), paint);
		}
		return canvas;
	}
}
