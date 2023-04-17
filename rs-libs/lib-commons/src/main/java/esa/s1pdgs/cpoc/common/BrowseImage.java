package esa.s1pdgs.cpoc.common;

public class BrowseImage {

	private BrowseImage() {}
	
	public static final String S1_BROWSE_IMAGE_DIRECTORY = "preview";
	public static final String S1_BROWSE_IMAGE_FORMAT = ".png";
	public static final String BROWSE_IMAGE_TAG = "_bwi";
	
	public static String s1BrowseImageName(String productName) {
		return productName + BROWSE_IMAGE_TAG + S1_BROWSE_IMAGE_FORMAT; 
	}
	
	public static String browseImagePrefix(String productName) {
		return productName + BROWSE_IMAGE_TAG;
	}
	
}
