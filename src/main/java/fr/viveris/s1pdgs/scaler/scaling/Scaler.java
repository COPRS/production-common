package fr.viveris.s1pdgs.scaler.scaling;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.scaler.monitoring.kafka.KafkaMonitoring;
import fr.viveris.s1pdgs.scaler.monitoring.kafka.KafkaMonitoringProperties;
import fr.viveris.s1pdgs.scaler.monitoring.kafka.SpdgsTopic;
import fr.viveris.s1pdgs.scaler.monitoring.kafka.model.KafkaPerGroupPerTopicMonitor;
import fr.viveris.s1pdgs.scaler.monitoring.wrappers.model.*;
import io.fabric8.kubernetes.client.AutoAdaptableKubernetesClient;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * L1 resources scaler
 * 
 * @author Cyrielle Gailliard
 *
 */
@Component
public class Scaler {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Scaler.class);

	/**
	 * Service for monitoring KAFKA
	 */
	private final KafkaMonitoring kafkaMonitoring;

	/**
	 * Kafka properties
	 */
	private final KafkaMonitoringProperties kafkaProperties;

	
	/**
	 * Constructor
	 * 
	 * @param kafkaMonitoring
	 * @param properties
	 */
	@Autowired
	public Scaler(final KafkaMonitoringProperties kafkaProperties, final KafkaMonitoring kafkaMonitoring) {
		this.kafkaMonitoring = kafkaMonitoring;
		this.kafkaProperties = kafkaProperties;
	}

	/**
	 * <ul>
	 * Scaling:
	 * <li>1: Monitor topic of L1 jobs</li>
	 * <li>2: Monitor L1 wrappers</li>
	 * <li>3: Calculate the value</li>
	 * <li>4: Scales the L1 resources</li>
	 * <ul>
	 */
	@Scheduled(fixedDelayString = "${scaler.fixed-delay-ms}")
	public void scale() {
		List<Wrapper> listWrapper = new ArrayList<>();
		LOGGER.info("[MONITOR] [Step 0] Starting scaling");
		

		try {
			
	        
			// Monitor KAFKA
			LOGGER.info("[MONITOR] [Step 1] Starting monitoring KAFKA");
			KafkaPerGroupPerTopicMonitor monitorKafka = this.kafkaMonitoring.getPerGroupPerTopicMonitor(
					kafkaProperties.getGroupIdPerTopic().get(SpdgsTopic.L1_JOBS),
					kafkaProperties.getTopics().get(SpdgsTopic.L1_JOBS));
			LOGGER.info("[MONITOR] [Step 1] KAFKA successfully monitored: {}", monitorKafka);

			// Monitor K8S
			//Listing all the L1 wrappers pods
			LOGGER.info("[MONITOR] [Step 2] Starting monitoring Wrappers");
	        String master = "https://192.168.42.51:6443";
			Config config = new ConfigBuilder().withMasterUrl(master)
	          /*.withTrustCerts(true)*/
	          .withUsername("cluster-admin")
	          /*.withClientKeyData("LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcFFJQkFBS0NBUUVBcmJsSVpvYTdrQVpXM3pYb3h5K1hGZzVOb0hUM1lNRjJvSnBJ                                                                                                                                          WkRRT25hU0tPUkd0CmFLMzhVWmlpQWRVcXBtS0RxZG1ZcVZiOGJyNXR4MzZXWS85Q2hhUWRnWFlSNEJWeXcrMXBTOW93Y1FQK0ZXTEEKQ2p6UmNBQ0lmeGx0SGdNaHRCZm1Ib                                                                                                                                          Uh2dGcyWnkwUGFwN05SRHRrTWg4YW5vcWwwNzFMa0Y2b0JxM2gxY1pGMAplWEZMaDkwb05rOWhrYUIvTEFHVzk3RHpNeXpUcUJRVy9XdDNQTjVvVkNHOUo2RnMyaXcvNTNUcW                                                                                                                                          RoVnVTVjFwCjlDRUZHSGdhN2l1UzNILzlISWk1dGZ0WFdlckV4VGJxV1J1WFVxTDB6UWtyV1hzTUkyME9yTHJiOU5ndng2eGUKZVhkanpLZHlxb1Z2SmNDN1pQUjZQNHZWT25                                                                                                                                          BZUFkWitIVE1pU1FJREFRQUJBb0lCQVFDTlVobU1sZlFFc0xPQQp0dmthK0NMZkpWbU91emYyTk10TTBOVXM5cEFoTzVYWjRRQ0JGSEFhN0tCMS96UEgwSUlzN0w5Y21rK1Z3                                                                                                                                          MEhJCnRMaWd0aWttVUNCVWpYanpJbCtPOVJYZ1I2bDZkblgrYmF1dGFGWXoxNnN5UWJ2YlcwN1NrMUoyeXRMVzlXOXoKeEVvZWZDZm5mVGZOU0JSY3BaOWpoTG5hYWtrYmMvV                                                                                                                                          2haam5Xbk8vbHBRQ3dBQ2xzaU9rQVNEOExoa1AwNUt3dwpqRHpzS0hMb3gwNitUT212NHE0YXRTV0RTM2RUTUgzNXY2eXFXUnhFdWo0SGJDRm5sNXlXM0kyTGNFc3RiR01WCl                                                                                                                                          FXTnhvNm9HZEpFcTZOV2g4NTg3RzBWYW9ZY1MwT1JxcmJuS04zTm4vdHdibVh1b3MzZjUwODJKeTU1NWhtVFEKRXlkT1djbUJBb0dCQU9IdkZTM2YvSE9ONlM5ZlUvMVpWWWU                                                                                                                                          wbnFlSzZrLzNPK2Q4SW11QlFjand5R0s2aklKVwo0SFhVTmdHdnFXQzI4T21XQXlEUCtJL2l4Tk5LeWpvOXU3MW1DSW9FQXNSeVVydTRyZzRjSEVzQ2F3ZVJGclY0Ckc0dXVi                                                                                                                                          RTNhamlJQ3Bpa3lIbVZidTFFUDJQeDcyNC9FZlBVUGV0UnVsaFp1ZXl6L3lJelpkNGJSQW9HQkFNVFgKalBKVC9Vby9YNkhkUEJXaWRIVHJCZUVHZXlWUFo1cXNob0FXeG5Jb                                                                                                                                          C9MemIzN1RKcDZlUGpDdG0rbHVWWUpDQQptVmllRUMxRS94b1c4ekF1TlBFeUphM3dyd1Z3ZFMrZnNIS3kzZ1hrMk1CZEV5NnVDazZNVVhjSUg3U0lZeUlICldqeG1VV2IvZH                                                                                                                                          RlWEN2MEZVRm92aGNlNmpJNGEvdDE5dXdjRHN6SDVBb0dBSXovN0RQSkNZQUVISGJZQTA2bEoKZCtmTlRSU1daQzJOc2hzaS82VG1ENlRKanVYT0lGUFBwM0taam4vS3JHVSt                                                                                                                                          oeU01ajdnQzd1Z1JqMm0rellGdQpOaW1pTVc1WXhDK1dDdVhRZWpFV2xQbG1tNEtlaVdlWTNKMDFGcHgveW55aFVoSVl2ZldtN3duSzcvR2ZHdm9zCkNNd0dmUGhZQUYzeVo5M3NlMVUrbWRFQ2dZRUFrVTkrWVRYWGVnUW1tTnMxQzlPTm5QSVN1UGVMMlJNeExHSEkKT0s2WGVKVEthckQyQ0FRRm5CREFMUm9zSDRlNmJYSkJ3Y1dOczUySHBMN2tiK0RzZkZIRXR3OUNaUVdMdk1ocAovWUpGbkp3LzFtSGZVMHB2bVdURWp0YVVjVFZ0MlNVTVhDSThYWWloTnEzdUVyTGxpbTRpbURzQ243VVdDSFJVCnFPejJVQ0VDZ1lFQW1OV01lelg4QlRka1pXVGZOV1Y3YThVNjhQSllFSUoxVXVuSGFEb1pHWDJOOWU0dkJzSVIKMkJzUWZoTUp3UjB6Y2JUNXN5TnNWN0VGVVJlejZnNkxrVStRZ2xFV0tCWXg3VHJ1Nmw1c1k2Z1pLQmlpS1FUeAo1TFlRRk1jaEJiVjUyaFdRS0xzejlWOUdCWWhZeTZYN1oxTjA2eTByczFjcllpRGQwRm1yamJ3PQotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQo=")
	          .withNamespace("default")*/
	          .build();
	        try (final KubernetesClient client = new AutoAdaptableKubernetesClient(config)) {
	        	LOGGER.info("[MONITOR] [Step 2] Wrappers successfully monitored: {}", client.pods().list());
			}
	        //Retrieving L1 Wrappers pods info
	        
	        // Retrieving the status from the L1 Wrapper
	        for (Wrapper wrapper : listWrapper) {
	        	wrapper.getHostName();
	        }
	        // Calculate value for scaling

			// Scale
		} catch (Exception e) {
			LOGGER.error("Error during scaling: {}", e.getMessage());
		}
		LOGGER.info("[MONITOR] [Step 0] End");
	}
}
