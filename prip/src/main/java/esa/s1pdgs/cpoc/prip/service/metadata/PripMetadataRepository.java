package esa.s1pdgs.cpoc.prip.service.metadata;

import java.time.LocalDateTime;
import java.util.List;

import esa.s1pdgs.cpoc.prip.model.PripMetadata;

public interface PripMetadataRepository {
	
	public void save(PripMetadata pripMetadata);
	
	public List<PripMetadata> findAll();
	
	public List<PripMetadata> findByCreationDate(LocalDateTime creationDateStart, LocalDateTime creationDateStop);
	
	public PripMetadata findById(String id);

}
