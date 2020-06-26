package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;

public class AuxQueryHandler {	
    private final MetadataClient metadataClient;
	private final ProductMode mode;
	private final InputTimeoutChecker timeoutChecker;
	
	public AuxQueryHandler(
			final MetadataClient metadataClient, 
			final ProductMode mode,
			final InputTimeoutChecker timeoutChecker
	) {
		this.metadataClient = metadataClient;
		this.mode = mode;
		this.timeoutChecker = timeoutChecker;
	}
	
	public AuxQuery queryFor(final JobGen jobGen) {		
		return new AuxQuery(
				metadataClient, 
				jobGen, 
				mode, 
				timeoutChecker
		);
	}
}
