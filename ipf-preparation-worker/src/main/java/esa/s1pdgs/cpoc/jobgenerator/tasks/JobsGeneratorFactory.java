package esa.s1pdgs.cpoc.jobgenerator.tasks;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenBuildTaskTableException;
import esa.s1pdgs.cpoc.jobgenerator.config.AiopProperties;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.ProductMode;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.jobgenerator.tasks.l0app.L0AppJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.l0segmentapp.L0SegmentAppJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.levelproducts.LevelProductsJobsGenerator;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

@Service
public class JobsGeneratorFactory {

	/**
	 * 
	 */
	private final ProcessSettings l0ProcessSettings;

	/**
	 * 
	 */
	private final JobGeneratorSettings jobGeneratorSettings;

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
	 * @param jobGeneratorSettings
	 * @param xmlConverter
	 * @param metadataService
	 * @param outputFactory
	 */
	@Autowired
	public JobsGeneratorFactory(final ProcessSettings l0ProcessSettings,
			final JobGeneratorSettings jobGeneratorSettings, final AiopProperties aiopProperties,
			final XmlConverter xmlConverter, final MetadataClient metadataClient,
			final OutputProducerFactory outputFactory, final ProcessConfiguration processConfiguration) {
		this.l0ProcessSettings = l0ProcessSettings;
		this.jobGeneratorSettings = jobGeneratorSettings;
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
	 * @throws JobGenBuildTaskTableException
	 */
	public AbstractJobsGenerator<IngestionEvent> createJobGeneratorForEdrsSession(final File xmlFile,
			final AppCatalogJobClient<IngestionEvent> appDataService) throws JobGenBuildTaskTableException {
		AbstractJobsGenerator<IngestionEvent> processor = new L0AppJobsGenerator(this.xmlConverter,
				this.metadataClient, this.l0ProcessSettings, this.jobGeneratorSettings, this.outputFactory,
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
	 * @throws JobGenBuildTaskTableException
	 */
	public AbstractJobsGenerator<ProductDto> createJobGeneratorForL0Slice(final File xmlFile,
			final AppCatalogJobClient<ProductDto> appDataService)
			throws JobGenBuildTaskTableException {

		AbstractJobsGenerator<ProductDto> processor = new LevelProductsJobsGenerator(this.xmlConverter, this.metadataClient,
				this.l0ProcessSettings, this.jobGeneratorSettings, this.outputFactory, appDataService, processConfiguration);
		processor.setMode(ProductMode.SLICING);
		processor.initialize(xmlFile);

		return processor;
	}

	/**
	 * 
	 * @param xmlFile
	 * @param appDataService
	 * @return
	 * @throws JobGenBuildTaskTableException
	 */
	public AbstractJobsGenerator<ProductDto> createJobGeneratorForL0Segment(final File xmlFile,
			final AppCatalogJobClient<ProductDto> appDataService) throws JobGenBuildTaskTableException {
		
		AbstractJobsGenerator<ProductDto> processor = new L0SegmentAppJobsGenerator(this.xmlConverter,
				this.metadataClient, this.l0ProcessSettings, this.jobGeneratorSettings, this.outputFactory,
				appDataService, processConfiguration);
		processor.setMode(ProductMode.SLICING);
		processor.initialize(xmlFile);
		return processor;
	}

}
