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
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "slice")
public class SlicesConsumer {
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
    
    private final AppStatus appStatus;

    /**
     * @param l0SlicesS3Services
     * @param l1SlicesS3Services
     */
    public SlicesConsumer(final ObsClient obsClient,
            @Value("${file.slices.local-directory}") final String sharedVolume,
            final DevProperties devProperties, final AppStatus appStatus) {
        this.obsClient = obsClient;
        this.sharedVolume = sharedVolume;
        this.devProperties = devProperties;
        this.appStatus = appStatus;
    }

    @KafkaListener(topics = "#{'${kafka.topics.slices}'.split(',')}", groupId = "${kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void receive(final ProductionEvent dto,
            final Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) final String topic) {
   
    	//this.appStatus.setProcessing("SLICES"); // Commented out because AppStatusImpl can currently only track one thing and is used by the ReportsConsumer

    	Reporting reporting = ReportingUtils.newReportingBuilder().newTaskReporting("Archives");
    	reporting.begin(new ReportingMessage("Start archiving"));

        try {
            if (!devProperties.getActivations().get("download-all")) {
                this.obsClient.download(Arrays.asList(new ObsDownloadObject(dto.getProductFamily(),
                        dto.getKeyObjectStorage() + "/manifest.safe",
                        this.sharedVolume + "/"
                                + dto.getProductFamily().name().toLowerCase())), reporting.getChildFactory());
            } else {
                this.obsClient.download(Arrays.asList(new ObsDownloadObject(dto.getProductFamily(),
                        dto.getKeyObjectStorage(), this.sharedVolume + "/"
                                + dto.getProductFamily().name().toLowerCase())), reporting.getChildFactory());
            }
            acknowledgment.acknowledge();
        } catch (ObsException e) {
            //this.appStatus.setError("SLICES"); // Commented out because AppStatusImpl can currently only track one thing and is used by the ReportsConsumer
        } catch (Exception exc) {
            //this.appStatus.setError("SLICES"); // Commented out because AppStatusImpl can currently only track one thing and is used by the ReportsConsumer
        }
        //this.appStatus.setWaiting(); // Commented out because AppStatusImpl can currently only track one thing and is used by the ReportsConsumer
        
        reporting.end(new ReportingMessage("End archiving"));
    }
}
