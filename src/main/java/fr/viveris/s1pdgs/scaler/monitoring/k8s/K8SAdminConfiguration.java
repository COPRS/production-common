package fr.viveris.s1pdgs.scaler.monitoring.k8s;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

@Configuration
public class K8SAdminConfiguration {
	
	@Autowired
	private K8SProperties properties;

	@Bean
	public Config k8sConfig() {
		Config config = new ConfigBuilder().withMasterUrl(properties.getMasterUrl())
		          /*.withTrustCerts(true)*/
		          .withUsername(properties.getUsername())
		          .withPassword(properties.getClientKey())
		          //.withClientKeyData(properties.getClientKey())
		          .withNamespace("default")
		          .build();
		return config;
	}
	
	@Bean
	public KubernetesClient k8sClient() {
		KubernetesClient client = new DefaultKubernetesClient(k8sConfig());
		return client;
	}

}
