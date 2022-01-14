package de.werum.coprs.nativeapi;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.werum.coprs.nativeapi.rest.model.stac.StacItem;
import de.werum.coprs.nativeapi.rest.model.stac.StacItemCollection;
import de.werum.coprs.nativeapi.service.mapping.PripToStacMapper;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;

public class TestStacTypeMapping {

	@Test
	public void testStacItemCreation() throws Exception {
		final StacItem stacItem = new StacItem();
		stacItem.setId("bc170330-3d38-43ba-97fb-7bb6e61f8fc4");

		final double[][] coordArray = { { -75.312935, -14.93704 }, { -75.312935, -11.387472 }, { -14.312935, -11.387472 }, { -14.312935, -14.387472 },	{ -75.312935, -14.93704 } };
		final List<LngLatAlt> coords = Arrays.asList(coordArray).stream().map(longLatArray -> new LngLatAlt(longLatArray[0], longLatArray[1])).collect(Collectors.toList());
		stacItem.setGeometry(new Polygon(coords));

		if (null != stacItem.getGeometry()) {
			stacItem.setBbox(this.getBoundingBox(stacItem.getGeometry()));
		}

		final String stacItemJsonStr = new ObjectMapper().writeValueAsString(stacItem);
		System.out.println("stac item:\n" + stacItemJsonStr);

		assertTrue(true);
	}

	private double[] getBoundingBox(final GeoJsonObject geometry) {
		if (geometry instanceof Polygon) {
			final List<LngLatAlt> allPoints = CollectionUtil.nullToEmptyList(((Polygon) geometry).getCoordinates()).stream().flatMap(List::stream)
					.collect(Collectors.toList());

			if (!allPoints.isEmpty()) {
				final List<Double> allXcoords = allPoints.stream().map(lngLat -> lngLat.getLongitude()).collect(Collectors.toList());
				final List<Double> allYcoords = allPoints.stream().map(lngLat -> lngLat.getLatitude()).collect(Collectors.toList());

				// [minX,   minY,   maxX,   maxY  ]
				// [minLng, minLat, maxLng, maxLat]
				final double minX = Collections.min(allXcoords);
				final double minY = Collections.min(allYcoords);
				final double maxX = Collections.max(allXcoords);
				final double maxY = Collections.max(allYcoords);

				// TODO: handle cases which cross the Antimeridian (https://datatracker.ietf.org/doc/html/rfc7946#section-5.2)
				// TODO: handle cases which contains a pole (https://datatracker.ietf.org/doc/html/rfc7946#section-5.3)

				return new double[] { minX, minY, maxX, maxY };
			}
		} else {
			System.out.println("minimal bounding rectangle algorithm not implemented for type: " + geometry.getClass().getSimpleName());
		}

		return null;
	}

	@Test
	public void testPripProductToStacItemMapping() throws Exception {
		final String pripOdataJsonStr = "{\"@odata.context\":\"$metadata#Products\",\"value\":[{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"6838e65e-8e1b-45d8-9128-8bf5db364e39\",\"Name\":\"S1A_OPER_AMV_ERRMAT_MPC__20200120T040010_V20000101T000000_20200119T201427.EOF.zip\",\"ContentType\":\"application/octet-stream\",\"ContentLength\":11500,\"PublicationDate\":\"2021-12-15T15:11:38.327Z\",\"EvictionDate\":\"2021-12-22T15:11:38.327Z\",\"Checksum\":[{\"Algorithm\":\"MD5\",\"Value\":\"82a69f6301551aa457b5d3e9128529fa\",\"ChecksumDate\":\"2021-12-15T15:10:54Z\"}],\"ProductionType\":\"systematic_production\",\"ContentDate\":{\"Start\":\"2000-01-01T00:00:00Z\",\"End\":\"2020-01-19T20:14:27Z\"},\"Footprint\":null},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"936e019d-1000-4805-96dc-7d95f0c1d752\",\"Name\":\"S3A_SR_0_SRA____20040703T142623_20040703T143623_20220105T150101_DDDD_001_010_FFFF_WER_D_NR_NNN.SEN3.zip\",\"ContentType\":\"application/octet-stream\",\"ContentLength\":2499,\"PublicationDate\":\"2022-01-05T15:01:32.059Z\",\"EvictionDate\":\"2022-01-12T15:01:32.059Z\",\"Checksum\":[{\"Algorithm\":\"MD5\",\"Value\":\"846aecc78343f0f6eda6d2aeef31c307\",\"ChecksumDate\":\"2022-01-05T15:01:04Z\"}],\"ProductionType\":\"systematic_production\",\"ContentDate\":{\"Start\":\"2004-07-03T14:26:23Z\",\"End\":\"2004-07-03T14:36:23Z\"},\"Footprint\":{\"type\":\"Polygon\",\"coordinates\":[[[-75.312935,-14.93704],[-75.312935,-11.387472],[-14.312935,-11.387472],[-14.312935,-14.387472],[-75.312935,-14.93704]]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}}]}";
		final JSONObject pripOdataJsonObj = new JSONObject(pripOdataJsonStr);

		final JSONArray pripOdataJsonProducts = pripOdataJsonObj.getJSONArray("value");
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		for (int i = 0; i < pripOdataJsonProducts.length(); i++) {
			final StacItem item = PripToStacMapper.mapFromPripOdataJsonProduct(pripOdataJsonProducts.getJSONObject(i));

			System.out.println("stac item:\n" + objectMapper.writeValueAsString(item));
		}

		assertTrue(true);
	}

