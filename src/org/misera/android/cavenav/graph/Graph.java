package org.misera.android.cavenav.graph;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Graph {
	
	public HashMap<Integer,Edge> edges;
	public HashMap<Integer,Vertex> vertices;

	public Graph(String str) {
		try {
			JSONObject json = new JSONObject(str);
			this.vertices = readVertices(json);
			this.edges = readEdges(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * clearing referrences between vertices, 
	 * needed for reconstructing the path after A* 
	 * 
	 * also resetting f g h 
	 */
	public void clearReferrences() {
		for (Vertex v : vertices.values()) {
			v.f = Double.MAX_VALUE;
			v.g = Double.MAX_VALUE;
			v.h = Double.MAX_VALUE;
			v.prevOnRoute = null;
		}
	} 
	
	public Edge findEdge(Vertex v1, Vertex v2) {
		for (Integer key : edges.keySet()) {
			Edge e = edges.get(key);
			if ((e.startVertex == v1 || e.startVertex == v2) && (e.endVertex == v1 || e.endVertex == v2)) {
				return e;
			}
		}
		Log.e("CaveNav", String.format("No edge found with %s %s", v1, v2));
		return null;
	}
	
	public Vertex nearestVertex(int posX, int posY) {
		Vertex out = null;
		double distOut = Integer.MAX_VALUE;
		for (Integer key : vertices.keySet()) {
			Vertex v = vertices.get(key);
			if (out == null) {
				out = v;
				distOut = distance(posX, posY, out.x, out.y);
			} else {
				double distV = distance(posX, posY, v.x, v.y);
				if (distV < distOut) {
					out = v;
					distOut = distV;
				}
			}
		}
		if (distOut < 10) {
			return out;
		} else {
			return null;
		}
	}
	
	
	public static double getDistance(Vertex v1, Vertex v2) {
		return distance(v1.x, v1.y, v2.x, v2.y);
	}

	public static double distance(float x1, float y1, float x2, float y2) {
		float a = x1 - x2;
		float b = y1 - y2;
		return Math.sqrt(a*a + b*b);
	}

	private HashMap<Integer, Vertex> readVertices(JSONObject json) throws JSONException {
		HashMap<Integer,Vertex> vertices = new HashMap<Integer,Vertex>();
		JSONObject nodes = json.getJSONObject("nodes");
		Iterator<String> nodeIds = nodes.keys();
		while (nodeIds.hasNext()) {
			String id = nodeIds.next();
			JSONObject node = nodes.getJSONObject(id);
			JSONObject coords = node.getJSONObject("coordinates");
			int x = coords.getInt("x") - 7;
			int y = coords.getInt("y") - 52;
			Vertex newVertex = new Vertex(x, y);
			vertices.put(Integer.decode(id), newVertex);
		}
		return vertices;
	}
	
    private HashMap<Integer, Edge> readEdges(JSONObject json) throws JSONException {
    	HashMap<Integer,Edge> edges = new HashMap<Integer, Edge>();
		JSONObject edgesJson = json.getJSONObject("edges");
		Iterator<String> edgeIds = edgesJson.keys();
		while (edgeIds.hasNext()) {
			String edgeId = edgeIds.next();
			JSONObject endpoints = edgesJson.getJSONObject(edgeId).getJSONObject("nodes");
			Integer startVertexId = new Integer(endpoints.getInt("start"));
			Integer endVertexId = new Integer(endpoints.getInt("end"));
			
			if (vertices.containsKey(startVertexId) && vertices.containsKey(endVertexId)) {
				Vertex startVertex = vertices.get(startVertexId);
				Vertex endVertex = vertices.get(endVertexId);
				Edge newEdge = new Edge(startVertex, endVertex);
				startVertex.addEdge(newEdge);
				endVertex.addEdge(newEdge);
				Integer id = Integer.decode(edgeId);
				edges.put(id, newEdge);
			} else {
				// there seem to be non-existing vertex id referenced in the json
		    	Log.e("CaveNav", String.format("%s. vertex ids: (%d, %d)", edgeId, startVertexId, endVertexId));
			}
		}
		return edges;
	}

	public static float[] closestPointOnEdge(float x3, float y3, Edge edge) {
		int x1 = edge.startVertex.x;
		int y1 = edge.startVertex.y;
		int x2 = edge.endVertex.x;
		int y2 = edge.endVertex.y;
		
		double[] AP = {x3-x1, y3-y1};
		double[] AB = {x2-x1, y2-y1};
		double AB2 = AB[0] * AB[0] + AB[1] * AB[1];
		double AP_dot_AB = AP[0] * AB[0] + AP[1] * AB[1];
		double t = AP_dot_AB / AB2;
		// clamp to segment
		if (t < 0) { t = 0; }
		else if (t > 1) { t = 1; }
		
		double outX = x1 + AB[0] * t;
		double outY = y1 + AB[1] * t;
		float[] out = {(float) outX, (float) outY};
		return out;
	}

}
