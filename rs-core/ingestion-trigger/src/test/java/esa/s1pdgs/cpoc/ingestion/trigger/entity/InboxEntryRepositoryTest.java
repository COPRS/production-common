package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import esa.s1pdgs.cpoc.common.ProductFamily;

@Disabled // test collides with MongoConfiguration
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