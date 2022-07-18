package esa.s1pdgs.cpoc.common.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class FootprintUtilTest {

	@Test
	public void testWhenDatelineNotCrossedShallHaveAnticlockwiseOrientation1() {
		assertTrue("counterclockwise".equals(
				FootprintUtil.elasticsearchPolygonOrientation(new Double[]{170.0, 170.0, 150.0, 170.0})));
	}

	@Test
	public void testWhenDatelineNotCrossedShallHaveAnticlockwiseOrientation2() {
		assertTrue("counterclockwise".equals(
				FootprintUtil.elasticsearchPolygonOrientation(new Double[]{2.2002, -0.5619, -1.7321, 2.2002})));
	}

	@Test
	public void testWhenDatelineNotCrossedShallHaveAnticlockwiseOrientation3() {
		assertTrue("counterclockwise".equals(
				FootprintUtil.elasticsearchPolygonOrientation(new Double[]{48.279240, 50.6038440, -50.958828, 48.649940})));
	}
	
	@Test
	public void testWhenDatelineCrossedShallHaveClockwiseOrientation1() {
		assertTrue("clockwise".equals(
				FootprintUtil.elasticsearchPolygonOrientation(new Double[]{-90.0, 90.0, 50.0, 70.0})));
	}

	@Test
	public void testWhenDatelineCrossedShallHaveClockwiseOrientation2() {
		assertTrue("clockwise".equals(
				FootprintUtil.elasticsearchPolygonOrientation(new Double[]{-170.0, -170.0, 150.0, -170.0})));
	}

	@Test
	public void testWhenDatelineCrossedShallHaveClockwiseOrientation3() {
		// S1OPS-965 slice coordinates: [177.049759, 50.774021], [176.350082, 52.883804], [-179.945923, 53.278927], [-179.415802, 51.165802], [177.049759, 50.774021]
		assertTrue("clockwise".equals(
				FootprintUtil.elasticsearchPolygonOrientation(new Double[]{177.049759, 176.350082, -179.945923, -179.415802})));
	}
	

	public void testCalculateMaxDifference() {
		Double[] input = new Double[] { 2.2, 22.5, -180.0, 70.0, -180.0, -180.0, 180.5 };
		Double expected = 360.5;
		assertEquals(expected, FootprintUtil.calculateMaxDifference(input));
	}
}
