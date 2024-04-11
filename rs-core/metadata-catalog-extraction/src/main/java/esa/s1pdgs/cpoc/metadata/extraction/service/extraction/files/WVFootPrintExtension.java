/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import esa.s1pdgs.cpoc.common.utils.FootprintUtil;

class FootPrint {
	private Point p1 = null;
	private Point p2 = null;
	private Point p3 = null;
	private Point p4 = null;
	final ArrayList<Point> coords = new ArrayList<Point>();

	// -63.324780,101.210495
	// -63.489353,101.382439
	// -63.570759,100.997200
	public FootPrint(final String _footprint) {

		final String[] tmpCoords = _footprint.split(" ");

		for (int i = 0; i < tmpCoords.length; i++) {
			if (i == 0) {
				p1 = new Point(tmpCoords[0]);
			} else if (i == 1) {
				p2 = new Point(tmpCoords[1]);
			} else if (i == 2) {
				p3 = new Point(tmpCoords[2]);
			} else if (i == 3) {
				p4 = new Point(tmpCoords[3]);
			}

			coords.add(new Point(tmpCoords[i]));
		}
	}

	public Point getP1() {
		return p1;
	}

	public Point getP2() {
		return p2;
	}

	public Point getP3() {
		return p3;
	}

	public Point getP4() {
		return p4;
	}

	public int size() {
		return coords.size();
	}

	public String toString() {
		final StringBuffer strBuffer = new StringBuffer();

		for (final Point p : coords) {
			strBuffer.append(p + " ");
		}
		return ("Coords: " + strBuffer.toString());
	}
}

class Point {
	private double lat;
	private double lon;

	public Point(final double lon, final double lat) {
	   this.lon = lon;
	   this.lat = lat;
	}
	
	public Point(final String _coord) {
		final String[] tmpPoint = _coord.split(",");
	   lat = Double.parseDouble(tmpPoint[0]);
		lon = Double.parseDouble(tmpPoint[1]);
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public String toString() {
		return (lat + "," + lon);
	}
}

public class WVFootPrintExtension {

	private static final Logger LOGGER = LogManager.getLogger(WVFootPrintExtension.class);

	public static Map<String, Object> getBoundingPolygon(final String _manifestFile) {

		final Map<String, Object> geoShape = new HashMap<>();
		final List<List<Double>> geoShapeCoordinates = new ArrayList<>();
		final ArrayList<Point> boundingPolygon = new ArrayList<Point>();

		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document doc = builder.parse(new File(_manifestFile));
			final XPathFactory xPathfactory = XPathFactory.newInstance();
			final XPath xpath = xPathfactory.newXPath();
			final XPathExpression expr = xpath.compile(
					"//*[local-name()='frameSet']/*[local-name()='frame']/*[local-name()='footPrint']/*[local-name()='coordinates']");

			final NodeList nodeFootprints = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
						
			// System.out.println("#List: " + nodeFootprints.getLength());
			
			final FootPrint firstFootPrint = new FootPrint(nodeFootprints.item(0).getTextContent());
			if (nodeFootprints.getLength() == 1) {
				boundingPolygon.add(firstFootPrint.getP1());
				boundingPolygon.add(firstFootPrint.getP2());
				boundingPolygon.add(firstFootPrint.getP3());
				boundingPolygon.add(firstFootPrint.getP4());
			} else {
			   final int indexA;
			   final int indexB;
			   final int indexC;
			   final int indexD;
			   if (isPolygonOfStartAndEndFrameCounterClockwise(nodeFootprints)) {
			      indexD = 0;
			      indexC = 1;
			      if (nodeFootprints.getLength() == 3) {
			         indexB = 2;
                  indexA = 2;
			      } else if (nodeFootprints.getLength() % 2 == 0) {
   			      indexB = nodeFootprints.getLength() - 1; // 2nd point of last pair
   			      indexA = nodeFootprints.getLength() - 2; // 1st point of last pair
			      } else {
			         indexB = nodeFootprints.getLength() - 2; // pairwise equivalent to 2nd point from the above case
                  indexA = nodeFootprints.getLength() - 1; // the additional single standing frame
			      }
			   } else {
               LOGGER.info("Inverse frame pair order detected");
               indexD = 1;
               indexC = 0;
               if (nodeFootprints.getLength() == 3) {
                  indexB = 2;
                  indexA = 2;
               } else if (nodeFootprints.getLength() % 2 == 0) {
                  indexB = nodeFootprints.getLength() - 2; // 2nd point of last pair
                  indexA = nodeFootprints.getLength() - 1; // 1st point of last pair
               } else {
                  indexB = nodeFootprints.getLength() - 1; // the additional single standing frame
                  indexA = nodeFootprints.getLength() - 2; // pairwise equivalent to 1st point from the above case
               }
			   }
			   
   			final Point pointD = new FootPrint(nodeFootprints.item(indexD).getTextContent()).getP4();
   			final Point pointC = new FootPrint(nodeFootprints.item(indexC).getTextContent()).getP3();
   			final Point pointB = new FootPrint(nodeFootprints.item(indexB).getTextContent()).getP2();
   			final Point pointA = new FootPrint(nodeFootprints.item(indexA).getTextContent()).getP1();

            boundingPolygon.add(pointA);
            boundingPolygon.add(pointB);
            boundingPolygon.add(pointC);
            boundingPolygon.add(pointD);
 			}

			for (final Point p : boundingPolygon) {
				geoShapeCoordinates.add(List.of(p.getLon(), p.getLat()));

				if (p.getLat() > 90 || p.getLat() < -90) {
					LOGGER.error("Error Latitude is not in the range (-90|90): {}", p.getLat());
				}

				if (p.getLon() > 180 || p.getLon() < -180) {
					LOGGER.error("Error Longitude is not in the range (-180|180): {}", p.getLon());
				}
			}

		} catch (final DOMException | ParserConfigurationException | XPathExpressionException | SAXException | IOException ex) {
			LOGGER.error("Error creating bounding polygon!");
			return null;
		} 
		//add the last one again
		geoShapeCoordinates.add(geoShapeCoordinates.get(0));
		geoShape.put("type", "Polygon");
		geoShape.put("orientation", "counterclockwise");
		geoShape.put("coordinates", List.of(geoShapeCoordinates));
		
