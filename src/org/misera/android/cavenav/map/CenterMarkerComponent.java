package org.misera.android.cavenav.map;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class CenterMarkerComponent {
	
	public Canvas draw(Canvas canvas, MapScreen ms) {
	    int[] c = ms.getScreenCenter();
	    Paint paint = new Paint();
	    paint.setColor(Color.RED);
	    canvas.drawCircle(c[0], c[1], 1, paint);
	    paint.setStyle(Paint.Style.STROKE);
	    canvas.drawCircle(c[0], c[1], 3, paint);
	    canvas.drawCircle(c[0], c[1], 5, paint);
	    return canvas;
	}
}
