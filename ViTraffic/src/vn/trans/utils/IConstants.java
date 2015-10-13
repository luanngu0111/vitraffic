package vn.trans.utils;

public interface IConstants {
	public static String FTP_SERVER = "ftp.byethost12.com";
	public static String FORMAT_DATE = "dd/MM/yyyy HH:mm:ss";
	public static long REQUEST_TRAFF = 3600000+11*3600000;
	public static long ALARM_INTERVAL = 900000;
	public static int INTERVAL = 4000; // time to request location mms
	public static int FAST_INTV = 4000; // fast time to request location mms
	public static String KEY_API_SERV = "AIzaSyAjg-AyQtRQTb472UPe9TrLQU-ADlzWYKw";
	public static String RQ_ROAD = "https://roads.googleapis.com/v1";
	public static String RQ_DIST = "https://maps.googleapis.com/maps/api/distancematrix";
	// public static int[] COLORS = new int[] { 0x50FF0000, 0x50FD0E0A,
	// 0x50F92A1E, 0x50F54632, 0x50F16246, 0x50E18B6B,
	// 0x50D29480, 0x50BEA09C, 0x50AAACB8, 0x5096B8D4, 0x5082CAFF, 0x507ECEEF,
	// 0x507AD2DF, 0x5076D6CF, 0x5072DABF,
	// 0x506EDEAF, 0x506AE29F, 0x5066E68F, 0x5062EA7F, 0x505EEE6F, 0x505FE964 };
	public static int[] COLORS = new int[] { 0x80FF0000,0x80FF0F00,0x80FF2300,0x80FF3700,0x80FF4B00,0x80FF6600,0x80E17827,0x80B9905B,0x8091A88F,0x8069C0C3,0x8037D9FF,0x8031DDE5,0x802BE1CB,0x8025E5B1,0x801FE997,0x8019ED7D,0x8013F163,0x800DF549,0x8007F92F,0x8001FD15,0x8000FF00};
	public static String ROOT_PATH = "/storage/sdcard0/vitraff";
	public static String USERNAME = "b12_16735066";
	public static String PASSWORD = "123456789";
	public static int PORT = 21;

}
