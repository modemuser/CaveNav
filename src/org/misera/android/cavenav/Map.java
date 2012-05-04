package org.misera.android.cavenav;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Map {
	
	private Bitmap pic;
    private Rect screen = new Rect();
	private Rect crop = screen;

	public Map(Bitmap pic, Rect screen) {
		this.pic = pic;
		this.screen = screen;
	}

	public void draw(Canvas canvas) {
		canvas.drawBitmap(pic, crop, screen, null);
	}

}