	@Test
	public void testPripProductsToStacItemCollectionMapping() throws Exception {
		final String pripOdataJsonStr = "{\"@odata.context\":\"$metadata#Products\",\"value\":[{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"6838e65e-8e1b-45d8-9128-8bf5db364e39\",\"Name\":\"S1A_OPER_AMV_ERRMAT_MPC__20200120T040010_V20000101T000000_20200119T201427.EOF.zip\",\"ContentType\":\"application/octet-stream\",\"ContentLength\":11500,\"PublicationDate\":\"2021-12-15T15:11:38.327Z\",\"EvictionDate\":\"2021-12-22T15:11:38.327Z\",\"Checksum\":[{\"Algorithm\":\"MD5\",\"Value\":\"82a69f6301551aa457b5d3e9128529fa\",\"ChecksumDate\":\"2021-12-15T15:10:54Z\"}],\"ProductionType\":\"systematic_production\",\"ContentDate\":{\"Start\":\"2000-01-01T00:00:00Z\",\"End\":\"2020-01-19T20:14:27Z\"},\"Footprint\":null},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"273e7f8d-f91a-4921-9e16-decbcfc348aa\",\"Name\":\"S3A_TM_0_NAT__G_20040703T140000_20040703T142623_20220105T145826___________________WER_D_NR____.ISIP.zip\",\"ContentType\":\"application/octet-stream\",\"ContentLength\":3025,\"PublicationDate\":\"2022-01-05T14:58:38.569Z\",\"EvictionDate\":\"2022-01-12T14:58:38.569Z\",\"Checksum\":[{\"Algorithm\":\"MD5\",\"Value\":\"fa15cd0bb064e7108826d0a00e746e58\",\"ChecksumDate\":\"2022-01-05T14:58:35Z\"}],\"ProductionType\":\"systematic_production\",\"ContentDate\":{\"Start\":\"2004-07-03T14:00:00Z\",\"End\":\"2004-07-03T14:26:23Z\"},\"Footprint\":null},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"936e019d-1000-4805-96dc-7d95f0c1d752\",\"Name\":\"S3A_SR_0_SRA____20040703T142623_20040703T143623_20220105T150101_DDDD_001_010_FFFF_WER_D_NR_NNN.SEN3.zip\",\"ContentType\":\"application/octet-stream\",\"ContentLength\":2499,\"PublicationDate\":\"2022-01-05T15:01:32.059Z\",\"EvictionDate\":\"2022-01-12T15:01:32.059Z\",\"Checksum\":[{\"Algorithm\":\"MD5\",\"Value\":\"846aecc78343f0f6eda6d2aeef31c307\",\"ChecksumDate\":\"2022-01-05T15:01:04Z\"}],\"ProductionType\":\"systematic_production\",\"ContentDate\":{\"Start\":\"2004-07-03T14:26:23Z\",\"End\":\"2004-07-03T14:36:23Z\"},\"Footprint\":{\"type\":\"Polygon\",\"coordinates\":[[[-75.312935,-14.93704],[-75.312935,-11.387472],[-14.312935,-11.387472],[-14.312935,-14.387472],[-75.312935,-14.93704]]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"35ea8b19-4804-4dae-bbf7-84eac85bbcd7\",\"Name\":\"S3A_OL_0_EFR____20040703T142454_20040703T142654_20220105T150129_DDDD_001_010_FFFF_WER_D_NR_NNN.SEN3.zip\",\"ContentType\":\"application/octet-stream\",\"ContentLength\":2519,\"PublicationDate\":\"2022-01-05T15:01:37.411Z\",\"EvictionDate\":\"2022-01-12T15:01:37.411Z\",\"Checksum\":[{\"Algorithm\":\"MD5\",\"Value\":\"2d0fcb2249bf97aa7f6dd81f502783b6\",\"ChecksumDate\":\"2022-01-05T15:01:31Z\"}],\"ProductionType\":\"systematic_production\",\"ContentDate\":{\"Start\":\"2004-07-03T14:24:54Z\",\"End\":\"2004-07-03T14:26:54Z\"},\"Footprint\":{\"type\":\"Polygon\",\"coordinates\":[[[-75.312935,-14.93704],[-75.312935,-11.387472],[-14.312935,-11.387472],[-14.312935,-14.387472],[-75.312935,-14.93704]]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}}]}";
		final JSONObject pripOdataJsonObj = new JSONObject(pripOdataJsonStr);

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		final StacItemCollection itemCollection = PripToStacMapper.mapFromPripOdataJson(pripOdataJsonObj);
		System.out.println("stac item colletion:\n" + objectMapper.writeValueAsString(itemCollection));

		assertTrue(true);
	}

}
