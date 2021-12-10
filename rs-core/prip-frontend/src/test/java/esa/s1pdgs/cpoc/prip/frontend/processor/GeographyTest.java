package esa.s1pdgs.cpoc.prip.frontend.processor;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.server.core.uri.UriParameterImpl;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor.ProductsFilterVisitor;
import esa.s1pdgs.cpoc.prip.frontend.service.rest.OdataController;

public class GeographyTest {

	private static final int SRID = 4326;

	private static final Coordinate[] POINT_COORDINATES = { new Coordinate(44.8571, 20.3411) };
	private static final String POINT_WKT = "POINT(44.8571 20.3411)";
	private static final String POINT_EWKT = "SRID=" + SRID + ";" + POINT_WKT;
	private static final String POINT_ODATA_PARAMETER = "geography'" + POINT_EWKT + "'";

	private static final String LINESTRING_WKT = "LINESTRING(44.8571 20.3411, 11.4484 49.9204, 2.4321 32.3625)";
	private static final String LINESTRING_EWKT = "SRID=" + SRID + ";" + LINESTRING_WKT;
	private static final String LINESTRING_ODATA_PARAMETER = "geography'" + LINESTRING_EWKT + "'";
	private static final Coordinate[] LINESTRING_COORDINATES = { new Coordinate(44.8571, 20.3411), new Coordinate(11.4484, 49.9204),
			new Coordinate(2.4321, 32.3625) };

	private static final String POLYGON_WKT = "POLYGON((44.8571 20.3411, 11.4484 49.9204, 2.4321 32.3625, 13.4321 1.3625, 44.8571 20.3411))";
	private static final String POLYGON_EWKT = "SRID=" + SRID + ";" + POLYGON_WKT;
	private static final String POLYGON_ODATA_PARAMETER = "geography'" + POLYGON_EWKT + "'";
	private static final Coordinate[] POLYGON_COORDINATES = { new Coordinate(44.8571, 20.3411), new Coordinate(11.4484, 49.9204),
			new Coordinate(2.4321, 32.3625), new Coordinate(13.4321, 1.3625), new Coordinate(44.8571, 20.3411) };

	private static final Pattern ODATA_EWKT_EXTRACTION_PATTERN = ProductsFilterVisitor.ODATA_EWKT_EXTRACTION_PATTERN;

	@Test
	public void testPointWktAndSridExtraction() throws Exception {

		final Matcher matcher = ODATA_EWKT_EXTRACTION_PATTERN.matcher(POINT_ODATA_PARAMETER);

		assertTrue(matcher.matches());
		assertTrue(String.valueOf(SRID).equals(matcher.group(2)));
		assertTrue(POINT_WKT.equals(matcher.group(3)));
	}

	@Test
	public void testLinestringWktAndSridExtraction() throws Exception {

		final Matcher matcher = ODATA_EWKT_EXTRACTION_PATTERN.matcher(LINESTRING_ODATA_PARAMETER);

		assertTrue(matcher.matches());
		assertTrue(String.valueOf(SRID).equals(matcher.group(2)));
		assertTrue(LINESTRING_WKT.equals(matcher.group(3)));
	}

	@Test
	public void testPolygonWktAndSridExtraction() throws Exception {

		final Matcher matcher = ODATA_EWKT_EXTRACTION_PATTERN.matcher(POLYGON_ODATA_PARAMETER);

		assertTrue(matcher.matches());
		assertTrue(String.valueOf(SRID).equals(matcher.group(2)));
		assertTrue(POLYGON_WKT.equals(matcher.group(3)));
	}

	@Test
	public void testOdataPointEwktToGeometryConversion() throws Exception {
		final UriParameterImpl uriParameter = new UriParameterImpl();
		uriParameter.setText(POINT_ODATA_PARAMETER);

		final Geometry geometry = ProductsFilterVisitor.asGeometry(uriParameter);
		assertTrue(null != geometry);
		assertTrue(SRID == geometry.getSRID());
		assertTrue(null != geometry.getCoordinates() && Arrays.equals(POINT_COORDINATES, geometry.getCoordinates()));
	}

	@Test
	public void testOdataLinestringEwktToGeometryConversion() throws Exception {
		final UriParameterImpl uriParameter = new UriParameterImpl();
		uriParameter.setText(LINESTRING_ODATA_PARAMETER);

		final Geometry geometry = ProductsFilterVisitor.asGeometry(uriParameter);
		assertTrue(null != geometry);
		assertTrue(SRID == geometry.getSRID());
		assertTrue(null != geometry.getCoordinates() && Arrays.equals(LINESTRING_COORDINATES, geometry.getCoordinates()));
	}

	@Test
	public void testOdataPolygonEwktToGeometryConversion() throws Exception {
		final UriParameterImpl uriParameter = new UriParameterImpl();
		uriParameter.setText(POLYGON_ODATA_PARAMETER);

		final Geometry geometry = ProductsFilterVisitor.asGeometry(uriParameter);
		assertTrue(null != geometry);
		assertTrue(SRID == geometry.getSRID());
		assertTrue(null != geometry.getCoordinates() && Arrays.equals(POLYGON_COORDINATES, geometry.getCoordinates()));
	}

	@Test
	public void testOdataPointWithinParameterHandling() throws Exception {
		final String functionName = EdmProvider.FUNCTION_WITHIN_FQN.getFullQualifiedNameAsString();
		final String queryString = "$filter=" + functionName + "(area=" + POINT_ODATA_PARAMETER + ")";
		final String processedQueryStr = OdataController.handleGeometricRequests(queryString);

		assertTrue(("$filter=" + functionName + "(geo_property=Footprint,geo_shape=" + POINT_ODATA_PARAMETER + ")")
				.equals(processedQueryStr));
	}

	@Test
	public void testOdataLinestringDisjointsParameterHandling() throws Exception {
		final String functionName = EdmProvider.FUNCTION_DISJOINT_FQN.getFullQualifiedNameAsString();
		final String queryString = "$filter=" + functionName + "(area=" + LINESTRING_ODATA_PARAMETER + ")";
		final String processedQueryStr = OdataController.handleGeometricRequests(queryString);

		assertTrue(("$filter=" + functionName + "(geo_property=Footprint,geo_shape=" + LINESTRING_ODATA_PARAMETER + ")")
				.equals(processedQueryStr));
	}

	@Test
	public void testOdataPolygonIntersectsParameterHandling() throws Exception {
		final String functionName = EdmProvider.FUNCTION_INTERSECTS_FQN.getFullQualifiedNameAsString();
		final String queryString = "$filter=" + EdmProvider.FUNCTION_INTERSECTS_FQN.getFullQualifiedNameAsString() + "(area=" + POLYGON_ODATA_PARAMETER + ")";
		final String processedQueryStr = OdataController.handleGeometricRequests(queryString);

		assertTrue(("$filter=" + functionName + "(geo_property=Footprint,geo_shape=" + POLYGON_ODATA_PARAMETER + ")")
				.equals(processedQueryStr));
	}

}
