package standalone.prip.frontend.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripTextFilter;

public class DummyPripMetadataRepositoryImpl implements PripMetadataRepository {

	private List<PripMetadata> pripMetadataList = createListOfDummyData();

	@Override
	public void save(PripMetadata pripMetadata) {
		// TODO Auto-generated method stub
	}

	@Override
	public List<PripMetadata> findAll() {
		return pripMetadataList;
	}

	@Override
	public List<PripMetadata> findByCreationDate(List<PripDateTimeFilter> creationDateFilters) {
		return findByCreationDateAndProductName(creationDateFilters, Collections.emptyList());
	}

	@Override
	public List<PripMetadata> findByProductName(List<PripTextFilter> nameFilters) {
		return findByCreationDateAndProductName(Collections.emptyList(), nameFilters);
	}

	@Override
	public List<PripMetadata> findByCreationDateAndProductName(List<PripDateTimeFilter> creationDateFilters,
			List<PripTextFilter> nameFilters) {
		List<PripMetadata> searchResult = new ArrayList<>();
		for (PripMetadata item : pripMetadataList) {
			boolean itemMatchesCreationDateFilters = true; 
			boolean itemMatchNameFilters = true;
			
			for (PripDateTimeFilter creationDateFilter: creationDateFilters) {
				switch (creationDateFilter.getOperator()) {
					case GT: itemMatchesCreationDateFilters &= item.getCreationDate().isAfter(creationDateFilter.getDateTime()); break;
					case LT: itemMatchesCreationDateFilters &= item.getCreationDate().isBefore(creationDateFilter.getDateTime()); break;
				}
			}
			for (PripTextFilter nameFilter : nameFilters) {
				switch (nameFilter.getFunction()) {
					case CONTAINS: itemMatchNameFilters &= item.getName().indexOf(nameFilter.getText()) >= 0; break;
					case STARTS_WITH: itemMatchNameFilters &= item.getName().startsWith(nameFilter.getText()); break;						
				}
			}
			
			if (itemMatchesCreationDateFilters && itemMatchNameFilters) {
				searchResult.add(item);
			}
		}
		return searchResult;
	}
		
	@Override
	public PripMetadata findById(String id) {
		UUID uuid = UUID.fromString(id);
		for (PripMetadata pripMetadata : pripMetadataList) {
			if (pripMetadata.getId().equals(uuid)) {
				return pripMetadata;
			}
		}
		return null;
	}

	private List<PripMetadata> createListOfDummyData() {
		return Arrays.asList( //
				createDummyMetadata( //
						"00000000-0000-0000-0000-000000000001", // id
						"DummyProduct1.ZIP", // name
						1000L, // size
						"2011-01-01T01:00:00.000Z", // creation date
						"2011-01-01T01:00:00.000Z", // eviction date
						"00000000000000000000000000000001" // checksum value
				), //
				createDummyMetadata( //
						"00000000-0000-0000-0000-000000000002", // id
						"DummyProduct2.ZIP", // name
						2000L, // size
						"2012-01-01T02:00:00.000Z", // creation date
						"2012-01-01T02:00:00.000Z", // eviction date
						"00000000000000000000000000000003" // checksum value
				), //
				createDummyMetadata( //
						"00000000-0000-0000-0000-000000000003", // id
						"DummyProduct3.ZIP", // name
						3000L, // size
						"2013-01-01T03:00:00.000Z", // creation date
						"2013-01-01T03:00:00.000Z", // eviction date
						"00000000000000000000000000000003" // checksum value
				));
	}

	private PripMetadata createDummyMetadata(String id, String name, long size, String creationDate,
			String evictionDate, String checksumValue) {
		PripMetadata metadata = new PripMetadata();
		metadata.setId(UUID.fromString(id));
		metadata.setName(name);
		metadata.setObsKey("Prefix/" + name);
		metadata.setProductFamily(ProductFamily.AUXILIARY_FILE_ZIP);
		metadata.setContentLength(size);
		metadata.setContentType("application/octet-stream");
		metadata.setCreationDate(DateUtils.parse(creationDate));
		metadata.setEvictionDate(DateUtils.parse(evictionDate));
		Checksum checksum = new Checksum();
		checksum.setAlgorithm("MD5");
		checksum.setValue(checksumValue);
		metadata.setChecksums(Arrays.asList(checksum));
		return metadata;
	}

}
