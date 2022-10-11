package esa.s1pdgs.cpoc.mqi.model.queue.util;

import java.util.List;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.metadata.model.MissionId;

public class CompressionEventUtil {
	public static final List<String> COMPRESSION_SUFFIXES = List.of(
			".zip",
			".tgz",
			".tar.gz",
			".tar"
	);	
	
	public static final String SUFFIX_ZIPPRODUCTFAMILY = "_ZIP";
	public static final String SUFFIX_ZIPPPRODUCTFILE = ".zip";
	public static final String SUFFIX_TARPRODUCTFILE = ".tar";
	
	
	public static String composeCompressedKeyObjectStorage(final String inputKeyObjectStorage,
			final MissionId mission) {

		switch (mission) {
			case S1:
			case S3:
				return inputKeyObjectStorage + SUFFIX_ZIPPPRODUCTFILE;
			case S2:
				return inputKeyObjectStorage + SUFFIX_TARPRODUCTFILE;
			default:
				throw new IllegalArgumentException("Not applicable mission " + mission);
		}
	}
	
	public static String removeZipFromKeyObjectStorage(final String inputKeyObjectStorage) {
		return removeSuffixFromFilename(inputKeyObjectStorage);
	}

	public static ProductFamily composeCompressedProductFamily(final ProductFamily inputFamily) {
		return ProductFamily.fromValue(inputFamily.toString() + SUFFIX_ZIPPRODUCTFAMILY);
	}
	
	public static ProductFamily removeZipSuffixFromProductFamily(final ProductFamily productFamily) {
		return ProductFamily.fromValue(removeZipFromFamily(productFamily));
	}
	
	public static final boolean isCompressed(final String name) {
		for (final String suffix : COMPRESSION_SUFFIXES) {
			if (name.toLowerCase().endsWith(suffix)) {
				return true;
			} 
		}
		return false;
	}
	
	private static final String removeSuffixFromFilename(final String name) {
		for (final String suffix : COMPRESSION_SUFFIXES) {
			if (name.toLowerCase().endsWith(suffix)) {
				return name.substring(0, name.length() - suffix.length());
			} 
		}
		return name;
	}
	
	private static final String removeZipFromFamily(final ProductFamily productFamily) {
		final String name = productFamily.toString();
		if (name.toUpperCase().endsWith(SUFFIX_ZIPPRODUCTFAMILY)) {
			return name.substring(0, name.length() - SUFFIX_ZIPPRODUCTFAMILY.length());
		}
		return name;
	}
}
