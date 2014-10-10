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
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android_to_pc_presentation.shared.DrawingFunctons;
import android_to_pc_presentation.shared.InputHistory;

public class SlideView extends View {
	
	// FIXME: silginin orantilamasi yapilmiyor. Ekran kucuk oldugunda yine ayni stroke width var.
	
	public DrawingFunctons df = new DrawingFunctons();
	public InputHistory inputHistory = new InputHistory();
	public InputSyncAndroid inputSyncAndroid = new InputSyncAndroid();
	
	/** tablette gorunen slayt katmani */
	Bitmap slideImageScaled = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
	/** tablette gorunen cizim katmani */
	Bitmap drawImageScaled = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

	/** resimde cizim yolu */
	private Path drawPathScaled, drawPathOrig;
	/** TODO: aciklama */
	private Paint drawPaint, canvasPaint;
	private Paint eraserPaint;

	/** bir onceki dokunulan x, y */
	private float prevTouchX = 0, prevTouchY = 0;

	/** slayt listesi */
	private ArrayList<Slide> slides = new ArrayList<Slide>();
	private int _currSlideNo = 0;
	
	public SlideView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupDrawing();
		
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				inputHistory.addRefresh();
				inputSyncAndroid.sync(inputHistory);
			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(timerTask, 0, 250);

	}

	private void setupDrawing() {
		drawPathScaled = new Path();
		drawPathOrig = new Path();

		drawPaint = new Paint();
		drawPaint.setColor(Color.parseColor(df.getPaintColor()));
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(df.getStrokeWidth());
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);

		eraserPaint = new Paint();
		eraserPaint.setColor(0);
		eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		eraserPaint.setAlpha(0);
		eraserPaint.setAntiAlias(true);
		eraserPaint.setDither(true);
		eraserPaint.setStyle(Paint.Style.STROKE);
		eraserPaint.setStrokeJoin(Paint.Join.ROUND);
		eraserPaint.setStrokeCap(Paint.Cap.ROUND);
		eraserPaint.setStrokeWidth(df.getStrokeWidthEraser());
		
		canvasPaint = new Paint(Paint.DITHER_FLAG);

	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (slides.size() == 0)
			return false;

		/* orana gore x, y hesapla */
		float xRatio = getCurrSlide().getWidth() / (float)getWidth();
		float yRatio = getCurrSlide().getHeight() / (float)getHeight();
		float x = event.getX()*xRatio;
		float y = event.getY()*yRatio;
		
		if (event.getAction() == MotionEvent.ACTION_UP || prevTouchX != x || prevTouchY != y) {
			inputHistory.addTouch(0, event.getAction(), x, y);
			inputSyncAndroid.sync(inputHistory);
			doTouchEvent(event.getAction(), x, y);
		}
		
		invalidate();
		return true;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.i("tnr", "onSizeChanged");
		if (slides.size() == 0)
			return;
		try {
			drawImageScaled = getCurrSlide().getScaledDrawImage(w, h);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		assert drawImageScaled != null;
		changeSlide(0);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (slides.size() == 0)
			return;
		canvas.drawBitmap(slideImageScaled, 0, 0, canvasPaint);
		canvas.drawBitmap(drawImageScaled, 0, 0, canvasPaint);
		
		if (df.isEraserMode()) {
			canvas.drawPath(drawPathScaled, eraserPaint);
		} else {
			drawPaint.setColor(Color.parseColor(df.getPaintColor()));
			drawPaint.setStrokeWidth(df.getScaledStrokeWidth(getXRatio()));
			canvas.drawPath(drawPathScaled, drawPaint);
			drawPaint.setStrokeWidth(df.getStrokeWidth());
		}
	}
	
	public float getXRatio() {
		return getCurrSlide().getWidth() / (float)getWidth();
	}
	
	public float getYRatio() {
		return getCurrSlide().getHeight() / (float)getHeight();
	}

	void doTouchEvent(int action, float X, float Y) {
		if (slides.size() == 0)
			return;

		Canvas orig;
		try {
			orig = new Canvas(getCurrSlide().getDrawImage());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		Canvas scaled = new Canvas(drawImageScaled);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			drawPathScaled.moveTo(X/getXRatio(), Y/getYRatio());
			drawPathOrig.moveTo(X, Y);
			break;
		case MotionEvent.ACTION_MOVE:
			// Log.i("tnr", "ACTION_MOVE");
			drawPathScaled.lineTo(prevTouchX/getXRatio(), prevTouchY/getYRatio());
			drawPathOrig.lineTo(prevTouchX, prevTouchY);		
		    break;
		case MotionEvent.ACTION_UP:
			// Log.i("tnr", "ACTION_UP");
			
			if (df.isEraserMode()) {
				eraserPaint.setStrokeWidth(df.getScaledStrokeWidthEraser(getXRatio()));
				scaled.drawPath(drawPathScaled, eraserPaint);
				eraserPaint.setStrokeWidth(df.getStrokeWidthEraser());
				orig.drawPath(drawPathOrig, eraserPaint);
			} else {
				drawPaint.setStrokeWidth(df.getScaledStrokeWidth(getXRatio()));
				scaled.drawPath(drawPathScaled, drawPaint);
				drawPaint.setStrokeWidth(df.getStrokeWidth());
				orig.drawPath(drawPathOrig, drawPaint);
			}

			drawPathScaled.reset();
			drawPathOrig.reset();
			
			break;
		default:
			break;
		}
		
		prevTouchX = X;
		prevTouchY = Y;
	}
	
	
	
	void _doChangeSlide_stageDownload(final int no) {
		Log.i("tnr", "_doChangeSlide_stageDownload");
		// download background image
		new DownloadFileFromURL() {
			@Override
			protected void onPostExecute(String result) {
				Log.i("tnr", "onPostExecute: " + result);
				if (result == null)
					_doChangeSlide_stageChange(no);
				else
					Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
			};
		}.execute(Config.APP_FOLDER_PATH, Config.SLIDE_URL() + "x-" + no + ".png.draw");
	}
	
	void _doChangeSlide_stageChange(final int no) {
		Log.i("tnr", "changeSlide() " + no + " / " + slides.size());
		if (no < 0 || no > slides.size())
			return;
		slides.get(_currSlideNo).closeDrawImage();
		
		_currSlideNo = no;
		Slide slide = slides.get(no);
		
		Log.i("tnr", ""+getWidth() + " " + getHeight());
		slideImageScaled = slide.getScaledBackgroundImage(getWidth(), getHeight());
		try {
			drawImageScaled = slide.getScaledDrawImage(getWidth(), getHeight());	
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		invalidate();
	}
	
	void doChangeSlide(int no) {
		_doChangeSlide_stageDownload(no);
	}
	
	public void setPaintColor(String newColor) {
		inputHistory.setPaintColor(newColor);
		inputSyncAndroid.sync(inputHistory);
		df.setPaintColor(newColor);
	}

	public void setSelectEraser() {
		inputHistory.selectEraser();
		inputSyncAndroid.sync(inputHistory);
		df.selectEraser();
	}
	
	public void setSelectPen() {
		inputHistory.selectPen();
		inputSyncAndroid.sync(inputHistory);
		df.selectPen();
	}
	
	public void changeSlide(int no) {
		if (no < 0 || no >= slides.size())
			return;
		inputHistory.changeSlide(no);
		inputSyncAndroid.sync(inputHistory);
		doChangeSlide(no);
		UtilAndroid.logHeap();
	}
	
	public void nextSlide() {
		if (slides.size() == 0)
			return;
		changeSlide((_currSlideNo+1) % slides.size());
	}
	
	public void prevSlide() {
		if (slides.size() == 0)
			return;
		changeSlide((_currSlideNo-1) % slides.size());
	}

	private Slide getCurrSlide() {
		if (slides.size() == 0)
			return null;
		return slides.get(_currSlideNo);
	}

	public void loadSlides(int w, int h, String path, int slideCount) {
		Log.i("tnr", "SlideView.loadSlides()");
		
		slides.subList(0, slides.size()).clear();
		inputHistory.clear();
		inputHistory.loadSlides(slideCount);
		inputSyncAndroid.sync(inputHistory);
		
		for (int i = 0 ; i < slideCount ; i++) {
			String fileName = "x-"+i+".png";
			File f = new File(path + fileName);
			if (!f.isFile())
				break;
			slides.add(new Slide(f.getAbsolutePath()));
		}

		ViewGroup.LayoutParams lp = getLayoutParams();
		lp.width = w;
		lp.height = h;
		this.setLayoutParams(lp);
		
		doChangeSlide(0);
	}
	
	public void incStrokeWidth() {
		df.incStrokeWidth();
		inputSyncAndroid.sync(inputHistory);
	}
	
	public void decStrokeWidth() {
		df.decStrokeWidth();
		inputSyncAndroid.sync(inputHistory);
	}

	public void redrawCurrSlide() {
		if (slides.size() == 0)
			return;
		doChangeSlide(_currSlideNo);
	}
}
