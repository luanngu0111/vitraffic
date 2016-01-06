package vn.trans.traff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import vn.trans.direction.FastestPath.FetchingData;
import vn.trans.vitraffic.TraffTab.DownloadTask;

public class AlarmDownloadService extends BroadcastReceiver {
//	DownloadTask dl;

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub

//		dl = new DownloadTask();
//		dl.execute((Void) null);
		new FetchingData().execute();
	}

}
