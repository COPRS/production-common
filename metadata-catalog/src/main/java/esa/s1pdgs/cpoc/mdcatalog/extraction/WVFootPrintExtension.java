package esa.s1pdgs.cpoc.mdcatalog.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

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
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class FootPrint {
	private Point p1 = null;
	private Point p2 = null;
	private Point p3 = null;
	private Point p4 = null;
	ArrayList<Point> coords = new ArrayList<Point>();

	// -63.324780,101.210495
	// -63.489353,101.382439
	// -63.570759,100.997200
	public FootPrint(String _footprint) {

		String[] tmpCoords = _footprint.split(" ");

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
		StringBuffer strBuffer = new StringBuffer();

		for (Point p : coords) {
			strBuffer.append(p + " ");
		}
		return ("Coords: " + strBuffer.toString());
	}
}

class Point {
	private double lat;
	private double lon;

	public Point(String _coord) {
		String[] tmpPoint = _coord.split(",");
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

	public static JSONObject getBoundingPolygon(String _manifestFile) {

		JSONObject geoShape = new JSONObject();
		JSONArray coordinates = new JSONArray();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(_manifestFile));
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile(
					"//*[local-name()='frameSet']/*[local-name()='frame']/*[local-name()='footPrint']/*[local-name()='coordinates']");

			NodeList nodeFootprints = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			// System.out.println("#List: " + nodeFootprints.getLength());

			ArrayList<Point> boundingPolygon = new ArrayList<Point>();
			int footPrintScenario = 0; // 0 : start left side and number of footPrints is even
										// 1 : start left side and number of footPrints is odd
										// 2 : start right side and number of footPrints is even
										// 3 : start right side and number of footPrints is odd

			if (nodeFootprints.getLength() == 1) {
				FootPrint firstFootPrint;
				firstFootPrint = new FootPrint(nodeFootprints.item(0).getTextContent());
				boundingPolygon.add(firstFootPrint.getP1());
				boundingPolygon.add(firstFootPrint.getP2());
				boundingPolygon.add(firstFootPrint.getP3());
				boundingPolygon.add(firstFootPrint.getP4());
			} else {
				// first iteration to get right boundaries
				for (int i = 0; i < nodeFootprints.getLength(); i++) {
					FootPrint footPrint = new FootPrint(nodeFootprints.item(i).getTextContent());

					if (i == 0) {
						FootPrint secondFootPrint = new FootPrint(nodeFootprints.item(1).getTextContent());

						if (footPrintStartsLeftSide(footPrint, secondFootPrint)
								&& (nodeFootprints.getLength() % 2 == 0)) {
							footPrintScenario = 2;
							// System.out.println("Start on Right-Side and number of footPrints is even!
							// Scenario: " + footPrintScenario);

						} else if (footPrintStartsLeftSide(footPrint, secondFootPrint)
								&& (nodeFootprints.getLength() % 2 != 0)) {
							footPrintScenario = 3;
							// System.out.println("Start on Right-Side and number of footPrints is odd!
							// Scenario: " + footPrintScenario);
						} else if (!footPrintStartsLeftSide(footPrint, secondFootPrint)
								&& (nodeFootprints.getLength() % 2 != 0)) {
							footPrintScenario = 1;
							// System.out.println("Start on Left-Side and number of footPrints is odd!
							// Scenario: " + footPrintScenario);
						} else if (!footPrintStartsLeftSide(footPrint, secondFootPrint)
								&& (nodeFootprints.getLength() % 2 == 0)) {
							footPrintScenario = 0;
							// System.out.println("Start on Left-Side and number of footPrints is even!
							// Scenario: " + footPrintScenario);
						}

						else {
							// System.out.println("Good case!");
						}
						LOGGER.debug("Imagette #{}, scenatio: {}", i, footPrintScenario);
					}
					// System.out.println("Imagette #" + i + ", coords: " + footPrint);
				}

			}

			Point pointD = new FootPrint(nodeFootprints.item(0).getTextContent()).getP4();
			Point pointC = new FootPrint(nodeFootprints.item(1).getTextContent()).getP3();
			Point pointA = null;
			Point pointB = null;

			if (nodeFootprints.getLength() % 2 == 0) {
				pointB = new FootPrint(nodeFootprints.item(nodeFootprints.getLength() - 1).getTextContent()).getP2();
				pointA = new FootPrint(nodeFootprints.item(nodeFootprints.getLength() - 2).getTextContent()).getP1();
			} else {
				pointB = new FootPrint(nodeFootprints.item(nodeFootprints.getLength() - 1).getTextContent()).getP2();
				pointA = new FootPrint(nodeFootprints.item(nodeFootprints.getLength() - 1).getTextContent()).getP1();
			}

			boundingPolygon.add(pointA);
			boundingPolygon.add(pointB);
			boundingPolygon.add(pointC);
			boundingPolygon.add(pointD);

			;

			for (Point p : boundingPolygon) {

				// coordinates.put(new JSONArray("[" + String.format ("%f", p.getLat()) + "," +
				// String.format ("%f", p.getLon()) + "]"));
				coordinates.put(new JSONArray("[" + String.format(Locale.ROOT, "%f", p.getLon()) + ","
						+ String.format(Locale.ROOT, "%f", p.getLat()) + "]"));
				// boundingPolygonAsString.append("<point>\n");
				// boundingPolygonAsString.append("\t<latitude>" + String.format ("%f",
				// p.getLat()) + "</latitude>\n");
				// boundingPolygonAsString.append("\t<longitude>" + String.format ("%f",
				// p.getLon()) + "</longitude>\n");
				// boundingPolygonAsString.append("</point>\n");

				if (p.getLat() > 90 || p.getLat() < -90) {
					LOGGER.error("Error Latitude is not in the range (-90|90): {}", p.getLat());
				}

				if (p.getLon() > 180 || p.getLon() < -180) {
					LOGGER.error("Error Longitude is not in the range (-180|180): {}", p.getLon());
				}
			}

		} catch (DOMException | ParserConfigurationException | XPathExpressionException | SAXException | IOException  ex) {
			LOGGER.error("Error creating bounding polygon!");
			return null;
		} 
		//add the last one again
		coordinates.put(coordinates.get(0));
		geoShape.put("type", "Polygon");
		geoShape.put("orientation", "counterclockwise");
		geoShape.put("coordinates", new JSONArray().put(coordinates));

		LOGGER.debug(String.format("geo shape: %s", geoShape));

		return geoShape;
		// return boundingPolygonAsString.toString();
	}

	private static boolean footPrintStartsLeftSide(FootPrint footPrint, FootPrint secondFootPrint) {

		if (footPrint.getP1().getLon() > 0 && secondFootPrint.getP1().getLon() > 0) {
			return (footPrint.getP1().getLon() > secondFootPrint.getP1().getLon());
		} else if (footPrint.getP1().getLon() < 0 && secondFootPrint.getP1().getLon() < 0) {
			return (footPrint.getP1().getLon() > secondFootPrint.getP1().getLon());
		} else if (footPrint.getP1().getLon() < 0 && secondFootPrint.getP1().getLon() > 0) {
			return true;
		}

		return false;
	}
}