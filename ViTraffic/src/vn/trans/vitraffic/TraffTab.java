package vn.trans.vitraffic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import vn.trans.entities.Road;
import vn.trans.traff.AlarmDownloadService;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;

public class TraffTab extends FragmentActivity
		implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	private static GoogleMap map;
	boolean onCurrentTab = false;
	GoogleApiClient mGoogleApiClient;
	boolean mRequestingLocationUpdates = true;
	boolean init = true;
	private AlarmManager alarmMan;
	private PendingIntent mAlarmIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traff_tab);
		mRequestingLocationUpdates = true;
		initilizeMap();
		buildGoogleApiClient();
		createLocationRequest();
		init = true;

		Intent launchIntent = new Intent(this, AlarmDownloadService.class);
		mAlarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, launchIntent, 0);

	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.traff_tab, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initilizeMap() {
		// if (map == null) {
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maptraffic)).getMap();
		Log.i("Tag", "map");
		map.setMyLocationEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);
		map.clear();
		CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(10.7804, 106.6896));
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

		map.moveCamera(center);
		map.animateCamera(zoom);
		// check if map is created successfully or not
		if (map == null) {
			Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}

	}

	public void GetTrafficUpdate() {
		// RequestQueue mQueue = Volley.newRequestQueue(this);
		// String url = IURLConst.URL_GET_TRAFFIC ;
		// StringRequest jsReq = new StringRequest(Request.Method.GET, url, new
		// Listener<String>() {
		//
		// @Override
		// public void onResponse(String arg0) {
		// // TODO Auto-generated method stub
		// Log.i("Traffic", "Update data " + arg0);
		// }
		// }, new ErrorListener() {
		//
		// @Override
		// public void onErrorResponse(VolleyError arg0) {
		// // TODO Auto-generated method stub
		// Log.i("Traffic", "Update Error :" + arg0.getMessage());
		// }
		// });
		// mQueue.add(jsReq);
		String url = IURLConst.URL_GET_TRAFFIC_PERIOD;
		URL obj;
		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// add request header

			int responseCode = con.getResponseCode();
			Log.i("Traffic", "Update data :" + responseCode);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		if (mGoogleApiClient.isConnected() == false) {
			mGoogleApiClient.connect();
		}
		if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
			startLocationUpdates();
		}
		// new LoadTraffic().execute((Void) null);
		alarmMan = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmMan.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 2000,
				IConstants.ALARM_INTERVAL, mAlarmIntent);
		
		// loadTrafficLayer();
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stup

		super.onStop();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	private static int getColor(double speed) {
		if (speed >= 0 && speed < 5)
			return IConstants.RED_CODE;
		if (speed >= 5 && speed < 15)
			return IConstants.ORANGE_CODE;
		if (speed >= 15 && speed < 30)
			return IConstants.BLUE_CODE;

		return IConstants.GREEN_CODE;
	}

	/**
	 * Thuc hien ve mot doan duong dua vao thong tin danh sach toa do va van
	 * toc.
	 * 
	 * @param road
	 */
	@SuppressWarnings("null")
	public static void DrawTrafficRoad(Road pre, Road road) {
		int color = 0;
		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;

		List<LatLng> paths = new ArrayList<LatLng>();
		Road preroad = pre;
		// for (int i = 0; i < roads.size() - 1; i++) {
		// preroad = roads.get(i);
		// road = roads.get(i + 1);
		// PolylineOptions pol = new PolylineOptions();
		paths.clear();
		if (road != null) {
			int speed = (int) road.getAvg_speed();

			color = getColor(speed);

			LatLng start = pre.getPos_end();
			LatLng end = road.getPos_end();
			LatLng sideStart = null, sideEnd = null;
			sideStart = new LatLng(start.latitude, start.longitude);
			sideEnd = new LatLng(end.latitude, end.longitude);
			boolean neg = false;
			neg = end.latitude < start.latitude;

			if (neg) {
				sideStart = new LatLng(start.latitude, start.longitude - IConstants.AUT_LONG);
				sideEnd = new LatLng(end.latitude, end.longitude - IConstants.AUT_LONG);
			} else {
				sideStart = new LatLng(start.latitude, start.longitude + IConstants.AUT_LONG);
				sideEnd = new LatLng(end.latitude, end.longitude + IConstants.AUT_LONG);
			}

			if (bounds.contains(end)) // Kiem tra toa do co nam
			// trong
			// vung ban do dang hien thi
			// hay
			// khong
			{

				// pol.add(sideStart, sideEnd).width(6).color(color);
				map.addPolyline(new PolylineOptions().add(sideStart, sideEnd).width(6).color(color)).setVisible(true);
				// map.addPolyline(pol).setVisible(true);
				Log.v("draw", end.latitude + " " + end.longitude);
			}

			// Log.v("draw", "finish 1 line" + roads.size());
		}
	}

	/*
	 * public void DrawTrafficStatus(Road[] roads) { int color = 0; LatLng start
	 * = new LatLng(0, 0); LatLng end = new LatLng(0, 0); if (roads != null) {
	 * for (Road road : roads) { List<LatLng> paths = new ArrayList<LatLng>();
	 * if (road == null) continue; paths = road.getArr_paths(); if (paths !=
	 * null && paths.size() > 0) { start = paths.get(0); int speed = (int)
	 * road.getAvg_speed(); if (speed > 20) { color =
	 * IConstants.COLORS[IConstants.COLORS.length - 1]; } else { color =
	 * IConstants.COLORS[speed]; } for (int i = 1; i < paths.size(); i++) { end
	 * = paths.get(i); Polyline line = map.addPolyline(new
	 * PolylineOptions().add(start, end).width(8).color(color));
	 * line.setVisible(true); Log.v("draw", "Drawing"); } } } } }
	 */

	LocationRequest mLocationRequest;

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(IConstants.INTERVAL);
		mLocationRequest.setFastestInterval(IConstants.FAST_INTV);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	// Dang ky gui request toa do vi tri cua user.
	private void startLocationUpdates() {
		// TODO Auto-generated method stub
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onLocationChanged(android.location.Location location) {
		// TODO Auto-generated method stub
		if (init) {
			CameraPosition camPos = new CameraPosition.Builder()
					.target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15)
					.bearing(location.getBearing()).build();
			CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
			map.animateCamera(camUpd3);
			init = false;
		}

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Log.d("connect", "Connected");
		if (mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	public static class LoadTraffic extends AsyncTask<Void, Road, Void> {
		List<Road> bag_roads = new ArrayList<Road>();
		public void GetTrafficUpdate() {
			String url = IURLConst.URL_GET_TRAFFIC_PERIOD;
			URL obj;
			try {
				obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				// optional default is GET
				con.setRequestMethod("GET");

				// add request header

				int responseCode = con.getResponseCode();
				Log.i("Traffic", "Update data :" + responseCode);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void readFileFromURL() {
			URL url;
			for (int i = 1; i <= IURLConst.NUM_FILES; i++) {
				Log.i("LoadTraff", i + "");
				try {
					url = new URL(IURLConst.URL_TRAFFIC + "map" + i + ".json");
					List<Road> roads = new ArrayList<Road>();
					roads = Road.conv2Object(IOUtils.toString(url));
					bag_roads.addAll(roads);
					
					
					

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					Log.e("Traffic", "Invalid URL");
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("Traffic", "Unable to reach URL  ");
					e.printStackTrace();
				} finally {

				}
			}
			Collections.sort(bag_roads);
		}

		public void readFileFromSD() {
			GetTrafficUpdate();
			File fmap = null;
			for (int i = 1; (fmap = new File(IConstants.ROOT_PATH + "/map" + i + ".json")).exists(); i++) {
				Log.i("LoadTraff", i + "");

				try {
					String jsonStr = IOUtils.toString(new FileInputStream(fmap));
					List<Road> roads = new ArrayList<Road>();
					roads = Road.conv2Object(jsonStr);
					Road pre = null;
					Collections.sort(roads);
					for (Road road : roads) {
						if (pre == null) {
							pre = new Road();
							pre = road;
						}
						if (pre.getDirect() / 100 != road.getDirect() / 100)
							pre = road;
						Log.i("Traffic", road.getDirect() + " " + road.getTimestamps());
						publishProgress(new Road[] { pre, road });
						pre = road;
					}

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(Void... param) {
			// TODO Auto-generated method stub
			// android.os.Debug.waitForDebugger();
			GetTrafficUpdate();
			readFileFromURL();
			return null;
		}

//		@Override
//		protected void onProgressUpdate(Road... values) {
//			// TODO Auto-generated method stub
//			DrawTrafficRoad(values[0], values[1]);
//			super.onProgressUpdate(values);
//		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			Road pre = null;
			for (Road road : bag_roads) {
				if (pre == null) {
					pre = new Road();
					pre = road;
				}
				if (pre.getDirect() / IConstants.SUB_COEF != road.getDirect() / IConstants.SUB_COEF)
					pre = road;
				Log.i("Traffic", road.getDirect() + " " + road.getTimestamps());
				DrawTrafficRoad(pre, road );
				pre = road;
			}
			super.onPostExecute(result);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

}
