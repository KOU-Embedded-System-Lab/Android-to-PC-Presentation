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

public class DrawingFunctons {
	
	public CustomPaint paintPen;
	public CustomPaint paintEraser;
	public CustomPaint paintEraser_small;

	boolean eraserMode = false;
	boolean lineMode = false;
	
	int selectedNo = 0;
	
	public int getSelectedNo() {
		return selectedNo;
	}

	public DrawingFunctons(CustomPaint paintPen, CustomPaint paintEraser) {
		this.paintPen = paintPen;
		this.paintEraser = paintEraser;
		this.paintEraser_small = new CustomPaint(paintEraser.getColor_argb_html(), paintPen.getStrokeWidth()*SharedConfig.DEFAULT_ERASER_0_PEN_RATIO);
	}
	
	public void select_pen(int no) {
		eraserMode = false;
		selectedNo = no;
	}
	
	public void select_eraser(int no) {
		eraserMode = true;
		selectedNo = no;
	}

	public boolean is_eraserMode() {
		return eraserMode;
	}
	
	public void select_drawMode() {
		lineMode = false;
	}
	
	public void select_lineMode() {
		lineMode = true;
	}
	
	public boolean is_lineMode() {
		return lineMode;
	}
	
	
	public CustomPaint getCurrentPaint() {
		if (eraserMode) {
			if (selectedNo == 0)
				return paintEraser_small;
			return paintEraser; 
		}
		return paintPen;
	}
}
