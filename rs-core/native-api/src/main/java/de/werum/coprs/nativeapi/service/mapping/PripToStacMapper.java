package de.werum.coprs.nativeapi.service.mapping;

import static de.werum.coprs.nativeapi.service.mapping.PripOdataEntityProperties.Attributes;
import static de.werum.coprs.nativeapi.service.mapping.PripOdataEntityProperties.Footprint;
import static de.werum.coprs.nativeapi.service.mapping.PripOdataEntityProperties.Id;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.geojson.Crs;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.geojson.jackson.CrsType;
import org.springframework.http.MediaType;

import de.werum.coprs.nativeapi.rest.model.stac.AdditionalAttributes;
import de.werum.coprs.nativeapi.rest.model.stac.Checksum;
import de.werum.coprs.nativeapi.rest.model.stac.ContentDate;
import de.werum.coprs.nativeapi.rest.model.stac.GeoJsonBase.GeoJsonType;
import de.werum.coprs.nativeapi.rest.model.stac.StacAsset;
import de.werum.coprs.nativeapi.rest.model.stac.StacItem;
import de.werum.coprs.nativeapi.rest.model.stac.StacItemCollection;
import de.werum.coprs.nativeapi.rest.model.stac.StacLink;
import esa.s1pdgs.cpoc.common.utils.ArrayUtil;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.StringUtil;

/**
 * Mapping to STAC item/collection (extended GeoJSON feature/collection)
 *
 * https://github.com/radiantearth/stac-api-spec/blob/master/stac-spec/item-spec/item-spec.md
 *
 */
public class PripToStacMapper {

	static final List<String> ATTRIBUTES_COLLECTIONS = PripOdataEntityProperties.getAttributesCollectionProperties()
			.stream().map(PripOdataEntityProperties::name).collect(Collectors.toList());

	public static StacItemCollection mapFromPripOdataJson(final JsonObject pripOdataJson, final URI externalPripUrl,
			final boolean includeAdditionalAttributes) throws URISyntaxException {
		if (null != pripOdataJson) {
			final JsonArray pripOdataJsonProducts = pripOdataJson.getJsonArray("value");

			final List<StacItem> items = new ArrayList<>();
			for (int i = 0; i < pripOdataJsonProducts.size(); i++) {
				items.add(mapFromPripOdataJsonProduct(pripOdataJsonProducts.getJsonObject(i), externalPripUrl,
						includeAdditionalAttributes));
			}

			final StacItemCollection itemCollection = new StacItemCollection();
			itemCollection.setFeatures(items);

			return itemCollection;
		}

		return null;
	}

	public static StacItem mapFromPripOdataJsonProduct(final JsonObject pripOdataJsonProduct, final URI externalPripUrl,
			final boolean includeAdditionalAttributes) throws URISyntaxException {
		final StacItem stacItem = new StacItem();

		// ID
		final String productId = pripOdataJsonProduct.getString(Id.name());
		stacItem.setId(productId);

		// geometry and bbox
		JsonObject footprint = null;
		if (!pripOdataJsonProduct.isNull(Footprint.name())) {
			footprint = pripOdataJsonProduct.getJsonObject(Footprint.name());
			stacItem.setGeometry(asGeoJson(footprint));
		}

		// if this item has a geometry a bbox is required
		if (null != stacItem.getGeometry()) {
			stacItem.setBbox(getBoundingBox(stacItem.getGeometry()));
		}

		// properties
		// at least 'datetime' or, if a single point in time is not appropriate,
		// 'start_datetime' and 'end_datetime' required
		final JsonObject contentDateJson = pripOdataJsonProduct
				.getJsonObject(PripOdataEntityProperties.ContentDate.name());
		stacItem.setProperty("datetime", null);
		final String contentDateStartStr = contentDateJson.getString(PripOdataEntityProperties.Start.name());
		stacItem.setProperty("start_datetime", contentDateStartStr);
		final String contentDateEndStr = contentDateJson.getString(PripOdataEntityProperties.End.name());
		stacItem.setProperty("end_datetime", contentDateEndStr);

		final List<String> dontInclude = new LinkedList<>();
		dontInclude.addAll(Arrays.asList(Id.name(), Footprint.name())); // already included one level above as 'id' and
																		// 'geometry'
		if (!includeAdditionalAttributes) {
			dontInclude.addAll(ATTRIBUTES_COLLECTIONS);
		}

		for (final String propertyKey : pripOdataJsonProduct.keySet()) {
			if (!propertyKey.startsWith("@odata") && !dontInclude.contains(propertyKey)) {

				final PripOdataEntityProperties attribute;
				try {
					attribute = PripOdataEntityProperties.valueOf(propertyKey);
				} catch (final Exception e) {
					continue; // ignore unknown attributes
				}

				switch (attribute) {
				case Id:
				case Name:
				case Online:
				case ContentType:
				case ContentLength:
				case PublicationDate:
				case EvictionDate:
				case ProductionType:
					stacItem.setProperty(attribute.name(), pripOdataJsonProduct.get(propertyKey));
					continue;
				case Attributes:
					continue; // it's an empty array
				case StringAttributes:
				case IntegerAttributes:
				case DoubleAttributes:
				case DateTimeOffsetAttributes:
				case BooleanAttributes:
					addAdditionalAttributes(stacItem, attribute, pripOdataJsonProduct.getJsonArray(propertyKey));
					continue;
				case Footprint:
					if (footprint != null) {
						setFootprint(stacItem, footprint);
					}
					continue;
				case ContentDate:
					setContentDate(stacItem, contentDateStartStr, contentDateEndStr);
					continue;
				case Checksum:
					setChecksum(stacItem, pripOdataJsonProduct.getJsonArray(propertyKey));
					continue;
				default:
					continue;
				}
			}
		}

		Map<String, StacAsset> assets = new HashMap<>();
		
		JsonArray quicklooks = pripOdataJsonProduct.getJsonArray("Quicklooks");		
		if (quicklooks != null) {
			if (quicklooks.size() > 0) {
				// Currently just a single item is supported
				JsonObject obj = quicklooks.getJsonObject(0);
				String quicklookId = obj.getString("Image");
			
				assets.put("quicklook", createQuicklookAsset(externalPripUrl, productId, quicklookId));
			}

		}

		final String filename = pripOdataJsonProduct.getString(PripOdataEntityProperties.Name.name(), null);
		// links (product metadata link) and assets (product download link)
		if (null != externalPripUrl) {
			stacItem.setLinks(Collections.singletonList(createSelfLink(externalPripUrl, productId, filename)));
			assets.put("product", createDownloadAsset(externalPripUrl, productId, filename));
		}

		stacItem.setAssets(assets);

		return stacItem;
	}

