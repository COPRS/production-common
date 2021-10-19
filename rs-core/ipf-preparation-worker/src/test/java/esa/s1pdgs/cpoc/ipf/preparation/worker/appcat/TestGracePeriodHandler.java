package esa.s1pdgs.cpoc.ipf.preparation.worker.appcat;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;

public class TestGracePeriodHandler {
	private final IpfPreparationWorkerSettings settings = new IpfPreparationWorkerSettings();

	private GracePeriodHandler uut;

	@Before
	public final void setUp() {
		settings.getWaitmetadatainput().setTempo(42);
		settings.getWaitprimarycheck().setTempo(23);
		uut = new GracePeriodHandler(settings);
	}

	@Test
	public final void testInitial_withinTimeInterval_ShallReturnTrue() {
		final Date now = new Date();
		final AppDataJob initJob = newJob(now, AppDataJobGenerationState.INITIAL, AppDataJobGenerationState.INITIAL);
		assertEquals(true, uut.isWithinGracePeriod(now, initJob.getGeneration()));
	}

	@Test
	public final void testInitial_withinTimeIntervalButDifferentState_ShallReturnFalse() {
		final Date now = new Date();
		final AppDataJob initJob = newJob(now, AppDataJobGenerationState.READY, AppDataJobGenerationState.READY);
		assertEquals(false, uut.isWithinGracePeriod(now, initJob.getGeneration()));
	}
	
	@Test
	public final void testPrimaryCheck_withinTimeInterval_ShallReturnTrue() {
		final Date now = new Date();
		final AppDataJob primJob = newJob(now, AppDataJobGenerationState.PRIMARY_CHECK,
				AppDataJobGenerationState.PRIMARY_CHECK);
		assertEquals(true, uut.isWithinGracePeriod(now, primJob.getGeneration()));
	}

	@Test
	public final void testPrimaryCheck_withinTimeIntervalButDifferentState_ShallReturnFalse() {
		final Date now = new Date();
		final AppDataJob primJob = newJob(now, AppDataJobGenerationState.READY,
				AppDataJobGenerationState.READY);
		assertEquals(false, uut.isWithinGracePeriod(now, primJob.getGeneration()));
	}
	
	@Test
	public final void testStateChange_ShallReturnFalse() {
		final Date now = new Date();
		final AppDataJob primJob = newJob(now, AppDataJobGenerationState.PRIMARY_CHECK,
				AppDataJobGenerationState.INITIAL);
		assertEquals(false, uut.isWithinGracePeriod(now, primJob.getGeneration()));
	}

	private final AppDataJob newJob(final Date lastUpdate, final AppDataJobGenerationState state,
			final AppDataJobGenerationState previousState) {
		final AppDataJob result = new AppDataJob();
		result.getGeneration().setState(state);
		result.getGeneration().setPreviousState(previousState);
		result.getGeneration().setLastUpdateDate(lastUpdate);
		return result;
	}
}
