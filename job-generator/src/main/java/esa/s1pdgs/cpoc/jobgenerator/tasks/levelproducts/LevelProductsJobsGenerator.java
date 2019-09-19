package esa.s1pdgs.cpoc.jobgenerator.tasks.levelproducts;

import java.util.HashMap;
import java.util.Map;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.JobGeneration;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderSensingTime;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsGenerator;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Customization of the job generator for L1 and L2 slice products
 * 
 * @author birol_colak@net.werum
 *
 */
public class LevelProductsJobsGenerator extends AbstractJobsGenerator<ProductDto> {

	/**
	 * @param xmlConverter
	 * @param metadataClient
	 * @param processSettings
	 * @param taskTablesSettings
	 * @param outputFactory
	 * @param appDataService
	 */
	public LevelProductsJobsGenerator(XmlConverter xmlConverter, MetadataClient metadataClient, ProcessSettings processSettings,
			JobGeneratorSettings taskTablesSettings, OutputProducerFactory outputFactory,
			AppCatalogJobClient appDataService) {
		super(xmlConverter, metadataClient, processSettings, taskTablesSettings, outputFactory, appDataService);
	}

	/**
	 * Check the product and retrieve useful information before searching inputs
	 */
	@Override
	protected void preSearch(JobGeneration job) throws JobGenInputsMissingException {
		Map<String, String> missingMetadata = new HashMap<>();
		// Retrieve instrument configuration id and slice number
		try {
			L0SliceMetadata file = this.metadataClient.getL0Slice(job.getAppDataJob().getProduct().getProductName());
			job.getAppDataJob().getProduct().setProductType(file.getProductType());
			job.getAppDataJob().getProduct().setInsConfId(file.getInstrumentConfigurationId());
			job.getAppDataJob().getProduct().setNumberSlice(file.getNumberSlice());
			job.getAppDataJob().getProduct().setDataTakeId(file.getDatatakeId());
		} catch (JobGenMetadataException e) {
			missingMetadata.put(job.getAppDataJob().getProduct().getProductName(), "No Slice: " + e.getMessage());
			throw new JobGenInputsMissingException(missingMetadata);
		}
		// Retrieve Total_Number_Of_Slices
		try {
			L0AcnMetadata acn = this.metadataClient.getFirstACN(job.getAppDataJob().getProduct().getProductName(),
					job.getAppDataJob().getProduct().getProcessMode());
			job.getAppDataJob().getProduct().setTotalNbOfSlice(acn.getNumberOfSlices());
			job.getAppDataJob().getProduct().setSegmentStartDate(acn.getValidityStart());
			job.getAppDataJob().getProduct().setSegmentStopDate(acn.getValidityStop());
		} catch (JobGenMetadataException e) {
			missingMetadata.put(job.getAppDataJob().getProduct().getProductName(), "No ACNs: " + e.getMessage());
			throw new JobGenInputsMissingException(missingMetadata);
		}

	}

	/**
	 *	Custom job order before building the job DTO
	 */
	@Override
	protected void customJobOrder(JobGeneration job) {
		// Rewrite job order sensing time
		String jobOrderStart = DateUtils.convertToAnotherFormat(job.getAppDataJob().getProduct().getSegmentStartDate(),
				AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER);
		String jobOrderStop = DateUtils.convertToAnotherFormat(job.getAppDataJob().getProduct().getSegmentStopDate(),
				AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER);
		job.getJobOrder().getConf().setSensingTime(new JobOrderSensingTime(jobOrderStart, jobOrderStop));

		this.updateProcParam(job.getJobOrder(), "Mission_Id",
				job.getAppDataJob().getProduct().getMissionId() + job.getAppDataJob().getProduct().getSatelliteId());
		this.updateProcParam(job.getJobOrder(), "Slice_Number", "" + job.getAppDataJob().getProduct().getNumberSlice());
		this.updateProcParam(job.getJobOrder(), "Total_Number_Of_Slices",
				"" + job.getAppDataJob().getProduct().getTotalNbOfSlice());
		this.updateProcParam(job.getJobOrder(), "Slice_Overlap",
				"" + jobGeneratorSettings.getTypeOverlap().get(job.getAppDataJob().getProduct().getAcquisition()));
		this.updateProcParam(job.getJobOrder(), "Slice_Length",
				"" + jobGeneratorSettings.getTypeSliceLength().get(job.getAppDataJob().getProduct().getAcquisition()));
		this.updateProcParam(job.getJobOrder(), "Slicing_Flag", "TRUE");

	}

	/**
	 * Customization of the job DTO before sending it
	 */
	@Override
	protected void customJobDto(JobGeneration job, LevelJobDto dto) {
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
		for (JobOrderProcParam param : jobOrder.getConf().getProcParams()) {
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
