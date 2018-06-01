package fr.viveris.s1pdgs.jobgenerator.tasks.generator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.controller.JobsProducer;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;
import fr.viveris.s1pdgs.jobgenerator.exception.InputsMissingException;
import fr.viveris.s1pdgs.jobgenerator.exception.MetadataException;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrder;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderProcParam;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderSensingTime;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.L0AcnMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.L0SliceMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.product.L0Slice;
import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;
import fr.viveris.s1pdgs.jobgenerator.service.metadata.MetadataService;

/**
 * Customization of the job generator for L0 slice products
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L0SlicesJobsGenerator extends AbstractJobsGenerator<L0Slice> {

	/**
	 * 
	 * @param xmlConverter
	 * @param metadataService
	 * @param l0ProcessSettings
	 * @param taskTablesSettings
	 * @param kafkaJobsSender
	 */
	public L0SlicesJobsGenerator(final XmlConverter xmlConverter, final MetadataService metadataService,
			final ProcessSettings l0ProcessSettings, final JobGeneratorSettings taskTablesSettings,
			final JobsProducer kafkaJobsSender) {
		super(xmlConverter, metadataService, l0ProcessSettings, taskTablesSettings, kafkaJobsSender);
	}

	/**
	 * Check the product and retrieve usefull information before searching inputs
	 */
	@Override
	protected void preSearch(final Job<L0Slice> job) throws InputsMissingException {
		Map<String, String> missingMetadata = new HashMap<>();
		// Retrieve instrument configuration id and slice number
		try {
			L0SliceMetadata file = this.metadataService.getSlice("blank", job.getProduct().getIdentifier());
			job.getProduct().setProductType(file.getProductType());
			job.getProduct().setInsConfId(file.getInsConfId());
			job.getProduct().getObject().setNumberSlice(file.getNumberSlice());
			job.getProduct().getObject().setDataTakeId(file.getDatatakeId());
		} catch (MetadataException e) {
			missingMetadata.put(job.getProduct().getIdentifier(), "No Slice: " + e.getMessage());
			throw new InputsMissingException(missingMetadata);
		}
		// Retrieve Total_Number_Of_Slices
		try {
			L0AcnMetadata acn = this.metadataService.getFirstACN(job.getProduct().getProductType(),
					job.getProduct().getIdentifier());
			job.getProduct().getObject().setTotalNbOfSlice(acn.getNumberOfSlices());
			job.getProduct().getObject().setSegmentStartDate(acn.getValidityStart());
			job.getProduct().getObject().setSegmentStopDate(acn.getValidityStop());
		} catch (MetadataException e) {
			missingMetadata.put(job.getProduct().getIdentifier(), "No ACNs: " + e.getMessage());
			throw new InputsMissingException(missingMetadata);
		}
	}

	/**
	 * Custom job order before building the job DTO
	 */
	@Override
	protected void customJobOrder(final Job<L0Slice> job) {
		// Rewrite job order sensing time
		DateTimeFormatter formatterJobOrder = DateTimeFormatter.ofPattern(JobOrderSensingTime.DATE_FORMAT);
		DateTimeFormatter formatterProduct = SearchMetadata.DATE_FORMATTER;
		LocalDateTime startDate = LocalDateTime.parse(job.getProduct().getObject().getSegmentStartDate(),
				formatterProduct);
		String jobOrderStart = startDate.format(formatterJobOrder);
		LocalDateTime stopDate = LocalDateTime.parse(job.getProduct().getObject().getSegmentStopDate(),
				formatterProduct);
		String jobOrderStop = stopDate.format(formatterJobOrder);
		job.getJobOrder().getConf().setSensingTime(new JobOrderSensingTime(jobOrderStart, jobOrderStop));

		this.updateProcParam(job.getJobOrder(), "Mission_Id",
				job.getProduct().getMissionId() + job.getProduct().getSatelliteId());
		this.updateProcParam(job.getJobOrder(), "Slice_Number", "" + job.getProduct().getObject().getNumberSlice());
		this.updateProcParam(job.getJobOrder(), "Total_Number_Of_Slices",
				"" + job.getProduct().getObject().getTotalNbOfSlice());
		this.updateProcParam(job.getJobOrder(), "Slice_Overlap",
				"" + jobGeneratorSettings.getTypeOverlap().get(job.getProduct().getObject().getAcquisition()));
		this.updateProcParam(job.getJobOrder(), "Slice_Length",
				"" + jobGeneratorSettings.getTypeSliceLength().get(job.getProduct().getObject().getAcquisition()));
		this.updateProcParam(job.getJobOrder(), "Slicing_Flag", "TRUE");
	}

	/**
	 * Update or create a proc param in the job order
	 * 
	 * @param jobOrder
	 * @param name
	 * @param newValue
	 */
	protected void updateProcParam(final JobOrder jobOrder, final String name, final String newValue) {
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

	/**
	 * Customisation of the job DTO before sending it
	 */
	@Override
	protected void customJobDto(final Job<L0Slice> job, final JobDto dto) {
		// NOTHING TO DO

	}

}
