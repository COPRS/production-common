package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.SppObsProperties;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles("sppobs")
public class SppObsPropertiesAdapterTest {

    private static final Logger LOG = LoggerFactory.getLogger(SppObsPropertiesAdapterTest.class);

    @Autowired
    SppObsProperties properties; // obsTimeout = 25h, minimalWaiting = 3h

    private SppObsPropertiesAdapter uut;

    @Before
    public void init() {
        uut = SppObsPropertiesAdapter.of(properties);
    }

    @Test
    public void jobWithOldProductShouldMinimalWait() {
        boolean shouldWait = uut.shouldWait(
                jobWithProductStartingAtAndCreationDate(
                        Instant.now().minus(Duration.ofDays(30)),
                        Instant.now().minus(Duration.ofHours(2))));

        assertThat(shouldWait, is(true));
    }

    @Test
    public void jobWithOldProductShouldTimeoutShortly() {
        boolean shouldWait = uut.shouldWait(
                jobWithProductStartingAtAndCreationDate(
                        Instant.now().minus(Duration.ofDays(30)),
                        Instant.now().minus(Duration.ofHours(4))));

        assertThat(shouldWait, is(false));
    }

    @Test
    public void jobWithActualProductShouldWaitNormally() {
        boolean shouldWait = uut.shouldWait(
                jobWithProductStartingAtAndCreationDate(
                        Instant.now().minus(Duration.ofHours(24)),
                        Instant.now().minus(Duration.ofHours(22))));

        assertThat(shouldWait, is(true));
    }

    @Test
    public void jobWithActualProductShouldTimeoutNormally() {
        boolean shouldWait = uut.shouldWait(
                jobWithProductStartingAtAndCreationDate(
                        Instant.now().minus(Duration.ofHours(26)),
                        Instant.now().minus(Duration.ofHours(22))));

        assertThat(shouldWait, is(false));
    }

    private AppDataJob jobWithProductStartingAtAndCreationDate(Instant startDate, Instant creationDate) {
        AppDataJob appDataJob = new AppDataJob(123L);
        appDataJob.setCreationDate(Date.from(creationDate));
        AppDataJobProduct product = new AppDataJobProduct();
        product.getMetadata().put("startTime", DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.ofInstant(startDate, ZoneId.of("UTC"))));
        appDataJob.setProduct(product);

        LOG.info("created job {}", appDataJob);
        System.out.println("created job " + appDataJob);

        return appDataJob;
    }
}