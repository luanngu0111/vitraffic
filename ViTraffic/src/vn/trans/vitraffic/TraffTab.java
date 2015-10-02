package vn.trans.vitraffic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.net.ftp.FTPFile;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
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
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import vn.trans.entities.Location;
import vn.trans.entities.Road;
import vn.trans.ftpserver.ServerUtil;
import vn.trans.track.RequestTrack;
import vn.trans.track.ResponseTrack;
import vn.trans.traff.AlarmDownloadService;
import vn.trans.utils.IConstants;

public class TraffTab extends FragmentActivity {
	private static GoogleMap map;
	boolean onCurrentTab = false;
	private PendingIntent mAlarmIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traff_tab);
		initilizeMap();
		Intent launchIntent = new Intent(this, AlarmDownloadService.class);
		mAlarmIntent = PendingIntent.getBroadcast(this, 0, launchIntent, 0);
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
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		Log.i("Tag", "map");
		map.setMyLocationEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);
		map.clear();
		map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(106, 10)));

		// check if map is created successfully or not
		if (map == null) {
			Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		AlarmManager man = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		man.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 2000, IConstants.ALARM_INTERVAL,
				mAlarmIntent);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		// AlarmManager man = (AlarmManager)
		// getSystemService(Context.ALARM_SERVICE);
		// man.cancel(mAlarmIntent);

		super.onPause();
	}

	public static class DownloadTask extends AsyncTask<Void, Road, Void> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			map.clear();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Road... values) {
			// TODO Auto-generated method stub
			DrawTrafficRoad(values[0]);
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			boolean status = false;
			ServerUtil server = ServerUtil.createServer();
			status = server.serverConnect("b3_16668287", "123456789", 21);
			if (status == true) {
				FTPFile[] files = server.getAllFile(false);
				if (files == null)
					return null;
				for (FTPFile f : files) {
					if (f.isDirectory())
						continue;
					Log.v("file", f.getTimestamp().getTimeInMillis() + " " + (new Date()).getTime());
					String json = server.Download(f.getName());
					if (json != null) {
						Location loc = new Location();
						loc.conv2Obj(json);
						Road r = new Road();
						r.setArr_paths(loc.getArr_coord());
						r.setAvg_speed(loc.getSpeed());
						publishProgress(r);
					}
				}
				Log.d("server", "Connection success files " + files.length);
			} else {
				Log.d("server", "Connection failed");
			}
			return null;
		}

	}
	public static void DrawTrafficRoad(Road road) {
		int color = 0;
		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
		List<LatLng> paths = new ArrayList<LatLng>();
		if (road != null) {
			paths = road.getArr_paths();
			if (paths != null && paths.size() > 0) {
				LatLng start = paths.get(0);
				int speed = (int) road.getAvg_speed();
				if (speed > 20) {
					color = IConstants.COLORS[IConstants.COLORS.length - 1];
				} else {
					color = IConstants.COLORS[speed];
				}
				Log.v("draw", start.latitude + " " + start.longitude);
				for (int i = 1; i < paths.size(); i++) {
					LatLng end = paths.get(i);
					if (bounds.contains(end)) {
						Polyline line = map.addPolyline(new PolylineOptions().add(start, end).width(8).color(color));
						line.setVisible(true);
					}
					Log.v("draw", end.latitude + " " + end.longitude);
					start = end;
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
}
