package esa.s1pdgs.cpoc.preparation.worker.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.xml.sax.SAXException;

import esa.s1pdgs.cpoc.common.MaskType;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public class GeoIntersection {
	
	private static final Logger LOG = LogManager.getLogger(GeoIntersection.class);
	
	private final File maskFile;
	
	private final MaskType maskType;
	
	private final List<Polygon> maskPolygos;
	
	public GeoIntersection(final File maskFile, final MaskType maskType) {
	
		this.maskFile = maskFile;
		this.maskType = maskType;
		this.maskPolygos = new ArrayList<>();
	}
	
	
	public void loadMaskFile() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		
		final List<Map<String, Object>> featureCollection = new MaskExtractor().extract(maskFile);
		
		LOG.info("loading {} {} polygons", featureCollection.size(), maskType.toString());
		for (final Map<String, Object> feature : featureCollection) {
			LOG.trace("{} json: {}", maskType, feature.toString());
			@SuppressWarnings("unchecked")
			final Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
			Optional<Polygon> polygon = extractPolygon(geometry);
			if (polygon.isPresent()) {
				maskPolygos.add(polygon.get());
			}
		}
	}
	
	public long getCoverage(CatalogEvent catalogEvent) {
		
		long coverage = 0;
		double intersectionArea = 0.0;
		
		Optional<Polygon> polygon = extractPolygonFrom(catalogEvent); 
		
		if (polygon.isPresent()) {
			
			for (Polygon maskPolygon: maskPolygos) {
				if (polygon.get().intersects(maskPolygon)) {
					Geometry intersection = polygon.get().intersection(maskPolygon);
					intersectionArea += (100.0 * intersection.getArea() / polygon.get().getArea());
				}
			}
			coverage = 100 - Math.round(intersectionArea);
		}
		return coverage;
	}
	
	public boolean isIntersecting(CatalogEvent catalogEvent) {
		
		boolean intersects = false;
		
		Optional<Polygon> polygon = extractPolygonFrom(catalogEvent); 
		
		if (polygon.isPresent()) {
			
			for (Polygon maskPolygon: maskPolygos) {
				if (polygon.get().intersects(maskPolygon)) {
					intersects = true;
					break;
				}
			}
		}
		return intersects;
	}
	
	private Optional<Polygon> extractPolygonFrom(CatalogEvent catalogEvent) {
		@SuppressWarnings("unchecked")
		final Map<String, Object> sliceCoordinates = (Map<String, Object>) catalogEvent.getMetadata().get("sliceCoordinates");
		return extractPolygon(sliceCoordinates);

	}
	
	private Optional<Polygon> extractPolygon(final Map<String, Object> geometry) {
		
		String type = (String) geometry.get("type");
		
		if (!"Polygon".equalsIgnoreCase(type)) {
			LOG.trace("geometry {} is not of type polygon, skipping", type);
			return Optional.empty();
		}
		
		@SuppressWarnings("unchecked")
		final List<List<List<Double>>> geometryCoordinates = (List<List<List<Double>>>) geometry.get("coordinates");
	
		final List<List<Double>> exteriorRing = (List<List<Double>>) geometryCoordinates.get(0);
		
		final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		final Coordinate[] coordinates = new Coordinate[exteriorRing.size()];
		
		for (int i=0; i < exteriorRing.size(); i++) {
			final List<Double> coords = (List<Double>) exteriorRing.get(i);
			final double lon = coords.get(0).doubleValue();
			final double lat = coords.get(1).doubleValue();
			coordinates[i] = new Coordinate(lon, lat);
		}
		
		final LinearRing linearRing = geometryFactory.createLinearRing(coordinates);
		final Polygon polygon = geometryFactory.createPolygon(linearRing);
		
		if (polygon.isValid()) {
		
			return Optional.of(polygon);
		} else {
			return Optional.empty();
		}
	}

}
