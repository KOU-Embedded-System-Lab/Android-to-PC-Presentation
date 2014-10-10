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
package com.example.presentation;

import com.example.presentation.android.FileTools;
import com.example.presentation.android.SlideView;
import com.example.presentation.android.UtilAndroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	/** renk secimi buttonu */
	private ImageButton currPaint;
	
	private SlideView slideView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		/* sadece yatay ekran ve cihazin durumuna gore yonu degisir */ 
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		
		setContentView(R.layout.activity_main);
		
		slideView = (SlideView)findViewById(R.id.drawing);
		
		assert slideView.inputSyncAndroid != null;
	
		UtilAndroid.init(getApplicationContext(), this);
		
		LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		currPaint = (ImageButton)paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
		
		ToggleButton toggleButton = (ToggleButton)findViewById(R.id.toggleButton_eraser);
		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					slideView.setSelectEraser();
				else
					slideView.setSelectPen();
			}
		});
		
		new Thread(slideView.inputSyncAndroid).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void button_loadClicked(View view) {
		try {
			Bitmap bmp = BitmapFactory.decodeFile(Config.APP_FOLDER_PATH + "x-0.png");
			loadSlides(bmp.getWidth(), bmp.getHeight());
		} catch (Exception e) {
			// Toast.makeText(getApplicationContext(), "load fail", Toast.LENGTH_LONG).show();
		}
	}

	private void loadSlides(int slide_w, int slide_h) {
		View parent = (View) slideView.getParent();
		int w = parent.getWidth();
		int h = parent.getHeight();
		Log.i("tnr", "w: " + w + " " + "h: " + h);
		float slideRatio = (float)slide_w / slide_h;
		float screenRatio = (float) w / (float) h;
		if (screenRatio > slideRatio)
			w = Math.round(h * slideRatio);
		else
			h = Math.round(w / slideRatio);
		Log.i("tnr", "new w: " + w + " " + "h: " + h);
		
		slideView.loadSlides(w, h, Config.APP_FOLDER_PATH, FileTools.readInfoFile());
	}
	
	private void download_slides() {
		FileTools fileTools = new FileTools() {
			@Override
			protected void slideDownloadOk(int slideCount) {
				Toast.makeText(getApplicationContext(), "download ok. " + slideCount + " slides", Toast.LENGTH_LONG).show();
				Log.i("tnr", "downloadSlidesOk()");
				// logHeap();
			}
		};
		fileTools.downloadSlides(Config.SLIDE_URL(), getApplicationContext());
	}
	
	public void button_downloadClicked(View view) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("PC_IP");
		// Set up the input
		final EditText input = new EditText(this);
		input.setText(Config.PC_IP);
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		builder.setView(input);
		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        Config.PC_IP = input.getText().toString();
		        download_slides();
		    }
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});

		builder.show();
	}
	
	public void button_nextSlideClicked(View view) {
		slideView.nextSlide();
	}

	public void button_prevSlideClicked(View view) {
		slideView.prevSlide();
	}
	
	public void button_redrawClicked(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("RECORDS file name");
		// Set up the input
		final EditText input = new EditText(this);
		input.setText(Config.getRecordsFileName());
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		builder.setView(input);
		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        Config.setRecordsFileName(input.getText().toString());
		        slideView.redrawCurrSlide();
		    }
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});

		builder.show();
	}
	
	public void paintClicked(View view) {
		if (view != currPaint) {
			ImageButton imgView = (ImageButton) view;
			String color = view.getTag().toString();
			slideView.setPaintColor(color);
			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
			currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			currPaint = (ImageButton) view;
		}
	}
	
	public void button_dec_clicked(View view) {
		slideView.decStrokeWidth();
	}
	
	public void button_inc_clicked(View view) {
		slideView.incStrokeWidth();
	}
	
}
