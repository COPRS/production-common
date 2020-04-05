package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import com.sun.xml.internal.bind.v2.util.CollisionCheckStack;
import esa.s1pdgs.cpoc.common.mongodb.sequence.SequenceDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataMongoTest
@EnableMongoRepositories(basePackageClasses = InboxEntryRepository.class)
public class InboxEntryRepositoryTest {

    @Autowired
    InboxEntryRepository repository;

    @Autowired
    SequenceDao sequence;

    @Test
    public void findByPickupURL() {
        InboxEntry entry = new InboxEntry(sequence.getNextSequenceId(InboxEntry.ENTRY_SEQ_KEY), "tehName", "tehPath", "tehPickUrl", new Date(), 100);
        repository.save(entry);

        assertThat(repository.findByPickupURL("tehPickUrl"), is(not(empty())));
        assertThat(repository.findByPickupURL("tehPickUrl33"), is(empty()));

        final InboxEntry fetchedEntry = repository.findByPickupURL("tehPickUrl").get(0);
        assertThat(fetchedEntry.getPickupURL(), is(equalTo("tehPickUrl")));
    }

    @Test
    public void findById() {
        InboxEntry entry = new InboxEntry(33, "name", "relativePath", "pickupUrl", new Date(), 25);
        repository.save(entry);

        final Optional<InboxEntry> byId = repository.findById(33L);
        assertThat(byId.isPresent(), is(true));
        assertThat(byId.get().getId(), is(equalTo(33L)));
    }
}