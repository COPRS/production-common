package fr.viveris.s1pdgs.mdcatalog.controllers.rest.dto;

/**
 * Class describing the metdata of a file
 * 
 * @author Cyrielle Gailliard
 *
 */
public class SearchMetadataDto extends MetadataDto {

	public SearchMetadataDto(String productName, String productType, String keyObjectStorage,
			String validityStart, String validityStop) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
	}

	public SearchMetadataDto(SearchMetadataDto obj) {
		this(obj.getProductName(), obj.getProductType(), obj.getKeyObjectStorage(), obj.getValidityStart(),
				obj.getValidityStop());
	}
	
}
