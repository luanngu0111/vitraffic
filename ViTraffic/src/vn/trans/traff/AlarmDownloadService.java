package vn.trans.traff;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import vn.trans.direction.FastestPath.FetchingData;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;

public class AlarmDownloadService extends BroadcastReceiver {
	DownloadTask dl;

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub

		 dl = new DownloadTask();
		 dl.execute();
		// new FetchingData(arg0).execute();
		
	}

	public class DownloadTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			try {
				String root = IConstants.ROOT_PATH;
				File froot = new File(root);
				if (!froot.exists()) {
					froot.mkdir();
				}
				for (int i = 2; i <= IURLConst.NUM_FILES; i++) {
					FileUtils.copyURLToFile(new URL(IURLConst.URL_TRAFFIC + "/map" + i + ".kml"),
							new File(IConstants.ROOT_PATH + "/map" + i + ".kml"));
					FileUtils.copyURLToFile(new URL(IURLConst.URL_TRAFFIC + "/traffic" + i + ".json"),
							new File(IConstants.ROOT_PATH + "/traffic" + i + ".json"));
					Log.i("Download", "file "+i);
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			Log.i("Download", "Complete");
			//new FetchingData(null).execute();
			super.onPostExecute(result);
		}

	}

}
