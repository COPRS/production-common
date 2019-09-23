package esa.s1pdgs.cpoc.ingestion.product;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

public class EdrsSessionFactory implements ProductFactory<EdrsSessionDto> {

	final static String PATTERN_STR_XML = ".*(DCS_[0-9]{2}_([a-zA-Z0-9_]{24})_ch([12])_DSIB\\.(xml))";
	final static String PATTERN_STR_RAW = ".*(DCS_[0-9]{2}_([a-zA-Z0-9_]{24})_ch([12])_DSDB_([0-9]{5})\\.(raw|aisp))";

	protected final Pattern xmlpattern = Pattern.compile(PATTERN_STR_XML);
	protected final Pattern rawpattern = Pattern.compile(PATTERN_STR_RAW);

	private final String hostname;
	
	public EdrsSessionFactory(final String hostname) {
		this.hostname = hostname;
	}
	
	@Override
	public List<Product<EdrsSessionDto>> newProducts(final File file, final IngestionDto ingestionDto,
			final ObsAdapter obsAdapter) throws ProductException {

		final List<Product<EdrsSessionDto>> result = new ArrayList<>();

		String objectStorageKey = ingestionDto.getRelativePath();
		int channelId = extractChannelId(ingestionDto.getRelativePath());
		EdrsSessionFileType edrsSessionFileType = extractEdrsSessionFileType(ingestionDto.getRelativePath());
		String missionId = ingestionDto.getMissionId();
		String satelliteId = ingestionDto.getSatelliteId();
		String stationCode = ingestionDto.getStationCode();
		String sessionId = extractSessionId(ingestionDto.getRelativePath());

		EdrsSessionDto edrsSessionDto = new EdrsSessionDto(objectStorageKey, channelId, edrsSessionFileType, missionId,
				satelliteId, stationCode, sessionId);
		edrsSessionDto.setHostname(hostname);

		final Product<EdrsSessionDto> prod = new Product<>();
		prod.setFamily(ProductFamily.EDRS_SESSION);
		prod.setFile(file);
		prod.setDto(edrsSessionDto);

		result.add(prod);
		return result;
	}

	int extractChannelId(String relativePath) {
		Matcher xmlmatcher = xmlpattern.matcher(relativePath);
		Matcher rawmatcher = rawpattern.matcher(relativePath);

		if (xmlmatcher.matches()) {
			return Integer.parseInt(xmlmatcher.group(3));
		} else if (rawmatcher.matches()) {
			return Integer.parseInt(rawmatcher.group(3));
		} else {
			throw new IllegalArgumentException(String.format("can not match %s", relativePath));
		}
	}

	EdrsSessionFileType extractEdrsSessionFileType(String relativePath) {

		Matcher xmlmatcher = xmlpattern.matcher(relativePath);
		Matcher rawmatcher = rawpattern.matcher(relativePath);

		if (xmlmatcher.matches()) {
			return EdrsSessionFileType.valueFromExtension(FileExtension.valueOfIgnoreCase(xmlmatcher.group(4)));
		} else if (rawmatcher.matches()) {
			return EdrsSessionFileType.valueFromExtension(FileExtension.valueOfIgnoreCase(rawmatcher.group(5)));
		} else {
			throw new IllegalArgumentException(String.format("can not match %s", relativePath));
		}
	}
	
	String extractSessionId(String relativePath) {
		Matcher xmlmatcher = xmlpattern.matcher(relativePath);
		Matcher rawmatcher = rawpattern.matcher(relativePath);

		if (xmlmatcher.matches()) {
			return xmlmatcher.group(2);
		} else if (rawmatcher.matches()) {
			return rawmatcher.group(2);
		} else {
			throw new IllegalArgumentException(String.format("can not match %s", relativePath));
		}
	}

}
