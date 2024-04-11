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

package de.werum.coprs.nativeapi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import de.werum.coprs.nativeapi.config.NativeApiProperties;
import de.werum.coprs.nativeapi.config.NativeApiProperties.AttributesOfMission;
import de.werum.coprs.nativeapi.config.NativeApiProperties.AttributesOfProductType;
import de.werum.coprs.nativeapi.rest.model.PripMetadataResponse;
import de.werum.coprs.nativeapi.service.exception.NativeApiBadRequestException;
import de.werum.coprs.nativeapi.service.exception.NativeApiException;
import de.werum.coprs.nativeapi.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm.PripSortOrder;
import esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDoubleFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripIntegerFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterTerm;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter.RelationalOperator;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter.Function;

@Service
public class NativeApiServiceImpl implements NativeApiService {

	private static final Logger LOG = LogManager.getLogger(NativeApiServiceImpl.class);

	private static final List<String> SUPPORTED_ATTRIBUTE_TYPES = new LinkedList<>();

	static {
		SUPPORTED_ATTRIBUTE_TYPES.add("date");
		SUPPORTED_ATTRIBUTE_TYPES.add("string");
		SUPPORTED_ATTRIBUTE_TYPES.add("long");
		SUPPORTED_ATTRIBUTE_TYPES.add("double");
		SUPPORTED_ATTRIBUTE_TYPES.add("boolean");
	}

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
	public PripMetadataResponse findProduct(final String missionName, final String productId) {
		if ("S1".equalsIgnoreCase(missionName)) {
			final PripMetadata productMetadata = this.pripRepo.findById(productId);
			if (null == productMetadata) {
				throw new NativeApiException(String.format("product with ID '%s' not found for mission %s: ", productId, missionName), HttpStatus.NOT_FOUND);
			}
			return MappingUtil.pripMetadataToResponse(productMetadata, missionName);
		} else {
			throw new NativeApiException(String.format("mission not supported (yet): %s", missionName), HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public List<PripMetadataResponse> findWithFilters(final String missionName, final String productType, final String filterStr) {
		if ("S1".equalsIgnoreCase(missionName)) {
			final List<PripQueryFilter> filterTerms = new LinkedList<>();

			if (StringUtil.isNotBlank(productType)) {
				filterTerms.add(new PripTextFilter(PripMetadata.FIELD_NAMES.PRODUCT_FAMILY.fieldName(), PripTextFilter.Function.EQUALS, productType));
			}
			filterTerms.addAll(this.parseFilters(missionName, productType, filterStr));

			final PripQueryFilterList filters = PripQueryFilterList.matchAll(filterTerms);
			LOG.debug("filter string --> {} <-- parsed to --> {} <--", filterStr, filters);
			final PripSortTerm pripSortTerm = new PripSortTerm(PripMetadata.FIELD_NAMES.ID, PripSortOrder.ASCENDING); // TODO: default sort by publication date
			final List<PripMetadata> result = this.pripRepo.findWithFilter(filters, Optional.empty(), Optional.empty(),
					Collections.singletonList(pripSortTerm));

			return MappingUtil.pripMetadataToResponse(result, missionName);
		} else {
			throw new NativeApiException(String.format("mission not supported (yet): %s", missionName), HttpStatus.NOT_FOUND);
		}
	}

	private List<PripQueryFilter> parseFilters(final String missionName, final String productType, final String filterStr) {
		final List<PripQueryFilter> filters = new LinkedList<>();

		if (StringUtil.isNotBlank(filterStr)) {
			final List<String> attributes = this.getAttributes(missionName, productType);
			final String[] filterTermStrings = filterStr.split(" AND ");

			for (final String filterTermStr : filterTermStrings) {
				final List<String> singleFilterTermParts = this.deconstructSingleFilterTerm(filterTermStr);

				if (null == singleFilterTermParts || singleFilterTermParts.size() != 3) {
					throw new NativeApiBadRequestException(
							"the filter term must consist of the three parts attribute name, comparator and value: " + filterTermStr);
				}

				String attributeName = singleFilterTermParts.get(0);
				if (!attributes.contains(attributeName)) {
					throw new NativeApiBadRequestException("unknown filter attribute: " + attributeName);
				}
				// TODO: do some proper base attribute mapping via application.yaml (map instead of list)
				if ("publicationDate".equals(attributeName)) {
					attributeName = "creationDate";
				}

				final String type = this.getType(attributeName);
				final String comparator = singleFilterTermParts.get(1);
				final String value = singleFilterTermParts.get(2);

				filters.add(this.createFilter(attributeName, type, comparator, value));
			}
		}

		return filters;
	}

	private List<String> deconstructSingleFilterTerm(final String singleFilterTermStr) {
		final List<String> filterTermParts = new LinkedList<String>();
		final String trimmedFilterTermStr = StringUtil.trimToNull(singleFilterTermStr);

		if (null != trimmedFilterTermStr) {
			final Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
			final Matcher matcher = regex.matcher(trimmedFilterTermStr);

			while (matcher.find()) {
				if (null != matcher.group(1)) {
					// double-quoted
					filterTermParts.add(matcher.group(1));
				} else if (null != matcher.group(2)) {
					// single-quoted
					filterTermParts.add(matcher.group(2));
				} else {
					// unquoted
					filterTermParts.add(matcher.group());
				}
			}
		}

		return filterTermParts;
	}

	private String getType(final String attrName) {
		final String type;
		if (attrName.startsWith("attr_")) {
			// custom attribute of format: attr_[attributenName]_[valueType]
			final String extracted = attrName.substring(attrName.lastIndexOf("_"));

			if (SUPPORTED_ATTRIBUTE_TYPES.contains(extracted)) {
				type = extracted;
			} else {
				throw new NativeApiBadRequestException("attribute type not supported: " + extracted);
			}
		} else {
			switch (attrName) {
			case "name":
			case "productionType":
				type = "string";
				break;
			case "contentLength":
				type = "long";
				break;
			case "creationDate":
			case "evictionDate":
			case "contentDate.start":
			case "contentDate.end":
				type = "date";
				break;
			default:
				throw new NativeApiBadRequestException("attribute name not supported: " + attrName);
			}
		}

		return type;
	}

	private PripQueryFilterTerm createFilter(final String attrName, final String attrType, final String comparatorStr, final String valueStr) {
		switch (attrType) {
		case "date":
			return this.createDateTimeFilter(attrName, comparatorStr, valueStr);
		case "string":
			return this.createTextFilter(attrName, comparatorStr, valueStr);
		case "long":
			return this.createIntegerFilter(attrName, comparatorStr, valueStr);
		case "double":
			return this.createDoubleFilter(attrName, comparatorStr, valueStr);
		case "boolean":
			return this.createBooleanFilter(attrName, comparatorStr, valueStr);
		default:
			throw new NativeApiBadRequestException("filter does not support attribute type: " + attrType);
		}
	}

	private LocalDateTime convertDateTime(final String dateTimeAsString) {
		return (null != dateTimeAsString) ? DateUtils.parse(dateTimeAsString) : null;
	}

	private PripDateTimeFilter createDateTimeFilter(final String attrName, final String comparatorStr, final String value) {
		final RelationalOperator op;
		try {
			op = RelationalOperator.fromString(comparatorStr);
		} catch (final Exception e) {
			throw new NativeApiBadRequestException("error parsing comparison operator: " + e.getMessage(), e);
		}

		final LocalDateTime date;
		try {
			date = this.convertDateTime(value);
		} catch (final Exception e) {
			throw new NativeApiBadRequestException("error parsing date time value: " + e.getMessage(), e);
		}

		return new PripDateTimeFilter(attrName, op, date);
	}

	private PripTextFilter createTextFilter(final String attrName, final String comparatorStr, final String value) {
		final Function op;
		try {
			op = Function.fromString(comparatorStr);
		} catch (final Exception e) {
			throw new NativeApiBadRequestException("error parsing comparison operator: " + e.getMessage(), e);
		}

		return new PripTextFilter(attrName, op, value);
	}

	private PripIntegerFilter createIntegerFilter(final String attrName, final String comparatorStr, final String value) {
		final RelationalOperator op;
		try {
			op = RelationalOperator.fromString(comparatorStr);
		} catch (final Exception e) {
			throw new NativeApiBadRequestException("error parsing comparison operator: " + e.getMessage(), e);
		}

		final Long number;
		try {
			number = Long.parseLong(value);
		} catch (final Exception e) {
			throw new NativeApiBadRequestException("error parsing integer value: " + e.getMessage(), e);
		}

		return new PripIntegerFilter(attrName, op, number);
	}

	private PripDoubleFilter createDoubleFilter(final String attrName, final String comparatorStr, final String value) {
		final RelationalOperator op;
		try {
			op = RelationalOperator.fromString(comparatorStr);
		} catch (final Exception e) {
			throw new NativeApiBadRequestException("error parsing comparison operator: " + e.getMessage(), e);
		}

		final Double number;
		try {
			number = Double.parseDouble(value);
		} catch (final Exception e) {
			throw new NativeApiBadRequestException("error parsing floating point number: " + e.getMessage(), e);
		}

		return new PripDoubleFilter(attrName, op, number);
	}

	private PripBooleanFilter createBooleanFilter(final String attrName, final String comparatorStr, final String value) {
		final esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter.Function op;
		try {
			op = esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter.Function.fromString(comparatorStr);
		} catch (final Exception e) {
			throw new NativeApiBadRequestException("error parsing comparison operator: " + e.getMessage(), e);
		}

		final Boolean bool;
		try {
			bool = Boolean.parseBoolean(value);
		} catch (final Exception e) {
			throw new NativeApiBadRequestException("error parsing boolean value: " + e.getMessage(), e);
		}

		return new PripBooleanFilter(attrName, op, bool);
	}

	@Override
	public byte[] downloadProduct(final String missionName, final String productId) {
		if ("s1".equalsIgnoreCase(missionName)) {
			final PripMetadata productMetadata = this.pripRepo.findById(productId);
			if (null == productMetadata) {
				throw new NativeApiException(String.format("product with ID '%s' not found for mission %s: ", productId, missionName), HttpStatus.NOT_FOUND);
			}

			if (StringUtil.isNotBlank(this.apiProperties.getDummyDownloadFile())) {
				final Path productDummyFile = Paths.get(this.apiProperties.getDummyDownloadFile());

				if (!Files.isReadable(productDummyFile)||!Files.isRegularFile(productDummyFile)) {
					throw new NativeApiException(String.format("the product file %s does not exists, cannot be read or isn't a file.", productDummyFile),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

				try {
					return Files.readAllBytes(productDummyFile);
				} catch (final IOException ioe) {
					throw new NativeApiException(String.format("error reading the product file %s: %s", productDummyFile, ioe.getMessage()), ioe,
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				throw new NativeApiException("the product file download from OBS storage is not yet implemented.", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			throw new NativeApiException(String.format("mission not supported (yet): %s", missionName), HttpStatus.NOT_FOUND);
		}
	}

}
