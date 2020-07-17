package esa.s1pdgs.cpoc.ipf.preparation.worker.type.slice;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProductAdapter;

public class LevelSliceProduct {
	private final AppDataJobProductAdapter product;

	public LevelSliceProduct(final AppDataJobProductAdapter product) {
		this.product = product;
	}
	
	public static final LevelSliceProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final LevelSliceProduct of(final AppDataJobProduct product) {
		return new LevelSliceProduct(
				new AppDataJobProductAdapter(product)
		);
	}

	public final void setProductType(final String productType) {
		product.setStringValue("productType", productType);
	}

	public final void setInsConfId(final int instrumentConfigurationId) {
		product.setInsConfId(instrumentConfigurationId);		
	}

	public final void setNumberSlice(final int numberSlice) {
		product.setIntegerValue("numberSlice", numberSlice);
	}

	public final void setDataTakeId(final String datatakeId) {
		product.setStringValue("dataTakeId", datatakeId);			
	}

	public final String getProductName() {
		return product.getProductName();
	}

	public final String getProcessMode() {
		return product.getProcessMode();
	}
	
	public final int getNumberSlice() {
		return product.getIntegerValue("numberSlice");
	}

	public final int getTotalNbOfSlice() {
		return product.getIntegerValue("totalNbOfSlice");
	}

	public final void setTotalNbOfSlice(final int numberOfSlices) {
		product.setIntegerValue("totalNbOfSlice", numberOfSlices);		
	}

	public final void setSegmentStartDate(final String validityStart) {
		product.setStringValue("segmentStartDate", validityStart);		
	}

	public final void setSegmentStopDate(final String validityStop) {
		product.setStringValue("segmentStopDate", validityStop);
	}

	public final void setAcquisition(final String swathType) {
		product.setStringValue("acquistion", swathType);	
	}

	public final String getAcquisition() {
		return product.getStringValue("acquistion");
	}
	
	public final void setPolarisation(final String polarisation) {
		product.setStringValue("polarisation", polarisation);
	}

	public final String getSegmentStartDate() {
		return product.getStringValue("segmentStartDate");
	}

	public final String getSegmentStopDate() {
		return product.getStringValue("segmentStopDate");
	}

	public final String getMissionId() {
		return product.getMissionId();
	}

	public final String getSatelliteId() {
		return product.getSatelliteId();
	}
}
