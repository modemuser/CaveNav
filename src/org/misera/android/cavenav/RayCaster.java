package org.misera.android.cavenav;

import android.graphics.Bitmap;


public class RayCaster
{
	
	public static final int EMPTY = 0;
	public static final int WALL = 1;
	public static final int BOUNDARY = 2;

	private int[][] map;
	
	private int[] res = {320, 240};
	
	private int wallHeight = 64;
	private int wallWidth = 64;
	
	private int gridSize = 64;
	
	private double column;
	
	private int playerHeight = wallHeight / 2;
	
	private int[] playerPos = {0,0};
	
	private double viewingAngle = 0;
	private int FOV;
	
	
	
	public RayCaster(int[][] map){
		this.map = map;
		int[] defaultWallDimensions = {64,64};
		this.setWallDimensions(defaultWallDimensions);
		this.setFOV(60);
	
	}
	
	public void setResolution(int[] resolution){
		this.res = resolution;
		
		updateColumn();
	}
	
	public void setFOV(int FOV){
		this.FOV = FOV;
		updateColumn();
	}	
	
	public void setWallDimensions(int[] dimensions){
		this.wallWidth = dimensions[0];
		this.wallHeight = dimensions[1];
		this.playerHeight = wallHeight / 2;
	}
	
	public Ray[] castRays(){
		Ray[] out = new Ray[res[0]];
		
		double angle = this.viewingAngle - FOV/2;
		for(int i = 0; i < this.res[0]; i++){
			angle += i * this.column;
			
			Ray wallPiece = castRay(angle);
			out[i] = wallPiece;
		}
		
		return out;
	}
	
	private Ray castRay(double angle){
		
		double distance = 0;
		int id = 0;
		
		int[] playerCenter = getUnitCoordinates(playerPos);
		
		boolean upward = angle < 180;
		boolean right = angle < 90 || angle > 270;
		//boolean vertical = (angle > 315 || angle < 45) || (angle > 135 && angle < 225);
		

		// First horizontal intersection point
		int[] a = new int[2];
		// First vertical intersection point
		int[] b = new int[2];
		
		int YaH;
		int XaH;
		
		if(upward){
			a[1] = (int) Math.floor(playerCenter[1] / gridSize) * gridSize - 1;
			YaH = -gridSize;
		}
		else{
			a[1] = (int) Math.floor(playerCenter[1] / gridSize) * gridSize + gridSize;
			YaH = gridSize;
		}
		
		a[0] = (int) Math.floor((playerCenter[0] + ((playerCenter[1] - a[1]) / Math.tan(degToRad(angle)))));
			
		XaH = (int) Math.floor((YaH / Math.tan(degToRad(angle))));
		
		int[] gridCoords = new int[2];
		int[] unitCoords = new int[2];
		
		int[] unitCoordsH = { a[0], a[1]  };
		int[] gridCoordsH = { unitCoordsH[0] / gridSize, unitCoordsH[1] / gridSize };

		double distanceH = 0;
		
		boolean inBoundsH = map.length > gridCoordsH[1] && map[0].length > gridCoordsH[0];
		boolean collisionFoundH = false;
		
		while(inBoundsH && !collisionFoundH){
			collisionFoundH = map[gridCoordsH[1]][gridCoordsH[0]] != RayCaster.EMPTY;
			if(collisionFoundH){
				double dA = Math.pow(playerCenter[0] - unitCoordsH[0], 2);
				double dB = Math.pow(playerCenter[1] - unitCoordsH[1], 2);
				distanceH = Math.sqrt(dA + dB);
				break;	
			}	
			unitCoordsH[0] = unitCoordsH[0] + XaH;
			unitCoordsH[1] = unitCoordsH[1] + YaH;

			gridCoordsH[0] = unitCoordsH[0] / gridSize;
			gridCoordsH[1] = unitCoordsH[1] / gridSize;

			inBoundsH = map.length > gridCoordsH[1] && map[0].length > gridCoordsH[0];
		}
		
		
		int XaV;
		int YaV;
		
		if(right){
			b[0] = (int) Math.floor(playerCenter[0] / gridSize) * gridSize + gridSize;
		}
		else{
			b[0] = (int) Math.floor(playerCenter[0] / gridSize) * gridSize -1;
		}	
		b[1] = (int) Math.floor(playerCenter[1] + (playerCenter[0] - b[0]) * Math.tan(degToRad(angle)));
		
		XaV = right ? gridSize : -gridSize;
		YaV = (int) Math.floor(gridSize * Math.tan(degToRad(angle)));
		
		
		int[] unitCoordsV = { b[0], b[1]  };
		int[] gridCoordsV = { unitCoordsV[0] / gridSize, unitCoordsV[1] / gridSize };
		
		double distanceV = 0;
		
		boolean inBoundsV = map.length > gridCoordsV[1] && map[0].length > gridCoordsV[0];
		boolean collisionFoundV = false;
		
		while(inBoundsV && !collisionFoundV){
			collisionFoundV = map[gridCoordsV[1]][gridCoordsV[0]] != RayCaster.EMPTY;
			if(collisionFoundV){
				double dA = Math.pow(playerCenter[0] - unitCoordsH[0], 2);
				double dB = Math.pow(playerCenter[1] - unitCoordsH[1], 2);
				distanceV = Math.sqrt(dA + dB);
				break;	
			}	
			unitCoordsV[0] = unitCoordsV[0] + XaV;
			unitCoordsV[1] = unitCoordsV[1] + YaV;

			gridCoordsV[0] = unitCoordsV[0] / gridSize;
			gridCoordsV[1] = unitCoordsV[1] / gridSize;

			inBoundsH = map.length > gridCoordsV[1] && map[0].length > gridCoordsV[0];
		}
		
		
		if(!collisionFoundH && !collisionFoundV){
			id = RayCaster.BOUNDARY;
			distance = distanceV;
		}
		else{
			// Check if only one collision was found
			if(collisionFoundH ^ collisionFoundV){
				if(collisionFoundH){
					gridCoords = gridCoordsH;
					distance = distanceH;
				}
				else{
					gridCoords = gridCoordsV;
					distance = distanceV;
				}
			}
			else if(collisionFoundH && collisionFoundV){
				if(distanceH < distanceV){
					gridCoords = gridCoordsH;
					distance = distanceH;
				}
				else{
					gridCoords = gridCoordsV;
					distance = distanceV;
				}
			}
			
			id = map[gridCoords[1]][gridCoords[0]];
			// Remove distortion
			//distance = distance * Math.cos(
		}
		
		Ray out = new Ray(id, distance);
		
		return out;
	}
	
	
	private int[] getUnitCoordinates(int[] gridCoordinates){
		int [] unit = {gridCoordinates[0] * gridSize + (gridSize / 2), gridCoordinates[1] * gridSize + (gridSize / 2) }; 
		return unit;
	}
	
	private void updateColumn(){
		this.column = this.FOV / this.res[0];
	}
	
	private double playerPlaneDistance(){
		return (res[0] / 2) / Math.tan(FOV/2);
	}
	
	private double degToRad(double angle){
		return angle * Math.PI / 180;
	}
	
	public static int[][] bitmapToMap(Bitmap bitmap){
		int[][] map = new int[bitmap.getHeight()][bitmap.getWidth()];
		for(int y = 0; y < map.length; y++){
			for(int x = 0; x < map[y].length; x++){
				
			}
		}
		return map;
	}
}
	

