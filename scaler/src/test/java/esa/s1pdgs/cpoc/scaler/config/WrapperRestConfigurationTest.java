package esa.s1pdgs.cpoc.scaler.config;

import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import esa.s1pdgs.cpoc.scaler.k8s.services.WrapperRestConfiguration;

public class WrapperRestConfigurationTest {

    @Test
    public void testBuilder() {
        WrapperRestConfiguration conf = new WrapperRestConfiguration();
        conf.restWrapperTemplate(new RestTemplateBuilder());
    }
    
}
