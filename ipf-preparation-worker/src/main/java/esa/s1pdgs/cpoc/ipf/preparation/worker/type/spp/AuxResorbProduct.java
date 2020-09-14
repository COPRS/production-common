package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
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

    public void setSelectedOrbitFirstAzimuthTimeUtc(String value) {
        product.setStringValue("selectedOrbitFirstAzimuthTimeUtc", value);
    }

    public String getSelectedOrbitFirstAzimuthTimeUtc() {
        return product.getStringValue("selectedOrbitFirstAzimuthTimeUtc");
    }

    @Override
    public List<AppDataJobTaskInputs> overridingInputs() {
        return null; //to add this product as input
    }
}
