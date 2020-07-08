package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

@Ignore // test collides with MongoConfiguration
@RunWith(SpringRunner.class)
@DataMongoTest
@EnableMongoRepositories(basePackageClasses = InboxEntryRepository.class)
public class InboxEntryRepositoryTest {

    @Autowired
    InboxEntryRepository repository;

    @Test
    public void findByPickupURL() {
        InboxEntry entry = new InboxEntry("tehName", "tehPath", "tehPickUrl", new Date(), 100);
        entry.setProcessingPod("ingestor-01");
        repository.save(entry);

        assertThat(repository.findByPickupURLAndStationNameAndProcessingPod("tehPickUrl", null, "ingestor-01"), is(not(empty())));
        assertThat(repository.findByPickupURLAndStationNameAndProcessingPod("tehPickUrl33", null, "ingestor-01"), is(empty()));

        final InboxEntry fetchedEntry = repository.findByPickupURLAndStationNameAndProcessingPod(
                "tehPickUrl",
                null,
                "ingestor-01").get(0);
        assertThat(fetchedEntry.getPickupURL(), is(equalTo("tehPickUrl")));
    }

}