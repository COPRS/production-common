package fr.viveris.s1pdgs.level0.wrapper.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.level0.wrapper.AppStatus;
import fr.viveris.s1pdgs.level0.wrapper.config.ApplicationProperties;
import fr.viveris.s1pdgs.level0.wrapper.config.DevProperties;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.OutputProcuderFactory;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.S3Factory;

/**
 * @author Olivier Bex-Chauvet
 *
 */
@Component
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "l1-jobs")
public class L1JobConsumer extends AbstractJobConsumer {

	/**
	 * @param s3Factory
	 * @param outputProcuderFactory
	 * @param sizeS3UploadBatch
	 * @param sizeS3DownloadBatch
	 */
	@Autowired
	public L1JobConsumer(final S3Factory s3Factory, final OutputProcuderFactory outputProcuderFactory,
			final ApplicationProperties properties, final DevProperties devProperties, final AppStatus appStatus,
			final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
		super(s3Factory, outputProcuderFactory, properties, devProperties, appStatus, kafkaListenerEndpointRegistry,
				"kafkaListenerContainerL1");
	}

	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	@KafkaListener(id = "kafkaListenerContainerL1", topics = "${kafka.topic.l1-jobs}", groupId = "${kafka.group-id}")
	public void receive(JobDto job, Acknowledgment acknowledgment) {
		File workdir = new File(job.getWorkDirectory());
		String outputListFile = job.getWorkDirectory() + workdir.getName() + ".LIST";
		this.internalReceive(job, acknowledgment, outputListFile);
	}

}
