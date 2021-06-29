package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import java.util.ArrayList;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;

public class L2Product extends AbstractProduct {
	
	private final List<AppDataJobTaskInputs> overridingInputs = new ArrayList<>();
	
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
	
	public void setStartTime(final String startTime) {
        product.setStartTime(startTime);
    }

    public void setStopTime(final String stopTime) {
        product.setStopTime(stopTime);
    }

    public final void overridingInputs(final List<AppDataJobTaskInputs> overridingInputs) {
    	this.overridingInputs.addAll(overridingInputs);
    }
    
    @Override
	public List<AppDataJobTaskInputs> overridingInputs() {
		return overridingInputs;
	}
}