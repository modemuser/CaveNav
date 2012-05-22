package org.misera.android.cavenav;

import android.graphics.Bitmap;


/*
 * Contains the resources that belong together: the map, graph, routing, POIs...
 */
public class MapBundle {
	
	public MapComponent map;
	public GraphComponent graph;
	public RouteComponent route;
	public double pixelLength; // length in meters of one pixel in pic
	
	public MapBundle() {
	}
	
	public void initMap(Bitmap pic, double pixelLength) {
		this.map = new MapComponent(pic);
		this.pixelLength = pixelLength;
	}
	
	public void initGraph(String json) {
		this.graph = new GraphComponent(json);
		this.route = new RouteComponent(graph, this.pixelLength);
	}

}
