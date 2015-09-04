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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android_to_pc_presentation.shared.CustomPaint;
import android_to_pc_presentation.shared.DrawingFunctons;
import android_to_pc_presentation.shared.InputHistory;
import android_to_pc_presentation.shared.SharedConfig;

public class SlideView extends View {
	
	// FIXME: silginin orantilamasi yapilmiyor. Ekran kucuk oldugunda yine ayni stroke width var.
	
	public DrawingFunctons df;
	private InputHistory inputHistory = new InputHistory();
	public InputSyncAndroid inputSyncAndroid = new InputSyncAndroid();
	
	/** tablette gorunen slayt katmani */
	Bitmap slideImageScaled = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
	/** tablette gorunen cizim katmani */
	Bitmap drawImageScaled = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

	/** resimde cizim yolu */
	private Path drawPathScaled, drawPathOrig;
	/** TODO: aciklama */
	private final Paint canvasPaint = new Paint(Paint.DITHER_FLAG);

	/** down sirasindaki x, y */
	private float firstTouchX = 0, firstTouchY = 0;
	
	/** bir onceki dokunulan x, y */
	private float prevTouchX = 0, prevTouchY = 0;

	/** slayt listesi */
	private ArrayList<Slide> slides = new ArrayList<Slide>();
	private int _currSlideNo = 0;

	/** slaytin degistirilip degistirilmedigi kontrolu*/
	protected boolean slideChanged = false;
	
