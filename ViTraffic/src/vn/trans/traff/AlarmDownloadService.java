package vn.trans.traff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import vn.trans.vitraffic.TraffTab.DownloadTask;

public class AlarmDownloadService extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		DownloadTask dl = new DownloadTask();
		dl.execute((Void) null);
	}
}
