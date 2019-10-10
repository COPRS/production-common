package esa.s1pdgs.cpoc.jobgenerator.tasks.l0segmentapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.JobGeneration;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.metadata.MetadataService;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsGenerator;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * Customization of the job generator for L0 slice products
 * 
 * @author Cyrielle Gailliard
 */
public class L0SegmentAppJobsGenerator extends AbstractJobsGenerator<ProductDto> {

    /**
     * @param xmlConverter
     * @param metadataService
     * @param l0ProcessSettings
     * @param taskTablesSettings
     * @param JobsSender
     */
    public L0SegmentAppJobsGenerator(final XmlConverter xmlConverter,
            final MetadataService metadataService,
            final ProcessSettings l0ProcessSettings,
            final JobGeneratorSettings taskTablesSettings,
            final OutputProducerFactory outputFactory,
            final AppCatalogJobClient appDataService) {
        super(xmlConverter, metadataService, l0ProcessSettings,
                taskTablesSettings, outputFactory, appDataService);
    }

    /**
     * Check the product and retrieve usefull information before searching
     * inputs
     */
    @Override
    protected void preSearch(final JobGeneration job)
            throws JobGenInputsMissingException {
        boolean fullCoverage = false;

        // Retrieve the segments
        Map<String, String> missingMetadata = new HashMap<>();
        List<String> pols = new ArrayList<>();
        Map<String, List<LevelSegmentMetadata>> segmentsGroupByPol =
                new HashMap<>();
        String lastName = "";
        try {
        	@SuppressWarnings("unchecked")
			final AppDataJobDto<ProductDto> appDataJob = job.getAppDataJob();
        	
            for (GenericMessageDto<ProductDto> message : appDataJob.getMessages()) {
                ProductDto dto = (ProductDto) message.getBody();
                lastName = dto.getProductName();
                LevelSegmentMetadata metadata = metadataService
                        .getLevelSegment(dto.getFamily(), dto.getProductName());
                if (metadata == null) {
                    missingMetadata.put(dto.getProductName(), "Missing segment");
                } else {
                    if (!segmentsGroupByPol
                            .containsKey(metadata.getPolarisation())) {
                        pols.add(metadata.getPolarisation());
                        segmentsGroupByPol.put(metadata.getPolarisation(),
                                new ArrayList<>());
                    }
                    segmentsGroupByPol.get(metadata.getPolarisation())
                            .add(metadata);
                }
            }
        } catch (JobGenMetadataException e) {
            missingMetadata.put(lastName, "Missing segment: " + e.getMessage());
        }

        // If missing one segment
        if (!missingMetadata.isEmpty()) {
            throw new JobGenInputsMissingException(missingMetadata);
        }

        // Check polarisation right
        String sensingStart = null;
        String sensingStop = null;
        if (pols.size() <= 0 || pols.size() > 2) {
            missingMetadata.put(
                    job.getAppDataJob().getProduct().getProductName(),
                    "Invalid number of polarisation " + pols.size());
        } else if (pols.size() == 1) {
            // Sort segments
            String polA = pols.get(0);
            List<LevelSegmentMetadata> segmentsA = segmentsGroupByPol.get(polA);
            // Check coverage ok
            if (isSinglePolarisation(polA)) {
                sortSegmentsPerStartDate(segmentsA);
                if (isCovered(segmentsA)) {
                    fullCoverage = true;
                } else {
                    fullCoverage = false;
                    missingMetadata.put(
                            job.getAppDataJob().getProduct().getProductName(),
                            "Missing segments for the coverage of polarisation "
                                    + polA + ": "
                                    + extractConsolidation(segmentsA));
                }
            } else {
                fullCoverage = false;
                missingMetadata.put(
                        job.getAppDataJob().getProduct().getProductName(),
                        "Missing the other polarisation of " + polA);
            }
            // Get sensing start and stop
            sensingStart = getStartSensingDate(segmentsA,
                    AppDataJobProductDto.TIME_FORMATTER);
            sensingStop = getStopSensingDate(segmentsA,
                    AppDataJobProductDto.TIME_FORMATTER);

        } else {
            String polA = pols.get(0);
            String polB = pols.get(1);
            // Sort segments
            List<LevelSegmentMetadata> segmentsA = segmentsGroupByPol.get(polA);
            List<LevelSegmentMetadata> segmentsB = segmentsGroupByPol.get(polB);
            // Check coverage ok
            if (isDoublePolarisation(polA, polB)) {
                boolean fullCoverageA = false;
                sortSegmentsPerStartDate(segmentsA);
                if (isCovered(segmentsA)) {
                    fullCoverageA = true;
                } else {
                    fullCoverageA = false;
                    missingMetadata.put(
                            job.getAppDataJob().getProduct().getProductName(),
                            "Missing segments for the coverage of polarisation "
                                    + polA + ": "
                                    + extractConsolidation(segmentsA));
                }
                boolean fullCoverageB = false;
                sortSegmentsPerStartDate(segmentsB);
                if (isCovered(segmentsB)) {
                    fullCoverageB = true;
                } else {
                    fullCoverageB = false;
                    missingMetadata.put(
                            job.getAppDataJob().getProduct().getProductName(),
                            "Missing segments for the coverage of polarisation "
                                    + polB + ": "
                                    + extractConsolidation(segmentsB));
                }
                fullCoverage = fullCoverageA && fullCoverageB;
            } else {
                fullCoverage = false;
                missingMetadata.put(
                        job.getAppDataJob().getProduct().getProductName(),
                        "Invalid double polarisation " + polA + " - " + polB);
            }
            // Get sensing start and stop
            DateTimeFormatter formatter = AppDataJobProductDto.TIME_FORMATTER;
            sensingStart = least(getStartSensingDate(segmentsA, formatter),
                    getStartSensingDate(segmentsB, formatter), formatter);
            sensingStop = more(getStopSensingDate(segmentsA, formatter),
                    getStopSensingDate(segmentsB, formatter), formatter);
        }

        // Check if we add the coverage
        if (!fullCoverage) {
            Date currentDate = new Date();
            if (job.getGeneration().getCreationDate()
                    .getTime() < currentDate.getTime() - jobGeneratorSettings
                            .getWaitprimarycheck().getMaxTimelifeS() * 1000) {
                LOGGER.warn("Continue generation of {} {} even if sensing gaps",
                        job.getAppDataJob().getProduct().getProductName(),
                        job.getGeneration());
                job.getAppDataJob().getProduct().setStartTime(sensingStart);
                job.getAppDataJob().getProduct().setStopTime(sensingStop);
            } else {
                throw new JobGenInputsMissingException(missingMetadata);
            }
        } else {
            job.getAppDataJob().getProduct().setStartTime(sensingStart);
            job.getAppDataJob().getProduct().setStopTime(sensingStop);
        }
    }

