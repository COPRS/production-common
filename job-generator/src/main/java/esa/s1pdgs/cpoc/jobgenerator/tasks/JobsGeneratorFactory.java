package esa.s1pdgs.cpoc.jobgenerator.tasks;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenBuildTaskTableException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.ProductMode;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.metadata.MetadataService;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.jobgenerator.tasks.l0app.L0AppJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.l0segmentapp.L0SegmentAppJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.l1app.L1AppJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.l2app.L2AppJobsGenerator;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
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
     * XML converter
     */
    private final XmlConverter xmlConverter;

    /**
     * 
     */
    private final MetadataService metadataService;

    /**
     * Producer in topic
     */
    private final OutputProducerFactory outputFactory;

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
            final JobGeneratorSettings jobGeneratorSettings,
            final XmlConverter xmlConverter,
            final MetadataService metadataService,
            final OutputProducerFactory outputFactory) {
        this.l0ProcessSettings = l0ProcessSettings;
        this.jobGeneratorSettings = jobGeneratorSettings;
        this.xmlConverter = xmlConverter;
        this.metadataService = metadataService;
        this.outputFactory = outputFactory;
    }

    /**
     * 
     * @param xmlFile
     * @param appDataService
     * @return
     * @throws JobGenBuildTaskTableException
     */
    public AbstractJobsGenerator<EdrsSessionDto> createJobGeneratorForEdrsSession(
            final File xmlFile,
            final AppCatalogJobClient appDataService)
            throws JobGenBuildTaskTableException {
        AbstractJobsGenerator<EdrsSessionDto> processor =
                new L0AppJobsGenerator(this.xmlConverter,
                        this.metadataService, this.l0ProcessSettings,
                        this.jobGeneratorSettings, this.outputFactory,
                        appDataService);
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
	public AbstractJobsGenerator<ProductDto> createJobGeneratorForL0Slice(
            final File xmlFile,
            final ApplicationLevel applicationLevel,
            final AppCatalogJobClient appDataService)
            throws JobGenBuildTaskTableException {
    	
		AbstractJobsGenerator<ProductDto> processor = null;
		if (applicationLevel == ApplicationLevel.L2) {
			processor = new L2AppJobsGenerator(this.xmlConverter, this.metadataService, this.l0ProcessSettings,
					this.jobGeneratorSettings, this.outputFactory, appDataService);
			processor.setMode(ProductMode.SLICING);
			processor.initialize(xmlFile);
		} else { // FIXME default L1
			processor = new L1AppJobsGenerator(this.xmlConverter, this.metadataService, this.l0ProcessSettings,
					this.jobGeneratorSettings, this.outputFactory, appDataService);
			processor.setMode(ProductMode.SLICING);
			processor.initialize(xmlFile);
		}
        return processor;
    }

    
    /**
     * 
     * @param xmlFile
     * @param appDataService
     * @return
     * @throws JobGenBuildTaskTableException
     */
    public AbstractJobsGenerator<ProductDto> createJobGeneratorForL0Segment(
            final File xmlFile,
            final AppCatalogJobClient appDataService)
            throws JobGenBuildTaskTableException {
        AbstractJobsGenerator<ProductDto> processor =
                new L0SegmentAppJobsGenerator(this.xmlConverter,
                        this.metadataService, this.l0ProcessSettings,
                        this.jobGeneratorSettings, this.outputFactory,
                        appDataService);
        processor.setMode(ProductMode.SLICING);
        processor.initialize(xmlFile);
        return processor;
    }

}
