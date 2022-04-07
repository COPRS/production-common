package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractor;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class EdrsMetadataExtractor extends AbstractMetadataExtractor {
	private final PathMetadataExtractor pathExtractor;

	public EdrsMetadataExtractor(final EsServices esServices, final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, final String localDirectory,
			final ProcessConfiguration processConfiguration, final ObsClient obsClient,
			final PathMetadataExtractor pathExtractor) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
		this.pathExtractor = pathExtractor;
	}

	@Override
	public JSONObject extract(final ReportingFactory reportingFactory, final CatalogJob catJob)
			throws AbstractCodedException {
		final ProductFamily family = ProductFamily.EDRS_SESSION;
		final File product = new File(this.localDirectory, catJob.getKeyObjectStorage());

		final EdrsSessionFileDescriptor edrsFileDescriptor = fileDescriptorBuilder
				.buildEdrsSessionFileDescriptor(product, additionalMetadataFor(catJob), catJob);
		// Only when it is a DSIB
		if (edrsFileDescriptor.getEdrsSessionFileType() == EdrsSessionFileType.SESSION) {
			downloadMetadataFileToLocalFolder(reportingFactory, family, catJob.getKeyObjectStorage());

			final String dsibName = new File(edrsFileDescriptor.getRelativePath()).getName();
			final File dsib = new File(localDirectory, dsibName);
			try {
				return mdBuilder.buildEdrsSessionFileMetadata(edrsFileDescriptor, dsib);
			} finally {
				FileUtils.delete(dsib.getPath());
			}
		}
		// RAW files
		return mdBuilder.buildEdrsSessionFileRaw(edrsFileDescriptor);
	}

	private Map<String, String> additionalMetadataFor(final CatalogJob catJob) {
		final Map<String, String> additionalMetadata = new LinkedHashMap<>();
		additionalMetadata.putAll(catJob.getAdditionalMetadata());

		// S1OPS-971: This is a workaround to keep the old requests working: Only if
		// additionalMetadata
		// is already provided with the CatalogJob, the path evaluation will be omitted
		// Once all metadata path based extraction is moved to ingestion trigger and
		// there are no
		// old requests within the system, this conditional check can be removed.
		if (additionalMetadata.isEmpty()) {
			for (final Map.Entry<String, String> entry : pathExtractor.metadataFrom(catJob.getRelativePath())
					.entrySet()) {
				additionalMetadata.put(entry.getKey(), entry.getValue());
			}
		}
		return additionalMetadata;
	}
}
