package esa.s1pdgs.cpoc.preparation.worker.type.pdu.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.time.TimeInterval;

public class PDUGeneratorTest {

	@Test
	public void generateTimeIntervalsShouldMergeLastTImeInterval() {
		PDUFrameGenerator generator = new PDUFrameGenerator(null, null, null);

		List<TimeInterval> intervals = generator.generateTimeIntervals("2023-07-20T10:00:00.000000Z",
				"2023-07-20T11:00:00.500000Z", 60, 1);
		
		assertEquals(intervals.size(), 60);
	}
}
