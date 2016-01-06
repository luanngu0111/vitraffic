package vn.trans.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class GISDbAdapter extends SQLiteOpenHelper {
	private Context context;

	private String DB_PATH = "data/data/vn.trans.vitraffic/";
	private static String DB_NAME = "HCM.osm.db";
	private SQLiteDatabase GISdatabase;

	public GISDbAdapter(Context context) {
		super(context, DB_NAME, null, 1);

		this.context = context;
		boolean dbexist = checkdatabase();

		if (dbexist) {
		} else {
			System.out.println("Database doesn't exist!");

			createDatabse();
		}
	}

	public void createDatabse() {

		this.getReadableDatabase();

		try {
			copyDatabase();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SQLiteDatabase getGISdatabase() {
		return GISdatabase;
	}

	private void copyDatabase() throws IOException {

		AssetManager dirPath = context.getAssets();

		InputStream myinput = context.getAssets().open(DB_NAME);

		String outFileName = DB_PATH + DB_NAME;

		OutputStream myOutput = new FileOutputStream("data/data/vi.trans.vitraffic/HCM.osm.db");

		byte[] buffer = new byte[1024];
		int length;

		while ((length = myinput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		myOutput.flush();
		myOutput.close();
		myinput.close();
	}

	public void open() {
		String myPath = DB_PATH + DB_NAME;
		GISdatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
	}

	public synchronized void close() {
		GISdatabase.close();
		super.close();
	}

	private boolean checkdatabase() {

		boolean checkdb = false;

		try {
			String myPath = DB_PATH + DB_NAME;
			File dbfile = new File(myPath);
			checkdb = dbfile.exists();
		} catch (SQLiteException e) {
			System.out.println("Databse doesn't exist!");
		}

		return checkdb;
	}

	public GISDbAdapter(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
