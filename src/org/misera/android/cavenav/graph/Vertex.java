package org.misera.android.cavenav.graph;

import java.util.ArrayList;

public class Vertex {

	private ArrayList<Edge> edges;
	public int x;
	public int y;
	

	public Vertex(int x, int y) {
		this.x = x;
		this.y = y;
		this.edges = new ArrayList<Edge>();
	}

	public void addEdge(Edge edge) {
		this.edges.add(edge);
	}

}
