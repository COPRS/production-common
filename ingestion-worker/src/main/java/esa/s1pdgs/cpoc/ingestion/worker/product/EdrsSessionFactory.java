package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.worker.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class EdrsSessionFactory implements ProductFactory<IngestionEvent> {

	final static String PATTERN_STR_XML = ".*(DCS_[0-9]{2}_([a-zA-Z0-9_]{24})_ch([12])_DSIB\\.(xml))";
	final static String PATTERN_STR_RAW = ".*(DCS_[0-9]{2}_([a-zA-Z0-9_]{24})_ch([12])_DSDB_([0-9]{5})\\.(raw|aisp))";

	protected final Pattern xmlpattern = Pattern.compile(PATTERN_STR_XML);
	protected final Pattern rawpattern = Pattern.compile(PATTERN_STR_RAW);

	private final String hostname;
	
	public EdrsSessionFactory(final String hostname) {
		this.hostname = hostname;
	}
	
	@Override
	public List<Product<IngestionEvent>> newProducts(final File file, final IngestionJob ingestionJob,
			final ObsAdapter obsAdapter) throws ProductException {

		final List<Product<IngestionEvent>> result = new ArrayList<>();

		String objectStorageKey = ingestionJob.getRelativePath();
		int channelId = extractChannelId(ingestionJob.getRelativePath());
		EdrsSessionFileType edrsSessionFileType = extractEdrsSessionFileType(ingestionJob.getRelativePath());
		String missionId = ingestionJob.getMissionId();
		String satelliteId = ingestionJob.getSatelliteId();
		String stationCode = ingestionJob.getStationCode();
		String sessionId = extractSessionId(ingestionJob.getRelativePath());

		IngestionEvent ingestionEvent = new IngestionEvent(objectStorageKey, file.getPath(), channelId, edrsSessionFileType, missionId,
				satelliteId, stationCode, sessionId);
		ingestionEvent.setCreationDate(LocalDateTime.now());
		ingestionEvent.setHostname(hostname);

		final Product<IngestionEvent> prod = new Product<>();
		prod.setFamily(ProductFamily.EDRS_SESSION);
		prod.setFile(file);
		prod.setDto(ingestionEvent);

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
