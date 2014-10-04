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
package com.example.presentation.android;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import android.util.Log;

public class DownloadFileFromURL extends AsyncTask<String, String, String> {

	@Override
	protected String doInBackground(String... values) {
		String path = values[0];
		String urlStr = values[1];
		
		String fileName = urlStr.substring(urlStr.lastIndexOf('/'));
		String filePath = path + fileName;

		int count;
		try {
			URL url = new URL(urlStr);
			URLConnection conection = url.openConnection();
			conection.connect();

			int lenghtOfFile = conection.getContentLength();
			InputStream input = new BufferedInputStream(url.openStream(), 8192);
			OutputStream output = new FileOutputStream(filePath);

			byte data[] = new byte[1024];
			long total = 0;
			while ((count = input.read(data)) != -1) {
				total += count;
				publishProgress("" + (int) ((total * 100) / lenghtOfFile));
				output.write(data, 0, count);
			}
			output.flush();
			output.close();
			input.close();
		} catch (Exception e) {
			Log.e("tnr", "a Error: " + e.getMessage());
			return e.getMessage();
		}
		
		return null;
	}
	
	@Override
	protected void onProgressUpdate(String... value) {

	};

}
