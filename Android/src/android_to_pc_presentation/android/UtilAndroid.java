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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.webkit.WebView.FindListener;
import android.widget.ProgressBar;
import android.widget.Toast;
import android_to_pc_presentation.shared.AbstractImageFunctions;

public class UtilAndroid {
	public static void logHeap() {
        Double allocated = new Double(Debug.getNativeHeapAllocatedSize())/new Double((1048576));
        Double available = new Double(Debug.getNativeHeapSize())/1048576.0;
        Double free = new Double(Debug.getNativeHeapFreeSize())/1048576.0;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        Log.d("tnr", "debug. =================================");
        Log.d("tnr", "debug.heap native: allocated " + df.format(allocated) + "MB of " + df.format(available) + "MB (" + df.format(free) + "MB free)");
        Log.d("tnr", "debug.memory: allocated: " + df.format(new Double(Runtime.getRuntime().totalMemory()/1048576)) + "MB of " + df.format(new Double(Runtime.getRuntime().maxMemory()/1048576))+ "MB (" + df.format(new Double(Runtime.getRuntime().freeMemory()/1048576)) +"MB free)");
    }
	
	private static Context context;
	private static Activity activity;
	
	public static void init(Context context, Activity activity) {
		UtilAndroid.context = context;
		UtilAndroid.activity = activity;
	}
	
	private static boolean syncError;
	
	public static void setSyncError(boolean error) {
		UtilAndroid.syncError = error;
	}
	
	public static void syncInfo(final boolean running) {
		final ProgressBar progressBar = (ProgressBar)activity.findViewById(R.id.progressBar_sync);

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (syncError || running)
					progressBar.setVisibility(View.VISIBLE);
				else
					progressBar.setVisibility(View.GONE);
			}
		});
	}
	
	public static void errorMessage(final String s) {
		if (context != null) {
			Log.i("tnr", "errorMessage created: " + s);
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					//Toast.makeText(context, "error: " + s, Toast.LENGTH_LONG).show();
				}
			});
		}
	}
	
	public static final AbstractImageFunctions<Bitmap> imageFunctons = new AbstractImageFunctions<Bitmap>() {

		@Override
		public Bitmap readImage(String path, boolean editable) {
			Bitmap bmp = BitmapFactory.decodeFile(path);
			if (bmp != null && editable)
				return bmp.copy(Bitmap.Config.ARGB_8888, true);
			return bmp;
		}

		@Override
		public int getWidth(Bitmap image) {
			return image.getWidth();
		}

		@Override
		public int getHeight(Bitmap image) {
			return image.getHeight();
		}

		@Override
		public Bitmap createImage_ARGB(int w, int h) {
			return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		}

		@Override
		public void saveImageToFile(Bitmap image, String path) {
			try {
				assert image != null;
				assert path != null;
				FileOutputStream out = new FileOutputStream(path);
				image.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (FileNotFoundException e) {
				Log.e("tnr", "Slide.getDrawImage() write error: " + path);
				// FIXME: txt log file ekle
				e.printStackTrace();
			}
		}

		@Override
		public Bitmap createScaledImage(Bitmap image, int w, int h) {
			return Bitmap.createScaledBitmap(image, w, h, false);
		}
		
	};
}
