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
package com.example.presentation.PC;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

class CustomPaint {
	Composite composite;
	Color color;
	float strokeWidth;
	int strokeCap;
	int strokeRound;
	RenderingHints renderingHints;
	
	public CustomPaint(float strokeWidth, int strokeCap, int strokeRound, Color color, RenderingHints renderingHints, Composite composite) {
		this.strokeWidth = strokeWidth;
		this.strokeCap = strokeCap;
		this.strokeRound = strokeRound;
		this.composite = composite;
		this.color = color;
		this.renderingHints = renderingHints;
	}
	
	public CustomPaint(Graphics2D g2d) {
		this.composite = g2d.getComposite();
		this.color = g2d.getColor();
		this.renderingHints = g2d.getRenderingHints();
	}
	
	public void setPaintTo(Graphics2D g2d) {
		g2d.setComposite(composite);
		g2d.setColor(color);
		g2d.setStroke(new BasicStroke(strokeWidth, strokeCap, strokeRound));
		g2d.setRenderingHints(renderingHints);
	}
	
	public float getStrokeWidth() {
		return strokeWidth;
	}
	
	public void setStrokeWidth(float w) {
		strokeWidth = w;
	}
	
	public CustomPaint getScled(float scale) {
		return new CustomPaint(strokeWidth*scale, strokeCap, strokeRound, color, renderingHints, composite);
	}
}