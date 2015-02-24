package android_to_pc_presentation.shared;

public abstract class AbstractImageFunctions<ImageClassType> {
	public abstract ImageClassType readImage(String path, boolean editable);
	public abstract ImageClassType createImage_ARGB(int w, int h);
	public abstract ImageClassType createScaledImage(ImageClassType image, int w, int h);
	public abstract void saveImageToFile(ImageClassType image, String path) throws Exception;
	public abstract int getWidth(ImageClassType image);
	public abstract int getHeight(ImageClassType image);
}
