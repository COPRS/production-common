package esa.s1pdgs.cpoc.ingestion.worker.product;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.ingestion.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

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
	
/*
	
	@Test
	public void testIngest() throws ProductException, InternalErrorException, ObsEmptyFileException {
		final ProductFamily family = ProductFamily.AUXILIARY_FILE;
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setPickupBaseURL("file:///dev");
		ingestionJob.setRelativePath("null");
		ingestionJob.setProductFamily(family);
		ingestionJob.setProductName("productName");
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
		final IngestionResult expectedResult = new IngestionResult(Arrays.asList(product), 0L);
		final IngestionResult actualResult = uut.ingest(family, ingestionJob, ReportingFactory.NULL);
		assertEquals(expectedResult.getIngestedProducts().size(), actualResult.getIngestedProducts().size());
		assertEquals(expectedResult.getTransferAmount(), actualResult.getTransferAmount());
	}

	@Test
	public void testMarkInvalid() throws AbstractCodedException, ObsEmptyFileException {
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setPickupBaseURL("file:///pickup/path");
		ingestionJob.setRelativePath("relative/path");
		uut.markInvalid(ingestionJob, ReportingFactory.NULL);
		final FileObsUploadObject uploadObj = new FileObsUploadObject(ProductFamily.INVALID, AbstractMessage.NOT_DEFINED,
				new File("/pickup/path/relative/path"));
		verify(obsClient, times(1)).upload(Mockito.eq(Arrays.asList(uploadObj)), Mockito.any());
	}
*/
	@Test
	public void testToObsKey() {
		assertEquals(Paths.get("/tmp/foo/bar/baaaaar").toString(), uut.toObsKey(Paths.get("/tmp/foo/bar/baaaaar")));
	}
}
