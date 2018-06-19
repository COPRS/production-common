package fr.viveris.s1pdgs.archives.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.archives.controller.dto.SliceDto;
import fr.viveris.s1pdgs.archives.model.ProductFamily;
import fr.viveris.s1pdgs.archives.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.archives.model.exception.ObsUnknownObjectException;
import fr.viveris.s1pdgs.archives.services.ObsService;

public class SlicesConsumerTest {

	@Mock
	private ObsService obsService;
	
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	private void mockSliceExists(boolean result) throws ObjectStorageException {
		doReturn(result).when(obsService).exist(Mockito.any(ProductFamily.class), Mockito.anyString());
	}
	
	private void mockSliceDownloadFiles(File result) throws ObjectStorageException, ObsUnknownObjectException {
		doReturn(result).when(obsService).downloadFile(Mockito.any(ProductFamily.class), Mockito.anyString(), Mockito.anyString());
	}
	
	private void mockSliceObjectStorageException() throws ObjectStorageException {
		doThrow(new ObjectStorageException(ProductFamily.L0_PRODUCT, "kobs", new Throwable())).when(obsService).exist(Mockito.any(ProductFamily.class),Mockito.anyString());
	}
	
	private void mockSliceObsUnknownObjectException() throws ObsUnknownObjectException, ObjectStorageException {
		doThrow(new ObsUnknownObjectException(ProductFamily.UNKNOWN, "kobs")).when(obsService).downloadFile(Mockito.any(ProductFamily.class), Mockito.anyString(), Mockito.anyString());
	}
		
	@Test
	public void testReceiveL0Slice() throws ObjectStorageException, ObsUnknownObjectException {
		SlicesConsumer consumer = new SlicesConsumer(obsService, "test/data/slices");
		this.mockSliceExists(true);
		File expectedResult = new File("test/data/slices/l0_product/productName");
		this.mockSliceDownloadFiles(expectedResult);
		consumer.receive(new SliceDto("productName", "kobs", "L0_PRODUCT"));
	}
	
	@Test
	public void testReceiveL0SliceNotPresentInOBS() throws ObjectStorageException, ObsUnknownObjectException {
		SlicesConsumer consumer = new SlicesConsumer(obsService, "test/data/slices");
		this.mockSliceExists(false);
		consumer.receive(new SliceDto("productName", "kobs", "L0_PRODUCT"));
		verify(obsService, never()).downloadFile(Mockito.any(ProductFamily.class), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void testReceiveL0SliceObjectStorageException() throws ObjectStorageException, ObsUnknownObjectException {
		SlicesConsumer consumer = new SlicesConsumer(obsService, "test/data/slices");
		this.mockSliceObjectStorageException();
		consumer.receive(new SliceDto("productName", "kobs", "L0_PRODUCT"));
		verify(obsService, never()).downloadFile(Mockito.any(ProductFamily.class), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void testReceiveL0SliceObsUnknownObjectException() throws ObjectStorageException, ObsUnknownObjectException {
		SlicesConsumer consumer = new SlicesConsumer(obsService, "test/data/slices");
		this.mockSliceObsUnknownObjectException();
		consumer.receive(new SliceDto("productName", "kobs", "L0_PRODUCT"));
		verify(obsService, never()).downloadFile(Mockito.any(ProductFamily.class), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void testReceiveL1Slice() throws ObjectStorageException, ObsUnknownObjectException {
		SlicesConsumer consumer = new SlicesConsumer(obsService, "test/data/slices");
		this.mockSliceExists(true);
		File expectedResult = new File("test/data/slices/l1_product/productName");
		this.mockSliceDownloadFiles(expectedResult);
		consumer.receive(new SliceDto("productName", "kobs", "L1_PRODUCT"));
	}
	
	@Test
	public void testReceiveL1SliceNotPresentInOBS() throws ObjectStorageException, ObsUnknownObjectException {
		SlicesConsumer consumer = new SlicesConsumer(obsService, "test/data/slices");
		this.mockSliceExists(false);
		consumer.receive(new SliceDto("productName", "kobs", "L1_PRODUCT"));
		verify(obsService, never()).downloadFile(Mockito.any(ProductFamily.class), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void testReceiveL1SliceObjectStorageException() throws ObjectStorageException, ObsUnknownObjectException {
		SlicesConsumer consumer = new SlicesConsumer(obsService, "test/data/slices");
		this.mockSliceObjectStorageException();
		consumer.receive(new SliceDto("productName", "kobs", "L1_PRODUCT"));
		verify(obsService, never()).downloadFile(Mockito.any(ProductFamily.class), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void testReceiveL1SliceObsUnknownObjectException() throws ObjectStorageException, ObsUnknownObjectException {
		SlicesConsumer consumer = new SlicesConsumer(obsService, "test/data/slices");
		this.mockSliceObsUnknownObjectException();
		consumer.receive(new SliceDto("productName", "kobs", "L1_PRODUCT"));
		verify(obsService, never()).downloadFile(Mockito.any(ProductFamily.class), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void testReceiveUnknownSlice() throws ObjectStorageException {
		SlicesConsumer consumer = new SlicesConsumer(obsService, "test/data/slices");
		consumer.receive(new SliceDto("productName", "kobs", "L2_PRODUCT"));
	}

}
