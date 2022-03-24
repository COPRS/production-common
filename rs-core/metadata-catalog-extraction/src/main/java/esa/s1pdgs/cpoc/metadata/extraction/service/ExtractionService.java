package esa.s1pdgs.cpoc.metadata.extraction.service;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.filter.MessageFilter;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractor;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.metadata.extraction.config.MdcWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.MetadataExtractorFactory;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

public class ExtractionService implements Function<CatalogJob, CatalogEvent> {
	
	private final ErrorRepoAppender errorAppender;
	private final ProcessConfiguration processConfiguration;
	private final EsServices esServices;
	private final MdcWorkerConfigurationProperties properties;
//	private final MetadataExtractorFactory extractorFactory;

	@Autowired
	public ExtractionService(final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfiguration, final EsServices esServices,
			final MdcWorkerConfigurationProperties properties,
			final MetadataExtractorFactory extractorFactory) {
		this.errorAppender = errorAppender;
		this.processConfiguration = processConfiguration;
		this.esServices = esServices;
		this.properties = properties;
		//this.extractorFactory = extractorFactory;
	}
	
	@Override
	public CatalogEvent apply(CatalogJob catalogJob) {
		MissionId mission = null;

		if (catalogJob.getProductFamily().isSessionFamily()) {
			PathMetadataExtractor mExtractor = MetadataExtractorFactory
					.newPathMetadataExtractor(properties.getProductCategories().get(ProductCategory.EDRS_SESSIONS));
			mission = MissionId.valueOf(mExtractor.metadataFrom(catalogJob.getRelativePath()).get(MissionId.FIELD_NAME));
		} else {
			mission = MissionId.fromFileName(catalogJob.getKeyObjectStorage());
		}
		
		CatalogEvent result = new CatalogEvent();
		return result;
	}

}
