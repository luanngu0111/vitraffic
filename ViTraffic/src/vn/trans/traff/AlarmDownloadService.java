package vn.trans.traff;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import vn.trans.direction.FastestPath.FetchingData;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;
import vn.trans.vitraffic.TraffTab.LoadTraffic;

public class AlarmDownloadService extends BroadcastReceiver {
	DownloadTask dl;
	private NotificationManager mNotifyManager;
	private Builder mBuilder;

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		mNotifyManager = (NotificationManager) arg0.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(arg0);
		mBuilder.setContentTitle("Download").setContentText("Download in progress");
		dl = new DownloadTask(arg0);
		dl.execute();
//		new LoadTraffic().execute();
		// new FetchingData(arg0).execute();

	}

	public class DownloadTask extends AsyncTask<Object, Integer, Object> {

		int id = 1;
		Context context;

		public DownloadTask(Context c) {
			context = c;

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mBuilder.setProgress(100, 0, false);
			mNotifyManager.notify(id, mBuilder.build());
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			mBuilder.setProgress(100, values[0], false);
			mNotifyManager.notify(id, mBuilder.build());
			super.onProgressUpdate(values);
		}

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			String root = IConstants.ROOT_PATH;
			File froot = new File(root);
			if (!froot.exists()) {
				froot.mkdir();
			}
			for (int i = 1; i <= IURLConst.NUM_FILES; i++) {
				Log.i("Download", "file " + i);
				try {
					FileUtils.copyURLToFile(new URL(IURLConst.URL_TRAFFIC + "/map" + i + ".json"),
							new File(IConstants.ROOT_PATH + "/map" + i + ".json"));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						FileUtils.copyURLToFile(new URL(IURLConst.URL_TRAFFIC + "/traffic" + i + ".json"),
								new File(IConstants.ROOT_PATH + "/traffic" + i + ".json"));
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				publishProgress(Math.min(2, 100));
			}

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			mBuilder.setContentText("Download complete");
			// Removes the progress bar
			mBuilder.setProgress(0, 0, false);
			mNotifyManager.notify(id, mBuilder.build());
			Log.i("Download", "Complete");
			// new FetchingData(null).execute();
			super.onPostExecute(result);
		}

	}

}
