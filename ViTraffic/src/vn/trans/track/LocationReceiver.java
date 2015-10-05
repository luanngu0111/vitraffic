package vn.trans.track;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LocationReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		Bundle bundle = intent.getExtras();
		Log.i("Here", "onreceived");
		if (bundle != null) {

			double  latitude = bundle.getDouble("lati");
			Log.i("Tag", latitude + "");

			double longi = bundle.getDouble("longi");
			Log.i("tag", longi + "");
		}
	}

}
