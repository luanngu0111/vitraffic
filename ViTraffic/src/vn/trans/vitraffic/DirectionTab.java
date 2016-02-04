package vn.trans.vitraffic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import vn.trans.direction.FastPath;
import vn.trans.json.JSONParser;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;

public class DirectionTab extends FragmentActivity
		implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	private GoogleMap map;
	private Button bnLoad, bnSwap, bnClear;
	private static LatLng[] directions = new LatLng[2];
	private static int index = 0;
	GoogleApiClient mGoogleApiClient;
	boolean mRequestingLocationUpdates = true;
	LocationRequest mLocationRequest;
	boolean init = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_direction_tab);
		initilizeMap();
		buildGoogleApiClient();
		createLocationRequest();
		bnLoad = (Button) findViewById(R.id.bnLoad);
		bnLoad.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				GetFastestPath2(directions[0], directions[1]);
			}
		});
		bnSwap = (Button) findViewById(R.id.bnSwap);
		bnSwap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LatLng tmp = directions[0];
				directions[0] = directions[1];
				directions[1] = tmp;
			}
		});

		bnClear = (Button) findViewById(R.id.bnClear);
		bnClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (map != null) {
					map.clear();
				}
				index = 0;

			}
		});

	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(IConstants.INTERVAL);
		mLocationRequest.setFastestInterval(IConstants.FAST_INTV);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	private void initilizeMap() {
		// if (map == null) {
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapdirect)).getMap();
		Log.i("Tag", "map");
		// draw line b/w intial and final location.
		map.setMyLocationEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);

		map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(106, 10)));

		// check if map is created successfully or not
		if (map == null) {
			Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}

		map.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng position) {
				// TODO Auto-generated method stub
				if (index < 2) {
					directions[index++] = position;
					map.addMarker(new MarkerOptions().position(position).title("Somewhere").visible(true));
					Toast.makeText(getApplicationContext(), "Add marker " + index, Toast.LENGTH_SHORT).show();
				} else
					Toast.makeText(getApplicationContext(), "Cannot Add Marker" + directions.length, Toast.LENGTH_SHORT)
							.show();
			}
		});
		// }
	}

	private void displayResult(List<LatLng> points) {
		if (points != null) {
			PolylineOptions po = new PolylineOptions();
			po.addAll(points);
			map.addPolyline(po.color(0xff0000ff).visible(true));
			Log.i("fast", "Drew");
		} else {
			Toast.makeText(this, "Not found fastest way. Common way is recommended", Toast.LENGTH_LONG).show();
		}
	}

	// Need a function to get point in map

	/*
	 * Get fastest path function
	 */
	private void GetFastestPath(LatLng start, LatLng end) {
		FastPath fast = FastPath.createPath();
		List<LatLng> points;
		points = fast.getFastPath(start, end);
		displayResult(points);
		// fast.getFastPath(start, end);

	}

	/**
	 * @param start
	 *            id cua nut start
	 * @param end
	 *            id cua nut end
	 */
	private void GetFastestPath2(LatLng src, LatLng dest) {
		// find nearest src nodes nodes.lat < src.lat && nodes.lon <src.lon
		String start = FastPath.getNearestLoc(src);
		// find nearest dest nodes nodeslat < dest.lat && nodes.lon <dest.lon
		String end = FastPath.getNearestLoc(dest);
		List<LatLng> points;
		try {
			points = new FindPath().execute(new String[] { start, end }).get();
			displayResult(points);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // fast.getFastPath(start, end);

	}

	public class FindPath extends AsyncTask<Object, Object, List<LatLng>> {

		@Override
		protected List<LatLng> doInBackground(Object... params) {
			// TODO Auto-generated method stub

			List<NameValuePair> param = new ArrayList<NameValuePair>();
			param.add(new BasicNameValuePair("start", "Node_" + params[0].toString()));
			param.add(new BasicNameValuePair("end", "Node_" + params[1].toString()));

			JSONParser jParser = new JSONParser();
			JSONObject json = jParser.makeHttpRequest(IURLConst.URL_FAST_PATH, "GET", param);
			int success;
			try {

				success = json.getInt(IURLConst.TAG_SUCCESS);

				if (success == 1) {

					JSONArray jarr = json.getJSONArray(IURLConst.TAG_PATH);
					List<LatLng> paths = new ArrayList<LatLng>();

					for (int i = 0; i < jarr.length(); i++) {
						JSONObject jso = jarr.getJSONObject(i);
						LatLng l = new LatLng(jso.getDouble("lat"), jso.getDouble("lon"));
						paths.add(l);
					}
					return paths;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.direction_tab, menu);
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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		// Open Google map
		if (mGoogleApiClient.isConnected() == false) {
			mGoogleApiClient.connect();
		}
		if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
			startLocationUpdates();
		}
		super.onResume();
	}

	private void startLocationUpdates() {
		// TODO Auto-generated method stub
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onLocationChanged(Location location) {
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
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Log.d("connect", "Connected");
		if (mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}
}
