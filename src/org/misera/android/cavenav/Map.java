package org.misera.android.cavenav;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

public class Map {
	
	private Bitmap pic;
	private float rotX;
	private float rotY;
	private float angle = 0;
	private float focusX;
	private float focusY;
	private float scale = 1;
	private float transX = 0;
	private float transY = 0;

	public Map(Bitmap pic) {
		this.pic = pic;
	}

	public void setRotation(double angle, double rotX, double rotY) {
		this.angle = (float) angle;
		this.rotX = (float) rotX;
		this.rotY = (float) rotY;
	}
	
	public void setScaling(double scale, double focusX, double focusY) {
		this.scale = (float) scale;
		this.focusX = (float) focusX;
		this.focusY = (float) focusY;
	}

	public void setTranslation(double transX, double transY) {
		this.transX = (float) transX;
		this.transY = (float) transY;
	}
		
	public void draw(Canvas canvas) {
		canvas.drawBitmap(pic, getMatrix(), null);
	}
	
	public float[] toMapCoords(float[] input) {
		//getMatrix().mapPoints(input);
		return input;
	}
	
	private Matrix getMatrix() {
		Matrix matrix = new Matrix();
		
		Matrix scaleMatrix = new Matrix();
		//scaleMatrix.postTranslate(-focusX, -focusY);
	    scaleMatrix.postScale(scale, scale, focusX, focusY);
		//scaleMatrix.postTranslate(focusX, focusY);
		

		Matrix rotationMatrix = new Matrix();
	    rotationMatrix.postRotate(angle, rotX-transX, rotY-transY);
	    

		Matrix translationMatrix = new Matrix();
		translationMatrix.postTranslate(transX, transY);
		
		matrix.postConcat(scaleMatrix);
		matrix.postConcat(rotationMatrix);
		matrix.postConcat(translationMatrix);
		return matrix;
	}
}
