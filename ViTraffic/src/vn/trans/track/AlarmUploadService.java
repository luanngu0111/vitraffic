package vn.trans.track;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import vn.trans.ftpserver.ServerUtil;
import vn.trans.utils.IConstants;

public class AlarmUploadService extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		UploadTask ul = new UploadTask();
		ul.execute((Void) null);

	}

	public class UploadTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			File file = new File(IConstants.ROOT_PATH);
			if (!file.exists()) {
				file.mkdir();
			}
			File[] files = file.listFiles();
			ServerUtil ftp = ServerUtil.createServer();
			boolean status = ftp.serverConnect("b3_16668287", "123456789", 21);
			if (status) {
				Log.d("upload", "Connection Success");
				for (File f : files) {
					if (f.isDirectory())
						continue;
					ftp.Upload(f);

				}
			}
			return null;
		}

	}
}
