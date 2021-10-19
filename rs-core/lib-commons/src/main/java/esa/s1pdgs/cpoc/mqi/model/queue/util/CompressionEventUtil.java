package esa.s1pdgs.cpoc.mqi.model.queue.util;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CompressionEventUtil {
	
	public static final String SUFFIX_ZIPPRODUCTFAMILY = "_ZIP";
	public static final String SUFFIX_ZIPPPRODUCTFILE = ".zip";
	
	
	public static String composeCompressedKeyObjectStorage(final String inputKeyObjectStorage) {
		return inputKeyObjectStorage + SUFFIX_ZIPPPRODUCTFILE;
	}

	public static ProductFamily composeCompressedProductFamily(final ProductFamily inputFamily) {
		return ProductFamily.fromValue(inputFamily.toString() + SUFFIX_ZIPPRODUCTFAMILY);
	}
	
	public static ProductFamily removeZipSuffixFromProductFamily(final ProductFamily productFamily) {
		return ProductFamily.fromValue(removeZipSuffix(productFamily.toString()));
	}
	
	public static String removeZipSuffix(final String name) {
		if (name.toLowerCase().endsWith(SUFFIX_ZIPPPRODUCTFILE)) {
			return name.substring(0, name.length() - SUFFIX_ZIPPPRODUCTFILE.length());
		} else if (name.toUpperCase().endsWith(SUFFIX_ZIPPRODUCTFAMILY)) {
			return name.substring(0, name.length() - SUFFIX_ZIPPRODUCTFAMILY.length());
		}
		return name;
	}

}
