package esa.s1pdgs.cpoc.prip.service.metadata;

import java.util.List;

import esa.s1pdgs.cpoc.prip.model.PripMetadata;

public interface PripMetadataIfc {
	
	public void save(PripMetadata pripMetadata);
	
	public List<PripMetadata> findAll();

}
