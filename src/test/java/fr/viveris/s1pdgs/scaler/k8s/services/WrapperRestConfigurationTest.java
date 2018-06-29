package fr.viveris.s1pdgs.scaler.k8s.services;

import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

public class WrapperRestConfigurationTest {

    @Test
    public void testBuilder() {
        WrapperRestConfiguration conf = new WrapperRestConfiguration();
        conf.restWrapperTemplate(new RestTemplateBuilder());
    }
    
}
