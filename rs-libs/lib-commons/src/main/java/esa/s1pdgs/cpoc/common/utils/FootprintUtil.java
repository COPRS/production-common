package esa.s1pdgs.cpoc.common.utils;

public class FootprintUtil {
	
	/**
	 * Elasticsearch checks whether the polygon's document-level orientation differs from the
	 * default orientation. If the orientation differs, Elasticsearch considers the polygon
	 * to cross the international dateline and splits the polygon at the dateline.
	 * NOTE: While the elasticsearch documentation is about the document-level field 'orientation',
	 * this goes hand in hand with the actual order of the points. So it's possible to either
	 * reverse the 'orientation' field or the actual order, but not both at the same time,
	 * as this would nullify.
	 * See: https://www.elastic.co/guide/en/elasticsearch/reference/7.14/geo-shape.html#polygon-orientation
	 * 
	 * @param longitudes
	 * @return
	 */
	static public String elasticsearchPolygonOrientation(Double... longitudes) {

		return (calculateMaxDifference(longitudes) >= 180.0) ? "clockwise" : "counterclockwise";
	}
	
	/**
	 * Calculate maximum difference of an array of values.
	 * 
	 * @param values
	 * @return maxDifference
	 */
	static Double calculateMaxDifference(final Double[] values) {
		Double max = 0.0;
		for (int i = 0; i < values.length - 1; i++) {
			Double d = Math.abs(values[i] - values[i + 1]);
			if (d > max)
				max = d;
		}
		return max;
	}
}
