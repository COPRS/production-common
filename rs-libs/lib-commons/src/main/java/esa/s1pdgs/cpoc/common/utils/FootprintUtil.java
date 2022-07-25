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
        final String orientation;

        if (maxDifference(longitudes) < 180.0) {
        	
            // Elasticsearch checks the minimum and the maximum longitude and if the difference
            // is less than 180°, the polygon is considered to not cross the dateline.
        	
            orientation = "counterclockwise";
            
        } else {

        	// When the difference is 180° or greater, we have the choice to tell Elasticsearch
            // if the polygon crosses the dateline or not.
            //
        	// As not only small product footprints are stored but also very wide landmask polygons,
        	// we need to detect if the orientation shall be switched or not.
            //
        	// For this we calculate the max. distance of the longitudes again but from the perspective
        	// of having a dateline crossing polygon.
        	//
        	// Example: Ambiguous polygon with four points A, B, A', B'
            // ----------------|----------------
            //  A              |            B
            //   B'            |          A'
            // ----------------|----------------
            // -180            0            +180
            //
            // We shift the range from -180..180 to 0..360 by adding 360 to all negative longitudes
            // ----------------|----------------
            //             B   | A              
            //           A'    |  B'       
            // ----------------|----------------
            // 0              180            360
        	//
        	// Then we calculate the max. distance of the shifted longitudes.
        	//
        	// If the difference is less than 180°, then the new polygon is smaller than the previous one,
        	// as it is expected for a polygon that was huge because of wrong interpretation,
        	// and we switch the orientation.
            //
            // If the difference is 180° or greater, we are having a state where we cannot decide,
        	// because the previous and the current polygon both appear to possibly cross the dateline.
        	// Thus we leave things as they are.
        	//
        	// Note: We use the same threshold from the initial check instead of just comparing the new
        	// distance against the previous to avoid problems with distances that are very near to each other.
        	// This adds a safety margin and is per its reciprocal nature the most fair countercheck strategy.
        	
        	final Double[] shiftedLongitudes = new Double[longitudes.length];
        	for (int idx = 0; idx < shiftedLongitudes.length; idx++) {
        		shiftedLongitudes[idx] = longitudes[idx] < 0.0 ? longitudes[idx] + 360.0 : longitudes[idx];
        	}        	

        	orientation = maxDifference(shiftedLongitudes) < 180.0 ? "clockwise" : "counterclockwise";
        }

        return orientation;
    }
    
    /**
     * Calculate maximum difference of an array of values.
     * 
     * @param values
     * @return maxDifference
     */
    static Double maxDifference(final Double[] values) {
    	if (values.length <= 1) {
    		return 0.0;
    	} else {
    		Double min = values[0];
    		Double max = values[0];    		
	        for (int idx = 1; idx < values.length; idx++) {
	        	if (values[idx] < min) {
	        		min = values[idx];
	        	}
	        	if (values[idx] > max) {
	        		max = values[idx];
	        	}
	        }
        	return Math.abs(min - max);
        }
    }
}
