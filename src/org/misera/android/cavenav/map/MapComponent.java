package org.misera.android.cavenav.map;

import java.util.ArrayList;

import org.misera.android.cavenav.graph.Vertex;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class MapComponent {
	
	private Bitmap pic;
    public ArrayList<Vertex> waypoints;
	public float pixelLength = 0.5f;

	public MapComponent(Bitmap pic) {
		this.pic = pic;
	}
	
	public Canvas draw(Canvas canvas, MapScreen ms) {
		// draw pic
		canvas.drawBitmap(pic, ms.getMapToScreenMatrix(), null);
		return canvas;
	}
	
		
}
