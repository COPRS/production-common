package esa.s1pdgs.cpoc.dissemination.worker.path;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.dissemination.worker.service.DisseminationException;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public class MyOceanPathEvaluator implements PathEvaluator {

	private static final String PATTERN_STR = "^(S1|AS)(A|B)_(S[1-6]|IW|EW|WV|GP|HK|N[1-6]|EN|IM)_(SLC|GRD|OCN|RAW)(F|H|M|_)_(0|1|2)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(\\.zip|/.*)?$";
	private static final Pattern PATTERN = Pattern.compile(PATTERN_STR, Pattern.CASE_INSENSITIVE);

	// --------------------------------------------------------------------------

	@Override
	public Path outputPath(final String basePath, final ObsObject obsObject) {
		//		final String sourceFilename = getFilename(obsObject);
		//		if (sourceFilename.contains("manifest")) {
		//			throw new IllegalArgumentException(
		//					"MyOceanPathEvaluator does not support retrieving output path from manifest file! Use product file instead.");
		//		}

		final LocalDateTime sensingStart = getSensingStart(obsObject.getKey());
		final String year = String.valueOf(sensingStart.getYear());
		final String month = fixedLengthString(2, '0', String.valueOf(sensingStart.getMonthValue()));
		final String day = fixedLengthString(2, '0', String.valueOf(sensingStart.getDayOfMonth()));

		// e.g. /data/public/2020/12/14/
		return Paths.get(basePath, year, month, day);
	}

	@Override
	public String outputFilename(final ObsObject mainFile, final ObsObject sourceFile) {
		final String sourceFilename = getFilename(sourceFile);

		if (sourceFilename.contains("manifest")) {
			// renaming to [mainFilename].manifest
			final String mainFilename = getFilename(mainFile);
			return mainFilename + ".manifest";
		} else {
			return sourceFilename;
		}
	}

	// --------------------------------------------------------------------------

	private static String getFilename(final ObsObject file) {
		Path path = Paths.get(file.getKey()).getFileName();
		if (null == path) {
			throw new RuntimeException(String.format("Cannot get filename due to corrupt obs object: %s", file));
		}
		return path.toString();
	}

	private static LocalDateTime getSensingStart(final String productName) {
		final Matcher m = PATTERN.matcher(productName);

		if (m.matches()) {
			try {
				return DateUtils.parse(m.group(9));
			} catch (final Exception e) {
				throw new DisseminationException(
						"unable to extract sensing start from product " + productName + ": " + e.getMessage(), e);
			}
		} else {
			throw new DisseminationException("unable to extract sensing start as product name " + productName
					+ " does not match pattern " + PATTERN_STR);
		}
	}

	private static String fixedLengthString(final int width, final char fillChar, final String str) {
		return new String(new char[width - str.length()]).replace('\0', fillChar) + str;
	}

}
