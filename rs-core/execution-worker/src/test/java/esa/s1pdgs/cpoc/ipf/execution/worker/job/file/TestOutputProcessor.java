package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.Test;

import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public class TestOutputProcessor {

	@Test
	public final void test() {
		final OutputProcessor uut = newOutputProcessor();
		final IpfExecutionJob job = new IpfExecutionJob();
		final UUID id = UUID.fromString("493460d9-7f1b-434e-9d90-776be731bbf6");
		
		job.setKeyObjectStorage("S1A_IW_RAW__0SDV_20200120T184423_20200120T184455_030888_038B80_3138.SAFE");
		
		final String expected = "s1pro-l0-asp-ipf-execution-worker-0_"
				+ "S1A_IW_RAW__0SDV_20200120T184423_20200120T184455_030888_038B80_3138.SAFE_"
				+ "493460d9-7f1b-434e-9d90-776be731bbf6_0";
		final String actual = uut.debugOutputPrefix("s1pro-l0-asp-ipf-execution-worker-0", id, job);
		assertEquals(expected, actual);
		System.out.println(actual);
	}
	
	private OutputProcessor newOutputProcessor() {
		return new OutputProcessor(
				null, 
				null, 
				null, 
				null, 
				null, 
				42, 
				null, 
				null, 
				null, 
				true);
	}
}
