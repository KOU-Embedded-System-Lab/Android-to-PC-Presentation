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

public abstract class SlideBase<ImageClassType> {
	
	private String backgroundFilePath = null;
	private String drawImagePath = null;
	
	private ImageClassType drawImage = null;
	private int _width = 0, _height = 0;
	
	private final AbstractImageFunctions<ImageClassType> imageFunctions;
	
	public SlideBase(final AbstractImageFunctions<ImageClassType> imageFunctions, String backgroundFilePath) {
		this.backgroundFilePath = backgroundFilePath;
		this.imageFunctions = imageFunctions;
		this.drawImagePath = backgroundFilePath + ".draw";
	}
	
	public ImageClassType getBackgroundImage() {
		ImageClassType bmp = imageFunctions.readImage(backgroundFilePath, false);
		_width = imageFunctions.getWidth(bmp);
		_height = imageFunctions.getHeight(bmp);
		return bmp;
	}
	
	public ImageClassType getDrawImage() throws Exception {
		if (drawImage == null)
			drawImage = imageFunctions.readImage(drawImagePath, true);
		if (drawImage == null) {
			System.out.println("getDrawImage() creating new image");
			drawImage = imageFunctions.createImage_ARGB(getWidth(), getHeight());
			imageFunctions.saveImageToFile(drawImage, drawImagePath);
		}
		assert drawImage != null;
		return drawImage;
	}

	public void saveDrawImage() throws Exception {
		System.out.println("closeDrawImage() " + drawImagePath);
		if (drawImage == null)
			return;
		assert drawImagePath != null;
		imageFunctions.saveImageToFile(drawImage, drawImagePath);
	}
	
	public void closeDrawImage() {
		drawImage = null;
	}

	public ImageClassType getScaledBackgroundImage(int w, int h) {
		ImageClassType bmp = getBackgroundImage();
		ImageClassType r = imageFunctions.createScaledImage(bmp, w, h);
		return r;
	}

	public ImageClassType getScaledDrawImage(int w, int h) throws Exception {
		return imageFunctions.createScaledImage(getDrawImage(), w, h);
	}
	
	public int getWidth() {
		if (_width == 0)
			getBackgroundImage();
		return _width;
	}

	public int getHeight() {
		if (_height == 0)
			getBackgroundImage();
		return _height;
	}
}
