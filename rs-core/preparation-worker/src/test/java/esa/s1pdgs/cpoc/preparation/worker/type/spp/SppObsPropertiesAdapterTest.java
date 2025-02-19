/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.type.spp;

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
import esa.s1pdgs.cpoc.preparation.worker.config.type.SppObsProperties;

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
        product.getMetadata().put("productName", "productName");
        appDataJob.setProduct(product);


        LOG.info("created job {}", appDataJob);
        System.out.println("created job " + appDataJob);

        return appDataJob;
    }
}