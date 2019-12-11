package esa.s1pdgs.cpoc.mdc.worker.extraction;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

public abstract class AbstractMetadataExtractor implements MetadataExtractor {
	protected final Logger logger = LogManager.getLogger(getClass());
	
	protected final EsServices esServices;
	protected final MetadataBuilder mdBuilder;
	protected final FileDescriptorBuilder fileDescriptorBuilder; 
    protected final String localDirectory;
    protected final ProcessConfiguration processConfiguration;
    private final ObsClient obsClient;

	public AbstractMetadataExtractor(
			final EsServices esServices, 
			final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, 
			final String localDirectory,
			final ProcessConfiguration processConfiguration, 
			final ObsClient obsClient
	) {
		this.esServices = esServices;
		this.mdBuilder = mdBuilder;
		this.fileDescriptorBuilder = fileDescriptorBuilder;
		this.localDirectory = localDirectory;
		this.processConfiguration = processConfiguration;
		this.obsClient = obsClient;
	}

	final File downloadMetadataFileToLocalFolder(
    		final Reporting.Factory reportingFactory,  
    		final ProductFamily family, 
    		final String keyObs
    ) 
    	throws AbstractCodedException
    {
		// make sure than keyObs contains the metadata file
		final String metadataKeyObs = getMetadataKeyObs(keyObs);
		
        final Reporting reportDownload = reportingFactory.newReporting(1);            
        reportDownload.begin(new ReportingMessage(
        		"Starting download of {} to local directory {}", metadataKeyObs, localDirectory
        ));

		try {
			final List<File> files = Retries.performWithRetries(
					() -> obsClient.download(Arrays.asList(new ObsDownloadObject(family, metadataKeyObs, this.localDirectory))), 
					"Download of " + metadataKeyObs + " to " + localDirectory, 
					processConfiguration.getNumObsDownloadRetries(), 
					processConfiguration.getSleepBetweenObsRetriesMillis()
			);
			reportDownload.end(new ReportingMessage("End download of " + metadataKeyObs));
			return files.size() == 1 ? files.get(0) : null;
		} catch (final Exception e) {
			if (e instanceof AbstractCodedException) {
				final AbstractCodedException ace = (AbstractCodedException) e;
				reportDownload.error(new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage()));
				throw ace;
			}
			reportDownload.error(new ReportingMessage("Error downloading {} to local directory {}", metadataKeyObs, localDirectory));
			throw new RuntimeException(e);     
		}
    }

    final <E> E extractFromFilename(
			final Reporting.Factory reportingFactory,
			final ThrowingSupplier<E> supplier
	) 
		throws AbstractCodedException 
	{
    	return extractFrom(reportingFactory, 2, "filename", supplier);
	}
    
    final JSONObject extractFromFile(
			final Reporting.Factory reportingFactory,
			final ThrowingSupplier<JSONObject> supplier
	) 
		throws AbstractCodedException 
	{
    	return extractFrom(reportingFactory, 3, "file", supplier);
	}
    
	private final <E> E extractFrom(
			final Reporting.Factory reportingFactory,
			final int step,
			final String extraction,
			final ThrowingSupplier<E> supplier
	) 
		throws AbstractCodedException 
	{
		final Reporting reportExtractingFromFilename = reportingFactory.newReporting(step);
		reportExtractingFromFilename.begin(new ReportingMessage("Start extraction from {}", extraction));
		try {
			final E res = supplier.get();
					//;
			reportExtractingFromFilename.end(new ReportingMessage("End extraction from {}", extraction));
			return res;
		} catch (final AbstractCodedException e) {
			reportExtractingFromFilename.error(new ReportingMessage("[code {}] {}", e.getCode().getCode(), e.getLogMessage()));
			throw e;
		}
	}
	
	private final String getMetadataKeyObs(final String productKeyObs) {
		if (productKeyObs.toLowerCase().endsWith(processConfiguration.getFileWithManifestExt())) {
			return productKeyObs + "/" + processConfiguration.getManifestFilename();
		} 
		return productKeyObs;
	}
}
