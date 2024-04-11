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

package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;

@Ignore // test collides with MongoConfiguration
@RunWith(SpringRunner.class)
@DataMongoTest
@EnableMongoRepositories(basePackageClasses = InboxEntryRepository.class)
public class InboxEntryRepositoryTest {

    @Autowired
    InboxEntryRepository repository;

    @Test
    public void findByPickupURL() {
    	final ProductFamily productFamily = ProductFamily.EDRS_SESSION;
        InboxEntry entry = new InboxEntry("tehName", "tehPath", "tehPickUrl", new Date(), 100, null, "file", productFamily.name(), null, null);
        entry.setProcessingPod("ingestor-01");
        repository.save(entry);

        assertThat(repository.findByProcessingPodAndPickupURLAndStationNameAndMissionIdAndProductFamily(
        		"ingestor-01", "tehPickUrl", null, null, productFamily.name()), is(not(empty())));
        assertThat(repository.findByProcessingPodAndPickupURLAndStationNameAndMissionIdAndProductFamily(
        		"ingestor-01", "tehPickUrl33", null, null, productFamily.name()), is(empty()));

        final InboxEntry fetchedEntry = repository.findByProcessingPodAndPickupURLAndStationNameAndMissionIdAndProductFamily(
                "ingestor-01", "tehPickUrl", null, null, productFamily.name()).get(0);
        assertThat(fetchedEntry.getPickupURL(), is(equalTo("tehPickUrl")));
    }

}