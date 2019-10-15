package esa.s1pdgs.cpoc.prip.service.metadata;

import java.util.List;

import esa.s1pdgs.cpoc.prip.model.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripTextFilter;

public interface PripMetadataRepository {

	public void save(PripMetadata pripMetadata);

	public List<PripMetadata> findAll();

	public List<PripMetadata> findByCreationDate(List<PripDateTimeFilter> creationDateFilters);

	public List<PripMetadata> findByProductName(List<PripTextFilter> nameFilters);

	public List<PripMetadata> findByCreationDateAndProductName(List<PripDateTimeFilter> creationDateFilters,
			List<PripTextFilter> nameFilters);

	public PripMetadata findById(String id);

}
