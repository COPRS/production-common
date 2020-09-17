package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;

public class S3Product extends AbstractProduct {
	
	/**
	 * List of additional inputs, that were set during the main input search
	 */
	private List<AppDataJobTaskInputs> additionalInputs;
	
	public static final S3Product of(final AppDataJob job) {
		final S3Product returnValue = of(job.getProduct());
		
		returnValue.setAdditionalInputs(job.getAdditionalInputs());
		
		return returnValue;
	}

	public static final S3Product of(final AppDataJobProduct product) {
		return new S3Product(new AppDataJobProductAdapter(product));
	}

	public S3Product(final AppDataJobProductAdapter product) {
		super(product);
	}

	public void setAdditionalInputs(final List<AppDataJobTaskInputs> additionalInputs) {
		this.additionalInputs = additionalInputs;
	}
	
	public final void setStartTime(final String start) {
		product.setStartTime(start);
	}
	
	public final void setStopTime(final String stop) {
		product.setStopTime(stop);
	}
	
	@Override
	public List<AppDataJobTaskInputs> overridingInputs() {
		return additionalInputs;
	}
}
