package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.slice.LevelSliceProduct;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderSensingTime;

public class SppMbuTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SppMbuTypeAdapter.class);

    private final MetadataClient metadataClient;
    private final Map<String, Float> sliceOverlap;
	private final Map<String, Float> sliceLength;
	private final Map<String,String> timelinessMapping;

    public SppMbuTypeAdapter(
			final MetadataClient metadataClient,
			final Map<String, Float> sliceOverlap,
			final Map<String, Float> sliceLength,
			final Map<String,String> timelinessMapping
	) {
		this.metadataClient = metadataClient;
		this.sliceOverlap = sliceOverlap;
		this.sliceLength = sliceLength;
		this.timelinessMapping = timelinessMapping;
	}

    @Override
    public Product mainInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdpter) {
    	return L2Product.of(job);
    }

    @Override
	public void validateInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdpter) throws IpfPrepWorkerInputsMissingException {
    	final L2Product product = L2Product.of(job);
    	try {
			final SearchMetadata metadata = metadataClient.queryByFamilyAndProductName(ProductFamily.L2_SLICE.name(), product.getProductName());
		} catch (final MetadataQueryException e) {
			LOGGER.debug("L2 product for {} not found in MDC (error was {}). Trying next time...", product.getProductName(), Exceptions.messageOf(e));
			throw new IpfPrepWorkerInputsMissingException(Collections.emptyMap());
		}
    	LOGGER.info("Found WV_OCN__2S {}", product.getProductName());
	}

	@Override
	public List<AppDataJob> createAppDataJobs(final IpfPreparationJob job) {
		final AppDataJob appDataJob = AppDataJob.fromPreparationJob(job);
		
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(appDataJob);
		final LevelSliceProduct product = LevelSliceProduct.of(appDataJob);		
		product.setAcquisition(eventAdapter.swathType()); 
        product.setPolarisation(eventAdapter.polarisation());
        
        return Collections.singletonList(appDataJob);
	}

	@Override
	public final void customJobOrder(final AppDataJob job, final JobOrder jobOrder) {
	}

	@Override
	public final void customJobDto(final AppDataJob job, final IpfExecutionJob dto) {
		// NOTHING TO DO
	}
}
