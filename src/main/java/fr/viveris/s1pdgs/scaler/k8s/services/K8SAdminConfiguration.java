package fr.viveris.s1pdgs.scaler.k8s.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.viveris.s1pdgs.scaler.k8s.K8SProperties;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

@Configuration
public class K8SAdminConfiguration {
	
	private final K8SProperties properties;
	
	@Autowired
    public K8SAdminConfiguration(final K8SProperties properties) {
        this.properties = properties;
    }

	@Bean
	public Config k8sConfig() {
		Config config = new ConfigBuilder().withMasterUrl(properties.getMasterUrl())
		          /*.withTrustCerts(true)*/
		          .withUsername(properties.getUsername())
		          //.withPassword(properties.getClientKey())
		          .withClientKeyData(properties.getClientKey())
		          .withClientCertData(properties.getClientCertData())
		          .withNamespace(properties.getNamespace())
		          .build();
		return config;
	}
	
	@Bean
	public KubernetesClient k8sClient() {
		KubernetesClient client = new DefaultKubernetesClient(k8sConfig());
		return client;
	}

}
