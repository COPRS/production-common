package fr.viveris.s1pdgs.jobgenerator.tasks.generator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.controller.JobsProducer;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;
import fr.viveris.s1pdgs.jobgenerator.exception.MetadataException;
import fr.viveris.s1pdgs.jobgenerator.exception.MetadataMissingException;
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

public class L0SlicesJobsGenerator extends AbstractJobsGenerator<L0Slice> {

	public L0SlicesJobsGenerator(XmlConverter xmlConverter, MetadataService metadataService,
			ProcessSettings l0ProcessSettings, JobGeneratorSettings taskTablesSettings, JobsProducer kafkaJobsSender) {
		super(xmlConverter, metadataService, l0ProcessSettings, taskTablesSettings, kafkaJobsSender);
	}

	@Override
	protected void preSearch(Job<L0Slice> job) throws MetadataMissingException {
		List<String> missingMetadata = new ArrayList<>();
		// Retrieve instrument configuration id and slice number
		try {
			L0SliceMetadata file = this.metadataService.getSlice("blank", job.getProduct().getIdentifier());
			job.getProduct().setProductType(file.getProductType());
			job.getProduct().setInstrumentConfigurationId(file.getInstrumentConfigurationId());
			job.getProduct().getObject().setNumberSlice(file.getNumberSlice());
			job.getProduct().getObject().setDataTakeId(file.getDatatakeId());
		} catch (MetadataException e) {
			missingMetadata.add(job.getProduct().getIdentifier());
			throw new MetadataMissingException(missingMetadata);
		}
		// Retrieve Total_Number_Of_Slices
		try {
			L0AcnMetadata acn = this.metadataService.getFirstACN(job.getProduct().getProductType(),
					job.getProduct().getIdentifier());
			job.getProduct().getObject().setTotalNumberOfSlice(acn.getNumberOfSlices());
			job.getProduct().getObject().setStartDateFromMetadata(acn.getValidityStart());
			job.getProduct().getObject().setStopDateFromMetadata(acn.getValidityStop());
		} catch (MetadataException e) {
			missingMetadata.add(job.getProduct().getIdentifier());
			throw new MetadataMissingException(missingMetadata);
		}
	}

	@Override
	protected void customJobOrder(Job<L0Slice> job) {
		// Rewrite job order sensing time
		DateTimeFormatter formatterJobOrder = DateTimeFormatter.ofPattern(JobOrderSensingTime.DATE_FORMAT);
		DateTimeFormatter formatterProduct = SearchMetadata.DATE_FORMATTER;
		LocalDateTime startDate = LocalDateTime.parse(job.getProduct().getObject().getStartDateFromMetadata(),
				formatterProduct);
		String jobOrderStart = startDate.format(formatterJobOrder);
		LocalDateTime stopDate = LocalDateTime.parse(job.getProduct().getObject().getStopDateFromMetadata(),
				formatterProduct);
		String jobOrderStop = stopDate.format(formatterJobOrder);
		job.getJobOrder().getConf().setSensingTime(new JobOrderSensingTime(jobOrderStart, jobOrderStop));

		this.updateProcParam(job.getJobOrder(), "Mission_Id",
				job.getProduct().getMissionId() + job.getProduct().getSatelliteId());
		this.updateProcParam(job.getJobOrder(), "Slice_Number", "" + job.getProduct().getObject().getNumberSlice());
		this.updateProcParam(job.getJobOrder(), "Total_Number_Of_Slices",
				"" + job.getProduct().getObject().getTotalNumberOfSlice());
		this.updateProcParam(job.getJobOrder(), "Slice_Overlap",
				"" + jobGeneratorSettings.getTypeOverlap().get(job.getProduct().getObject().getAcquisition()));
		this.updateProcParam(job.getJobOrder(), "Slice_Length",
				"" + jobGeneratorSettings.getTypeSliceLength().get(job.getProduct().getObject().getAcquisition()));
		this.updateProcParam(job.getJobOrder(), "Slicing_Flag", "TRUE");
	}

	private void updateProcParam(JobOrder jobOrder, String name, String newValue) {
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

	@Override
	protected void customJobDto(Job<L0Slice> job, JobDto dto) {
		// NOTHING TO DO

	}

}
