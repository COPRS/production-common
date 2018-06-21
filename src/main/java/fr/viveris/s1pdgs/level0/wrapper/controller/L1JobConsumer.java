package fr.viveris.s1pdgs.level0.wrapper.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import fr.viveris.s1pdgs.level0.wrapper.services.s3.ObsService;

/**
 * Implementation of a job consumer for L0 level
 * 
 * @author Viveris Technologies
 */
@Component
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "l1-jobs")
public class L1JobConsumer extends AbstractJobConsumer {

    /**
     * Spring consumer identifier
     */
    public static final String CONSUMER_ID = "kafkaListenerContainerL1";

    /**
     * @param obsService
     * @param outputProcuderFactory
     * @param properties
     * @param devProperties
     * @param appStatus
     * @param kafkaListenerEndpointRegistry
     * @param topic
     */
    @Autowired
    public L1JobConsumer(final ObsService obsService,
            final OutputProcuderFactory outputProcuder,
            final ApplicationProperties properties,
            final DevProperties devProperties, final AppStatus appStatus,
            final KafkaListenerEndpointRegistry consumersRegistry,
            @Value("${kafka.topic.l1-jobs}") final String topic) {
        super(obsService, outputProcuder, properties, devProperties, appStatus,
                consumersRegistry, CONSUMER_ID, topic);
    }

    /**
     * Message listener container. Read a message
     * 
     * @param payload
     */
    @KafkaListener(id = CONSUMER_ID, topics = "${kafka.topic.l1-jobs}", groupId = "${kafka.group-id}")
    public void receive(final JobDto job, final Acknowledgment acknowledgment) {
        File workdir = new File(job.getWorkDirectory());
        String outputListFile =
                job.getWorkDirectory() + workdir.getName() + ".LIST";
        this.internalReceive(job, acknowledgment, outputListFile);
    }

}
