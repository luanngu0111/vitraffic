package vn.trans.traff;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.UrlValidator;

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

//		dl = new DownloadTask(arg0);
//		dl.execute();
		new LoadTraffic().execute((Void)null);

	}

	public class DownloadTask extends AsyncTask<Object, Integer, Object> {

		int id = 1;
		Context context;

		public DownloadTask(Context c) {
			context = c;

		}

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			String root = IConstants.ROOT_PATH;
			File froot = new File(root);
			if (!froot.exists()) {
				froot.mkdir();
			}
			String[] schemes = {"http","https"}; // DEFAULT schemes = "http", "https", "ftp"
			UrlValidator urlValidator = new UrlValidator(schemes);
			for (int i = 1;i<=IURLConst.NUM_FILES; i++) {
				Log.i("Download", "file " + i);
				URL fileUrl = null;
				
				try {
					fileUrl = new URL(IURLConst.URL_TRAFFIC + "/map" + i + ".json");
					FileUtils.copyURLToFile(fileUrl, new File(IConstants.ROOT_PATH + "/map" + i + ".json"));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				finally {
					if (fileUrl == null)
						break;
				}
				// finally {
				// try {
				// FileUtils.copyURLToFile(new URL(IURLConst.URL_TRAFFIC +
				// "/traffic" + i + ".json"),
				// new File(IConstants.ROOT_PATH + "/traffic" + i + ".json"));
				// } catch (MalformedURLException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// }
			}

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {

			Log.i("Download", "Complete");
			// new FetchingData(null).execute();
			super.onPostExecute(result);
		}

	}

}
