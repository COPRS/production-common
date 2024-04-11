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

package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

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