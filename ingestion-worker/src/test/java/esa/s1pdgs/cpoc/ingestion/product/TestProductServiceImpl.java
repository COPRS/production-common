package esa.s1pdgs.cpoc.ingestion.product;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.ingestion.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;

public class TestProductServiceImpl {
	
	ProductServiceImpl uut;
	
	@Mock
	ObsClient obsClient;
	
	@Mock
	File nonExistentFile;

	@Mock
	File notReadableFile;

	@Mock
	File notWritableFile;

	ProcessConfiguration processConfiguration;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		processConfiguration = new ProcessConfiguration();
		processConfiguration.setHostname("hostname");
		uut = new ProductServiceImpl(obsClient, processConfiguration);
		
		doReturn(false).when(nonExistentFile).exists();
		doReturn(false).when(nonExistentFile).canRead();
		doReturn(false).when(nonExistentFile).canWrite();
		doReturn("nonExistentFile").when(nonExistentFile).toString();

		doReturn(true).when(notReadableFile).exists();
		doReturn(false).when(notReadableFile).canRead();
		doReturn(false).when(notReadableFile).canWrite();
		doReturn("notReadableFile").when(notReadableFile).toString();
		
		doReturn(true).when(notWritableFile).exists();
		doReturn(true).when(notWritableFile).canRead();
		doReturn(false).when(notWritableFile).canWrite();
		doReturn("notWritableFile").when(notWritableFile).toString();
	}
	
	@Test
	public void testIngest() throws ProductException, InternalErrorException {
		final Date now = new Date();
		final ProductFamily family = ProductFamily.AUXILIARY_FILE;
		IngestionDto ingestionDto = new IngestionDto("productName");
		ingestionDto.setPickupPath("/dev");
		ingestionDto.setRelativePath("null");
		ingestionDto.setFamily(family);
		ingestionDto.setMissionId("S1");
		ingestionDto.setSatelliteId("A");
		ingestionDto.setStationCode("WILE");
		ingestionDto.setCreationDate(now);
		ingestionDto.setHostname("hostname");
		Product<AbstractDto> product = new Product<>();
		product.setFamily(family);
		ProductDto expectedProductDto = new ProductDto("null", "null", family);
		expectedProductDto.setHostname("hostname");
		expectedProductDto.setCreationDate(now);
		product.setDto(expectedProductDto);
		product.setFile(new File("/dev/null"));		
		IngestionResult expectedResult = new IngestionResult(Arrays.asList(product), 0L);
		IngestionResult actualResult = uut.ingest(family, ingestionDto);
		assertEquals(expectedResult.toString(), actualResult.toString());
	}

	@Test
	public void testMarkInvalid() throws AbstractCodedException {
		IngestionDto ingestionDto = new IngestionDto();
		ingestionDto.setPickupPath("pickup/path");
		ingestionDto.setRelativePath("relative/path");
		uut.markInvalid(ingestionDto);
		ObsUploadObject uploadObj = new ObsUploadObject(ProductFamily.INVALID, "relative/path", new File("pickup/path/relative/path"));
		verify(obsClient, times(1)).upload(Mockito.eq(Arrays.asList(uploadObj)));
	}

	@Test
	public void testToObsKey() {
		assertEquals("/tmp/foo/bar/baaaaar", uut.toObsKey(Paths.get("/tmp/foo/bar/baaaaar")));
	}

	@Test
	public void testToFile() {
		IngestionDto ingestionDto = new IngestionDto();
		ingestionDto.setPickupPath("/tmp/foo");
		ingestionDto.setRelativePath("bar/baaaaar");
		assertEquals(new File("/tmp/foo/bar/baaaaar"), uut.toFile(ingestionDto));
	}

	@Test
	public void testAssertPermissions() {
		IngestionDto ingestionDto = new IngestionDto();
		assertThatThrownBy(() -> ProductServiceImpl.assertPermissions(ingestionDto, nonExistentFile))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("File nonExistentFile of " + ingestionDto + " does not exist");

		assertThatThrownBy(() -> ProductServiceImpl.assertPermissions(ingestionDto, notReadableFile))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("File notReadableFile of " + ingestionDto + " is not readable");
		
		assertThatThrownBy(() -> ProductServiceImpl.assertPermissions(ingestionDto, notWritableFile))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("File notWritableFile of " + ingestionDto + " is not writeable");
	}
}
