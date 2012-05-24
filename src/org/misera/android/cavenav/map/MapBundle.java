package org.misera.android.cavenav.map;

import android.graphics.Bitmap;


/*
 * Contains the resources that belong together: the map, graph, routing, POIs...
 */
public class MapBundle {
	
	public String mapName;
	public MapComponent map;
	public GraphComponent graph;
	public RouteComponent route;
	public CenterMarkerComponent centerMarker;
	public POIComponent poi;
	public double pixelLength; // length in meters of one pixel in pic
	
	public MapBundle(String mapName, Bitmap pic, double pixelLength) {
		this.mapName = mapName;
		this.map = new MapComponent(pic);
		this.pixelLength = pixelLength;
		this.centerMarker = new CenterMarkerComponent();
		this.poi = new POIComponent(mapName);
		this.poi.readFile();
	}
	
	public void initGraph(String json) {
		this.graph = new GraphComponent(json);
		this.route = new RouteComponent(graph, this.pixelLength);
	}

}
