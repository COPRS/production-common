package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

public class SppObsTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {

    @Override
    public void customAppDataJob(AppDataJob job) {

    }

    @Override
    public void customJobOrder(AppDataJob job, JobOrder jobOrder) {

    }

    @Override
    public void customJobDto(AppDataJob job, IpfExecutionJob dto) {

    }
}
