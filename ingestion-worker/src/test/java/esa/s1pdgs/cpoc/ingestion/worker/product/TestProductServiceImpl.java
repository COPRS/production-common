package esa.s1pdgs.cpoc.ingestion.worker.product;

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
import esa.s1pdgs.cpoc.ingestion.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.report.Reporting;

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
		uut = new ProductServiceImpl(obsClient);
		
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
	public void testIngest() throws ProductException, InternalErrorException, ObsEmptyFileException {
		final ProductFamily family = ProductFamily.AUXILIARY_FILE;
		final IngestionJob ingestionJob = new IngestionJob("productName");
		ingestionJob.setPickupPath("/dev");
		ingestionJob.setRelativePath("null");
		ingestionJob.setProductFamily(family);
		ingestionJob.setCreationDate(new Date());
		ingestionJob.setHostname("hostname");
		final Product<IngestionEvent> product = new Product<>();
		product.setFamily(family);
		final IngestionEvent expectedProductionEvent = new IngestionEvent();
		expectedProductionEvent.setProductName("null");
		expectedProductionEvent.setKeyObjectStorage("null");
		expectedProductionEvent.setProductFamily(family);
		expectedProductionEvent.setHostname("hostname");
		expectedProductionEvent.setCreationDate(new Date());
		product.setDto(expectedProductionEvent);
		product.setFile(new File("/dev/null"));		
		final IngestionResult expectedResult = new IngestionResult(Arrays.asList(product), 0L);
		final IngestionResult actualResult = uut.ingest(family, ingestionJob, Reporting.ChildFactory.NULL);
		assertEquals(expectedResult.getIngestedProducts().size(), actualResult.getIngestedProducts().size());
		assertEquals(expectedResult.getTransferAmount(), actualResult.getTransferAmount());
	}

	@Test
	public void testMarkInvalid() throws AbstractCodedException, ObsEmptyFileException {
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setPickupPath("pickup/path");
		ingestionJob.setRelativePath("relative/path");
		uut.markInvalid(ingestionJob, Reporting.ChildFactory.NULL);
		final ObsUploadObject uploadObj = new ObsUploadObject(ProductFamily.INVALID, "relative/path", new File("pickup/path/relative/path"));
		verify(obsClient, times(1)).upload(Mockito.eq(Arrays.asList(uploadObj)), Mockito.any());
	}

	@Test
	public void testToObsKey() {
		assertEquals("/tmp/foo/bar/baaaaar", uut.toObsKey(Paths.get("/tmp/foo/bar/baaaaar")));
	}

	@Test
	public void testToFile() {
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setPickupPath("/tmp/foo");
		ingestionJob.setRelativePath("bar/baaaaar");
		assertEquals(new File("/tmp/foo/bar/baaaaar"), uut.toFile(ingestionJob));
	}

	@Test
	public void testAssertPermissions() {
		final IngestionJob ingestionJob = new IngestionJob();
		assertThatThrownBy(() -> ProductServiceImpl.assertPermissions(ingestionJob, nonExistentFile))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("File nonExistentFile of " + ingestionJob + " does not exist");

		assertThatThrownBy(() -> ProductServiceImpl.assertPermissions(ingestionJob, notReadableFile))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("File notReadableFile of " + ingestionJob + " is not readable");
		
		assertThatThrownBy(() -> ProductServiceImpl.assertPermissions(ingestionJob, notWritableFile))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("File notWritableFile of " + ingestionJob + " is not writeable");
	}
}
