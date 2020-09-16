package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import java.time.format.DateTimeFormatter;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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

    private static final Logger LOG = LoggerFactory.getLogger(SppObsPropertiesAdapter.class);

    private static final DateTimeFormatter JO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private final MetadataClient metadataClient;
    private final SppObsPropertiesAdapter configuration;

    public SppObsTypeAdapter(MetadataClient metadataClient, SppObsPropertiesAdapter configuration) {
        this.metadataClient = metadataClient;
        this.configuration = configuration;
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
                            DateUtils.convertToAnotherFormat(
                                    withZ(time),
                                    AppDataJobProduct.TIME_FORMATTER,
                                    JO_TIME_FORMATTER
                            )
                    ));

        } catch (MetadataQueryException e) {
            LOGGER.error("Error on query execution, retrying next time", e);
        }

        return auxResorb;
    }

    private String withZ(final String time) {
        if (!time.endsWith("Z")) {
            return time + 'Z';
        }
        return time;
    }

    @Override
    public void validateInputSearch(AppDataJob job) throws IpfPrepWorkerInputsMissingException {
        if (configuration.shouldWait(job)) {
            LOG.info("timeout for Spp Obs job {} for AUX_RESORB {} not reached yet", job.getId(), AuxResorbProduct.of(job).getProductName());
            throw new IpfPrepWorkerInputsMissingException(Collections.emptyMap());
        }

        final String selectedOrbitFirstAzimuthTimeUtc = AuxResorbProduct.of(job).getSelectedOrbitFirstAzimuthTimeUtc();

        if (StringUtils.isEmpty(selectedOrbitFirstAzimuthTimeUtc)) {
            LOG.error("the selectedOrbitFirstAzimuthTime is missing for job {} of AUX_RESORB {}", job.getId(), AuxResorbProduct.of(job).getProductName());
            throw new IpfPrepWorkerInputsMissingException(Collections.emptyMap());
        }
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
