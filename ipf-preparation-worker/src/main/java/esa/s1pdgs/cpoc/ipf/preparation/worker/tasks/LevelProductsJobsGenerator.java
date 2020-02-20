package esa.s1pdgs.cpoc.ipf.preparation.worker.tasks;

import java.util.Collections;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGeneration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderSensingTime;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.XmlConverter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

/**
 * Customization of the job generator for L1 and L2 slice products
 * 
 * @author birol_colak@net.werum
 *
 */
public class LevelProductsJobsGenerator extends AbstractJobsGenerator {

	/**
	 * @param xmlConverter
	 * @param metadataClient
	 * @param processSettings
	 * @param taskTablesSettings
	 * @param outputFactory
	 * @param appDataService
	 */
	public LevelProductsJobsGenerator(
			final XmlConverter xmlConverter, 
			final MetadataClient metadataClient, 
			final ProcessSettings processSettings,
			final IpfPreparationWorkerSettings taskTablesSettings, 
			final AppCatalogJobClient<CatalogEvent>  appDataService, 
			final ProcessConfiguration processConfiguration,
			final MqiClient mqiClient
	) {
		super(xmlConverter, metadataClient, processSettings, taskTablesSettings, appDataService, processConfiguration, mqiClient);
	}

	/**
	 * Check the product and retrieve useful information before searching inputs
	 */
	@Override
	protected void preSearch(final JobGeneration job) throws IpfPrepWorkerInputsMissingException {

		// Retrieve instrument configuration id and slice number
		try {
			final L0SliceMetadata file = this.metadataClient.getL0Slice(job.getAppDataJob().getProduct().getProductName());
			job.getAppDataJob().getProduct().setProductType(file.getProductType());
			job.getAppDataJob().getProduct().setInsConfId(file.getInstrumentConfigurationId());
			job.getAppDataJob().getProduct().setNumberSlice(file.getNumberSlice());
			job.getAppDataJob().getProduct().setDataTakeId(file.getDatatakeId());
		} catch (final MetadataQueryException e) {
			throw new IpfPrepWorkerInputsMissingException(
					Collections.singletonMap(
							job.getAppDataJob().getProduct().getProductName(), 
							"No Slice: " + e.getMessage()
					)
			);
		}
		// Retrieve Total_Number_Of_Slices
		try {
			final L0AcnMetadata acn = this.metadataClient.getFirstACN(job.getAppDataJob().getProduct().getProductName(),
					job.getAppDataJob().getProduct().getProcessMode());
			job.getAppDataJob().getProduct().setTotalNbOfSlice(acn.getNumberOfSlices());
			job.getAppDataJob().getProduct().setSegmentStartDate(acn.getValidityStart());
			job.getAppDataJob().getProduct().setSegmentStopDate(acn.getValidityStop());
		} catch (final MetadataQueryException e) {
			throw new IpfPrepWorkerInputsMissingException(	
					Collections.singletonMap(
							job.getAppDataJob().getProduct().getProductName(), 
							"No ACNs: " + e.getMessage()
					)
			);
		}
	}

	/**
	 *	Custom job order before building the job DTO
	 */
	@Override
	protected void customJobOrder(final JobGeneration job) {
		// Rewrite job order sensing time
		final String jobOrderStart = DateUtils.convertToAnotherFormat(job.getAppDataJob().getProduct().getSegmentStartDate(),
				AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER);
		final String jobOrderStop = DateUtils.convertToAnotherFormat(job.getAppDataJob().getProduct().getSegmentStopDate(),
				AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER);
		job.getJobOrder().getConf().setSensingTime(new JobOrderSensingTime(jobOrderStart, jobOrderStop));

		this.updateProcParam(job.getJobOrder(), "Mission_Id",
				job.getAppDataJob().getProduct().getMissionId() + job.getAppDataJob().getProduct().getSatelliteId());
		this.updateProcParam(job.getJobOrder(), "Slice_Number", "" + job.getAppDataJob().getProduct().getNumberSlice());
		this.updateProcParam(job.getJobOrder(), "Total_Number_Of_Slices",
				"" + job.getAppDataJob().getProduct().getTotalNbOfSlice());
		this.updateProcParam(job.getJobOrder(), "Slice_Overlap",
				"" + ipfPreparationWorkerSettings.getTypeOverlap().get(job.getAppDataJob().getProduct().getAcquisition()));
		this.updateProcParam(job.getJobOrder(), "Slice_Length",
				"" + ipfPreparationWorkerSettings.getTypeSliceLength().get(job.getAppDataJob().getProduct().getAcquisition()));
		this.updateProcParam(job.getJobOrder(), "Slicing_Flag", "TRUE");

	}

	/**
	 * Customization of the job DTO before sending it
	 */
	@Override
	protected void customJobDto(final JobGeneration job, final IpfExecutionJob dto) {
		// NOTHING TO DO

	}

	/**
	 * Update or create a proc param in the job order
	 * 
	 * @param jobOrder
	 * @param name
	 * @param newValue
	 */
	void updateProcParam(final JobOrder jobOrder, final String name, final String newValue) {
		boolean update = false;
		for (final JobOrderProcParam param : jobOrder.getConf().getProcParams()) {
			if (name.equals(param.getName())) {
				param.setValue(newValue);
				update = true;
			}
		}
		if (!update) {
			jobOrder.getConf().addProcParam(new JobOrderProcParam(name, newValue));
		}
	}

}
