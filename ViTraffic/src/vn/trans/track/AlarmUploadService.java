package vn.trans.track;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import vn.trans.ftpserver.ServerUtil;
import vn.trans.utils.IConstants;

public class AlarmUploadService extends BroadcastReceiver {
	UploadTask ul;

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		String inf = arg1.getExtras().getString("cancel");

		ul = new UploadTask();
		ul.execute((Void) null);

	}

	public class UploadTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			Log.i("Tag", "Start Upload ...");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			File file = new File(IConstants.ROOT_PATH);
			if (!file.exists()) {
				file.mkdir();
			}
			File[] files = file.listFiles();

			Log.d("upload", "Connection Success to upload " + files.length);
			for (File f : files) {
				ServerUtil ftp = ServerUtil.createServer();
				boolean status = ftp.serverConnect(IConstants.USERNAME, IConstants.PASSWORD, IConstants.PORT);
				if (f.isDirectory())
					continue;
				if (status)
					ftp.Upload(f);

			}
			return null;
		}

	}
}
