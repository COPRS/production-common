package de.werum.coprs.cadip.client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.werum.coprs.cadip.client.CadipClientFactory;
import de.werum.coprs.cadip.client.odata.CadipOdataClientFactory;

@Configuration
public class CadipClientConfiguration {

    private final CadipClientConfigurationProperties config;

    @Autowired
    public CadipClientConfiguration(CadipClientConfigurationProperties config) {
        this.config = config;
    }

    @Bean
    public CadipClientFactory cadipClientFactory() {
        return new CadipOdataClientFactory(config);
    }
}
