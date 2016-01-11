package vn.trans.utils;

public interface IConstants {
	public static String FTP_SERVER = "ftp.byethost12.com";
	public static String FORMAT_DATE = "dd/MM/yyyy HH:mm:ss";
	public static String SPEC_FORM_DATE = "EEE MMM dd HH:mm:ss zzz yyyy";
	public static long REQUEST_TRAFF = 3600000 + 11 * 3600000;
	public static long ALARM_INTERVAL = 300000;
	public static int INTERVAL = 7000; // time to request location mms
	public static int FAST_INTV = 5000; // fast time to request location mms
	public static String KEY_API_SERV = "AIzaSyAjg-AyQtRQTb472UPe9TrLQU-ADlzWYKw";
	public static String RQ_ROAD = "https://roads.googleapis.com/v1";
	public static String RQ_DIST = "https://maps.googleapis.com/maps/api/distancematrix";
	// public static int[] COLORS = new int[] { 0x50FF0000, 0x50FD0E0A,
	// 0x50F92A1E, 0x50F54632, 0x50F16246, 0x50E18B6B,
	// 0x50D29480, 0x50BEA09C, 0x50AAACB8, 0x5096B8D4, 0x5082CAFF, 0x507ECEEF,
	// 0x507AD2DF, 0x5076D6CF, 0x5072DABF,
	// 0x506EDEAF, 0x506AE29F, 0x5066E68F, 0x5062EA7F, 0x505EEE6F, 0x505FE964 };
	public static int[] COLORS = new int[] { 0x88FF0000, 0x88FF1400, 0x88FF2800, 0x88FF3C00, 0x88FF5000, 0x88FF6600,
			0x88F56C0D, 0x88EB721A, 0x88E17827, 0x88D77E34, 0x88CD8441, 0x8869C0C3, 0x885FC6D0, 0x8855CCDD, 0x884BD2EA,
			0x8837D9FF, 0x8834DBF2, 0x8831DDE5, 0x882EDFD8, 0x882BE1CB, 0x8828E3BE, 0x8825E5B1, 0x8822E7A4, 0x8813F163,
			0x8810F356, 0x880DF549, 0x880AF73C, 0x8807F92F, 0x8804FB22, 0x8801FD15, 0x8800FF00 };
	public static String ROOT_PATH = "/storage/sdcard0/vitraff";
	public static String USERNAME = "b12_16735066";
	public static String PASSWORD = "123456789";
	public static int PORT = 21;
	
	public static double AUT_LONG = 0.00006;
	public static double AUT_LAT = 0.00003;
	public static String COLOR_RED = "FFFF0000";
	public static String COLOR_ORANGE = "FFFF6600";
	public static String COLOR_BLUE = "FF37D9FF";
	public static String COLOR_GREEN = "FF00FF00";

}
