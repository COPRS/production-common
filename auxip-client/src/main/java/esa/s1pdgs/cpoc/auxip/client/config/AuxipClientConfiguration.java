package esa.s1pdgs.cpoc.auxip.client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.auxip.client.AuxipClientFactory;
import esa.s1pdgs.cpoc.auxip.client.odata.AuxipOdataClientFactory;

@Configuration
public class AuxipClientConfiguration {

    private final AuxipClientConfigurationProperties config;

    @Autowired
    public AuxipClientConfiguration(AuxipClientConfigurationProperties config) {
        this.config = config;
    }

    @Bean
    public AuxipClientFactory factory() {
        return new AuxipOdataClientFactory(config);
    }
}
