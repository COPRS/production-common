package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class S2ProductNameUtil {

	final public static Pattern HKTM_PRODUCT_NAME_PATTERN = Pattern.compile("^([A-Z][0-9A-Z])([0-9A-Z_])_([A-Z0-9]{4})_(PRD_HKTM__)_([0-9]{8}T[0-9]{6})_([0-9]{8}T[0-9]{6})_([0-9]{4})(.*)");
	final public static Pattern STANDARD_PRODUCT_NAME_PATTERN = Pattern.compile("^([A-Z][0-9A-Z])([0-9A-Z_])_([A-Z0-9]{4})_([0-9A-Z_]{10})_.{4}_([0-9]{8}T[0-9]{6})(.*)");
	final public static Pattern COMPACT_PRODUCT_NAME_PATTERN = Pattern.compile("^([A-Z][0-9A-Z])([0-9A-Z_])_([0-9A-Z_]{6})_([0-9]{8}T[0-9]{6})(.*)");
	final public static Pattern AUX_PRODUCT_TYPE_PATTERN = Pattern.compile("^(AUX|DEM|GIP)_.*");

	public static JSONObject extractMetadata(String productName) throws MetadataExtractionException, MetadataMalformedException {
		final JSONObject metadata = new JSONObject();

		// HKTM PRODUCT NAMES
		
		final Matcher hktmMatcher = HKTM_PRODUCT_NAME_PATTERN.matcher(productName);
		if (hktmMatcher.matches()) {
			metadata.put("productName", productName);
			metadata.put("missionId", hktmMatcher.group(1));
			metadata.put("satelliteId", hktmMatcher.group(2));
			metadata.put("productClass", hktmMatcher.group(3));
			metadata.put("productType", hktmMatcher.group(4));
			metadata.put("validityStartTime", DateUtils.formatToMetadataDateTimeFormat(DateUtils.parse(hktmMatcher.group(5))));
			metadata.put("validityStopTime", DateUtils.formatToMetadataDateTimeFormat(DateUtils.parse(hktmMatcher.group(6))));	
			return metadata;
		}
		
		// STANDARD, COMPACT AND AUX PRODUCT NAMES

		final Matcher standardMatcher = STANDARD_PRODUCT_NAME_PATTERN.matcher(productName);
		final Matcher compactMatcher = COMPACT_PRODUCT_NAME_PATTERN.matcher(productName);
		final boolean isCompact;
		final String variablePart;
		
		if (standardMatcher.matches()) {
			isCompact = false;
			metadata.put("productName", productName);
			metadata.put("missionId", standardMatcher.group(1));
			metadata.put("satelliteId", standardMatcher.group(2));
			metadata.put("productClass", standardMatcher.group(3));
			metadata.put("productType", standardMatcher.group(4)); // Category+Semantic
			metadata.put("creationTime", DateUtils.formatToMetadataDateTimeFormat(DateUtils.parse(standardMatcher.group(5))));
			variablePart = standardMatcher.group(6);
		} else if (compactMatcher.matches()) {
			isCompact = true;
			metadata.put("productName", productName);
			metadata.put("missionId", compactMatcher.group(1));
			metadata.put("satelliteId", compactMatcher.group(2));
			metadata.put("productType", compactMatcher.group(3));
			metadata.put("startTime", DateUtils.formatToMetadataDateTimeFormat(DateUtils.parse(compactMatcher.group(4))));
			variablePart = compactMatcher.group(5);
		} else {
			throw new MetadataExtractionException(
				new Exception(String.format("Product %s not matchting pattern %s", productName, STANDARD_PRODUCT_NAME_PATTERN)));
		}

		final boolean isAuxProduct = AUX_PRODUCT_TYPE_PATTERN.matcher(metadata.getString("productType")).matches();
		
		int idx = 0;
		while (idx + 1 < variablePart.length() && '_' == variablePart.charAt(idx++)) {
			int valueLength = 0;
			String valueFormat = "";
			String attributeName = "";
			char suffix = variablePart.charAt(idx++);
			switch(suffix) {
				case 'S': valueLength = 15; valueFormat = "^[0-9]{8}T[0-9]{6}$"; attributeName = isAuxProduct ? "validityStartTime" : "sensingTime"; break;
				case 'O': valueLength = 13; valueFormat = "^([0-9]{6})_([0-9]{6})$"; attributeName = "orbitPeriod"; break;
				case 'V': valueLength = 31; valueFormat = "^([0-9]{8}T[0-9]{6})_([0-9]{8}T[0-9]{6})$"; attributeName = "applicabilityTimePeriod"; break;
				case 'D': valueLength = 2; valueFormat = "^[0-9]{2}$"; attributeName = "detectorId"; break;
				case 'A': valueLength = 6; valueFormat = "^[0-9]{6}$"; attributeName = "absolutOrbit"; break;
				case 'R': valueLength = 3; valueFormat = "^[0-9]{3}$"; attributeName = "relativeOrbit"; break;
				case 'T': valueLength = 5; valueFormat = "^[0-9A-Z]{5}$"; attributeName = "tileNumber"; break;
				case 'N': valueLength = isCompact ? 4 : 5; valueFormat = "^[0-9]{2}\\.?[0-9]{2}$"; attributeName = "processingBaselineNumber"; break;
				case 'B': valueLength = 2; valueFormat = "^[0-9A-Z]{2}$"; attributeName = "bandIndexId";break;
				case 'W': valueLength = 1; valueFormat = "^(F|P)$"; attributeName = "completenessId"; break;
				case 'L': valueLength = 1; valueFormat = "^(N|D)$"; attributeName = "degradationId"; break;
				case '1': case '2': valueLength = 14; valueFormat = "^[0-9]{7}T[0-9]{6}$"; attributeName = "productDiscriminator"; break;
				default:
					throw new MetadataMalformedException(attributeName);
			}

			if (variablePart.length() < valueLength) {
				throw new MetadataMalformedException(attributeName);
			}

			String value = variablePart.substring(idx, idx += valueLength);

			Matcher valueMatcher = Pattern.compile(valueFormat).matcher(value);
			if (!valueMatcher.matches()) {
				throw new MetadataMalformedException(attributeName);
			}
			
			switch(attributeName) {
				case "absolutOrbit":
					metadata.put(attributeName, Integer.parseInt(value));
					break;
				case "applicabilityTimePeriod": // validity with start and stop or sensing start and stop
					if (isAuxProduct) {
						metadata.put("validityStartTime", DateUtils.formatToMetadataDateTimeFormat(DateUtils.parse(valueMatcher.group(1))));
						metadata.put("validityStopTime", DateUtils.formatToMetadataDateTimeFormat(DateUtils.parse(valueMatcher.group(2))));
					} else {
						metadata.put("startTime", DateUtils.formatToMetadataDateTimeFormat(DateUtils.parse(valueMatcher.group(1))));
						metadata.put("stopTime", DateUtils.formatToMetadataDateTimeFormat(DateUtils.parse(valueMatcher.group(2))));
					}
					break;
				case "orbitPeriod":
					metadata.put("absoluteStartOrbit", Integer.parseInt(valueMatcher.group(1)));
					metadata.put("absoluteStopOrbit", Integer.parseInt(valueMatcher.group(2)));
					break;
				case "processingBaselineNumber":
					if (4 == valueLength) {
						metadata.put(attributeName, value.substring(0, 2) + '.' + value.substring(2, 4));	
					} else {
						metadata.put(attributeName, value);
					}
					break;
				case "productDiscriminator":
					metadata.put(attributeName, DateUtils.formatToMetadataDateTimeFormat(DateUtils.parse(suffix + value)));
					break;
				case "relativeOrbit":
					metadata.put(attributeName, Integer.parseInt(value));
					break;
				case "sensingTime": // average sensing time
					metadata.put(attributeName, DateUtils.formatToMetadataDateTimeFormat(DateUtils.parse(value)));
					break;
				case "validityStartTime": // validity start without stop
					metadata.put(attributeName, DateUtils.formatToMetadataDateTimeFormat(DateUtils.parse(value)));
					metadata.put("validityStopTime", "9999-12-31T23:59:59.999999Z");						
					break;
				default:
					metadata.put(attributeName, value);
			}
		}
		return metadata;
	}
}
