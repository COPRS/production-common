package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3.gap;

import java.time.LocalDateTime;
import java.util.List;

import esa.s1pdgs.cpoc.metadata.model.S3Metadata;

public abstract class AbstractGapHandler {

	public abstract boolean isCovered(LocalDateTime startTime, LocalDateTime stopTime, List<S3Metadata> products);
}
