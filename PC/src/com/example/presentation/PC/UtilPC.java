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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.example.presentation.shared.AbstractImageFunctions;

public class UtilPC {

	public static BufferedImage imageToBufferedImage(Image image) {
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		return bufferedImage;
	}

	/** RGB icersinde belirlenmis bir rengi transparan yapar */
	public static Image makeColorTransparent(BufferedImage im, final Color color) {
		ImageFilter filter = new RGBImageFilter() {
			/* ARGB formatinda A = 255 ve RGB olarak ekleniyor */
			public final int markerRGB = color.getRGB() | 0xFF000000;
			public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == markerRGB)
					return 0x00FFFFFF & rgb; // eger renk belirlenen renk ise transparan yapar
				else
					return rgb; // degilse aynen birakir
			}
		};
		ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}
	
	public static BufferedImage imageToTransparentPNG(BufferedImage image) {
		Image transparent = makeColorTransparent(image, new Color(0xffffff));
		return imageToBufferedImage(transparent);
	}
	
	public final static AbstractImageFunctions<BufferedImage> imageFunctions = new AbstractImageFunctions<BufferedImage>() {
		@Override
		public BufferedImage readImage(String path, boolean editable) {
			System.out.println("read image: " + path);
			try {
				return toCompatibleImage(ImageIO.read(new File(path)));
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		public BufferedImage createImage_ARGB(int w, int h) {
			return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		}

		@Override
		public void saveImageToFile(BufferedImage image, String path) throws Exception {
			File out = new File(path);
			ImageIO.write(imageToTransparentPNG(image), "PNG", out);
			System.out.println("saved image:" + path);
		}

		@Override
		public int getWidth(BufferedImage image) {
			return image.getWidth();
		}

		@Override
		public int getHeight(BufferedImage image) {
			return image.getHeight();
		}

		@Override
		public BufferedImage createScaledImage(BufferedImage image, int w, int h) {
			AffineTransform at = new AffineTransform();
			BufferedImage r = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			at.scale((float) w / image.getWidth(), (float) h / image.getHeight());
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			return toCompatibleImage(scaleOp.filter(image, r));
		}

		/* kaynak: http://stackoverflow.com/questions/196890/java2d-performance-issues */
		private BufferedImage toCompatibleImage(BufferedImage image) {
			// obtain the current system graphical settings
			GraphicsConfiguration gfx_config = GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getDefaultScreenDevice().getDefaultConfiguration();

			/*
			 * if image is already compatible and optimized for current system
			 * settings, simply return it
			 */
			if (image.getColorModel().equals(gfx_config.getColorModel()))
				return image;

			// image is not optimized, so create a new image that is
			BufferedImage new_image = gfx_config.createCompatibleImage(image.getWidth(), image.getHeight(),
					image.getTransparency());

			// get the graphics context of the new image to draw the old image
			// on
			Graphics2D g2d = (Graphics2D) new_image.getGraphics();

			// actually draw the image and dispose of context no longer needed
			g2d.drawImage(image, 0, 0, null);
			g2d.dispose();

			// return the new optimized image
			return new_image;
		}
	};
}
