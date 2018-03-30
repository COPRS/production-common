package fr.viveris.s1pdgs.scaler.scaling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.scaler.monitoring.kafka.KafkaMonitoring;
import fr.viveris.s1pdgs.scaler.monitoring.kafka.KafkaMonitoringProperties;
import fr.viveris.s1pdgs.scaler.monitoring.kafka.SpdgsTopic;
import fr.viveris.s1pdgs.scaler.monitoring.kafka.model.KafkaPerGroupPerTopicMonitor;
import io.fabric8.kubernetes.client.AutoAdaptableKubernetesClient;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
/*import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.authenticators.GCPAuthenticator;*/
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
		LOGGER.info("[MONITOR] [Step 0] Starting scaling");
		

		try {
			
	        
			// Monitor KAFKA
			LOGGER.info("[MONITOR] [Step 1] Starting monitoring KAFKA");
			KafkaPerGroupPerTopicMonitor monitorKafka = this.kafkaMonitoring.getPerGroupPerTopicMonitor(
					kafkaProperties.getGroupIdPerTopic().get(SpdgsTopic.L1_JOBS),
					kafkaProperties.getTopics().get(SpdgsTopic.L1_JOBS));
			LOGGER.info("[MONITOR] [Step 1] KAFKA successfully monitored: {}", monitorKafka);

			// Monitor K8S
			LOGGER.info("[MONITOR] [Step 2] Starting monitoring Wrappers");
	        //V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
			String master = "https://192.168.42.51:6443";
			Config config = new ConfigBuilder().withMasterUrl(master)
	          .withTrustCerts(true)
	          .withUsername("admin")
	          .withPassword("admin")
	          .withNamespace("default")
	          .build();
	        try (final KubernetesClient client = new AutoAdaptableKubernetesClient(config)) {

	        	LOGGER.info("[MONITOR] [Step 2] Wrappers successfully monitored: {}", client.pods().list());
				

	        } catch (Exception e) {
	            e.printStackTrace();
	            LOGGER.error(e.getMessage(), e);


	            Throwable[] suppressed = e.getSuppressed();
	            if (suppressed != null) {
	                for (Throwable t : suppressed) {
	                	LOGGER.error(t.getMessage(), t);
	                }
	            }
			}
	        // Calculate value for scaling

			// Scale
		} catch (Exception e) {
			LOGGER.error("Error during scaling: {}", e.getMessage());
		}
		LOGGER.info("[MONITOR] [Step 0] End");
	}
}
