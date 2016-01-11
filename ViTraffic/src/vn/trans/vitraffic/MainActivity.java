package vn.trans.vitraffic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import vn.trans.traff.AlarmDownloadService;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;

public class MainActivity extends TabActivity {
	private PendingIntent mAlarmIntent;
	private AlarmManager alarmMan;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TabHost tabHost = getTabHost();

		// Tab for tracking
		TabSpec trackSpec = tabHost.newTabSpec("track");
		Intent trackIntent = new Intent(this, TrackTab.class);
		trackSpec.setIndicator("Tracking");
		trackSpec.setContent(trackIntent);

		// Tab for Traffic
		TabSpec traffSpec = tabHost.newTabSpec("traff");
		Intent traffIntent = new Intent(this, TraffTab.class);
		traffSpec.setIndicator("Traffic");
		traffSpec.setContent(traffIntent);

		// Tab for Direction
		TabSpec dirSpec = tabHost.newTabSpec("direct");
		Intent dirIntent = new Intent(this, DirectionTab.class);
		dirSpec.setIndicator("Direction");
		dirSpec.setContent(dirIntent);

		// Add all tabspec to tabhost
		tabHost.addTab(trackSpec);
		tabHost.addTab(traffSpec);
		tabHost.addTab(dirSpec);

		Intent launchIntent = new Intent(this, AlarmDownloadService.class);
		mAlarmIntent = PendingIntent.getBroadcast(this, 0, launchIntent, 0);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		alarmMan = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmMan.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 2000,
				IConstants.ALARM_INTERVAL, mAlarmIntent);
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Intent cancelIntent = new Intent(this, AlarmDownloadService.class);
		cancelIntent.putExtra("cancel", "cancel");
		mAlarmIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmMan.cancel(mAlarmIntent);
		super.onStop();
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
	/**
	 * Lop nay dung de thuc hien Download file tu server o che do chay ngam.
	 *
	 */

}
