package de.werum.csgrs.nativeapi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.werum.csgrs.nativeapi.config.NativeApiProperties;
import de.werum.csgrs.nativeapi.config.NativeApiProperties.AttributesOfMission;
import de.werum.csgrs.nativeapi.config.NativeApiProperties.AttributesOfProductType;
import de.werum.csgrs.nativeapi.rest.model.PripMetadataResponse;
import de.werum.csgrs.nativeapi.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm.PripSortOrder;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;

@Service
public class NativeApiServiceImpl implements NativeApiService {

	private static final Logger LOG = LogManager.getLogger(NativeApiServiceImpl.class);

	private final NativeApiProperties apiProperties;

	private final Map<String, List<String>> missionToBaseAttributes = new HashMap<>();
	private final Map<String, Map<String, Map<String, String>>> missionToTypeToAttributes = new HashMap<>();

	private final PripMetadataRepository pripRepo;

	@Autowired
	public NativeApiServiceImpl(final NativeApiProperties apiProperties, final PripMetadataRepository pripMetadataRepository) {

		this.pripRepo = pripMetadataRepository;
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

		if (StringUtil.isNotBlank(missionName)) {
			if (this.missionToBaseAttributes.containsKey(missionName)) {
				attributeNames.addAll(this.missionToBaseAttributes.get(missionName));
			}

			if (StringUtil.isNotBlank(productType) && this.missionToTypeToAttributes.containsKey(missionName)) {
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
	public List<PripMetadataResponse> findAll(final String missionName, final String productType) {
		if ("S1".equalsIgnoreCase(missionName) && null != productType && !productType.isEmpty()) {
			final PripQueryFilterList filters = PripQueryFilterList.matchAll( //
					new PripTextFilter(PripMetadata.FIELD_NAMES.PRODUCT_FAMILY.fieldName(), PripTextFilter.Function.EQUALS, productType));

			final PripSortTerm pripSortTerm = new PripSortTerm(PripMetadata.FIELD_NAMES.ID, PripSortOrder.ASCENDING);
			final List<PripMetadata> result = this.pripRepo.findWithFilter(filters, Optional.empty(), Optional.empty(),
					Collections.singletonList(pripSortTerm));

			return MappingUtil.pripMetadataToResponse(result);
		}

		return Collections.emptyList();
	}

}
