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
	
	String paintColor = "#ff000000";
	float strokeWidth = 4;
	float strokeWidthEraser = 80;
	boolean eraserMode = false;
	
	public void selectEraser() {
		eraserMode = true;
	}

	public void selectPen() {
		eraserMode = false;
	}
	
	public boolean isEraserMode() {
		return eraserMode;
	}
	
	public void setPaintColor(String newColor) {
		paintColor = newColor;
	}
	
	public String getPaintColor() {
		return paintColor;
	}
	
	public String getPaintColor_rgb() {
		assert paintColor.length() == 9;
		return paintColor.substring(3);
	}
	
	public void incStrokeWidth() {
		strokeWidth += 1;
	}
	
	public void decStrokeWidth() {
		strokeWidth -= 1;
	}
	
	public float getStrokeWidth() {
		return strokeWidth;
	}
	
	public float getScaledStrokeWidth(float xRatio) {
		return strokeWidth / xRatio;
	}
	
	public float getStrokeWidthEraser() {
		return strokeWidthEraser;
	}
	
	public float getScaledStrokeWidthEraser(float xRatio) {
		return strokeWidthEraser / xRatio;
	}
}
