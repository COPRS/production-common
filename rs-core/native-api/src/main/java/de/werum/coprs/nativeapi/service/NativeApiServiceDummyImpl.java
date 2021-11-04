package de.werum.coprs.nativeapi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.werum.coprs.nativeapi.config.NativeApiProperties;
import de.werum.coprs.nativeapi.config.NativeApiProperties.AttributesOfMission;
import de.werum.coprs.nativeapi.config.NativeApiProperties.AttributesOfProductType;
import de.werum.coprs.nativeapi.rest.model.Checksum;
import de.werum.coprs.nativeapi.rest.model.ContentDate;
import de.werum.coprs.nativeapi.rest.model.PripMetadataResponse;
import de.werum.coprs.nativeapi.service.helper.DownloadUrl;

@Service
public class NativeApiServiceDummyImpl implements NativeApiService {

	private static final Logger LOG = LogManager.getLogger(NativeApiServiceDummyImpl.class);

	private static final PripMetadataResponse PRIP_METADATA_DUMMY;
	private static final byte[] EMPTY_ZIP = { 80, 75, 05, 06, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00 };

	static {
		final PripMetadataResponse dummy = new PripMetadataResponse();
		dummy.setId("96992625-0572-419d-8b0b-65d646a8d753");
		dummy.setName("S1B_EW_OCN__2SDH_20200120T164007_20200120T164107_019903_025A63_C2EF.SAFE.zip");
		dummy.setContentType("application/octet-stream");
		dummy.setContentLength(18354715);
		dummy.setPublicationDate("2021-10-12T07:41:09.986Z");
		dummy.setEvictionDate("2021-10-19T07:41:09.986Z");
		dummy.setProductionType("systematic_production");

		final Checksum checksum = new Checksum();
		checksum.setAlgorithm("MD5");
		checksum.setDate("2021-10-12T07:41:08.000Z");
		checksum.setValue("1a5a3f370d289c4c224a0caed7bc8724");
		dummy.setChecksum(Collections.singletonList(checksum));

		final ContentDate contentDate = new ContentDate();
		contentDate.setStart("2020-01-20T16:40:07.723Z");
		contentDate.setEnd("2020-01-20T16:41:07.717Z");
		dummy.setContentDate(contentDate);

		final List<LngLatAlt> coordinates = new ArrayList<>();
		coordinates.add(new LngLatAlt(-127.800659, 78.575714));
		coordinates.add(new LngLatAlt(-135.942673, 75.412086));
		coordinates.add(new LngLatAlt(-150.582657, 76.651756));
		coordinates.add(new LngLatAlt(-146.265869, 80.148819));
		coordinates.add(new LngLatAlt(-127.800659, 78.575714));
		dummy.setFootprint(new Polygon(coordinates));

		final Map<String, Object> attributes = new HashMap<>();
		attributes.put("attr_orbitNumber_long", 19903);
		attributes.put("attr_productClass_string", "S");
		attributes.put("attr_relativeOrbitNumber_long", 102);
		attributes.put("attr_cycleNumber_long", 120);
		attributes.put("attr_productComposition_string", "Individual");
		attributes.put("attr_orbitDirection_string", "ASCENDING");
		attributes.put("attr_processingDate_date", "2021-10-12T03:29:04.254");
		attributes.put("attr_instrumentConfigurationID_string", "1");
		attributes.put("attr_instrumentShortName_string", "SAR");
		attributes.put("attr_beginningDateTime_date", "2020-01-20T16:40:07.723");
		attributes.put("attr_platformShortName_string", "SENTINEL-1");
		attributes.put("attr_processorVersion_string", "3.4");
		attributes.put("attr_productType_string", "EW_OCN__2S");
		attributes.put("attr_coordinates_string", "75.412086,-135.942673 76.651756,-150.582657 80.148819,-146.265869 78.575714,-127.800659 75.412086,-135.942673");
		attributes.put("attr_polarisationChannels_string", "HH,HV");
		attributes.put("attr_processorName_string", "Sentinel-1 IPF");
		attributes.put("attr_missionDatatakeID_long", 154211);
		attributes.put("attr_timeliness_string", "Fast-24h");
		attributes.put("attr_platformSerialIdentifier_string", "B");
		attributes.put("attr_completionTimeFromAscendingNode_double", 1698981.0);
		attributes.put("attr_endingDateTime_date", "2020-01-20T16:41:07.717");
		attributes.put("attr_processingCenter_string", "Airbus Defence and Space-Toulouse");
		attributes.put("attr_sliceProductFlag_boolean", false);
		attributes.put("attr_startTimeFromAscendingNode_double", 1638987.0);
		dummy.setAttributes(attributes);

		final Map<String, String> links = new HashMap<>();
		links.put("download", "/missions/s1/products/96992625-0572-419d-8b0b-65d646a8d753/download");
		dummy.setLinks(links);

		PRIP_METADATA_DUMMY = dummy;
	}

