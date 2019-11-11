package esa.s1pdgs.cpoc.archives.controller;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.archives.DevProperties;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;

/**
 * @author Viveris Technologies
 */
@Component
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "segments")
public class SegmentsConsumer {
    /**
     * Service for Object Storage
     */
    private final ObsClient obsClient;

    /**
     * Path to the shared volume
     */
    private final String sharedVolume;

    /**
     * Object containing the dev properties
     */
    private final DevProperties devProperties;
    
    /**
     * Application status for archives
     */
    private final AppStatus appStatus;

    /**
     * @param l0SlicesS3Services
     * @param l1SlicesS3Services
     */
    public SegmentsConsumer(final ObsClient obsClient,
            @Value("${file.segments.local-directory}") final String sharedVolume,
            final DevProperties devProperties, final AppStatus appStatus) {
        this.obsClient = obsClient;
        this.sharedVolume = sharedVolume;
        this.devProperties = devProperties;
        this.appStatus = appStatus;
    }

    @KafkaListener(topics = "#{'${kafka.topics.segments}'.split(',')}", groupId = "${kafka.group-id}", containerFactory = "segmentKafkaListenerContainerFactory")
    public void receive(final ProductDto dto,
            final Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) final String topic) {
  	
        //this.appStatus.setProcessing("SLICES"); // Commented out because AppStatusImpl can currently only track one thing and is used by the ReportsConsumer
        try {
            if (!devProperties.getActivations().get("download-all")) {
                this.obsClient.download(Arrays.asList(new ObsDownloadObject(dto.getFamily(),
                        dto.getKeyObjectStorage() + "/manifest.safe",
                        this.sharedVolume + "/"
                                + dto.getFamily().name().toLowerCase())));
            } else {
                this.obsClient.download(Arrays.asList(new ObsDownloadObject(dto.getFamily(),
                        dto.getKeyObjectStorage(), this.sharedVolume + "/"
                                + dto.getFamily().name().toLowerCase())));
            }
            acknowledgment.acknowledge();
        } catch (ObsException e) {        	
            //this.appStatus.setError("SLICES"); // Commented out because AppStatusImpl can currently only track one thing and is used by the ReportsConsumer
        } catch (Exception exc) {
            //this.appStatus.setError("SLICES"); // Commented out because AppStatusImpl can currently only track one thing and is used by the ReportsConsumer
        }
        this.appStatus.setWaiting();
    }
}
