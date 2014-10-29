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
package android_to_pc_presentation.PC;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import android_to_pc_presentation.shared.CustomPaint;
import android_to_pc_presentation.shared.DrawingFunctons;
import android_to_pc_presentation.shared.InputHistory;

public class SlideView extends JPanel {
	
	private static final long serialVersionUID = 5458568983898307239L;
	
	public DrawingFunctons df;
	public InputHistory inputHistory = new InputHistory();
	public InputSyncPC inputSyncPC;
	
	/** sunum katmani */
	BufferedImage _slideImageScaled = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	Graphics2D slideImageScaled = _slideImageScaled.createGraphics();
	/** cizim katmani */
	BufferedImage _drawImageScaled = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	Graphics2D drawImageScaled = _drawImageScaled.createGraphics();
	
	GeneralPath drawPathScaled= new GeneralPath(GeneralPath.WIND_NON_ZERO, 1);
	GeneralPath drawPathOrig = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1);
	
	float prevTouchX, prevTouchY;
	
	/** slayt listesi */
	private ArrayList<Slide> slides = new ArrayList<Slide>();
	private int _currSlideNo = 0;

	/* yenileme timeri degiskenleri */
	private long lastDrawTime = 0;
	private Timer repaintTimer;

	public SlideView(String redrawFile) {
		setupDrawing();
		
		startSyncThread(redrawFile);
	}
	
	/* FIXME: threadin yeniden baslatilmasi gerektiginde programin yeniden baslatilmasi gerekiyor
	 * threadin yeniden baslatilmasi gerekmedigi icin simdilik problem yok
	 */
	void startSyncThread(String redrawFile) {
		if (inputSyncPC != null)
			return;
		inputSyncPC = new InputSyncPC(this, redrawFile);
		new Thread(new Runnable() {
			@Override
			public void run() {
				inputSyncPC.run();
				System.out.println("run error");
			}
		}).start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				inputSyncPC.run_ui();
				System.out.println("run error");
			}
		}).start();
	}
	
	void setupDrawing() {
		
		df = new DrawingFunctons(
				new CustomPaint("#ff000000", 4),
				new CustomPaint("#00000000", 80)
				);
	}

	void doRedraw() {
		if (System.currentTimeMillis() - lastDrawTime < 50)
			return;
		lastDrawTime = System.currentTimeMillis();

		/* 250 ms sonrasina timer koy (kalintilarin gitmesi icin bir kez daha yenileniyor) */ 
		if (repaintTimer != null)
			repaintTimer.cancel();
		repaintTimer = new Timer();
		repaintTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("timer run");
				if (System.currentTimeMillis() - lastDrawTime > 50)
					repaint();
				repaintTimer = null;
			}
		}, 250);
		
		/* ekrani yenile */
		repaint();
	}
	
	int windowWidth = getWidth(), windowHeight = getHeight();
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (this.getWidth() != _drawImageScaled.getWidth() || this.getHeight() != _drawImageScaled.getHeight()) {
			System.out.println("imageScaled size changed");
			windowWidth = getParent().getWidth();
			windowHeight = getParent().getHeight();
			calculateBounds();
			try {
				doChangeSlide(_currSlideNo);
			} catch (Exception e) {
				return;
			}
		}
		
		// System.out.println("paintComponen");
		Graphics2D g2 = (Graphics2D)g;
		
		/* sunum resmini ekrana ciz */
		g2.drawImage(_slideImageScaled, null, 0, 0);

		/* cizim resmini ekrana ciz */
		g2.drawImage(_drawImageScaled, null, 0, 0);
		
		/* son cizimi ekrana ciz */
		this.setPaintTo(g2, 1.0f/getXRatio());
		g2.draw(drawPathScaled);
	}
	
	public void doChangeSlide(int no) throws Exception {
		System.out.println("doChangeSlide() " + no + " / " + slides.size());
		if (no < 0 || no >= slides.size())
			return;
		
		slides.get(_currSlideNo).saveDrawImage();
		slides.get(_currSlideNo).closeDrawImage();
		
		_currSlideNo = no;
		Slide slide = slides.get(no);
		
		System.out.println(""+getWidth() + " " + getHeight());
		System.out.println(""+windowWidth + " " + windowHeight);
		
		slideImageScaled.dispose();
		drawImageScaled.dispose();
		System.gc();

		_slideImageScaled = slide.getScaledBackgroundImage(getWidth(), getHeight()); 
		slideImageScaled = _slideImageScaled.createGraphics();
		try {
			_drawImageScaled = slide.getScaledDrawImage(getWidth(), getHeight());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		drawImageScaled = _drawImageScaled.createGraphics();
		doRedraw();
	}

	
	public float getXRatio() {
		if (slides.size() == 0)
			return 0;
		return getCurrSlide().getWidth() / (float)getWidth();
	}
	
	public float getYRatio() {
		if (slides.size() == 0)
			return 0;
		return getCurrSlide().getHeight() / (float)getHeight();
	}
	
	private Slide getCurrSlide() {
		if (slides.size() == 0)
			return null;
		return slides.get(_currSlideNo);
	}
	
	public void doTouchEvent(int action, float X, float Y) {
		// System.out.println("doTouchEvent " + action + " " + X + " " + Y);
		if (slides.size() == 0)
			return;
		
		Graphics2D orig;
		try {
			orig = (Graphics2D)getCurrSlide().getDrawImage().getGraphics();
		} catch (Exception e) {
			// TODO: error
			return;
		}
		
		Graphics2D scaled = drawImageScaled;

		// FIXME: convert hardcoded case values to enum
		switch (action) {
		case 0:
			drawPathScaled.moveTo(X/getXRatio(), Y/getYRatio());
			drawPathOrig.moveTo(X, Y);
			break;
		case 2:
			// Log.i("tnr", "ACTION_MOVE");
			drawPathScaled.lineTo(prevTouchX/getXRatio(), prevTouchY/getYRatio());
			drawPathOrig.lineTo(prevTouchX, prevTouchY);		
		    break;
		case 1:
		
			this.setPaintTo(drawImageScaled, 1.0f/getXRatio());
			this.setPaintTo(orig, 1);

			orig.draw(drawPathOrig);
			scaled.draw(drawPathScaled);	
			drawPathScaled.reset();
			drawPathOrig.reset();
			
			break;
		default:
			break;
		}
		
		prevTouchX = X;
		prevTouchY = Y;
		doRedraw();
	}

	public void doSelectEraser(int no) {
		System.out.println("doSelectEraser");
		df.select_eraser(no);
	}
	
	public void doSelectPen(int no) {
		System.out.println("doSelectPen");
		df.select_pen(no);
	}
	
	public void calculateBounds() {
		int w = getWidth();
		int h = getHeight();
		float slideRatio = 1067.0f / 800.0f;
		float screenRatio = (float) w / (float) h;
		if (screenRatio > slideRatio)
			w = Math.round(h * slideRatio);
		else
			h = Math.round(w / slideRatio);
		this.setBounds((windowWidth-w)/2, (windowHeight-h)/2, w, h);
	}
	
	public void loadSlides(String path, int fileCount) throws Exception {
		System.out.println("loadSlides");
		slides.subList(0, slides.size()).clear();
		
		for (int i = 0 ; ; i++) {
			String fileName = "x-"+i+".png";
			File f = new File(path + fileName);
			if (!f.isFile())
				break; // System.out.println("slide is not file: " + f.getAbsolutePath());
			slides.add(new Slide(f.getAbsolutePath()));
		}
		doChangeSlide(0);
	}
	
	public void setPaintTo(Graphics2D g2d, float scale) {
		
		g2d.setColor(Color.decode(df.getCurrentPaint().getColor_rgb_hex()));
		g2d.setStroke(new BasicStroke(df.getCurrentPaint().getStrokeWidth()*scale, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
		g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		
		if ((df.getCurrentPaint().getColor_argb_int()&0xff000000) == 0)
			g2d.setComposite(AlphaComposite.Clear);
		else
			g2d.setComposite(AlphaComposite.Src);
		
	}
	
}