	private static void setFootprint(final StacItem item, final JsonObject footprintOdataJson) {
		if (null != footprintOdataJson) {
			item.setProperty(PripOdataEntityProperties.Footprint.name(), asGeoJson(footprintOdataJson));
		} else {
			item.setProperty(PripOdataEntityProperties.Footprint.name(), null);
		}
	}

	private static void setContentDate(final StacItem item, final String contentDateStartStr,
			final String contentDateEndStr) {
		if (null != contentDateStartStr || null != contentDateEndStr) {
			final ContentDate contentDate = new ContentDate();
			contentDate.setStart(contentDateStartStr);
			contentDate.setEnd(contentDateEndStr);
			item.setProperty(PripOdataEntityProperties.ContentDate.name(), contentDate);
		} else {
			item.setProperty(PripOdataEntityProperties.ContentDate.name(), null);
		}
	}

	private static void setChecksum(final StacItem item, final JsonArray checksumOdataJsonArray) {
		if (null != checksumOdataJsonArray && checksumOdataJsonArray.size() > 0) {
			final List<Checksum> checksums = new ArrayList<>();

			for (int i = 0; i < checksumOdataJsonArray.size(); i++) {
				final JsonObject checksumJson = checksumOdataJsonArray.getJsonObject(i);

				final Checksum checksum = new Checksum();
				checksum.setAlgorithm(checksumJson.getString(PripOdataEntityProperties.Algorithm.name()));
				checksum.setValue(checksumJson.getString(PripOdataEntityProperties.Value.name()));
				checksum.setDate(checksumJson.getString(PripOdataEntityProperties.ChecksumDate.name()));

				checksums.add(checksum);
			}

			item.setProperty(PripOdataEntityProperties.Checksum.name(), checksums.stream().toArray(Checksum[]::new));
		}
	}

	private static void addAdditionalAttributes(final StacItem item, final PripOdataEntityProperties attribute,
			final JsonArray attributesOdataJsonArray) {
		if (Attributes != attribute && null != attributesOdataJsonArray && attributesOdataJsonArray.size() > 0) {

			AdditionalAttributes additionalAttributes = item.getProperty(AdditionalAttributes.class.getSimpleName());
			if (null == additionalAttributes) {
				additionalAttributes = new AdditionalAttributes();
				item.setProperty(AdditionalAttributes.class.getSimpleName(), additionalAttributes);
			}

			for (int i = 0; i < attributesOdataJsonArray.size(); i++) {
				final JsonObject attributeJson = attributesOdataJsonArray.getJsonObject(i);

				if (attributeJson.get("Value") != JsonValue.NULL) {
					switch (attribute) {
					case StringAttributes:
						additionalAttributes.addStringAttribute(attributeJson.getString("Name"),
								attributeJson.getString("Value"));
						continue;
					case IntegerAttributes:
						additionalAttributes.addIntegerAttribute(attributeJson.getString("Name"),
								attributeJson.getJsonNumber("Value").longValue());
						continue;
					case DoubleAttributes:
						additionalAttributes.addDoubleAttribute(attributeJson.getString("Name"),
								attributeJson.getJsonNumber("Value").doubleValue());
						continue;
					case DateTimeOffsetAttributes:
						additionalAttributes.addDateAttribute(attributeJson.getString("Name"),
								attributeJson.getString("Value"));
						continue;
					case BooleanAttributes:
						additionalAttributes.addBooleanAttribute(attributeJson.getString("Name"),
								attributeJson.getBoolean("Value"));
						continue;
					default:
						continue;
					}
				}
			}
		}
	}

