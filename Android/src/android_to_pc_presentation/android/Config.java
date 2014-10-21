package android_to_pc_presentation.android;

import android.os.Environment;

public class Config {
	public final static String APP_FOLDER_PATH = Environment.getExternalStorageDirectory().toString() + "/deneme/";
	public final static int PC_PORT = 8041;
	
	public final static int SLIDE_VIEW_REFRESH_MS = 500;
	
	// public static String RECORDS = Environment.getExternalStorageDirectory().toString() + "/deneme/deneme3.txt";
	private static String RECORDS = "";
	
	public static String PC_IP = "192.168.0.0";
	
	public static String SLIDE_URL() {
		return "http://"+PC_IP+":8000/";
	}
	
	public static String getRecordsFilePath() {
		if (RECORDS.equals(""))
			return null;
		return Environment.getExternalStorageDirectory().toString() + "/deneme/" + RECORDS;
	}
	
	public static void setRecordsFileName(String fileName) {
		Config.RECORDS = fileName;
	}
	
	public static String getRecordsFileName() {
		return RECORDS;
	}
}
