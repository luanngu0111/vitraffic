package vn.trans.vitraffic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.kml.KmlLayer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import vn.trans.entities.Road;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;

public class TraffTab extends FragmentActivity
		implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	private static GoogleMap map;
	boolean onCurrentTab = false;
	GoogleApiClient mGoogleApiClient;
	boolean mRequestingLocationUpdates = true;
	boolean init = true;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traff_tab);
		mRequestingLocationUpdates = true;
		handler = new Handler();
		initilizeMap();
		buildGoogleApiClient();
		createLocationRequest();
		init = true;

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
		map.animateCamera(CameraUpdateFactory.zoomTo(5), 2000, null);
		map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(106, 10)));

		// check if map is created successfully or not
		if (map == null) {
			Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
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

		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				File kml = new File(IConstants.ROOT_PATH + "/map" + 2 + ".kml");
				if (kml.exists()) {
					try {
						KmlLayer layer = new KmlLayer(map, new FileInputStream(kml), getApplicationContext());
						layer.addLayerToMap();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});
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

	/**
	 * Thuc hien ve mot doan duong dua vao thong tin danh sach toa do va van
	 * toc.
	 * 
	 * @param road
	 */
	public static void DrawTrafficRoad(Road road) {
		int color = 0;
		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
		List<LatLng> paths = new ArrayList<LatLng>();
		if (road != null) {
			paths = road.getArr_paths();
			if (paths != null) {
				int speed = (int) road.getAvg_speed();
				double hs_lat = 0.0;
				double hs_long = 0.0;
				// Xac dinh mau dua tren van toc.
				if (speed >= IConstants.COLORS.length) {
					color = IConstants.COLORS[IConstants.COLORS.length - 1];
				} else {
					color = IConstants.COLORS[speed];
				}
				for (int i = 1; i < paths.size(); i++) {
					LatLng start = paths.get(i - 1);
					LatLng end = paths.get(i);
					LatLng sideStart = null, sideEnd = null;
					boolean neg = end.latitude < start.latitude;
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
						map.addPolyline(new PolylineOptions().add(sideStart, sideEnd).width(6).color(color))
								.setVisible(true);
						Log.v("draw", end.latitude + " " + end.longitude);
					}

				}
				Log.v("draw", "finish line");
			}
		}
	}

	public void DrawTrafficStatus(Road[] roads) {
		int color = 0;
		LatLng start = new LatLng(0, 0);
		LatLng end = new LatLng(0, 0);
		if (roads != null) {
			for (Road road : roads) {
				List<LatLng> paths = new ArrayList<LatLng>();
				if (road == null)
					continue;
				paths = road.getArr_paths();
				if (paths != null && paths.size() > 0) {
					start = paths.get(0);
					int speed = (int) road.getAvg_speed();
					if (speed > 20) {
						color = IConstants.COLORS[IConstants.COLORS.length - 1];
					} else {
						color = IConstants.COLORS[speed];
					}
					for (int i = 1; i < paths.size(); i++) {
						end = paths.get(i);
						Polyline line = map.addPolyline(new PolylineOptions().add(start, end).width(8).color(color));
						line.setVisible(true);
						Log.v("draw", "Drawing");
					}
				}
			}
		}
	}

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

	public void loadTrafficLayer() {

		// TODO Auto-generated method stub
		for (int i = 1; i <= IURLConst.NUM_FILES; i++) {
			File kml = new File(IConstants.ROOT_PATH + "/map" + i + ".kml");
			if (kml.exists()) {
				try {
					KmlLayer layer = new KmlLayer(map, new FileInputStream(kml), getApplicationContext());
					layer.addLayerToMap();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	public class LoadTraffic extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			for (int i = 2; i <= IURLConst.NUM_FILES; i++) {
				File kml = new File(IConstants.ROOT_PATH + "/map" + i + ".kml");
				if (kml.exists()) {
					try {
						KmlLayer layer = new KmlLayer(map, new FileInputStream(kml), getApplicationContext());
						layer.addLayerToMap();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			// TODO Auto-generated method stub
			KmlLayer layer = (KmlLayer) values[0];
			try {
				layer.addLayerToMap();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.onProgressUpdate(values);
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
