package org.misera.android.cavenav.map;

public class RotationDamper {
	
	private static final float EPSILON = 0.1f;
	private float physicalHeading;
	private float lastHeading;

	public RotationDamper() {
	}
	
	public void setHeading(float heading) {
		this.physicalHeading = heading;
	}
	
	public float getHeading() {
		float degreesToGo = Math.abs(physicalHeading - lastHeading);
		if (degreesToGo < EPSILON) {
			return physicalHeading;
		} 
		else if (degreesToGo > 90) {
			lastHeading = physicalHeading;
			return lastHeading;
		}
		else {
			if (physicalHeading > lastHeading) {
				lastHeading += 0.1 * degreesToGo;
			} else {
				lastHeading -= 0.1 * degreesToGo;
			}
			return lastHeading;
		}
	}

}
