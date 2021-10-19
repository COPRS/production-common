package esa.s1pdgs.cpoc.ingestion.trigger.name;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class AuxipProductNameEvaluator implements ProductNameEvaluator {

    @Override
    public String evaluateFrom(InboxEntry entry) {
        return entry.getName();
    }


}
