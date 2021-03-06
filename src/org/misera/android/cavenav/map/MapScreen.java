package org.misera.android.cavenav.map;

import android.graphics.Matrix;
import android.view.Display;

/*
 * This class deals with everything about the conversion between 
 * screen and map coordinate systems.
 */
public class MapScreen {
	
	private int screenCenterX;
	private int screenCenterY;
	private float posX;
	private float posY;
	private float angle;
    private float scaleFactor;
	private Matrix mapToScreenMatrix;
	private Matrix screenToMapMatrix;
	private Display display;
	private boolean allowRotation;
    
	public MapScreen(Display display) {
		this.display = display;
		angle = 0.f;
	    scaleFactor = 1.f;
		this.mapToScreenMatrix = new Matrix();
		this.screenToMapMatrix = new Matrix();
	}

	public void setDimensions(int width, int height) {		
		screenCenterX = width / 2;
		screenCenterY = height / 2;
	}
	
	public void setAllowRotation(boolean value) {
		this.allowRotation = value;
	}
		
	public float getAngle() {
		return this.angle;
	}
	
	public void setAngle(float heading) {
		// to make the angle compatible with the transform matrix:
    	// 1. invert sign of heading to turn the other way
		// 2. take screen orientation into account
		if (!allowRotation) {
			heading = 0;
		}
		this.angle = -heading - 90 * display.getOrientation();
	}

	public float getScale() {
		return this.scaleFactor;
	}
	
	public void setScale(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}
	
	public int[] getScreenCenter() {
		int[] out = {screenCenterX, screenCenterY};
		return out;
	}
	
	public float[] getPosition() {
		float[] out = {posX, posY};
		return out;
	}
	
	public Matrix getMapToScreenMatrix() {
		return this.mapToScreenMatrix;
	}
	

	/*
	 * move the map by dx, dy (in map pixels, but screen orientation :/ )
	 */
	public void move(float dx, float dy) {
		float[] transformed = rotateMovementScreenToMap(dx, dy);
		posX += transformed[0];
		posY += transformed[1];
	}
	
	public void centerOnMapPosition(float posX, float posY) {
		float[] p = mapToScreenCoords(posX, posY);
		float dx = p[0] - screenCenterX;
		float dy = p[1] - screenCenterY;
		float[] transformed = rotateMovementScreenToMap(dx, dy);
		this.posX += transformed[0] / scaleFactor;
		this.posY += transformed[1] / scaleFactor;
	}

	public float[] screenToMapCoords(float xScreen, float yScreen) {
		mapToScreenMatrix.invert(screenToMapMatrix);
		float[] out = {xScreen, yScreen};
		screenToMapMatrix.mapPoints(out);
		return out;
	}
	
	public float[] mapToScreenCoords(float xMap, float yMap) {
		float[] out = {xMap, yMap};
		mapToScreenMatrix.mapPoints(out);
		return out;
	}
	
	public void update() {
		mapToScreenMatrix.reset();
		
		/* 
			Convert the point that is C in the viewport coordinate system
			to map coordinate system and move it to (0,0)
		*/
		mapToScreenMatrix.setTranslate(-(screenCenterX + posX), -(screenCenterY + posY));
		
		/* 
			Now rotate the map around point (0,0) which 
			now corresponds to the viewport center
		*/
		mapToScreenMatrix.postRotate(angle, 0,0);		
		
		/* 
			Move map back so that only the (x,y) offset remains
		*/
		mapToScreenMatrix.postTranslate((screenCenterX), (screenCenterY));
		
		/*
		 	Scale around the C
		 */
		mapToScreenMatrix.postScale(scaleFactor, scaleFactor, screenCenterX, screenCenterY);
	}
	

	private float[] rotateMovementScreenToMap(float xMap,float yMap){
		
		float x = xMap;
		float y = yMap;
		
		// Convert x,y to polar coords

		double r = Math.sqrt(x*x + y*y);
		double theta = Math.atan2(y,x);

		// Rotate by angle
		double rotation = angle * Math.PI / 180;
		theta -= rotation;

		// Convert back to cartesian coords

		x = (float) (r * Math.cos(theta));
		y = (float) (r * Math.sin(theta));
		
		float[] returnArray = {x,y};
		return returnArray;
	}

	
}
