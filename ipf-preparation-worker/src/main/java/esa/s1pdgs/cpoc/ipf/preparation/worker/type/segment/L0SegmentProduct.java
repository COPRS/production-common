package esa.s1pdgs.cpoc.ipf.preparation.worker.type.segment;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;

public class L0SegmentProduct extends AbstractProduct {	

	public L0SegmentProduct(final AppDataJobProductAdapter product) {
		super(product);
	}

	public static final L0SegmentProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final L0SegmentProduct of(final AppDataJobProduct product) {
		return new L0SegmentProduct(
				new AppDataJobProductAdapter(product)
		);
	}

	public final void setAcquistion(final String swathType) {
		product.setStringValue("acquistion", swathType);		
	}
	
	public final String getAcquistion() {
		return product.getStringValue("acquistion");
	}

	public final void setDataTakeId(final String datatakeId) {
		product.setStringValue("dataTakeId", datatakeId);		
	}

	public final String getDataTakeId() {
		return product.getStringValue("dataTakeId");
	}		
}
