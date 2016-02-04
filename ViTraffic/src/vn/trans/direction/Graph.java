package vn.trans.direction;

import java.util.HashMap;
import java.util.List;

public class Graph {
	private final HashMap<String, Vertex> vertexes;
	private final HashMap<String, Edge> edges;
	private static Graph graph;

	private Graph(HashMap<String, Vertex> vertexes, HashMap<String, Edge>edges) {
		this.vertexes = vertexes;
		this.edges = edges;
	}

	public static Graph createGraph(HashMap<String, Vertex> vertexes, HashMap<String, Edge> edges) {
		if (graph == null) {
			graph = new Graph(vertexes, edges);
		}
		return graph;
	}

	public static Graph getGraph() {
		return graph;
	}

	public HashMap<String, Vertex> getVertexes() {
		return vertexes;
	}

	public HashMap<String, Edge> getEdges() {
		return edges;
	}
}