	private static StacLink createSelfLink(final URI externalPripUrl, final String productId, final String filename)
			throws URISyntaxException {
		final StacLink link = new StacLink();

		final URI pripProductMetadataUrl = Objects
				.requireNonNull(externalPripUrl, "cannot create metadata link without external PRIP URL")
				.resolve("Products(" + Objects.requireNonNull(productId, "product ID needed to create metadata link")
						+ ")?$format=JSON");
		link.setHref(pripProductMetadataUrl.toString());
		link.setType(MediaType.APPLICATION_JSON_VALUE);
		link.setRel("self");

		if (StringUtil.isNotBlank(filename)) {
			link.setTitle("metadata for product " + filename);
		} else {
			link.setTitle("product metadata");
		}

		return link;
	}

	private static StacAsset createDownloadAsset(final URI externalPripUrl, final String productId,
			final String filename) throws URISyntaxException {
		final StacAsset asset = new StacAsset();

		final URI pripDownloadUrl = Objects
				.requireNonNull(externalPripUrl, "cannot create download asset without external PRIP URL")
				.resolve("Products(" + Objects.requireNonNull(productId, "product ID needed to create download link")
						+ ")/$value");
		asset.setHref(pripDownloadUrl.toString());
		asset.setTitle(filename);

		if (null != filename && filename.toUpperCase().endsWith(".ZIP")) {
			asset.setType("application/zip");
		}
		
		if (null != filename && filename.toUpperCase().endsWith(".TAR")) {
			asset.setType("application/x-tar");
		}

		asset.setDescription("download link for product data");
		asset.setRoles(Collections.singletonList("data"));

		return asset;
	}

	private static StacAsset createQuicklookAsset(final URI externalPripUrl, final String productId, final String quicklookId) throws URISyntaxException {
		final StacAsset asset = new StacAsset();

		final URI pripDownloadUrl = Objects
				.requireNonNull(externalPripUrl, "cannot create quicklook asset without external PRIP URL")
				.resolve("Products(" + Objects.requireNonNull(productId, "product ID needed to create download link")
						+ ")/Quicklooks('"
						+ Objects.requireNonNull(quicklookId, "quicklook Id needed to create download link")
						+ "')/$value");
		asset.setHref(pripDownloadUrl.toString());
		asset.setTitle(quicklookId);

		if (null != quicklookId && quicklookId.toUpperCase().endsWith(".PNG")) {
			asset.setType("image/png");
		}

		asset.setDescription("download link for quicklook data");
		asset.setRoles(Collections.singletonList("data"));

		return asset;
	}

	private static GeoJsonObject asGeoJson(final JsonObject pripOdataJsonFootprint) {
		if (null != pripOdataJsonFootprint) {
			final String type = pripOdataJsonFootprint.getString("type");

			if (GeoJsonType.Polygon.name().equalsIgnoreCase(type)) {
				final JsonArray coordinates = pripOdataJsonFootprint.getJsonArray("coordinates");
				final List<LngLatAlt> polygonCoordinates = getCoordinatesFromArrayOfArrays(coordinates);

				if (!polygonCoordinates.isEmpty()) {
					final Polygon polygon = new Polygon(polygonCoordinates);

					final Optional<Crs> oCrs = extractCrs(pripOdataJsonFootprint);
					if (oCrs.isPresent()) {
						polygon.setCrs(oCrs.get());
					}
					return polygon;
				}
			} else if (GeoJsonType.LineString.name().equalsIgnoreCase(type)) {
				final JsonArray coordinates = pripOdataJsonFootprint.getJsonArray("coordinates");
				final LngLatAlt[] coordinatesArray = getCoordinatesFromArray(coordinates).stream()
						.toArray(LngLatAlt[]::new);

				if (ArrayUtil.isNotEmpty(coordinatesArray)) {
					final LineString lineString = new LineString(coordinatesArray);

					final Optional<Crs> oCrs = extractCrs(pripOdataJsonFootprint);
					if (oCrs.isPresent()) {
						lineString.setCrs(oCrs.get());
					}
					return lineString;
				}
			} else if (GeoJsonType.Point.name().equalsIgnoreCase(type)) {
				final JsonArray pointArray = pripOdataJsonFootprint.getJsonArray("coordinates");
				final LngLatAlt pointCoordinates = createCoordinate(pointArray);

				if (null != pointCoordinates) {
					final Point point = new Point(pointCoordinates);

					final Optional<Crs> oCrs = extractCrs(pripOdataJsonFootprint);
					if (oCrs.isPresent()) {
						point.setCrs(oCrs.get());
					}
					return point;
				}
			} else {
				throw new IllegalArgumentException(String.format("GeoJSON mapping not implemented for type: %s", type));
			}
		}

		return null;
	}

