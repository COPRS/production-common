package fr.viveris.s1pdgs.jobgenerator.tasks.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.controller.JobsProducer;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobInputDto;
import fr.viveris.s1pdgs.jobgenerator.exception.MetadataException;
import fr.viveris.s1pdgs.jobgenerator.exception.MetadataMissingException;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.ProductFamily;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderProcParam;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.EdrsSessionMetadata;
import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;
import fr.viveris.s1pdgs.jobgenerator.service.metadata.MetadataService;

public class EdrsSessionJobsGenerator extends AbstractJobsGenerator<EdrsSession> {

	public EdrsSessionJobsGenerator(XmlConverter xmlConverter, MetadataService metadataService,
			ProcessSettings l0ProcessSettings, JobGeneratorSettings taskTablesSettings, JobsProducer kafkaJobsSender) {
		super(xmlConverter, metadataService, l0ProcessSettings, taskTablesSettings, kafkaJobsSender);
	}

	@Override
	protected void preSearch(Job<EdrsSession> job) throws MetadataMissingException {
		List<String> missingRaws = new ArrayList<>();
		// Channel 1
		if (job.getProduct().getObject().getChannel1() != null
				&& job.getProduct().getObject().getChannel1().getRawNames() != null) {
			job.getProduct().getObject().getChannel1().getRawNames().forEach(raw -> {
				try {
					EdrsSessionMetadata file = this.metadataService.getEdrsSession("RAW", raw.getFileName());
					if (file != null) {
						raw.setObjectStorageKey(file.getKeyObjectStorage());
					} else {
						missingRaws.add(String.format("[raw %s]", raw.getFileName()));
					}
				} catch (MetadataException me) {
					missingRaws.add(String.format("[raw %s] [error %s]", raw.getFileName(), me.getMessage()));
				}
			});
		}
		// Channel 2
		if (job.getProduct().getObject().getChannel2() != null
				&& job.getProduct().getObject().getChannel2().getRawNames() != null) {
			job.getProduct().getObject().getChannel2().getRawNames().forEach(raw -> {
				try {
					EdrsSessionMetadata file = this.metadataService.getEdrsSession("RAW", raw.getFileName());
					if (file != null) {
						raw.setObjectStorageKey(file.getKeyObjectStorage());
					} else {
						missingRaws.add(String.format("[raw %s]", raw.getFileName()));
					}
				} catch (MetadataException me) {
					missingRaws.add(String.format("[raw %s] [error %s]", raw.getFileName(), me.getMessage()));
				}
			});
		}
		if (!missingRaws.isEmpty()) {
			throw new MetadataMissingException(missingRaws);
		}
	}

	@Override
	protected void customJobOrder(Job<EdrsSession> job) {
		// Add/Update mission Id
		boolean update = false;
		if (job.getJobOrder().getConf().getProcParams() != null) {
			for (JobOrderProcParam param : job.getJobOrder().getConf().getProcParams()) {
				if ("Mission_Id".equals(param.getName())) {
					param.setValue(job.getProduct().getMissionId() + job.getProduct().getSatelliteId());
					update = true;
				}
			}
		}
		if (!update) {
			job.getJobOrder().getConf()
					.addProcParam(new JobOrderProcParam("Mission_Id", job.getProduct().getMissionId() + job.getProduct().getSatelliteId()));
		}

	}

	@Override
	protected void customJobDto(Job<EdrsSession> job, JobDto dto) {
		// Add input relative to the channels
		if (job.getProduct() != null) {
			if (job.getProduct().getObject().getChannel1() != null
					&& !CollectionUtils.isEmpty(job.getProduct().getObject().getChannel1().getRawNames())) {
				dto.addInputs(job.getProduct().getObject().getChannel1().getRawNames().stream()
						.map(rawNames -> new JobInputDto(ProductFamily.RAW.name(),
								dto.getWorkDirectory() + "ch01/" + rawNames.getFileName(),
								rawNames.getObjectStorageKey()))
						.collect(Collectors.toList()));
			}
			if (job.getProduct().getObject().getChannel2() != null
					&& !CollectionUtils.isEmpty(job.getProduct().getObject().getChannel2().getRawNames())) {
				dto.addInputs(job.getProduct().getObject().getChannel2().getRawNames().stream()
						.map(rawNames -> new JobInputDto(ProductFamily.RAW.name(),
								dto.getWorkDirectory() + "ch02/" + rawNames.getFileName(),
								rawNames.getObjectStorageKey()))
						.collect(Collectors.toList()));
			}
		}
	}

}
