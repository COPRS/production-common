package fr.viveris.s1pdgs.mdcatalog.controllers.rest.dto;

/**
 * Class describing the metdata of a file
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L0SliceMetadataDto extends MetadataDto {
	
	private int instrumentConfigurationId;
	
	private int numberSlice;

	public L0SliceMetadataDto(String productName, String productType, String keyObjectStorage,
			String validityStart, String validityStop) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
	}

	public L0SliceMetadataDto(L0SliceMetadataDto obj) {
		this(obj.getProductName(), obj.getProductType(), obj.getKeyObjectStorage(), obj.getValidityStart(),
				obj.getValidityStop());
	}

	/**
	 * @return the instrumentConfigurationId
	 */
	public int getInstrumentConfigurationId() {
		return instrumentConfigurationId;
	}

	/**
	 * @param instrumentConfigurationId the instrumentConfigurationId to set
	 */
	public void setInstrumentConfigurationId(int instrumentConfigurationId) {
		this.instrumentConfigurationId = instrumentConfigurationId;
	}

	/**
	 * @return the numberSlice
	 */
	public int getNumberSlice() {
		return numberSlice;
	}

	/**
	 * @param numberSlice the numberSlice to set
	 */
	public void setNumberSlice(int numberSlice) {
		this.numberSlice = numberSlice;
	}
	
}
