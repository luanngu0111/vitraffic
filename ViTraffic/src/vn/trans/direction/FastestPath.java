package vn.trans.direction;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import vn.trans.ftpserver.ServerUtil;
import vn.trans.json.JSONParser;
import vn.trans.track.ResponseTrack;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;

/*
 * Fetch data from DB server
 * Create Adjacency Matrix from fetched data
 */

public class FastestPath {
	private static HashMap<String, Vertex> nodes;
	private static List<Edge> edges;
	public Context context;

	public FastestPath(Context context) {
		this.context = context;
	}

	public void fetchData() {
		// new FetchingData().execute();
		RequestQueue queue = Volley.newRequestQueue(context);

		String addr = IURLConst.URL_TRAFFIC;
		Log.v("url", addr);
		StringRequest strRequest = new StringRequest(Request.Method.GET, addr, new Listener<String>() {

			@Override
			public void onResponse(String arg0) {
				// TODO Auto-generated method stub
				android.os.Debug.waitForDebugger();
				Toast.makeText(context, arg0, Toast.LENGTH_LONG).show();
				Log.d("response", arg0.toString());

			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				// TODO Auto-generated method stub
				Toast.makeText(context, arg0.toString(), Toast.LENGTH_LONG).show();
				Log.v("response", arg0.toString());
			}
		});
		queue.add(strRequest);
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean status = false;
				ServerUtil ftpserver = ServerUtil.createServer();
				status = ftpserver.serverConnect(IConstants.USERNAME, IConstants.PASSWORD, IConstants.PORT);
				if (status == true) {
					Log.d("server", "Connection Success");
					ftpserver.Download("android.txt");
				} else {
					Log.d("server", "Connection failed");
				}
			}
		}).start();
		
	}

	public String getNearestLoc(LatLng src) {
		String node_id = null;
		try {
			node_id = new NearestNode().execute(new Double[] { src.latitude, src.longitude }).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node_id;
	}

	public List<LatLng> getFastestPath(LatLng src, LatLng dest) {
		// Fetching data at first
		fetchData();
		// Lets check from location Loc_1 to Loc_10
		/*Graph graph = Graph.createGraph(nodes, edges);
		Routing dijkstra = new Routing(graph);
		Vertex start = new Vertex("", "");
		Vertex end = new Vertex("", "");
		// find nearest src nodes nodes.lat < src.lat && nodes.lon <src.lon
		String start_node = getNearestLoc(src);
		// find nearest dest nodes nodes.lat < dest.lat && nodes.lon <dest.lon
		String end_node = getNearestLoc(dest);

		start = nodes.get(start_node);
		end = nodes.get(end_node);
		// Set source location
		dijkstra.execute(start);

		// Set destination location and get fastest path
		LinkedList<Vertex> path = dijkstra.getPath(end);
		List<LatLng> fastPath = new ArrayList<LatLng>();
		if (path != null) {
			for (Vertex vertex : path) {
				// System.out.println(vertex);
				fastPath.add(vertex.getLocation());
			}
			return fastPath;
		}*/
		return null;
	}

	private static void addLane(String laneId, String sourceLocNo, String destLocNo, double speed) {
		Edge lane = new Edge(laneId, nodes.get(sourceLocNo), nodes.get(destLocNo), speed);
		edges.add(lane);
	}

	public static class FetchingData extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... param) {
			// TODO Auto-generated method stub
			android.os.Debug.waitForDebugger();
			String urlTraffic = IURLConst.URL_TRAFFIC;
			JSONParser jParser = new JSONParser();
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			JSONObject json = jParser.makeHttpRequest(urlTraffic, "GET", params);
			double avg_speed = 0.0;
			String start = "", end = "";
			LatLng start_loc = null;
			nodes = new HashMap<String, Vertex>();
			edges = new ArrayList<Edge>();
			// Loop result to put vertex (nodes) value (id, name, coord)
			try {
				int success = json.getInt(IURLConst.TAG_SUCCESS);
				if (success == 1) {
					JSONArray traff_arr = json.getJSONArray(IURLConst.TAG_TRAFFIC);
					for (int i = 0; i < traff_arr.length(); i++) {
						JSONObject w = traff_arr.getJSONObject(i);
						start = w.getString(IURLConst.TAG_START);
						end = w.getString(IURLConst.TAG_END);
						start_loc = new LatLng(w.getDouble(IURLConst.TAG_START_LAT),
								w.getDouble(IURLConst.TAG_START_LON));
						new LatLng(w.getDouble(IURLConst.TAG_END_LAT), w.getDouble(IURLConst.TAG_END_LON));
						avg_speed = w.getDouble(IURLConst.TAG_AVG_SPEED);
						w.getInt(IURLConst.TAG_AMOUNT);
						Vertex location = new Vertex(start, "Node_" + start, start_loc);
						nodes.put(start, location);
						// Loop result array to put value (name, start, end,
						// avg_speed)
						addLane("Edge_" + start + end, start, end, avg_speed);
					}

				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

	private void addLane(String laneId, int sourceLocNo, int destLocNo, int duration) {
		Edge lane = new Edge(laneId, nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
		edges.add(lane);
	}

	public class NearestNode extends AsyncTask<Double, String, String> {

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
			return node_id;
		}

	}
}