    /**
     * Custom job order before building the job DTO
     */
    @Override
    protected void customJobOrder(final JobGeneration job) {
        this.updateProcParam(job.getJobOrder(), "Mission_Id",
                job.getAppDataJob().getProduct().getMissionId()
                        + job.getAppDataJob().getProduct().getSatelliteId());
    }

    /**
     * Update or create a proc param in the job order
     * 
     * @param jobOrder
     * @param name
     * @param newValue
     */
    protected void updateProcParam(final JobOrder jobOrder, final String name,
            final String newValue) {
        boolean update = false;
        for (JobOrderProcParam param : jobOrder.getConf().getProcParams()) {
            if (name.equals(param.getName())) {
                param.setValue(newValue);
                update = true;
            }
        }
        if (!update) {
            jobOrder.getConf()
                    .addProcParam(new JobOrderProcParam(name, newValue));
        }
    }

    /**
     * Customisation of the job DTO before sending it
     */
    @Override
    protected void customJobDto(final JobGeneration job,
            final LevelJobDto dto) {
        // NOTHING TO DO

    }

    protected void sortSegmentsPerStartDate(List<LevelSegmentMetadata> list) {
        list.sort((LevelSegmentMetadata s1, LevelSegmentMetadata s2) -> {
            LocalDateTime startDate1 = LocalDateTime
                    .parse(s1.getValidityStart(), AbstractMetadata.METADATA_DATE_FORMATTER);
            LocalDateTime startDate2 = LocalDateTime
                    .parse(s2.getValidityStart(), AbstractMetadata.METADATA_DATE_FORMATTER);
            return startDate1.compareTo(startDate2);
        });
    }