		// RS-280: Use Elasticsearch Dateline Support
		final String orientation = FootprintUtil.elasticsearchPolygonOrientation(
				boundingPolygon.get(0).getLon(),
				boundingPolygon.get(1).getLon(),
				boundingPolygon.get(2).getLon(),
				boundingPolygon.get(3).getLon()
		);
		geoShape.put("orientation", orientation);
		if ("clockwise".equals(orientation)) {
			LOGGER.info("Adding dateline crossing marker");
		}
		
		LOGGER.debug(String.format("geo shape: %s", geoShape));

		return geoShape;
	}
	
	private static boolean isPolygonOfStartAndEndFrameCounterClockwise(final NodeList frames) {
	   // The most simple polygon (triangle) will do for checking orientation.
	   // We will pick three frames and use their centroids to create a triangle polygon.
	   // As the frames are of width and height below 1°, possible dateline and pole crossings can
	   // be corrected. But as the resulting triangle polygon might exceed a height of 90°, the
	   // pole crossing correction must be skipped there.
	   final List<Point> triangle = new ArrayList<>();
	   triangle.add(calculateCentroid(new FootPrint(frames.item(0).getTextContent()), true, true)); 
	   triangle.add(calculateCentroid(new FootPrint(frames.item(1).getTextContent()), true, true));
      triangle.add(calculateCentroid(new FootPrint(frames.item(
            frames.getLength() - 1).getTextContent()), true, true));
	   return isCounterClockwise(transformLaps(triangle, true, false));
	}
	
	private static boolean isCounterClockwise(final List<Point> polygon) {
	   final List<Point> points;
	   if (polygon.get(0).getLon() == polygon.get(polygon.size() - 1).getLon() &&
	         polygon.get(0).getLat() == polygon.get(polygon.size() - 1).getLat()) {
	      points = polygon; // use closed polygon directly
	   } else {
	      points = new ArrayList<>(polygon);
	      points.add(polygon.get(0)); // close polygon
	   }
	   double sum = 0.0;
	   Point previous = null;
	   for (final Point p: points) {
	      if (null != previous) {
	         sum += (p.getLon() - previous.getLon()) * (p.getLat() + previous.getLat());
	      }
	      previous = p;
	   }
	   return sum < 0.0;
	}
	
	public static Point calculateCentroid(final FootPrint footprint,
	      boolean correctDatelineCrossings, boolean correctPoleCrossings) {
	   final List<Point> points = transformLaps(List.of(footprint.getP1(), footprint.getP2(),
	         footprint.getP3(), footprint.getP4()), correctDatelineCrossings, correctPoleCrossings);
      final double avgLon = (points.get(0).getLon() + points.get(1).getLon() +
            points.get(2).getLon() + points.get(3).getLon()) / 4.0;           
      final double avgLat = (points.get(0).getLat() + points.get(1).getLat() +
            points.get(2).getLat() + points.get(3).getLat()) / 4.0;
      final double normalizedLon = avgLon >= 360.0 ? avgLon - 360.0 : avgLon;
      final double normalizedLat = avgLat >= 90.0 ? avgLat - 90.0 : avgLat;
	   return new Point(normalizedLon, normalizedLat);
	}
	
	public static List<Point> transformLaps(final List<Point> polygon,
	      final boolean correctDatelineCrossings, final boolean correctPoleCrossings) {
	   // Transforms polygon where vertices lap others (e.g. longitude -179° following after 179°)
	   // to polygon without lapping. This is done by shifting negative longitudes by 360° and
	   // negative latitudes by 180°. 
	   // Longitude correction requires an input polygon with a width lower than 180° and latitude
	   // correction requires an input polygon with a height lower than 90°.
	   // As the corrected vertices may have longitudes >= 360° and latitudes >= 90°
	   // the result, after further processing, might have to be normalized back to bipolar ranges.
	   // See: esa.s1pdgs.cpoc.common.utils.FootprintUtil.elasticsearchPolygonOrientation(Double...)
	   //   for more information including a visual example
      final List<Point> result = new ArrayList<>();
	   double minLon = Double.MAX_VALUE;
      double maxLon = Double.MIN_VALUE;
      for (final Point p : polygon) {
         minLon = Math.min(minLon, p.getLon());
         maxLon = Math.max(maxLon, p.getLon());
      }      
      double minLat = Double.MAX_VALUE;
      double maxLat = Double.MIN_VALUE;
      for (final Point p : polygon) {
         minLat = Math.min(minLat, p.getLat());
         maxLat = Math.max(maxLat, p.getLat());
      }      
      final double lonShift = correctDatelineCrossings &&
            Math.abs(minLon - maxLon) >= 180.0 ? 360.0 : 0.0;
      final double latShift = correctPoleCrossings &&
            Math.abs(minLat - maxLat) >= 90.0 ? 180.0 : 0.0;      
      for (final Point p : polygon) {
         final double lon = (p.getLon() < 0.0 ? p.getLon() + lonShift : p.getLon());
         final double lat = (p.getLat() < 0.0 ? p.getLat() + latShift : p.getLat());
         result.add(new Point(lon, lat));
      }
	   return result;
	}

}