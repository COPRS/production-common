package esa.s1pdgs.cpoc.ipf.preparation.worker.type.pdu;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;

public class PDUProduct extends AbstractProduct {

	public static final PDUProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final PDUProduct of(final AppDataJobProduct product) {
		return new PDUProduct(new AppDataJobProductAdapter(product));
	}

	public PDUProduct(final AppDataJobProductAdapter product) {
		super(product);
	}
}