	private final NativeApiProperties apiProperties;

	private final Map<String, List<String>> missionToBaseAttributes = new HashMap<>();
	private final Map<String, Map<String, Map<String, String>>> missionToTypeToAttributes = new HashMap<>();

	//	private final PripMetadataRepository pripRepo;

	@Autowired
	public NativeApiServiceDummyImpl(final NativeApiProperties apiProperties) {
		this.apiProperties = apiProperties;

		if (apiProperties.getAttributesOfMission() != null) {
			this.initAttributes(apiProperties.getAttributesOfMission());
		}
	}

	private void initAttributes(final List<AttributesOfMission> allAttributes) {
		for (final AttributesOfMission attributesOfMission : allAttributes) {
			final String missionName = attributesOfMission.getMissionName();

			if (null == missionName || missionName.trim().isEmpty()) {
				LOG.error("invalid attributes configuration: missing mission name. skipping this particular attributes configuration subtree.");
				continue;
			}

			if (!this.missionToBaseAttributes.containsKey(missionName)) {
				this.missionToBaseAttributes.put(missionName, new LinkedList<>());
			}
			if (!this.missionToTypeToAttributes.containsKey(missionName)) {
				this.missionToTypeToAttributes.put(missionName, new HashMap<>());
			}

			final List<String> baseAttributesOfMission = attributesOfMission.getBaseAttributes();
			if (null != baseAttributesOfMission) {
				this.missionToBaseAttributes.get(missionName).addAll(baseAttributesOfMission);
			}

			final Map<String, Map<String, String>> attributesOfMissionMap = this.missionToTypeToAttributes.get(missionName);
			final List<AttributesOfProductType> attributesOfProductType = attributesOfMission.getAttributesOfProductType();

			if (null != attributesOfProductType) {
				for (final AttributesOfProductType attrsOfType : attributesOfProductType) {
					final String productType = attrsOfType.getProductType();

					if (null == productType || productType.trim().isEmpty()) {
						LOG.error("invalid attributes configuration: missing product type. skipping this particular attributes configuration subtree.");
						continue;
					}

					if (!attributesOfMissionMap.containsKey(productType)) {
						attributesOfMissionMap.put(productType, new HashMap<>());
					}

					final Map<String, String> attributesMap = attributesOfMissionMap.get(productType);
					final Map<String, String> attributes = attrsOfType.getAttributes();

					if (null != attributes) {
						attributesMap.putAll(attributes);
					}
				}
			}
		}
	}

	@Override
	public String getNativeApiVersion() {
		return this.apiProperties.getVersion();
	}

	@Override
	public List<String> getMissions() {
		final Set<String> missions = this.missionToTypeToAttributes.keySet();

		if (null == missions || missions.isEmpty()) {
			return Collections.emptyList();
		}

		return new ArrayList<>(missions);
	}

	@Override
	public List<String> getProductTypes(final String missionName) {
		if (null != missionName && !missionName.isEmpty() && this.missionToTypeToAttributes.containsKey(missionName)) {
			final Map<String, Map<String, String>> productTypesToAttributes = this.missionToTypeToAttributes.get(missionName);

			if (null != productTypesToAttributes && !productTypesToAttributes.isEmpty()) {
				return new ArrayList<>(productTypesToAttributes.keySet());
			}
		}

		return Collections.emptyList();
	}

	@Override
	public List<String> getAttributes(final String missionName, final String productType) {
		final List<String> attributeNames = new LinkedList<>();

		if (null != missionName && !missionName.isEmpty()) {
			if (this.missionToBaseAttributes.containsKey(missionName)) {
				attributeNames.addAll(this.missionToBaseAttributes.get(missionName));
			}

			if (null != productType && !productType.isEmpty() && this.missionToTypeToAttributes.containsKey(missionName)) {
				final Map<String, Map<String, String>> productTypesToAttributes = this.missionToTypeToAttributes.get(missionName);

				if (null != productTypesToAttributes && !productTypesToAttributes.isEmpty()	&& productTypesToAttributes.containsKey(productType)) {
					final Map<String, String> attributes = productTypesToAttributes.get(productType);

					if (null != attributes && !attributes.isEmpty()) {
						attributeNames.addAll(attributes.keySet());
					}
				}
			}
		}

		return attributeNames;
	}

	@Override
	public PripMetadataResponse findProduct(final String missionName, final String productId) {
		return PRIP_METADATA_DUMMY;
	}

	@Override
	public List<PripMetadataResponse> findWithFilters(final String missionName, final String productType, final String filterStr) {
		return Collections.singletonList(PRIP_METADATA_DUMMY);
	}

	@Override
	public DownloadUrl provideTemporaryProductDonwload(String missionName, String productId) {
		return null;
	}

}
