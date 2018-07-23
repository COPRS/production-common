package fr.viveris.s1pdgs.jobgenerator.tasks.generator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFileRaw;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderProcParam;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.EdrsSessionMetadata;
import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;
import fr.viveris.s1pdgs.jobgenerator.service.metadata.MetadataService;
import fr.viveris.s1pdgs.jobgenerator.service.mqi.OutputProcuderFactory;

public class EdrsSessionJobsGenerator
        extends AbstractJobsGenerator<EdrsSession> {

    public EdrsSessionJobsGenerator(XmlConverter xmlConverter,
            MetadataService metadataService, ProcessSettings l0ProcessSettings,
            JobGeneratorSettings taskTablesSettings,
            final OutputProcuderFactory outputFactory) {
        super(xmlConverter, metadataService, l0ProcessSettings,
                taskTablesSettings, outputFactory);
    }

    @Override
    protected void preSearch(Job<EdrsSession> job)
            throws JobGenInputsMissingException {
        Map<String, String> missingRaws = new HashMap<>();
        // Channel 1
        if (job.getProduct().getObject().getChannel1() != null && job
                .getProduct().getObject().getChannel1().getRawNames() != null) {
            job.getProduct().getObject().getChannel1().getRawNames()
                    .forEach(raw -> {
                        try {
                            EdrsSessionMetadata file = this.metadataService
                                    .getEdrsSession("RAW", raw.getFileName());
                            if (file != null) {
                                raw.setObjectStorageKey(
                                        file.getKeyObjectStorage());
                            } else {
                                missingRaws.put(raw.getFileName(),
                                        "No raw with name");
                            }
                        } catch (JobGenMetadataException me) {
                            missingRaws.put(raw.getFileName(), me.getMessage());
                        }
                    });
        }
        // Channel 2
        if (job.getProduct().getObject().getChannel2() != null && job
                .getProduct().getObject().getChannel2().getRawNames() != null) {
            job.getProduct().getObject().getChannel2().getRawNames()
                    .forEach(raw -> {
                        try {
                            EdrsSessionMetadata file = this.metadataService
                                    .getEdrsSession("RAW", raw.getFileName());
                            if (file != null) {
                                raw.setObjectStorageKey(
                                        file.getKeyObjectStorage());
                            } else {
                                missingRaws.put(raw.getFileName(),
                                        "No raw with name");
                            }
                        } catch (JobGenMetadataException me) {
                            missingRaws.put(raw.getFileName(), me.getMessage());
                        }
                    });
        }
        if (!missingRaws.isEmpty()) {
            throw new JobGenInputsMissingException(missingRaws);
        }
    }

    @Override
    protected void customJobOrder(Job<EdrsSession> job) {
        // Add/Update mission Id
        boolean update = false;
        if (job.getJobOrder().getConf().getProcParams() != null) {
            for (JobOrderProcParam param : job.getJobOrder().getConf()
                    .getProcParams()) {
                if ("Mission_Id".equals(param.getName())) {
                    param.setValue(job.getProduct().getMissionId()
                            + job.getProduct().getSatelliteId());
                    update = true;
                }
            }
        }
        if (!update) {
            job.getJobOrder().getConf()
                    .addProcParam(new JobOrderProcParam("Mission_Id",
                            job.getProduct().getMissionId()
                                    + job.getProduct().getSatelliteId()));
        }

    }

    @Override
    protected void customJobDto(Job<EdrsSession> job, LevelJobDto dto) {
        // Add input relative to the channels
        if (job.getProduct() != null) {
            int nb1 = 0;
            int nb2 = 0;

            // Retrieve number of channels and sort them per alphabetic order
            if (job.getProduct().getObject().getChannel1() != null
                    && !CollectionUtils.isEmpty(job.getProduct().getObject()
                            .getChannel1().getRawNames())) {
                nb1 = job.getProduct().getObject().getChannel1().getRawNames()
                        .size();
                // sort by alphabetic order
                job.getProduct().getObject().getChannel1().getRawNames()
                        .stream().sorted((p1, p2) -> p1.getFileName()
                                .compareTo(p2.getFileName()));
            }
            if (job.getProduct().getObject().getChannel2() != null
                    && !CollectionUtils.isEmpty(job.getProduct().getObject()
                            .getChannel2().getRawNames())) {
                nb2 = job.getProduct().getObject().getChannel2().getRawNames()
                        .size();
                // sort by alphabetic order
                job.getProduct().getObject().getChannel2().getRawNames()
                        .stream().sorted((p1, p2) -> p1.getFileName()
                                .compareTo(p2.getFileName()));
            }

            // Add raw to the job order, one file per channel
            int nb = Math.max(nb1, nb2);
            for (int i = 0; i < nb; i++) {
                if (i < nb1) {
                    EdrsSessionFileRaw raw = job.getProduct().getObject()
                            .getChannel1().getRawNames().get(i);
                    dto.addInput(new LevelJobInputDto(
                            ProductFamily.EDRS_SESSION.name(),
                            dto.getWorkDirectory() + "ch01/"
                                    + raw.getFileName(),
                            raw.getObjectStorageKey()));
                }
                if (i < nb2) {
                    EdrsSessionFileRaw raw = job.getProduct().getObject()
                            .getChannel2().getRawNames().get(i);
                    dto.addInput(new LevelJobInputDto(
                            ProductFamily.EDRS_SESSION.name(),
                            dto.getWorkDirectory() + "ch02/"
                                    + raw.getFileName(),
                            raw.getObjectStorageKey()));
                }
            }
        }
    }

}
