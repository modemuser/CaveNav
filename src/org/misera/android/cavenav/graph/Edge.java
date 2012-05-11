package org.misera.android.cavenav.graph;

public class Edge {

	public Vertex startVertex;
	public Vertex endVertex;
	public double length;

	public Edge(Vertex startVertex, Vertex endVertex) {
		this.startVertex = startVertex;
		this.endVertex = endVertex;
		this.length = Graph.getDistance(startVertex, endVertex);
	}


}
