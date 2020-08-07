package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;

public class S3Product extends AbstractProduct {

	public S3Product(final AppDataJobProductAdapter product) {
		super(product);
	}

	public static final S3Product of(final AppDataJob job) {
		return of(job.getProduct());
	}

	public static final S3Product of(final AppDataJobProduct product) {
		return new S3Product(new AppDataJobProductAdapter(product));
	}

}
