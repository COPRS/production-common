package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import java.time.format.DateTimeFormatter;

import org.springframework.util.Assert;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.AuxMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

public class SppObsTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {

    private static final DateTimeFormatter JO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSSSS");
    private final MetadataClient metadataClient;

    public SppObsTypeAdapter(MetadataClient metadataClient) {
        this.metadataClient = metadataClient;
    }

    @Override
    public Product mainInputSearch(AppDataJob job) {

        Assert.notNull(job, "Provided AppDataJob is null");
        Assert.notNull(job.getProduct(), "Provided AppDataJobProduct is null");

        final AuxResorbProduct auxResorb = AuxResorbProduct.of(job);

        try {

            AuxMetadata searchResult = metadataClient.queryAuxiliary("AUX_RESORB", auxResorb.getProductName());

            auxResorb.setStartTime(searchResult.getValidityStart());
            auxResorb.setStopTime(searchResult.getValidityStop());

            searchResult.ifPresent(
                    "selectedOrbitFirstAzimuthTimeUtc",
                    time -> auxResorb.setSelectedOrbitFirstAzimuthTimeUtc(
                            DateUtils.convertToAnotherFormat(time, AppDataJobProduct.TIME_FORMATTER, JO_TIME_FORMATTER)
                    ));

        } catch (MetadataQueryException e) {
            LOGGER.error("Error on query execution, retrying next time", e);
        }

        return auxResorb;
    }

    @Override
    public void validateInputSearch(AppDataJob job) throws IpfPrepWorkerInputsMissingException {
        //TODO implement
        //TODO check if the selectedOrbitFirstAzimuthTimeUtc is available
        //TODO check timeout (before other checks, analog to edrsSession) (always throw exception when timeout is NOT reached)
    }

    @Override
    public void customAppDataJob(AppDataJob job) {

        final CatalogEventAdapter catalogEvent = CatalogEventAdapter.of(job);
        final AuxResorbProduct auxResorb = AuxResorbProduct.of(job);

        auxResorb.setStartTime(catalogEvent.validityStartTime());
        auxResorb.setStopTime(catalogEvent.validityStopTime());
        job.setStartTime(catalogEvent.validityStartTime());
        job.setStopTime(catalogEvent.validityStopTime());
    }

    @Override
    public void customJobOrder(AppDataJob job, JobOrder jobOrder) {
        AuxResorbProduct auxResorb = AuxResorbProduct.of(job);

        updateProcParam(jobOrder, "selectedOrbitFirstAzimuthTime", auxResorb.getSelectedOrbitFirstAzimuthTimeUtc());
    }

    @Override
    public void customJobDto(AppDataJob job, IpfExecutionJob dto) {

    }
}
