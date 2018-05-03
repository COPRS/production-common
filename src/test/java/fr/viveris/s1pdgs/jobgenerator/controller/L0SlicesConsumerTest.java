package fr.viveris.s1pdgs.jobgenerator.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.jobgenerator.config.L0SlicePatternSettings;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.L0SliceDto;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.product.L0Slice;
import fr.viveris.s1pdgs.jobgenerator.model.product.L0SliceProduct;
import fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher.L0SliceJobsDispatcher;

public class L0SlicesConsumerTest {

	@Mock
	private L0SliceJobsDispatcher l0SliceJobsDispatcher;

	@Mock
	private L0SlicePatternSettings l0SlicePatternSettings;

	@Before
	public void setUp() throws Exception {

		// Mcokito
		MockitoAnnotations.initMocks(this);

		// Mock the dispatcher
		Mockito.doAnswer(i -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {

			}
			return true;
		}).when(l0SliceJobsDispatcher).dispatch(Mockito.any());

		// Mock the settings
		Mockito.doReturn(
				"^([0-9a-z]{2})([0-9a-z]){1}_(([0-9a-z]{2})_RAW__0([0-9a-z_]{3}))_([0-9a-z]{15})_([0-9a-z]{15})_([0-9a-z_]{6})\\w{1,}\\.SAFE(/.*)?$")
				.when(l0SlicePatternSettings).getRegexp();
		Mockito.doReturn(2).when(l0SlicePatternSettings).getPlaceMatchSatelliteId();
		Mockito.doReturn(1).when(l0SlicePatternSettings).getPlaceMatchMissionId();
		Mockito.doReturn(4).when(l0SlicePatternSettings).getPlaceMatchAcquisition();
		Mockito.doReturn(6).when(l0SlicePatternSettings).getPlaceMatchStartTime();
		Mockito.doReturn(7).when(l0SlicePatternSettings).getPlaceMatchStopTime();
	}

	@Test
	public void testProductNameNotMatch() throws AbstractCodedException {
		String file = "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		String fileNotMatch = "S1A_I_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		L0SliceDto dto = new L0SliceDto(fileNotMatch, file);

		L0SlicesConsumer consumer = new L0SlicesConsumer(l0SliceJobsDispatcher, l0SlicePatternSettings);
		consumer.receive(dto);

		verify(l0SliceJobsDispatcher, never()).dispatch(Mockito.any());
	}

	@Test
	public void testReceiveOk() throws AbstractCodedException, ParseException {
		String file = "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		L0SliceDto dto = new L0SliceDto(file, file);

		L0SlicesConsumer consumer = new L0SlicesConsumer(l0SliceJobsDispatcher, l0SlicePatternSettings);
		consumer.receive(dto);

		DateFormat format = new SimpleDateFormat(L0SlicesConsumer.DATE_FORMAT);
		L0Slice slice = new L0Slice("IW");
		L0SliceProduct product = new L0SliceProduct(file, "A", "S1", format.parse("20171213T121623"),
				format.parse("20171213T121656"), slice);
		Job<L0Slice> job = new Job<>(product);

		verify(l0SliceJobsDispatcher, times(1)).dispatch(Mockito.eq(job));
	}
}
