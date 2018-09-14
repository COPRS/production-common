package esa.s1pdgs.cpoc.jobgenerator.tasks.generator;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenBuildTaskTableException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.ProductMode;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.metadata.MetadataService;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;

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

    public AbstractJobsGenerator<EdrsSessionDto> createJobGeneratorForEdrsSession(
            File xmlFile,
            final AbstractAppCatalogJobService<EdrsSessionDto> appDataService)
            throws JobGenBuildTaskTableException {
        AbstractJobsGenerator<EdrsSessionDto> processor =
                new EdrsSessionsJobsGenerator(this.xmlConverter,
                        this.metadataService, this.l0ProcessSettings,
                        this.jobGeneratorSettings, this.outputFactory,
                        appDataService);
        processor.setMode(ProductMode.SLICING);
        processor.initialize(xmlFile);
        return processor;
    }

    public AbstractJobsGenerator<LevelProductDto> createJobGeneratorForL0Slice(
            File xmlFile,
            final AbstractAppCatalogJobService<LevelProductDto> appDataService)
            throws JobGenBuildTaskTableException {
        AbstractJobsGenerator<LevelProductDto> processor =
                new LevelProductsJobsGenerator(this.xmlConverter,
                        this.metadataService, this.l0ProcessSettings,
                        this.jobGeneratorSettings, this.outputFactory,
                        appDataService);
        processor.setMode(ProductMode.SLICING);
        processor.initialize(xmlFile);
        return processor;
    }

}