	public SlideView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupDrawing();
		
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				synchronized (inputHistory) {
					inputHistory.addRefresh();
					inputSyncAndroid.sync(inputHistory);	
				}
			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(timerTask, 0, Config.SLIDE_VIEW_REFRESH_MS);

	}

	private void setupDrawing() {
		drawPathScaled = new Path();
		drawPathOrig = new Path();

		df = new DrawingFunctons(new CustomPaint("#ff000000", SharedConfig.DEFAULT_PEN_SIZE), new CustomPaint("#00000000", SharedConfig.DEFAULT_ERASER_1_SIZE));
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (slides.size() == 0)
			return false;

		if(slideChanged)
			sendChangedSlide();

		/* parmak ile cizimleri gormezden gel */
		if (event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER)
			return false;
		
		/* orana gore x, y hesapla */
		float xRatio = getCurrSlide().getWidth() / (float)getWidth();
		float yRatio = getCurrSlide().getHeight() / (float)getHeight();
		float x = event.getX()*xRatio;
		float y = event.getY()*yRatio;
		
		if (event.getAction() == MotionEvent.ACTION_UP || prevTouchX != x || prevTouchY != y) {
			synchronized (inputHistory) {
				inputHistory.addTouch(0, event.getAction(), x, y);
				inputSyncAndroid.sync(inputHistory);
			}
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
		
		if (df.is_lineMode())
			canvas.drawLine(firstTouchX*getXScale(), firstTouchY*getYScale(), prevTouchX*getXScale(), prevTouchY*getYScale(), this.getPaint(getXScale()));
		else
			canvas.drawPath(drawPathScaled, this.getPaint(getXScale()));
	}
	
	public float getXScale() {
		return (float)getWidth() / getCurrSlide().getWidth();
	}
	
	public float getYScale() {
		return (float)getHeight() / getCurrSlide().getHeight();
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
			//Log.i("tnr", "ACTION_POINTER_DOWN");
			firstTouchX = X;
			firstTouchY = Y;
			drawPathScaled.moveTo(X*getXScale(), Y*getYScale());
			drawPathOrig.moveTo(X, Y);
			break;
		case MotionEvent.ACTION_MOVE:
			// Log.i("tnr", "ACTION_MOVE");
			drawPathScaled.lineTo(prevTouchX*getXScale(), prevTouchY*getYScale());
			drawPathOrig.lineTo(prevTouchX, prevTouchY);		
		    break;
		case MotionEvent.ACTION_UP:
			//Log.i("tnr", "ACTION_UP");
			
			if (df.is_lineMode()) {
				scaled.drawLine(firstTouchX*getXScale(), firstTouchY*getYScale(), X*getXScale(), Y*getYScale(), this.getPaint(getXScale()));
				orig.drawLine(firstTouchX, firstTouchY, X, Y, this.getPaint(1));
			} else {
				scaled.drawPath(drawPathScaled, this.getPaint(getXScale())); 
				orig.drawPath(drawPathOrig, this.getPaint(1));
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
	
	// FIXME: bu fonksiyonlar baska yere tasinabilir
	public void setPaintColor(String newColor) {
		synchronized (inputHistory) {
			inputHistory.select_pen();
			inputHistory.setPenColor(newColor);
			inputSyncAndroid.sync(inputHistory);
		}
		df.select_pen(0);
		df.paintPen.setColor(newColor);
	}

	public void setSelectEraser(int no) {
		synchronized (inputHistory) {
			inputHistory.select_eraser(no);
			inputSyncAndroid.sync(inputHistory);
		}
		df.select_eraser(no);
	}
	
	public void setSelect_lineMode() {
		synchronized (inputHistory) {
			inputHistory.select_lineMode();
			inputSyncAndroid.sync(inputHistory);
		}
		df.select_lineMode();
	}
	
	public void setSelect_drawMode() {
		synchronized (inputHistory) {
			inputHistory.select_drawMode();
			inputSyncAndroid.sync(inputHistory);
		}
		df.select_drawMode();
	}
	
	public void setSelectPen() {
		synchronized (inputHistory) {
			inputHistory.select_pen();
			inputSyncAndroid.sync(inputHistory);
		}
		df.select_pen(0);
	}
	
	public void changeSlide(int no) {
		if (no < 0 || no >= slides.size())
			return;

		slideChanged = true;

		doChangeSlide(no);
		UtilAndroid.logHeap();
	}

	// karar butonuna basildiginda hangi slide da olundugunu karsiya gonder
	public void sendChangedSlide() {
		if(slideChanged) {
			slideChanged = false;
			synchronized (inputHistory) {
				inputHistory.changeSlide(_currSlideNo);
				inputSyncAndroid.sync(inputHistory);
			}
		}
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
		synchronized (inputHistory) {
			inputHistory.clear();
			
			// aktif ayarlari karsiya gondermek icin ekle
			inputHistory.setPenColor(df.paintPen.getColor_argb_html());
			if (df.is_eraserMode())
				inputHistory.select_eraser(df.getSelectedNo());
			else
				inputHistory.select_pen();
			inputHistory.setPenStrokeWidth(df.paintPen.getStrokeWidth());

			inputHistory.loadSlides(slideCount);
			inputSyncAndroid.sync(inputHistory);
		}
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
		df.paintPen.setStrokeWidth(df.paintPen.getStrokeWidth()+1);
		synchronized (inputHistory) {
			inputHistory.setPenStrokeWidth(df.paintPen.getStrokeWidth());
			inputSyncAndroid.sync(inputHistory);
		}
	}
	
	public void decStrokeWidth() {
		df.paintPen.setStrokeWidth(df.paintPen.getStrokeWidth()-1);
		synchronized (inputHistory) {
			inputHistory.setPenStrokeWidth(df.paintPen.getStrokeWidth());
			inputSyncAndroid.sync(inputHistory);
		}
	}

	public void redrawCurrSlide() {
		if (slides.size() == 0)
			return;
		doChangeSlide(_currSlideNo);
	}
	
	public Paint getPaint(float scale) {
		Paint paint = new Paint();
		
		paint.setColor(Color.parseColor(df.getCurrentPaint().getColor_argb_html()));
		paint.setStrokeWidth(df.getCurrentPaint().getStrokeWidth()*scale);
		
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setAntiAlias(true);
		paint.setDither(true); // hizlandirma yapabilir
		
		if (df.getCurrentPaint().getColor_argb_int() == Color.TRANSPARENT)
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		
		return paint;
	}
}
