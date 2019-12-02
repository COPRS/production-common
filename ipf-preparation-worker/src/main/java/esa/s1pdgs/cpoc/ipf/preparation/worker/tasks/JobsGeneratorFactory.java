package esa.s1pdgs.cpoc.ipf.preparation.worker.tasks;

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
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.XmlConverter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.l0app.L0AppJobsGenerator;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.l0segmentapp.L0SegmentAppJobsGenerator;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.levelproducts.LevelProductsJobsGenerator;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

@Service
public class JobsGeneratorFactory {

	/**
	 * 
	 */
	private final ProcessSettings l0ProcessSettings;

	/**
	 * 
	 */
	private final IpfPreparationWorkerSettings ipfPreparationWorkerSettings;

	/**
	 * 
	 */
	private final AiopProperties aiopProperties;
	
	/**
	 * XML converter
	 */
	private final XmlConverter xmlConverter;

	/**
	 * 
	 */
	private final MetadataClient metadataClient;

	/**
	 * Producer in topic
	 */
	private final OutputProducerFactory outputFactory;

	private final ProcessConfiguration processConfiguration;
	
	/**
	 * 
	 * @param l0ProcessSettings
	 * @param ipfPreparationWorkerSettings
	 * @param xmlConverter
	 * @param metadataService
	 * @param outputFactory
	 */
	@Autowired
	public JobsGeneratorFactory(final ProcessSettings l0ProcessSettings,
			final IpfPreparationWorkerSettings ipfPreparationWorkerSettings, final AiopProperties aiopProperties,
			final XmlConverter xmlConverter, final MetadataClient metadataClient,
			final OutputProducerFactory outputFactory, final ProcessConfiguration processConfiguration) {
		this.l0ProcessSettings = l0ProcessSettings;
		this.ipfPreparationWorkerSettings = ipfPreparationWorkerSettings;
		this.aiopProperties = aiopProperties;
		this.xmlConverter = xmlConverter;
		this.metadataClient = metadataClient;
		this.outputFactory = outputFactory;
		this.processConfiguration = processConfiguration;
	}

	/**
	 * 
	 * @param xmlFile
	 * @param appDataService
	 * @return
	 * @throws IpfPrepWorkerBuildTaskTableException
	 */
	public AbstractJobsGenerator<CatalogEvent> createJobGeneratorForEdrsSession(final File xmlFile,
			final AppCatalogJobClient<CatalogEvent> appDataService) throws IpfPrepWorkerBuildTaskTableException {
		AbstractJobsGenerator<CatalogEvent> processor = new L0AppJobsGenerator(this.xmlConverter,
				this.metadataClient, this.l0ProcessSettings, this.ipfPreparationWorkerSettings, this.outputFactory,
				appDataService, aiopProperties, processConfiguration);
		processor.setMode(ProductMode.SLICING);
		processor.initialize(xmlFile);
		return processor;
	}

	/**
	 * @param xmlFile
	 * @param applicationLevel
	 * @param appDataService
	 * @return
	 * @throws IpfPrepWorkerBuildTaskTableException
	 */
	public AbstractJobsGenerator<CatalogEvent> createJobGeneratorForL0Slice(final File xmlFile,
			final AppCatalogJobClient<CatalogEvent> appDataService)
			throws IpfPrepWorkerBuildTaskTableException {

		AbstractJobsGenerator<CatalogEvent> processor = new LevelProductsJobsGenerator(this.xmlConverter, this.metadataClient,
				this.l0ProcessSettings, this.ipfPreparationWorkerSettings, this.outputFactory, appDataService, processConfiguration);
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
	public AbstractJobsGenerator<CatalogEvent> createJobGeneratorForL0Segment(final File xmlFile,
			final AppCatalogJobClient<CatalogEvent> appDataService) throws IpfPrepWorkerBuildTaskTableException {
		
		AbstractJobsGenerator<CatalogEvent> processor = new L0SegmentAppJobsGenerator(this.xmlConverter,
				this.metadataClient, this.l0ProcessSettings, this.ipfPreparationWorkerSettings, this.outputFactory,
				appDataService, processConfiguration);
		processor.setMode(ProductMode.SLICING);
		processor.initialize(xmlFile);
		return processor;
	}

}
