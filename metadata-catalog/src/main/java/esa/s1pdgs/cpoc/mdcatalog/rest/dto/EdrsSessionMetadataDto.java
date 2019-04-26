package esa.s1pdgs.cpoc.mdcatalog.rest.dto;

/**
 * Class describing the metdata of a file
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionMetadataDto extends MetadataDto {

	public EdrsSessionMetadataDto(String productName, String productType, String keyObjectStorage,
			String validityStart, String validityStop) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
	}

	public EdrsSessionMetadataDto(EdrsSessionMetadataDto obj) {
		this(obj.getProductName(), obj.getProductType(), obj.getKeyObjectStorage(), obj.getValidityStart(),
				obj.getValidityStop());
	}
	
}
