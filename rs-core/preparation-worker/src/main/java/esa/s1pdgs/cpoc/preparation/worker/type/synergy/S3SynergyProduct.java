package esa.s1pdgs.cpoc.preparation.worker.type.synergy;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProduct;

public class S3SynergyProduct extends AbstractProduct {

	/**
	 * List of additional inputs, that were set during the main input search
	 */
	private List<AppDataJobTaskInputs> additionalInputs;
	
	public static final S3SynergyProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final S3SynergyProduct of(final AppDataJobProduct product) {
		return new S3SynergyProduct(new AppDataJobProductAdapter(product));
	}
	
	public S3SynergyProduct(final AppDataJobProductAdapter product) {
		super(product);
	}
	
	public final void setStartTime(final String start) {
		product.setStartTime(start);
	}
	
	public final void setStopTime(final String stop) {
		product.setStopTime(stop);
	}
	
	public void setAdditionalInputs(final List<AppDataJobTaskInputs> additionalInputs) {
		this.additionalInputs = additionalInputs;
	}
	
	@Override
	public List<AppDataJobTaskInputs> overridingInputs() {
		return additionalInputs;
	}
}
