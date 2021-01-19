package esa.s1pdgs.cpoc.dissemination.worker.path;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.dissemination.worker.service.DisseminationException;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public class MbuPathEvaluator implements PathEvaluator {

	private static final String PATTERN_STR = "^(s1)(a|b)-([0-9a-z]{2})([0-9a-z]{1})-(mbu)-()()(vv|hh)-([0-9a-z]{15})-([0-9a-z]{15})-([0-9]{6})-([a-z0-9]{6})-([0-9]{3})_([A-Z0-9]{4})\\.(bufr)$";
	private static final Pattern PATTERN = Pattern.compile(PATTERN_STR, Pattern.CASE_INSENSITIVE);

	// --------------------------------------------------------------------------

	@Override
	public Path outputPath(final String basePath, final ObsObject obsObject) {

		// sensing start format: 20190628t190957, we need YYMMDD
		final String sensingStart = getSensingStart(obsObject.getKey());
		final String year = sensingStart.substring(2,4);
		final String month = sensingStart.substring(4, 6);
		final String day = sensingStart.substring(6, 8);

		// e.g. /data/public/METEO/190628
		return Paths.get(basePath, year + month + day);
	}

	@Override
	public String outputFilename(final ObsObject mainFile, final ObsObject sourceFile) {
		return getFilename(sourceFile);
	}

	private static String getFilename(final ObsObject file) {
		return Paths.get(file.getKey()).getFileName().toString();
	}

	private static String getSensingStart(final String productName) {
		final Matcher m = PATTERN.matcher(productName);

		if (m.matches()) {
			return m.group(9);
		} else {
			throw new DisseminationException("unable to extract sensing start as product name " + productName
					+ " does not match pattern " + PATTERN_STR);
		}
	}

}
