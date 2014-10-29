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

public class CustomPaint {

	// ortak
	private String color;
	protected int strokeWidth;
	
	public CustomPaint(String color, int strokeWidth) {
		this.setColor(color);
		this.strokeWidth = strokeWidth;
	}
	
	public int getStrokeWidth() {
		return strokeWidth;
	}
	
	public void setStrokeWidth(int w) {
		strokeWidth = w;
	}
	
	public void setColor(String color) {
		assert (color.length() == 9) && (color.charAt(0) == '#');
		this.color = color;
	}
	
	public int getColor_argb_int() {
		return (int) (Long.parseLong(this.color.substring(1), 16) & 0xffffffff);
	}
	
	public String getColor_argb_html() {
		return this.color;
	}
	
	public String getColor_rgb_hex() {
		return "0x"+this.color.substring(3);
	}	
}