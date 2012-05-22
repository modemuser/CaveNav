package org.misera.android.cavenav.map;

import org.misera.android.cavenav.graph.Edge;
import org.misera.android.cavenav.graph.Graph;
import org.misera.android.cavenav.graph.Vertex;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class GraphComponent extends Graph{
	
	public GraphComponent(String json) {
		super(json);
	}
	
	public Canvas draw(Canvas canvas, MapScreen ms) {
		Paint paint = new Paint(); 
		paint.setColor(Color.rgb(0, 127, 0));
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		// vertices
		for (Integer key : this.vertices.keySet()) {
			Vertex v = this.vertices.get(key);
			float[] coords = ms.mapToScreenCoords(v.x, v.y);
			canvas.drawCircle(coords[0], coords[1], ms.getScale(), paint);
		}
		// edges
		for (Integer key : this.edges.keySet()) {
			Edge e = this.edges.get(key);
			Vertex startVertex = e.startVertex;
			float[] start = ms.mapToScreenCoords(startVertex.x, startVertex.y);
			Vertex endVertex = e.endVertex;
			float[] end = ms.mapToScreenCoords(endVertex.x, endVertex.y);
			canvas.drawLine(start[0], start[1], end[0], end[1], paint);
		}
		return canvas;
	}

}
