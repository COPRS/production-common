package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class PositiveFileSizeFilter implements InboxFilter {

    private static final Logger LOG = LoggerFactory.getLogger(PositiveFileSizeFilter.class);

    @Override
    public boolean accept(InboxEntry entry) {
        if(entry.getSize() <= 0) {
            LOG.warn("ignoring file with negative / zero size file: {} size: {}", entry.getRelativePath(), entry.getSize());
            return false;
        } else {
            return true;
        }
    }
}