    protected boolean isSinglePolarisation(String polA) {
        if ("SH".equals(polA) || "SV".equals(polA)) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isDoublePolarisation(String polA, String polB) {
        if (("VH".equals(polA) && "VV".equals(polB))
                || ("VV".equals(polA) && "VH".equals(polB))) {
            return true;
        } else if (("HH".equals(polA) && "HV".equals(polB))
                || ("HV".equals(polA) && "HH".equals(polB))) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isCovered(List<LevelSegmentMetadata> sortedSegments) {
        if (CollectionUtils.isEmpty(sortedSegments)) {
            return false;
        } else if (sortedSegments.size() == 1) {
            if ("FULL".equals(sortedSegments.get(0).getConsolidation())) {
                return true;
            } else {
                return false;
            }
        } else {
            // Check consolidation first
            if ("START".equals(sortedSegments.get(0).getConsolidation())
                    && "END".equals(
                            sortedSegments.get(sortedSegments.size() - 1)
                                    .getConsolidation())) {
                LocalDateTime previousStopDate = LocalDateTime.parse(
                        sortedSegments.get(0).getValidityStop(),
                        AbstractMetadata.METADATA_DATE_FORMATTER);
                for (LevelSegmentMetadata segment : sortedSegments.subList(1,
                        sortedSegments.size())) {
                    LocalDateTime startDate = LocalDateTime.parse(
                            segment.getValidityStart(), AbstractMetadata.METADATA_DATE_FORMATTER);
                    if (startDate.isAfter(previousStopDate)) {
                        return false;
                    }
                    previousStopDate = LocalDateTime
                            .parse(segment.getValidityStop(), AbstractMetadata.METADATA_DATE_FORMATTER);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    protected String getStartSensingDate(
            List<LevelSegmentMetadata> sortedSegments,
            DateTimeFormatter outFormatter) {
        if (CollectionUtils.isEmpty(sortedSegments)) {
            return null;
        }
        LevelSegmentMetadata segment = sortedSegments.get(0);
        return DateUtils.convertToAnotherFormat(segment.getValidityStart(),
        		AbstractMetadata.METADATA_DATE_FORMATTER, outFormatter);
    }

    protected String getStopSensingDate(
            List<LevelSegmentMetadata> sortedSegments,
            DateTimeFormatter outFormatter) {
        if (CollectionUtils.isEmpty(sortedSegments)) {
            return null;
        }
        LevelSegmentMetadata segment =
                sortedSegments.get(sortedSegments.size() - 1);
        return DateUtils.convertToAnotherFormat(segment.getValidityStop(),
        		AbstractMetadata.METADATA_DATE_FORMATTER, outFormatter);
    }

    /**
     * TODO: move in common lib
     * 
     * @param a
     * @param b
     * @return
     */
    protected String least(String a, String b, DateTimeFormatter formatter) {
        LocalDateTime timeA = LocalDateTime.parse(a, formatter);
        LocalDateTime timeB = LocalDateTime.parse(b, formatter);
        return timeA == null ? b
                : (b == null ? a : (timeA.isBefore(timeB) ? a : b));
    }

    /**
     * TODO: move in common lib
     * 
     * @param a
     * @param b
     * @return
     */
    protected String more(String a, String b, DateTimeFormatter formatter) {
        LocalDateTime timeA = LocalDateTime.parse(a, formatter);
        LocalDateTime timeB = LocalDateTime.parse(b, formatter);
        return timeA == null ? b
                : (b == null ? a : (timeA.isAfter(timeB) ? a : b));
    }

    protected String extractConsolidation(
            List<LevelSegmentMetadata> sortedSegments) {
        String ret = "";
        for (LevelSegmentMetadata segment : sortedSegments) {
            ret += segment.getConsolidation() + " " + segment.getValidityStart()
                    + " " + segment.getValidityStop() + " | ";
        }
        return ret;
    }

}

class MinimalL0SegmentComparable {

}
