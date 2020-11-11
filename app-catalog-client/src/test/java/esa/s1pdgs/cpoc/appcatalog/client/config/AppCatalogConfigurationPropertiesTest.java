package esa.s1pdgs.cpoc.appcatalog.client.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class AppCatalogConfigurationPropertiesTest {

    @Autowired
    AppCatalogConfigurationProperties uut;

    @Test
    public void testConfiguration() {
        assertThat(uut.getHostUri(), is(equalTo("http://s1pro-app-catalog-svc:8080")));
        assertThat(uut.getMaxRetries(), is(equalTo(3)));
        assertThat(uut.getTempoRetryMs(), is(equalTo(1000)));
        assertThat(uut.getTmReadMs(), is(60000));
    }

    @SpringBootApplication
    public static class TestApplication {

    }

}