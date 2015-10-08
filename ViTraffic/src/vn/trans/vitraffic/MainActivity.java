package vn.trans.vitraffic;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import vn.trans.ftpserver.ServerUtil;

public class MainActivity extends TabActivity {

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
		traffSpec.setIndicator("Traffic Layer");
		traffSpec.setContent(traffIntent);

		// Add all tabspec to tabhost
		tabHost.addTab(trackSpec);
		tabHost.addTab(traffSpec);
		

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
}
