package org.misera.android.cavenav.graph;

public class Edge {

	public Vertex startVertex;
	public Vertex endVertex;
	public double length;
	
	public enum Direction{
		RIGHT, LEFT, STRAIGHT
		
	}

	public Edge(Vertex startVertex, Vertex endVertex) {
		// Since the input data is a directed graph, 
		// and the data was not gathered in such a way that 
		// for two adjacent edges, e1.start = e2.end
		// we need to make sure that startVertex is always before
		// than endVertex so that calculating angles etc does not
		// give strange results
		
		// This obviously does not work so I guess we have to fix this in the planned version of the graph
		
		double startDistance = Math.sqrt(startVertex.x * startVertex.x + startVertex.y * startVertex.y);
		double endDistance = Math.sqrt(endVertex.x * endVertex.x + endVertex.y * endVertex.y);
		
		if(startDistance < endDistance){
			this.startVertex = startVertex;
			this.endVertex = endVertex;		
		}
		else{
			this.startVertex = endVertex;
			this.endVertex = startVertex;
		}
		// length is in pixels, not meters!
		this.length = Graph.getDistance(startVertex, endVertex);
	}
	
	public double angle(Edge e){
		double[] thisVector = {endVertex.x - startVertex.x, endVertex.y - startVertex.y};
		double[] thatVector = {e.endVertex.x - e.startVertex.x, e.endVertex.y - e.startVertex.y};
		
		double dotProduct = thisVector[0] * thatVector[0] + thisVector[1] * thatVector[1];
		
		//double angle = Math.acos(dotProduct / (this.length * e.length)); //Gives result between 0 and 180. use atan2 version instead
		double angle = Math.atan2(thatVector[1], thatVector[0]) - Math.atan2(thisVector[1], thisVector[0]);
		
		// convert to value between 0 and 2pi
		if(angle > Math.PI){
			//angle -= 2 * Math.PI;
		}
		else if(angle < Math.PI){
			//angle += 2 * Math.PI;
		}
		double angleDeg = angle * 180 / Math.PI;
		return angleDeg;
		
	}
	
	public Direction direction(Edge e){
		double straightThreshold = 20; //everything within +- threshold counts as straight
		
		double angle = this.angle(e);
		
		if(Math.abs(angle) <= straightThreshold){
			return Direction.STRAIGHT;
		}
		else{
			if(angle < 0){
				return Direction.LEFT;
			}
			else if(angle > 0){
				return Direction.RIGHT;
			}
		}
		return Direction.STRAIGHT;
	}


}
