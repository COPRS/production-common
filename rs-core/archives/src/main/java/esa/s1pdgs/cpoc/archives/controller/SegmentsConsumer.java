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
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

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
    public void receive(final ProductionEvent dto,
            final Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) final String topic) {
  	
    	//this.appStatus.setProcessing("SLICES"); // Commented out because AppStatusImpl can currently only track one thing and is used by the ReportsConsumer

    	Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.fromFileName(dto.getKeyObjectStorage())).newReporting("Archives");
    	reporting.begin(new ReportingMessage("Start archiving"));
    	
        try {
            if (!devProperties.getActivations().get("download-all")) {
                this.obsClient.download(Arrays.asList(new ObsDownloadObject(dto.getProductFamily(),
                        dto.getKeyObjectStorage() + "/manifest.safe",
                        this.sharedVolume + "/"
                                + dto.getProductFamily().name().toLowerCase())), reporting);
            } else {
                this.obsClient.download(Arrays.asList(new ObsDownloadObject(dto.getProductFamily(),
                        dto.getKeyObjectStorage(), this.sharedVolume + "/"
                                + dto.getProductFamily().name().toLowerCase())), reporting);
            }
            acknowledgment.acknowledge();
        } catch (ObsException e) {        	
            //this.appStatus.setError("SLICES"); // Commented out because AppStatusImpl can currently only track one thing and is used by the ReportsConsumer
        } catch (Exception exc) {
            //this.appStatus.setError("SLICES"); // Commented out because AppStatusImpl can currently only track one thing and is used by the ReportsConsumer
        }
        //this.appStatus.setWaiting(); ; // Commented out because AppStatusImpl can currently only track one thing and is used by the ReportsConsumer
        
    	reporting.end(new ReportingMessage("End archiving"));

    }
}
