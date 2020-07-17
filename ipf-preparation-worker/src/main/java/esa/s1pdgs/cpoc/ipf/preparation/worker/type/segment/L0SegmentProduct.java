package esa.s1pdgs.cpoc.ipf.preparation.worker.type.segment;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProductAdapter;

public class L0SegmentProduct {	
	private final AppDataJobProductAdapter product;

	public L0SegmentProduct(final AppDataJobProductAdapter product) {
		this.product = product;
	}
	
	public static final L0SegmentProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final L0SegmentProduct of(final AppDataJobProduct product) {
		return new L0SegmentProduct(
				new AppDataJobProductAdapter(product)
		);
	}

	public void setAcquistion(final String swathType) {
		product.setStringValue("acquistion", swathType);		
	}

	public void setDataTakeId(final String datatakeId) {
		product.setStringValue("dataTakeId", datatakeId);		
	}

	public void setProductName(final String name) {
		product.setProductName(name);		
	}

	public final String getDataTakeId() {
		return product.getStringValue("dataTakeId");
	}

	public String getProductName() {
		return product.getProductName();
	}		
}
