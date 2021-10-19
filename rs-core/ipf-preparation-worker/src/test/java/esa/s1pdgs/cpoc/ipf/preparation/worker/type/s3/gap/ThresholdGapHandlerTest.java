package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3.gap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.S3Metadata;

public class ThresholdGapHandlerTest {

	private ThresholdGapHandler gapHandler;

	@Before
	public void before() {
		gapHandler = new ThresholdGapHandler(3.0);
	}

	@Test
	public void isCovered_ShouldReturnTrueIfIntervalHasNoBigGaps() {
		LocalDateTime intervalStart = LocalDateTime.of(2004, 7, 3, 0, 30, 17);
		LocalDateTime intervalStop = LocalDateTime.of(2004, 7, 3, 0, 32, 17);

		S3Metadata product = new S3Metadata();
		product.setValidityStart("2004-07-02T22:00:00.906000Z");
		product.setValidityStop("2004-07-03T10:00:00.906000Z");

		assertEquals(true, gapHandler.isCovered(intervalStart, intervalStop, Collections.singletonList(product)));
	}
	
	@Test
	public void isCovered_ShouldReturnFalseIfIntervalHasBigGaps() {
		LocalDateTime intervalStart = LocalDateTime.of(2004, 7, 3, 0, 30, 17);
		LocalDateTime intervalStop = LocalDateTime.of(2004, 7, 3, 0, 32, 17);

		S3Metadata product1 = new S3Metadata();
		product1.setValidityStart("2004-07-03T00:28:00.906000Z");
		product1.setValidityStop("2004-07-03T00:30:30.906000Z");
		
		S3Metadata product2 = new S3Metadata();
		product2.setValidityStart("2004-07-03T00:31:00.906000Z");
		product2.setValidityStop("2004-07-03T00:33:30.906000Z");
		
		List<S3Metadata> products = new ArrayList<>();
		products.add(product1);
		products.add(product2);

		assertEquals(false, gapHandler.isCovered(intervalStart, intervalStop, products));
	}
}
