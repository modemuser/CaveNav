package org.misera.android.cavenav.map;

import android.graphics.Bitmap;


/*
 * Contains the resources that belong together: the map, graph, routing, POIs...
 */
public class MapBundle {
	
	public MapComponent map;
	public GraphComponent graph;
	public RouteComponent route;
	public CenterMarkerComponent centerMarker;
	public POIComponent poi;
	public double pixelLength; // length in meters of one pixel in pic
	
	public MapBundle(Bitmap pic, double pixelLength) {
		this.map = new MapComponent(pic);
		this.pixelLength = pixelLength;
		this.centerMarker = new CenterMarkerComponent();
		this.poi = new POIComponent();
	}
	
	public void initGraph(String json) {
		this.graph = new GraphComponent(json);
		this.route = new RouteComponent(graph, this.pixelLength);
	}

}
