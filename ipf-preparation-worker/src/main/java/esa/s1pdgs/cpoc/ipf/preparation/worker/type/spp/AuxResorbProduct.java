package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;

public class AuxResorbProduct extends AbstractProduct {

    public AuxResorbProduct(AppDataJobProductAdapter product) {
        super(product);
    }

    public static AuxResorbProduct of(AppDataJob job) {
        return new AuxResorbProduct(new AppDataJobProductAdapter(job.getProduct()));
    }

    public void setStartTime(final String startTime) {
        product.setStartTime(startTime);
    }

    public void setStopTime(final String stopTime) {
        product.setStopTime(stopTime);
    }
}
