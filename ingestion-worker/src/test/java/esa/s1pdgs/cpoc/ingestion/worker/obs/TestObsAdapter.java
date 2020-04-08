package esa.s1pdgs.cpoc.ingestion.worker.obs;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductException;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class TestObsAdapter {
	@Mock
	ObsClient obsClient;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public final void testMove() throws ObsException, SdkClientException {
		final ObsAdapter uut = new ObsAdapter(obsClient, ReportingFactory.NULL);
		doReturn(true).when(obsClient).exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "bar/baaaaar"));
		uut.move(ProductFamily.AUXILIARY_FILE, ProductFamily.GHOST, "bar/baaaaar");
		verify(obsClient, times(1)).move(Mockito.eq(new ObsObject(ProductFamily.AUXILIARY_FILE, "bar/baaaaar")), Mockito.eq(ProductFamily.GHOST));
	}
	
	@Test
	public final void testMoveOnSourceDoesNotExists() throws ObsException, SdkClientException {
		final ObsAdapter uut = new ObsAdapter(obsClient, ReportingFactory.NULL);
		doReturn(false).when(obsClient).exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "bar/baaaaar"));
		doReturn(false).when(obsClient).exists(new ObsObject(ProductFamily.GHOST, "bar/baaaaar"));
		assertThatThrownBy(() ->  uut.move(ProductFamily.AUXILIARY_FILE, ProductFamily.GHOST, "bar/baaaaar"))
		.isInstanceOf(ProductException.class)
		.hasMessageContaining("File bar/baaaaar (AUXILIARY_FILE) to move does not exist");
	}
	
	@Test
	public final void testMoveOnDestinationAlreadyExists() throws ObsException, SdkClientException {
		final ObsAdapter uut = new ObsAdapter(obsClient, ReportingFactory.NULL);
		doReturn(true).when(obsClient).exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "bar/baaaaar"));
		doReturn(true).when(obsClient).exists(new ObsObject(ProductFamily.GHOST, "bar/baaaaar"));
		assertThatThrownBy(() ->  uut.move(ProductFamily.AUXILIARY_FILE, ProductFamily.GHOST, "bar/baaaaar"))
		.isInstanceOf(ProductException.class)
		.hasMessageContaining("File bar/baaaaar (GHOST) to already exist");
	}

}
