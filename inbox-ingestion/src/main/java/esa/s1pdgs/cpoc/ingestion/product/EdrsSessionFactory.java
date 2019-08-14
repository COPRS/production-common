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

	private final static String PATTERN_STR =
    		"^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW|AISP))$";

	protected final Pattern pattern = Pattern.compile(PATTERN_STR, Pattern.CASE_INSENSITIVE);

	@Override
	public List<Product<EdrsSessionDto>> newProducts(final File file, final IngestionDto ingestionDto,
			final ObsAdapter obsAdapter) throws ProductException {

		final List<Product<EdrsSessionDto>> result = new ArrayList<>();
		Matcher matcher = pattern.matcher(ingestionDto.getRelativePath());

		String objectStorageKey = ingestionDto.getRelativePath();
		int channelId  = Integer.parseInt(matcher.group(7));
		EdrsSessionFileType edrsSessionFileType = EdrsSessionFileType.valueFromExtension(FileExtension.valueOfIgnoreCase(matcher.group(12)));
		String missionId = ingestionDto.getMissionId();
		String satelliteId = ingestionDto.getSatelliteId();
		String stationCode = ingestionDto.getStationCode();

		EdrsSessionDto edrsSessionDto = new EdrsSessionDto(objectStorageKey, channelId, edrsSessionFileType, missionId,
				satelliteId, stationCode);
		
		final Product<EdrsSessionDto> prod = new Product<>();
		prod.setFamily(ProductFamily.EDRS_SESSION);
		prod.setFile(file);
		prod.setDto(edrsSessionDto);
		
		result.add(prod);
		return result;
	}

}
