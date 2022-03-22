package de.werum.coprs.nativeapi.service.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PripOdataEntityProperties {

	Id,
	Name,
	ContentType,
	ContentLength,
	PublicationDate,
	EvictionDate,
	ProductionType,
	ContentDate,
	Start,
	End,
	Checksum,
	Algorithm,
	Value,
	ChecksumDate,
	Footprint,
	Attributes,
	StringAttributes,
	IntegerAttributes,
	DoubleAttributes,
	DateTimeOffsetAttributes,
	BooleanAttributes;

	public static List<PripOdataEntityProperties> getAttributesCollectionProperties() {
		return new ArrayList<>(Arrays.asList(
				Attributes,
				StringAttributes,
				IntegerAttributes,
				DoubleAttributes,
				DateTimeOffsetAttributes,
				BooleanAttributes));
	}

}
