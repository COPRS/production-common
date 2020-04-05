package esa.s1pdgs.cpoc.ingestion.worker.product;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterEntry;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.StreamObsUploadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

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
	
	@Mock
	InboxAdapter inboxAdapter;

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
	public void testIngest() throws Exception {
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
		final List<Product<IngestionEvent>> expectedResult = Arrays.asList(product);
		final List<Product<IngestionEvent>> actualResult = uut.ingest(family, inboxAdapter, ingestionJob, ReportingFactory.NULL);
		assertEquals(expectedResult.size(), actualResult.size());
	}

	@Test
	public void testMarkInvalid() throws Exception {
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setProductName("productName");
		ingestionJob.setPickupBaseURL("file:///pickup/path");
		ingestionJob.setRelativePath("relative/path");
		InboxAdapterEntry inboxAdapterEntry = new InboxAdapterEntry("key",	new ByteArrayInputStream("test".getBytes()), 0L);
		doReturn(Arrays.asList(inboxAdapterEntry)).when(inboxAdapter).read(Mockito.any(), Mockito.anyString());
		uut.markInvalid(inboxAdapter, ingestionJob, ReportingFactory.NULL);
		final StreamObsUploadObject uploadObj = new StreamObsUploadObject(ProductFamily.INVALID, "key",
				new ByteArrayInputStream("test".getBytes()), 0L);
		verify(obsClient, times(1)).uploadStreams(Mockito.eq(Arrays.asList(uploadObj)), Mockito.any());
	}

	@Test
	public void testToObsKey() {
		assertEquals(Paths.get("/tmp/foo/bar/baaaaar").toString(), uut.toObsKey(Paths.get("/tmp/foo/bar/baaaaar")));
	}
}
