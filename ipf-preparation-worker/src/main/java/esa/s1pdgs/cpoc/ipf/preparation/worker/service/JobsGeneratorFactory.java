package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerBuildTaskTableException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.AiopProperties;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

@Service
public class JobsGeneratorFactory {
	private final ProcessSettings l0ProcessSettings;
	private final IpfPreparationWorkerSettings ipfPreparationWorkerSettings;
	private final AiopProperties aiopProperties;
	private final XmlConverter xmlConverter;
	private final MetadataClient metadataClient;
	private final ProcessConfiguration processConfiguration;
	private final MqiClient mqiClient;

	@Autowired
	public JobsGeneratorFactory(
			final ProcessSettings l0ProcessSettings,
			final IpfPreparationWorkerSettings ipfPreparationWorkerSettings, 
			final AiopProperties aiopProperties,
			final XmlConverter xmlConverter, 
			final MetadataClient metadataClient,
			final ProcessConfiguration processConfiguration,
			final MqiClient mqiClient
	) {
		this.l0ProcessSettings = l0ProcessSettings;
		this.ipfPreparationWorkerSettings = ipfPreparationWorkerSettings;
		this.aiopProperties = aiopProperties;
		this.xmlConverter = xmlConverter;
		this.metadataClient = metadataClient;
		this.processConfiguration = processConfiguration;
		this.mqiClient = mqiClient;
	}


	public AbstractJobsGenerator createJobGeneratorForEdrsSession(
			final File xmlFile,
			final AppCatalogJobClient<CatalogEvent> appDataService
	) throws IpfPrepWorkerBuildTaskTableException {
		final AbstractJobsGenerator processor = new L0AppJobsGenerator(
				xmlConverter,
				metadataClient, 
				l0ProcessSettings, 
				ipfPreparationWorkerSettings, 				
				appDataService, 
				aiopProperties, 
				processConfiguration,
				mqiClient
		);
		processor.setMode(ProductMode.SLICING);
		processor.initialize(xmlFile);
		return processor;
	}

	public AbstractJobsGenerator createJobGeneratorForL0Slice(
			final File xmlFile,
			final AppCatalogJobClient<CatalogEvent> appDataService
	) throws IpfPrepWorkerBuildTaskTableException {
		final AbstractJobsGenerator processor = new LevelProductsJobsGenerator(
				xmlConverter, 
				metadataClient,
				l0ProcessSettings, 
				ipfPreparationWorkerSettings, 
				appDataService, 
				processConfiguration,
				mqiClient
		);
		processor.setMode(ProductMode.SLICING);
		processor.initialize(xmlFile);
		return processor;
	}

	/**
	 * 
	 * @param xmlFile
	 * @param appDataService
	 * @return
	 * @throws IpfPrepWorkerBuildTaskTableException
	 */
	public AbstractJobsGenerator createJobGeneratorForL0Segment(
			final File xmlFile,
			final AppCatalogJobClient<CatalogEvent> appDataService
	) throws IpfPrepWorkerBuildTaskTableException {		
		final AbstractJobsGenerator processor = new L0SegmentAppJobsGenerator(
				xmlConverter,
				metadataClient, 
				l0ProcessSettings, 
				ipfPreparationWorkerSettings, 
				appDataService, 
				processConfiguration,
				mqiClient
		);
		processor.setMode(ProductMode.SLICING);
		processor.initialize(xmlFile);
		return processor;
	}
}
