/*
 * Copyright (C) 2014 Taner Guven <tanerguven@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package android_to_pc_presentation.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public abstract class FileTools {
	
	public static void delete(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		if (!f.delete())
			throw new FileNotFoundException("Failed to delete file: " + f);
	}
	
	
	public void downloadSlides(final String url, final Context context) {
		Log.i("tnr", "downloadSlides()");
		
		new DownloadFileFromURL() {
			@Override
			protected void onPostExecute(String result) {
				Log.i("tnr", "onPostExecute: " + result);
				if (result == null)
					infoFileDownloadOk(url, context);
				else
					Toast.makeText(context, result, Toast.LENGTH_LONG).show();
			};
		}.execute(Config.APP_FOLDER_PATH, url + "info.txt");
	}
	
	public static Integer readInfoFile() {
		try {
			File file = new File(Config.APP_FOLDER_PATH + "info.txt");
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] data = new byte[(int)file.length()];
			fileInputStream.read(data);
			fileInputStream.close();
			String fileData = new String(data, "UTF-8");
			// FIXME: duzgun bir dosya formati belirle
			return Integer.parseInt(fileData.substring(0, fileData.indexOf('\n')));	
		} catch (Exception e) {
			Log.e("tnr", "FileTools.readInfoFile() exception");
			e.printStackTrace();
			return null;
		}
	}
	
	private void infoFileDownloadOk(final String url, final Context context) {
		Integer slideCount = readInfoFile();
		if (slideCount == null)
			return;
		
		/* slayt sayisi okunabildiyse slaytlari indir */
		slideDownloadRunning(url, context, slideCount, -1);
		
	}
	
	/* recursive gibi gorunuyor fakat recursive degil, arka plandaki download islemi bitince tekrar cagriliyor */
	private void slideDownloadRunning(final String url, final Context context, final int slideCount, final int i) {
		if (i == slideCount-1) {
			slideDownloadOk(slideCount);
		} else {
			new DownloadFileFromURL() {
				@Override
				protected void onPostExecute(String result) {
					Log.i("tnr", "onPostExecute: " + result);
					if (result == null)
						slideDownloadRunning(url, context, slideCount, i+1);
					else
						Toast.makeText(context, result, Toast.LENGTH_LONG).show();
				};
			}.execute(Config.APP_FOLDER_PATH, url + "x-"+(i+1)+".png");
		}
	}
	
	protected abstract void slideDownloadOk(int count);
	
	
}
