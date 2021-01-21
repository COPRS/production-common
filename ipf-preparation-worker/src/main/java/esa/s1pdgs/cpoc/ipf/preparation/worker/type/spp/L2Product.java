package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;

public class L2Product extends AbstractProduct {
	
	public L2Product(final AppDataJobProductAdapter product) {
		super(product);
	}

	public static final L2Product of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final L2Product of(final AppDataJobProduct product) {
		return new L2Product(
				new AppDataJobProductAdapter(product)
		);
	}
}