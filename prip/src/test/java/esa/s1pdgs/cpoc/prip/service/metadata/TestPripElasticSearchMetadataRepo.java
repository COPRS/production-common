package esa.s1pdgs.cpoc.prip.service.metadata;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class TestPripElasticSearchMetadataRepo {

	@Autowired
	public PripElasticSearchMetadataRepo repo;

	@Test
	@Ignore
	public void testSave() {

		LocalDateTime creationDate = LocalDateTime.now();

		Checksum checksum1 = new Checksum();
		checksum1.setAlgorithm(Checksum.DEFAULT_ALGORITHM);
		checksum1.setValue("000000000000000000000");

		Checksum checksum2 = new Checksum();
		checksum2.setAlgorithm("SHA1");
		checksum2.setValue("111111111111111111111");

		List<Checksum> checksums = new ArrayList<>();
		checksums.add(checksum1);
		checksums.add(checksum2);

		PripMetadata pripMetadata = new PripMetadata();
		pripMetadata.setId(UUID.randomUUID());
		pripMetadata.setObsKey("productDto/keyObjectStorage");
		pripMetadata.setName("productDto.productName");
		pripMetadata.setContentType(PripMetadata.DEFAULT_CONTENTTYPE);
		pripMetadata.setContentLength(0);
		pripMetadata.setCreationDate(creationDate);
		pripMetadata.setEvictionDate(creationDate.plusDays(PripMetadata.DEFAULT_EVICTION_DAYS));
		pripMetadata.setChecksums(checksums);

		repo.save(pripMetadata);

	}

}
