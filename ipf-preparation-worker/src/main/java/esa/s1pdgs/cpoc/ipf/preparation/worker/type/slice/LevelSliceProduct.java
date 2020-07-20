package esa.s1pdgs.cpoc.ipf.preparation.worker.type.slice;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;

public class LevelSliceProduct extends AbstractProduct {
	public LevelSliceProduct(final AppDataJobProductAdapter product) {
		super(product);
	}

	public static final LevelSliceProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final LevelSliceProduct of(final AppDataJobProduct product) {
		return new LevelSliceProduct(
				new AppDataJobProductAdapter(product)
		);
	}


	public final void setInsConfId(final int instrumentConfigurationId) {
		product.setInsConfId(instrumentConfigurationId);		
	}

	public final void setNumberSlice(final int numberSlice) {
		product.setIntegerValue("numberSlice", numberSlice);
	}
	
	public final int getNumberSlice() {
		return product.getIntegerValue("numberSlice");
	}

	public final void setDataTakeId(final String datatakeId) {
		product.setStringValue("dataTakeId", datatakeId);			
	}

	public final void setTotalNbOfSlice(final int numberOfSlices) {
		product.setIntegerValue("totalNbOfSlice", numberOfSlices);		
	}
	
	public final int getTotalNbOfSlice() {
		return product.getIntegerValue("totalNbOfSlice");
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
	
	public final String getPolarisation() {
		return product.getStringValue("polarisation");
	}

	public final String getSegmentStartDate() {
		return product.getStringValue("segmentStartDate");
	}

	public final String getSegmentStopDate() {
		return product.getStringValue("segmentStopDate");
	}
}
