package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import org.springframework.util.Assert;

public class SppObsTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {

    private final MetadataClient metadataClient;

    public SppObsTypeAdapter(MetadataClient metadataClient) {
        this.metadataClient = metadataClient;
    }

    @Override
    public Product mainInputSearch(AppDataJob job) {

        Assert.notNull(job, "Provided AppDataJob is null");
        Assert.notNull(job.getProduct(), "Provided AppDataJobProduct is null");

        final AuxResorbProduct auxResob = AuxResorbProduct.of(job);

        try {
            SearchMetadata searchResult =
                    metadataClient.queryByFamilyAndProductName(
                            ProductFamily.AUXILIARY_FILE.name(),
                            auxResob.getProductName());

            auxResob.setStartTime(searchResult.getValidityStart());
            auxResob.setStopTime(searchResult.getValidityStop());
        } catch (MetadataQueryException e) {
            LOGGER.error("Error on query execution, retrying next time", e);
        }

        return auxResob;
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

    }

    @Override
    public void customJobDto(AppDataJob job, IpfExecutionJob dto) {

    }
}
