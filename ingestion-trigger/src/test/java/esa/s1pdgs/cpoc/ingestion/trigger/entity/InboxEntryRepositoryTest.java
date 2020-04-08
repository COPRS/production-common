package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataMongoTest
@EnableMongoRepositories(basePackageClasses = InboxEntryRepository.class)
public class InboxEntryRepositoryTest {

    @Autowired
    InboxEntryRepository repository;

    @Test
    public void findByPickupURL() {
        InboxEntry entry = new InboxEntry("tehName", "tehPath", "tehPickUrl", new Date(), 100);
        repository.save(entry);

        assertThat(repository.findByPickupURLAndStationName("tehPickUrl", null), is(not(empty())));
        assertThat(repository.findByPickupURLAndStationName("tehPickUrl33", null), is(empty()));

        final InboxEntry fetchedEntry = repository.findByPickupURLAndStationName("tehPickUrl", null).get(0);
        assertThat(fetchedEntry.getPickupURL(), is(equalTo("tehPickUrl")));
    }

}