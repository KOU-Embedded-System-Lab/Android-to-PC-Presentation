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
package android_to_pc_presentation.shared;

import java.io.Serializable;
import java.util.ArrayList;

public class InputHistory {
	
	public void addRefresh() {
		events.add(null);
	}
	
	public void setStrokeWidth(int value) {
		events.add(new ModeSelect(ModeSelect.Type.STROKE_WIDTH.ordinal(), value));	
	}
	
	public void setPaintColor(String value) {
		events.add(new ModeSelect(ModeSelect.Type.PAINT_COLOR.ordinal(), (int)Long.parseLong(value.substring(1), 16)));
	}

	public void selectEraser() {
		events.add(new ModeSelect(ModeSelect.Type.SELECT_ERASER.ordinal(), 0));
	}
	
	public void selectPen() {
		events.add(new ModeSelect(ModeSelect.Type.SELECT_PEN.ordinal(), 0));
	}
	
	public void changeSlide(int slideNo) {
		events.add(new ModeSelect(ModeSelect.Type.CHANGE_SLIDE.ordinal(), slideNo));
	}

	public void loadSlides(int slideCount) {
		events.add(new ModeSelect(ModeSelect.Type.LOAD_SLIDES.ordinal(), slideCount));
	}
	
	public void addTouch(int time, int action, float touchX, float touchY) {
		events.add(new TouchRecord(time, action, touchX, touchY));
	}
	
	public void add(ModeSelect modeSelect) {
		events.add(modeSelect);
	}
	
	public void add(TouchRecord touchRecord) {
		events.add(touchRecord);
	}
	
	public ArrayList<Object> events = new ArrayList<Object>();
	
	public void clear() {
		events.clear();
	}
	
	public static class TouchRecord implements Serializable {
		private static final long serialVersionUID = -9011883701553083654L;
		public final int time;
		public final int action;
		public final float x;
		public final float y;
		public TouchRecord(int time, int action, float x, float y) {
			this.time = time;
			this.action = action;
			this.x = x;
			this.y = y;
		}
	}

	public static class ModeSelect implements Serializable {
		private static final long serialVersionUID = 2915838316506588110L;

		enum Type {
			PAINT_COLOR,
			STROKE_WIDTH,
			SELECT_PEN,
			SELECT_ERASER,
			CHANGE_SLIDE,
			LOAD_SLIDES,
			DRAW_ENABLED
		}

		protected int type;
		protected int value;
		
		public int getValue() {
			return value;
		}

		ModeSelect(int type, int value) {
			this.type = type;
			this.value = value;
		}
		
		public boolean isPaintColor() {
			return this.type == Type.PAINT_COLOR.ordinal();
		}
		
		public boolean isStrokeWidth() {
			return this.type == Type.STROKE_WIDTH.ordinal();
		}
		
		public boolean isSelectPen() {
			return this.type == Type.SELECT_PEN.ordinal();
		}
		
		public boolean isSelectEraser() {
			return this.type == Type.SELECT_ERASER.ordinal();
		}

		public boolean isChangeSlide() {
			return this.type == Type.CHANGE_SLIDE.ordinal();
		}
		
		public boolean isLoadSlides() {
			return this.type == Type.LOAD_SLIDES.ordinal();
		}
		
	}
	
}
