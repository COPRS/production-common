package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles("sppobs")
public class SppObsPropertiesTest {

    @Autowired
    SppObsProperties properties;

    @Test
    public void testGetObsTimeoutSec() {
        assertThat(properties.getObsTimeoutSec(), is(equalTo(90_000L)));
    }

    @Test
    public void testGetMinimalWaitingTimeSec() {
        assertThat(properties.getMinimalWaitingTimeSec(), is(equalTo(10_800L)));
    }
}