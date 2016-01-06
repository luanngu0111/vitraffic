package vn.trans.vitraffic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;

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
import vn.trans.entities.Location;
import vn.trans.entities.Road;
import vn.trans.ftpserver.ServerUtil;
import vn.trans.traff.AlarmDownloadService;
import vn.trans.utils.IConstants;

public class TraffTab extends FragmentActivity
		implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	private static GoogleMap map;
	boolean onCurrentTab = false;
	private PendingIntent mAlarmIntent;
	private AlarmManager alarmMan;
	GoogleApiClient mGoogleApiClient;
	boolean mRequestingLocationUpdates = true;
	boolean init = true;

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
		mAlarmIntent = PendingIntent.getBroadcast(this, 0, launchIntent, 0);
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
		alarmMan = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmMan.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 2000,
				IConstants.ALARM_INTERVAL, mAlarmIntent);
		if (mGoogleApiClient.isConnected() == false) {
			mGoogleApiClient.connect();
		}
		if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
			startLocationUpdates();
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stup
		Intent cancelIntent = new Intent(this, AlarmDownloadService.class);
		cancelIntent.putExtra("cancel", "cancel");
		mAlarmIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmMan.cancel(mAlarmIntent);
		super.onStop();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	/**
	 * Lop nay dung de thuc hien Download file tu server o che do chay ngam.
	 *
	 */
	public static class DownloadTask extends AsyncTask<Void, Road, Void> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			if (map != null)
				map.clear();
			Log.i("Tag", "Start Download ...");
			super.onPreExecute();
		}

		/*
		 * Moi khi tai ve mot file thi thuc hien ve traffic len ban do.
		 */
		@Override
		protected void onProgressUpdate(Road... values) {
			// TODO Auto-generated method stub
			DrawTrafficRoad(values[0]);
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			Log.i("Tag", "Download finish...");
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			boolean status = false;
			ServerUtil server = ServerUtil.createServer();
			status = server.serverConnect(IConstants.USERNAME, IConstants.PASSWORD, IConstants.PORT);
			if (status == true) {
				/*
				 * Gia tri trong getAllFile = False la lay tat ca Neu = True la
				 * lay theo khoang thoi gian duoc dinh truoc trong file
				 * IConstants
				 */
				FTPFile[] files = server.getAllFile(false);
				if (files == null)
					return null;
				for (FTPFile f : files) {
					if (f.isDirectory())
						continue;
					// Log.v("file",
					// ServerUtil.converDate2String(f.getTimestamp()) + " " +
					// f.getName());
					String json = server.Download(f.getName());
					if (json != null) {
						try {
							Location loc = new Location();
							loc.conv2Obj(json);
							Road r = new Road();
							r.setArr_paths(loc.getArr_coord());
							r.setAvg_speed(loc.getSpeed());
							publishProgress(r);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
				Log.d("server", "Connection success files " + files.length);
			} else {
				Log.d("server", "Connection failed files");
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			Log.i("Tag", "Cancel Download ...");
			super.onCancelled();
		}

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
	
	private void loadTrafficLayer(){
		
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
