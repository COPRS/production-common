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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

/**
 * Filters entries with negative file sizes.
 * S1PRO-2588 -1 is returned by xbip endpoint occasionally:
 * "It seems you accessed the chunk while the webdav cache was refreshing. In this case you see file size equal -1.
 * Is it possible on your side performing a second access soon after you get -1 size?
 */
public class PositiveFileSizeFilter implements InboxFilter {

    private static final Logger LOG = LoggerFactory.getLogger(PositiveFileSizeFilter.class);

    @Override
    public boolean accept(InboxEntry entry) {
        if(entry.getSize() < 0) {
            LOG.warn("ignoring file with negative size file: {} size: {}", entry.getRelativePath(), entry.getSize());
            return false;
        } else {
            return true;
        }
    }
}