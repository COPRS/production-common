package esa.s1pdgs.cpoc.jobgenerator.tasks.generator;

import java.util.HashMap;
import java.util.Map;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobFileDto;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.JobGeneration;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.metadata.MetadataService;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;

public class EdrsSessionsJobsGenerator
        extends AbstractJobsGenerator<EdrsSessionDto> {

    public EdrsSessionsJobsGenerator(XmlConverter xmlConverter,
            MetadataService metadataService, ProcessSettings l0ProcessSettings,
            JobGeneratorSettings taskTablesSettings,
            final OutputProducerFactory outputFactory,
            final AbstractAppCatalogJobService<EdrsSessionDto> appDataService) {
        super(xmlConverter, metadataService, l0ProcessSettings,
                taskTablesSettings, outputFactory, appDataService);
    }

    @Override
    protected void preSearch(JobGeneration<EdrsSessionDto> job)
            throws JobGenInputsMissingException {
        Map<String, String> missingRaws = new HashMap<>();
        if (job.getAppDataJob() != null
                && job.getAppDataJob().getProduct() != null) {
            // Channel 1
            job.getAppDataJob().getProduct().getRaws1().forEach(raw -> {
                try {
                    EdrsSessionMetadata file = this.metadataService
                            .getEdrsSession("RAW", raw.getFilename());
                    if (file != null) {
                        raw.setKeyObs(file.getKeyObjectStorage());
                    } else {
                        missingRaws.put(raw.getFilename(), "No raw with name");
                    }
                } catch (JobGenMetadataException me) {
                    missingRaws.put(raw.getFilename(), me.getMessage());
                }
            });
            // Channel 2
            job.getAppDataJob().getProduct().getRaws2().forEach(raw -> {
                try {
                    EdrsSessionMetadata file = this.metadataService
                            .getEdrsSession("RAW", raw.getFilename());
                    if (file != null) {
                        raw.setKeyObs(file.getKeyObjectStorage());
                    } else {
                        missingRaws.put(raw.getFilename(), "No raw with name");
                    }
                } catch (JobGenMetadataException me) {
                    missingRaws.put(raw.getFilename(), me.getMessage());
                }
            });
        }
        if (!missingRaws.isEmpty()) {
            throw new JobGenInputsMissingException(missingRaws);
        }
    }

    @Override
    protected void customJobOrder(JobGeneration<EdrsSessionDto> job) {
        // Add/Update mission Id
        boolean update = false;
        if (job.getJobOrder().getConf().getProcParams() != null) {
            for (JobOrderProcParam param : job.getJobOrder().getConf()
                    .getProcParams()) {
                if ("Mission_Id".equals(param.getName())) {
                    param.setValue(
                            job.getAppDataJob().getProduct().getMissionId()
                                    + job.getAppDataJob().getProduct()
                                            .getSatelliteId());
                    update = true;
                }
            }
        }
        if (!update) {
            job.getJobOrder().getConf()
                    .addProcParam(new JobOrderProcParam("Mission_Id",
                            job.getAppDataJob().getProduct().getMissionId()
                                    + job.getAppDataJob().getProduct()
                                            .getSatelliteId()));
        }

    }

    @Override
    protected void customJobDto(JobGeneration<EdrsSessionDto> job,
            LevelJobDto dto) {
        // Add input relative to the channels
        if (job.getAppDataJob().getProduct() != null) {
            int nb1 = 0;
            int nb2 = 0;

            // Retrieve number of channels and sort them per alphabetic order
            nb1 = job.getAppDataJob().getProduct().getRaws1().size();
            job.getAppDataJob().getProduct().getRaws1().stream().sorted(
                    (p1, p2) -> p1.getFilename().compareTo(p2.getFilename()));

            nb2 = job.getAppDataJob().getProduct().getRaws2().size();
            job.getAppDataJob().getProduct().getRaws2().stream().sorted(
                    (p1, p2) -> p1.getFilename().compareTo(p2.getFilename()));

            // Add raw to the job order, one file per channel
            int nb = Math.max(nb1, nb2);
            for (int i = 0; i < nb; i++) {
                if (i < nb1) {
                    AppDataJobFileDto raw =
                            job.getAppDataJob().getProduct().getRaws1().get(i);
                    dto.addInput(
                            new LevelJobInputDto(
                                    ProductFamily.EDRS_SESSION.name(),
                                    dto.getWorkDirectory() + "ch01/"
                                            + raw.getFilename(),
                                    raw.getKeyObs()));
                }
                if (i < nb2) {
                    AppDataJobFileDto raw =
                            job.getAppDataJob().getProduct().getRaws2().get(i);
                    dto.addInput(
                            new LevelJobInputDto(
                                    ProductFamily.EDRS_SESSION.name(),
                                    dto.getWorkDirectory() + "ch02/"
                                            + raw.getFilename(),
                                    raw.getKeyObs()));
                }
            }
        }
    }

}
