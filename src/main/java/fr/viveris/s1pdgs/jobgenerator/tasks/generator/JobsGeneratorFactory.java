package fr.viveris.s1pdgs.jobgenerator.tasks.generator;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.errors.processing.JobGenBuildTaskTableException;
import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.ProductMode;
import fr.viveris.s1pdgs.jobgenerator.model.product.L0Slice;
import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;
import fr.viveris.s1pdgs.jobgenerator.service.metadata.MetadataService;
import fr.viveris.s1pdgs.jobgenerator.service.mqi.OutputProcuderFactory;

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
    private final OutputProcuderFactory outputFactory;

    @Autowired
    public JobsGeneratorFactory(final ProcessSettings l0ProcessSettings,
            final JobGeneratorSettings jobGeneratorSettings,
            final XmlConverter xmlConverter,
            final MetadataService metadataService,
            final OutputProcuderFactory outputFactory) {
        this.l0ProcessSettings = l0ProcessSettings;
        this.jobGeneratorSettings = jobGeneratorSettings;
        this.xmlConverter = xmlConverter;
        this.metadataService = metadataService;
        this.outputFactory = outputFactory;
    }

    public AbstractJobsGenerator<EdrsSession> createJobGeneratorForEdrsSession(
            File xmlFile) throws JobGenBuildTaskTableException {
        AbstractJobsGenerator<EdrsSession> processor =
                new EdrsSessionJobsGenerator(this.xmlConverter,
                        this.metadataService, this.l0ProcessSettings,
                        this.jobGeneratorSettings, this.outputFactory);
        processor.setMode(ProductMode.SLICING);
        processor.initialize(xmlFile);
        return processor;
    }

    public AbstractJobsGenerator<L0Slice> createJobGeneratorForL0Slice(
            File xmlFile) throws JobGenBuildTaskTableException {
        AbstractJobsGenerator<L0Slice> processor = new L0SlicesJobsGenerator(
                this.xmlConverter, this.metadataService, this.l0ProcessSettings,
                this.jobGeneratorSettings, this.outputFactory);
        processor.setMode(ProductMode.SLICING);
        processor.initialize(xmlFile);
        return processor;
    }

}
