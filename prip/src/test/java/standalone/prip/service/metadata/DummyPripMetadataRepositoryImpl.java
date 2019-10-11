package standalone.prip.service.metadata;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.service.metadata.PripMetadataRepository;

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
						"2000-01-01T00:00:00.000000Z", // creation date
						"2000-01-01T00:00:00.000000Z", // eviction date
						"00000000000000000000000000000001" // checksum value
				), //
				createDummyMetadata( //
						"00000000-0000-0000-0000-000000000002", // id
						"DummyProduct2.ZIP", // name
						2000L, // size
						"2000-01-01T00:00:00.000000Z", // creation date
						"2000-01-01T00:00:00.000000Z", // eviction date
						"00000000000000000000000000000003" // checksum value
				), //
				createDummyMetadata( //
						"00000000-0000-0000-0000-000000000003", // id
						"DummyProduct3.ZIP", // name
						3000L, // size
						"2000-01-01T00:00:00.000000Z", // creation date
						"2000-01-01T00:00:00.000000Z", // eviction date
						"00000000000000000000000000000003" // checksum value
				));
	}

	private PripMetadata createDummyMetadata(String id, String name, long size, String creationDate,
			String evictionDate, String checksumValue) {
		PripMetadata metadata = new PripMetadata();
		metadata.setId(UUID.fromString(id));
		metadata.setName(name);
		metadata.setObsKey("Prefix/" + name);
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
