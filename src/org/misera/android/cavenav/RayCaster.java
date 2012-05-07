package org.misera.android.cavenav;

import java.util.*;

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
	
	public Hashtable[] castRays(){
		Hashtable[] out = new Hashtable[res[0]];
		
		double angle = this.viewingAngle - FOV/2;
		for(int i = 0; i < this.res[0]; i++){
			angle += i * this.column;
			
			Hashtable wallPiece = castRay(angle);
			out[i] = wallPiece;
		}
		
		return out;
	}
	
	private Hashtable castRay(double angle){
		
		Hashtable<String, Object> out = new Hashtable<String, Object>();
		double distance = 0;
		int id = 0;
		
		int[] playerCenter = getUnitCoordinates(playerPos);
		
		boolean upward = angle < 180;
		boolean right = angle < 90 || angle > 270;

		int[] a = new int[2];
		int Ya;
		int Xa;
		
		if(upward){
			a[1] = (int) Math.floor(playerCenter[1] / 64) * 64 - 1;
			Ya = -gridSize;
		}
		else{
			a[1] = (int) Math.floor(playerCenter[1] / 64) * 64 + 64;
			Ya = gridSize;
		}
		
		a[0] = (int) (playerCenter[0] + ((playerCenter[1] - a[1]) / Math.tan(degToRad(angle))));
			
		Xa = (int) (Ya / Math.tan(degToRad(angle)));
		
		int[] unitCoords = { a[0], a[1]  };
		int[] gridCoords = { unitCoords[0] / 64, unitCoords[1] / 64 };
		boolean inBounds = map.length > gridCoords[1] && map[0].length > gridCoords[0];
		boolean collisionFound = false;
		
		while(inBounds && !collisionFound){
			collisionFound = map[gridCoords[1]][gridCoords[0]] != RayCaster.EMPTY;
			
			unitCoords[0] = unitCoords[0] + Xa;
			unitCoords[1] = unitCoords[1] + Ya;
			
			gridCoords[0] = unitCoords[0] / 64;
			gridCoords[1] = unitCoords[1] / 64;
			
			inBounds = map.length > gridCoords[1] && map[0].length > gridCoords[0];
		}
		
		if(!collisionFound){
			id = RayCaster.BOUNDARY;
		}
		else{
			id = map[gridCoords[1]][gridCoords[0]];
		}
		
		
		out.put("distance", distance);
		out.put("id", id);
		
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
	
}
	

