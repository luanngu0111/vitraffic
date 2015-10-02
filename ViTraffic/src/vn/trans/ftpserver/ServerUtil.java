package vn.trans.ftpserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import android.util.Log;
import vn.trans.entities.Location;
import vn.trans.entities.Road;
import vn.trans.utils.IConstants;

public class ServerUtil {
	private static ServerUtil instance;
	private static FTPClient mFTP;

	private ServerUtil() {
		this.mFTP = new FTPClient();
	}

	public static ServerUtil createServer() {
		if (instance != null) {
			return instance;
		} else {
			instance = new ServerUtil();
			instance.serverConnect("b3_16668287", "123456789", 21);
			return instance;
		}
	}

	public boolean serverConnect(String username, String password, int port) {
		mFTP = new FTPClient();
		try {
			mFTP.connect(IConstants.FTP_SERVER, port);
			if (FTPReply.isPositiveCompletion(mFTP.getReplyCode())) {
				boolean status = mFTP.login(username, password);
				mFTP.setFileType(FTP.BINARY_FILE_TYPE);
				mFTP.enterLocalPassiveMode();
				mFTP.changeWorkingDirectory("htdocs");
				return status;
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	public boolean serverDisconnect() {
		try {
			mFTP.logout();
			mFTP.disconnect();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	public void Upload(File file) {
		try {
			FileInputStream is = new FileInputStream(file);

			// mFTP.changeWorkingDirectory("htdocs");
			boolean result = mFTP.storeFile(file.getName(), is);
			is.close();
			if (result)
				Log.v("upload", "succeeded");
			else
				Log.v("upload", "failed");

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v("upload", e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v("upload", e.getMessage());
		}

	}

	public FTPFile[] getAllFile(boolean isfilter) {

		FTPFile[] files;
		try {
			// mFTP.changeWorkingDirectory("htdocs");
			FTPFileFilter filter = new FTPFileFilter() {

				@Override
				public boolean accept(FTPFile file) {
					// TODO Auto-generated method stub
					Date now = new Date();
					Calendar cal = Calendar.getInstance();
					Calendar timestamp = file.getTimestamp();
					long during = cal.getTimeInMillis() - timestamp.getTimeInMillis();
					if (during <= IConstants.REQUEST_TRAFF)
						return true;

					return false;
				}
			};
			// files = mFTP.listFiles(null, filter);
			if (isfilter == true) {
				files = mFTP.listFiles(".", filter);
			} else {
				files = mFTP.listFiles();
			}
			return files;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Road[] DownloadAllFile() {
		// mFTP.changeWorkingDirectory("htdocs");
		FTPFileFilter filter = new FTPFileFilter() {

			@Override
			public boolean accept(FTPFile file) {
				// TODO Auto-generated method stub
				Date now = new Date();
				Calendar cal = Calendar.getInstance();
				Calendar timestamp = file.getTimestamp();
				long during = cal.getTimeInMillis() - timestamp.getTimeInMillis();
				if (during <= IConstants.REQUEST_TRAFF)
					return true;

				return false;
			}
		};
		// FTPFile[] files = mFTP.listFiles(null, filter);
		FTPFile[] files;
		try {
			files = mFTP.listFiles();
			Log.d("files", " " + files.length);
			List<Road> lroad = new ArrayList<Road>();
			for (int i = 0; i < files.length; i++) {
				FTPFile f = files[i];
				Log.d("modify", f.getTimestamp() + " " + f.getName());
				if (f.isDirectory())
					continue;
				String json = Download(f.getName());
				if (json != null) {
					Location loc = new Location();
					loc.conv2Obj(json);
					Road r = new Road();
					r.setArr_paths(loc.getArr_coord());
					r.setAvg_speed(loc.getSpeed());
					lroad.add(r);
				}
			}
			Road[] roads = lroad.toArray(new Road[lroad.size()]);
			return roads;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String Download(String filename) {
		if (filename.contains(".csv")) {
			return null;
		}
		String rs = "";
		String root = "/storage/sdcard0/vitraff";
		String filepath = root + "/" + filename;
		if (filename.contains(".csv")) {
			String rootpath = root + "/csv";
			File croot = new File(rootpath);
			if (!croot.exists()) {
				croot.mkdir();
			}
			filepath = rootpath + "/" + filename;
		}

		File froot = new File(root);
		if (!froot.exists()) {
			froot.mkdir();
		}

		OutputStream out;
		try {
			File file = new File(filepath);

			if (!file.exists()) {
				out = new FileOutputStream(file);
				boolean result = mFTP.retrieveFile(filename, out);
				out.close();
				if (result) {
					BufferedReader br = null;

					String sCurrentLine;

					br = new BufferedReader(new FileReader(filepath));

					while ((sCurrentLine = br.readLine()) != null) {
						rs += sCurrentLine;
					}
					br.close();
				} else {
					Log.d("download", "error");
				}
			} else {
				BufferedReader br = null;

				String sCurrentLine;

				br = new BufferedReader(new FileReader(filepath));

				while ((sCurrentLine = br.readLine()) != null) {
					rs += sCurrentLine;
				}
				br.close();
			}

			Log.d("download", rs);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("download", e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("download", e.getMessage());
		}
		return rs;

	}

}
