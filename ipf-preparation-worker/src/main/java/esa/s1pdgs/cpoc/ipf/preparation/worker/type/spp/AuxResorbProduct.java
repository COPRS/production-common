package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import java.util.ArrayList;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobPreselectedInput;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;

public class AuxResorbProduct extends AbstractProduct {
	private final List<AppDataJobPreselectedInput> preselectedInputs = new ArrayList<>();

    public AuxResorbProduct(final AppDataJobProductAdapter product) {
        super(product);
    }

    public static AuxResorbProduct of(final AppDataJob job) {
        return new AuxResorbProduct(new AppDataJobProductAdapter(job.getProduct()));
    }

    public void setStartTime(final String startTime) {
        product.setStartTime(startTime);
    }

    public void setStopTime(final String stopTime) {
        product.setStopTime(stopTime);
    }

    public void setSelectedOrbitFirstAzimuthTimeUtc(final String value) {
        product.setStringValue("selectedOrbitFirstAzimuthTimeUtc", value);
    }

    public String getSelectedOrbitFirstAzimuthTimeUtc() {
        return product.getStringValue("selectedOrbitFirstAzimuthTimeUtc");
    }
    
    public final void preselectedInputs(final List<AppDataJobPreselectedInput> inputs) {
    	preselectedInputs.addAll(inputs);
    }
    
    @Override
	public List<AppDataJobPreselectedInput> preselectedInputs() {
		return preselectedInputs;
	}
}
