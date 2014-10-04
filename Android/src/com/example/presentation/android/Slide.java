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
package com.example.presentation.android;

import com.example.presentation.shared.SlideBase;

import android.graphics.Bitmap;

public class Slide extends SlideBase<Bitmap> {

	public Slide(String backgroundFilePath) {
		super(UtilAndroid.imageFunctons, backgroundFilePath);
	}
	
	@Override
	public void saveDrawImage() {
		// android uygulamasinda degisiklikler kaydedilmeyecek. her slayt gecisinde resim tekrar indiriliyor
	}
	
}