package esa.s1pdgs.cpoc.preparation.worker.type.spp;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.AuxMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.util.QueryUtils;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableFileNameType;

public class SppObsTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SppObsTypeAdapter.class);

    private static final DateTimeFormatter JO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private final MetadataClient metadataClient;
    private final SppObsPropertiesAdapter configuration;

    public SppObsTypeAdapter(final MetadataClient metadataClient, final SppObsPropertiesAdapter configuration) {
        this.metadataClient = metadataClient;
        this.configuration = configuration;
    }

    @Override
    public Product mainInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdapter) {

        Assert.notNull(job, "Provided AppDataJob is null");
        Assert.notNull(job.getProduct(), "Provided AppDataJobProduct is null");

        final AuxResorbProduct auxResorb = AuxResorbProduct.of(job);

        try {

            final AuxMetadata searchResult = metadataClient.queryAuxiliary("AUX_RESORB", auxResorb.getProductName());

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
            
        	final List<AppDataJobTaskInputs> appDataJobTaskInputs = QueryUtils.buildInitialInputs(tasktableAdapter);
        	final AppDataJobTaskInputs originalInput = appDataJobTaskInputs.get(0);
        	final AppDataJobInput first = originalInput.getInputs().get(0);
        	final AppDataJobFile file = new AppDataJobFile(
        			auxResorb.getProductName(),
        			auxResorb.getProductName(),
        			TaskTableAdapter.convertDateToJobOrderFormat(auxResorb.getStartTime()),
        			TaskTableAdapter.convertDateToJobOrderFormat(auxResorb.getStopTime()),
        			null
        	);
        	final AppDataJobInput input = new AppDataJobInput(
        			first.getTaskTableInputReference(),
        			"AUX_RES",
        			TaskTableFileNameType.PHYSICAL.toString(),
        			first.isMandatory(),
        			Collections.singletonList(file)
        	);
      		originalInput.setInputs(Collections.singletonList(input));
      		auxResorb.overridingInputs(appDataJobTaskInputs);
      		LOGGER.debug("Added AUXRESORB {}", originalInput);
        } catch (final MetadataQueryException e) {
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
    public void validateInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdpter) throws IpfPrepWorkerInputsMissingException {
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
	public List<AppDataJob> createAppDataJobs(final IpfPreparationJob job) {
		final AppDataJob appDataJob = AppDataJob.fromPreparationJob(job);

        final CatalogEventAdapter catalogEvent = CatalogEventAdapter.of(appDataJob);
        final AuxResorbProduct auxResorb = AuxResorbProduct.of(appDataJob);

        auxResorb.setStartTime(catalogEvent.validityStartTime());
        auxResorb.setStopTime(catalogEvent.validityStopTime());
        appDataJob.setStartTime(catalogEvent.validityStartTime());
        appDataJob.setStopTime(catalogEvent.validityStopTime());
        
        return Collections.singletonList(appDataJob);
    }

    @Override
    public void customJobOrder(final AppDataJob job, final JobOrder jobOrder) {
        final AuxResorbProduct auxResorb = AuxResorbProduct.of(job);

        updateProcParam(jobOrder, "selectedOrbitFirstAzimuthTime", auxResorb.getSelectedOrbitFirstAzimuthTimeUtc());
    }

    @Override
    public void customJobDto(final AppDataJob job, final IpfExecutionJob dto) {

    }
}
