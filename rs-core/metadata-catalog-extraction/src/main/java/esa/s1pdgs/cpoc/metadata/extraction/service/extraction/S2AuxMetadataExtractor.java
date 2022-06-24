package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.util.S2ProductNameUtil;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class S2AuxMetadataExtractor extends AbstractMetadataExtractor {

	private static final Logger LOG = LogManager.getLogger(S2AuxMetadataExtractor.class);

	private final boolean enableExtractionFromProductName;

	public S2AuxMetadataExtractor(final EsServices esServices, final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, final String localDirectory,
			final boolean enableExtractionFromProductName, final ProcessConfiguration processConfiguration,
			final ObsClient obsClient) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
		this.enableExtractionFromProductName = enableExtractionFromProductName;
	}

	@Override
	public JSONObject extract(ReportingFactory reportingFactory, CatalogJob catalogJob) throws AbstractCodedException {
		if (enableExtractionFromProductName) {
			LOG.trace("Extracting metadata from product name: {}", catalogJob.getProductName());
			JSONObject metadata = S2ProductNameUtil.extractMetadata(catalogJob.getProductName());
			metadata.put("productFamily", catalogJob.getProductFamily().name());
			metadata.put("url", catalogJob.getKeyObjectStorage());
			return metadata;
		} else {
			LOG.error("S2AuxMetadataExtractor only supports extraction from product name");
			throw new UnsupportedOperationException();
		}
	}

}
