package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class PositiveFileSizeFilterTest {

    @Test
    public void dont_accept_negative_size() {
        assertThat(new PositiveFileSizeFilter().accept(entryWithSize(-1)), is(false));
    }

    @Test
    public void accept_positive_size() {
        assertThat(new PositiveFileSizeFilter().accept(entryWithSize(1)), is(true));
    }

    @Test
    public void accept_zero_size() {
        assertThat(new PositiveFileSizeFilter().accept(entryWithSize(0)), is(true));
    }

    private InboxEntry entryWithSize(long size) {
        InboxEntry entry = new InboxEntry();
        entry.setSize(size);
        return entry;
    }
}