	private static Optional<Crs> extractCrs(final JsonObject pripOdataJsonFootprint) {
		final JsonObject crsObject = pripOdataJsonFootprint.getJsonObject("crs");

		if (null != crsObject) {
			final Crs crs = new Crs();
			crs.setType(CrsType.valueOf(crsObject.getString("type")));

			final JsonObject CrsProperties = crsObject.getJsonObject("properties");
			final Map<String, Object> crsPropertiesMap = new HashMap<>();

			CollectionUtil.nullToEmpty(CrsProperties.keySet())
					.forEach(crsPropKey -> crsPropertiesMap.put(crsPropKey, CrsProperties.get(crsPropKey)));

			if (!crsPropertiesMap.isEmpty()) {
				crs.setProperties(crsPropertiesMap);
			}

			return Optional.of(crs);
		}

		return Optional.empty();
	}

	private static List<LngLatAlt> getCoordinatesFromArrayOfArrays(final JsonArray coordinatesArrayOfArray) {
		final List<LngLatAlt> coordinates = new ArrayList<>();

		if (null != coordinatesArrayOfArray && coordinatesArrayOfArray.size() > 0) {
			for (int i = 0; i < coordinatesArrayOfArray.size(); i++) {
				final JsonArray coordinatesArray = coordinatesArrayOfArray.getJsonArray(i);

				coordinates.addAll(getCoordinatesFromArray(coordinatesArray));
			}
		}

		return coordinates;
	}

	private static List<LngLatAlt> getCoordinatesFromArray(final JsonArray coordinatesArray) {
		final List<LngLatAlt> coordinates = new ArrayList<>();

		if (null != coordinatesArray && coordinatesArray.size() > 0) {
			for (int j = 0; j < coordinatesArray.size(); j++) {
				final JsonArray point = coordinatesArray.getJsonArray(j);
				coordinates.add(createCoordinate(point));
			}
		}

		return coordinates;
	}

	private static LngLatAlt createCoordinate(final JsonArray point) {
		final double longitude = point.getJsonNumber(0).doubleValue();
		final double latitude = point.getJsonNumber(1).doubleValue();

		return new LngLatAlt(longitude, latitude);
	}

	private static double[] getBoundingBox(final GeoJsonObject geometry) {
		List<LngLatAlt> coordinates = null;

		if (geometry instanceof Polygon) {
			coordinates = CollectionUtil.nullToEmptyList(((Polygon) geometry).getCoordinates()).stream()
					.flatMap(List::stream).collect(Collectors.toList());

		} else if (geometry instanceof LineString) {
			coordinates = CollectionUtil.nullToEmptyList(((LineString) geometry).getCoordinates());

		} else if (geometry instanceof Point) {
			final LngLatAlt point = ((Point) geometry).getCoordinates();
			if (null != point) {
				coordinates = Collections.singletonList(point);
			}
		} else {
			throw new IllegalArgumentException(
					String.format("minimal bounding rectangle (bbox) algorithm not implemented for type: %s",
							geometry.getClass().getSimpleName()));
		}

		return getBoundingBox(coordinates);
	}

	private static double[] getBoundingBox(final List<LngLatAlt> coordinates) {
		if (CollectionUtil.isNotEmpty(coordinates)) {
			final List<Double> allXcoords = coordinates.stream().map(lngLat -> lngLat.getLongitude())
					.collect(Collectors.toList());
			final List<Double> allYcoords = coordinates.stream().map(lngLat -> lngLat.getLatitude())
					.collect(Collectors.toList());

			final double minX = Collections.min(allXcoords);
			final double minY = Collections.min(allYcoords);
			final double maxX = Collections.max(allXcoords);
			final double maxY = Collections.max(allYcoords);

			// TODO: handle cases which cross the Antimeridian
			// (https://datatracker.ietf.org/doc/html/rfc7946#section-5.2)
			// TODO: handle cases which contains a pole
			// (https://datatracker.ietf.org/doc/html/rfc7946#section-5.3)

			return new double[] { minX, minY, maxX, maxY };
		}

		return null;
	}

	// --------------------------------------------------------------------------

	private PripToStacMapper() {
	}

}
