package fr.viveris.s1pdgs.jobgenerator.tasks.generator;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.controller.JobsProducer;
import fr.viveris.s1pdgs.jobgenerator.exception.BuildTaskTableException;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.ProductMode;
import fr.viveris.s1pdgs.jobgenerator.model.product.L0Slice;
import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;
import fr.viveris.s1pdgs.jobgenerator.service.metadata.MetadataService;

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
	 * Producer in KAFKA topic
	 */
	private final JobsProducer kafkaJobsSender;

	@Autowired
	public JobsGeneratorFactory(final ProcessSettings l0ProcessSettings,
			final JobGeneratorSettings jobGeneratorSettings, final XmlConverter xmlConverter,
			final MetadataService metadataService, final JobsProducer kafkaJobsSender) {
		this.l0ProcessSettings = l0ProcessSettings;
		this.jobGeneratorSettings = jobGeneratorSettings;
		this.xmlConverter = xmlConverter;
		this.metadataService = metadataService;
		this.kafkaJobsSender = kafkaJobsSender;
	}

	public AbstractJobsGenerator<EdrsSession> createJobGeneratorForEdrsSession(File xmlFile)
			throws BuildTaskTableException {
		AbstractJobsGenerator<EdrsSession> processor = new EdrsSessionJobsGenerator(this.xmlConverter, this.metadataService,
				this.l0ProcessSettings, this.jobGeneratorSettings, this.kafkaJobsSender);
		processor.setMode(ProductMode.SLICING);
		processor.initialize(xmlFile);
		return processor;
	}

	public AbstractJobsGenerator<L0Slice> createJobGeneratorForL0Slice(File xmlFile)
			throws BuildTaskTableException {
		AbstractJobsGenerator<L0Slice> processor = new L0SlicesJobsGenerator(this.xmlConverter, this.metadataService,
				this.l0ProcessSettings, this.jobGeneratorSettings, this.kafkaJobsSender);
		processor.setMode(ProductMode.SLICING);
		processor.initialize(xmlFile);
		return processor;
	}

}
