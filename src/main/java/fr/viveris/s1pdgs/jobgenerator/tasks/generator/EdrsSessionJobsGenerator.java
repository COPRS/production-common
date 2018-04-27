package fr.viveris.s1pdgs.jobgenerator.tasks.generator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.controller.JobsProducer;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobInputDto;
import fr.viveris.s1pdgs.jobgenerator.exception.InputsMissingException;
import fr.viveris.s1pdgs.jobgenerator.exception.MetadataException;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFileRaw;
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
	protected void preSearch(Job<EdrsSession> job) throws InputsMissingException {
		Map<String, String> missingRaws = new HashMap<>();
		// Channel 1
		if (job.getProduct().getObject().getChannel1() != null
				&& job.getProduct().getObject().getChannel1().getRawNames() != null) {
			job.getProduct().getObject().getChannel1().getRawNames().forEach(raw -> {
				try {
					EdrsSessionMetadata file = this.metadataService.getEdrsSession("RAW", raw.getFileName());
					if (file != null) {
						raw.setObjectStorageKey(file.getKeyObjectStorage());
					} else {
						missingRaws.put(raw.getFileName(), "No raw with name");
					}
				} catch (MetadataException me) {
					missingRaws.put(raw.getFileName(), me.getMessage());
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
						missingRaws.put(raw.getFileName(), "No raw with name");
					}
				} catch (MetadataException me) {
					missingRaws.put(raw.getFileName(), me.getMessage());
				}
			});
		}
		if (!missingRaws.isEmpty()) {
			throw new InputsMissingException(missingRaws);
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
			int nb1 = 0;
			int nb2 = 0;
			
			// Retrieve number of channels and sort them per alphabetic order
			if (job.getProduct().getObject().getChannel1() != null
					&& !CollectionUtils.isEmpty(job.getProduct().getObject().getChannel1().getRawNames())) {
				nb1 = job.getProduct().getObject().getChannel1().getRawNames().size();
				// sort by alphabetic order
				job.getProduct().getObject().getChannel1().getRawNames().stream().sorted((p1, p2) -> p1.getFileName().compareTo(p2.getFileName()));
			}
			if (job.getProduct().getObject().getChannel2() != null
					&& !CollectionUtils.isEmpty(job.getProduct().getObject().getChannel2().getRawNames())) {
				nb2 = job.getProduct().getObject().getChannel2().getRawNames().size();
				// sort by alphabetic order
				job.getProduct().getObject().getChannel2().getRawNames().stream().sorted((p1, p2) -> p1.getFileName().compareTo(p2.getFileName()));
			}
			
			// Add raw to the job order, one file per channel
			int nb = Math.max(nb1, nb2);
			for (int i=0; i < nb; i++) {
				if (i < nb1) {
					EdrsSessionFileRaw raw = job.getProduct().getObject().getChannel1().getRawNames().get(i);
					dto.addInput(new JobInputDto(ProductFamily.RAW.name(),
								dto.getWorkDirectory() + "ch01/" + raw.getFileName(),
								raw.getObjectStorageKey()));
				}
				if (i < nb2) {
					EdrsSessionFileRaw raw = job.getProduct().getObject().getChannel2().getRawNames().get(i);
					dto.addInput(new JobInputDto(ProductFamily.RAW.name(),
								dto.getWorkDirectory() + "ch02/" + raw.getFileName(),
								raw.getObjectStorageKey()));
				}
			}
		}
	}

}
