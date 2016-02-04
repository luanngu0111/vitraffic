package vn.trans.direction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.os.AsyncTask;
import android.util.Log;
import vn.trans.json.JSONParser;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;

public class FastPath {
	private final HashMap<String, Vertex> nodes;
	private final DefaultDirectedWeightedGraph<Vertex, DefaultWeightedEdge> graph;
	private static FastPath object;

	
	private FastPath() {
		graph = new DefaultDirectedWeightedGraph<Vertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		nodes = new HashMap<String, Vertex>();
		for (int j = 1; j <= IURLConst.NUM_FILES; j++) {
			String path = IConstants.ROOT_PATH + "/traffic" + j + ".json";
			File file = new File(path);
			String jsonStr = "{}";
			JSONObject json = null;
			try {
				jsonStr = IOUtils.toString(new FileInputStream(file));
				json = new JSONObject(jsonStr);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			double avg_speed = 0.0;
			String start = "", end = "";
			LatLng start_loc = null;
			LatLng end_loc = null;
			// Loop result to put vertex (nodes) value (id, name, coord)
			try {
				int success = 1;// json.getInt(IURLConst.TAG_SUCCESS);
				if (success == 1) {
					JSONArray traff_arr = json.getJSONArray(IURLConst.TAG_TRAFFIC);
					for (int i = 0; i < traff_arr.length(); i++) {
						JSONObject w = traff_arr.getJSONObject(i);
						start = w.getString(IURLConst.TAG_START);
						end = w.getString(IURLConst.TAG_END);
						start_loc = new LatLng(w.getDouble(IURLConst.TAG_START_LAT),
								w.getDouble(IURLConst.TAG_START_LON));
						end_loc = new LatLng(w.getDouble(IURLConst.TAG_END_LAT), w.getDouble(IURLConst.TAG_END_LON));
						avg_speed = w.getDouble(IURLConst.TAG_AVG_SPEED);
						//w.getInt(IURLConst.TAG_AMOUNT);
						Vertex start_vertex = new Vertex(start, "Node_" + start, start_loc);
						Vertex end_vertex = new Vertex(end, "Node_" + end, end_loc);
						if (!nodes.containsKey(end)) {
							nodes.put(end, end_vertex);
							graph.addVertex(end_vertex);
						}
						if (!nodes.containsKey(start)) {
							nodes.put(start, start_vertex);
							graph.addVertex(start_vertex);
						}
						Log.e("fetching", j+ " "+ start + " " + end);
						DefaultWeightedEdge edge = graph.addEdge(start_vertex, end_vertex);
						graph.setEdgeWeight(edge, avg_speed);

					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e("fetching", "traffic id file "+j+" "+e.getMessage());
			}
		}
	}

	public static FastPath createPath() {
		if (object == null) {
			object = new FastPath();
		}
		return object;

	}

	public List<LatLng> getFastPath(LatLng src, LatLng dest) {
		
		// find nearest src nodes nodes.lat < src.lat && nodes.lon <src.lon
		String start_node = getNearestLoc(src);
		// find nearest dest nodes nodeslat < dest.lat && nodes.lon <dest.lon
		String end_node = getNearestLoc(dest);
		Vertex startVertex = nodes.get(start_node);
		Vertex endVertex = nodes.get(end_node);
		List<LatLng> result = new ArrayList<LatLng>();
		List<DefaultWeightedEdge> paths = DijkstraShortestPath.findPathBetween(graph, startVertex, endVertex);
		if (paths == null)
			return null;
		for (int i = 0; i < paths.size(); i++) {
			DefaultWeightedEdge defaultEdge = paths.get(i);
			Vertex s = graph.getEdgeSource(defaultEdge);
			Vertex d = graph.getEdgeTarget(defaultEdge);
			result.add(s.getLocation());
			if (i == paths.size() - 1)
				result.add(d.getLocation());
		}
		return result;
	}

	public static String getNearestLoc(LatLng src) {
		String node_id = null;
		try {
			node_id = new NearestNode().execute(new Double[]{ src.latitude, src.longitude }).get();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node_id;
	}

	public static class NearestNode extends AsyncTask<Double, String, String> {

		@Override
		protected String doInBackground(Double... param) {
			// TODO Auto-generated method stub
			JSONParser jParser = new JSONParser();
			String urlFindNode = IURLConst.URL_NEAREST_NODE;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("lat", String.valueOf(param[0])));
			params.add(new BasicNameValuePair("lon", String.valueOf(param[1])));

			JSONObject json = jParser.makeHttpRequest(urlFindNode, "GET", params);
			String node_id = "";
			try {
				int success = json.getInt("success");
				if (success == 1) {
					node_id = json.getString("node_id");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("nearnode", node_id);
			return node_id;
		}

	}

}
