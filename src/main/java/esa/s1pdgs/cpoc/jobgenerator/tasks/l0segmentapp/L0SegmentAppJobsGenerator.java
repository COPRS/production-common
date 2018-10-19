package esa.s1pdgs.cpoc.jobgenerator.tasks.l0segmentapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.JobGeneration;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadata;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.metadata.MetadataService;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsGenerator;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * Customization of the job generator for L0 slice products
 * 
 * @author Cyrielle Gailliard
 */
public class L0SegmentAppJobsGenerator
        extends AbstractJobsGenerator<LevelSegmentDto> {

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
            final AbstractAppCatalogJobService<LevelSegmentDto> appDataService) {
        super(xmlConverter, metadataService, l0ProcessSettings,
                taskTablesSettings, outputFactory, appDataService);
    }

    /**
     * Check the product and retrieve usefull information before searching
     * inputs
     */
    @Override
    protected void preSearch(final JobGeneration<LevelSegmentDto> job)
            throws JobGenInputsMissingException {
        boolean fullCoverage = false;

        // Retrieve the segments
        Map<String, String> missingMetadata = new HashMap<>();
        List<String> pols = new ArrayList<>();
        Map<String, List<LevelSegmentMetadata>> segmentsGroupByPol =
                new HashMap<>();
        String lastName = "";
        try {
            for (GenericMessageDto<LevelSegmentDto> message : job
                    .getAppDataJob().getMessages()) {
                LevelSegmentDto dto = message.getBody();
                lastName = dto.getName();
                LevelSegmentMetadata metadata = metadataService
                        .getLevelSegment(dto.getFamily(), dto.getName());
                if (metadata == null) {
                    missingMetadata.put(dto.getName(), "Missing segment");
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
            missingMetadata.put(
                    lastName,
                    "Missing segment: " + e.getMessage());
        }

        // If missing one segment
        if (!missingMetadata.isEmpty()) {
            throw new JobGenInputsMissingException(missingMetadata);
        }

        // Check polarisation right
        Date sensingStart = null;
        Date sensingStop = null;
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
            try {
                sensingStart = getStartSensingDate(segmentsA);
                sensingStop = getStopSensingDate(segmentsA);
            } catch (InternalErrorException e) {
                fullCoverage = false;
                missingMetadata.put(
                        job.getAppDataJob().getProduct().getProductName(),
                        "Cannot get sensing period: " + e.getMessage());
            }

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
            try {
                sensingStart = least(getStartSensingDate(segmentsA),
                        getStartSensingDate(segmentsB));
                sensingStop = more(getStopSensingDate(segmentsA),
                        getStopSensingDate(segmentsB));
            } catch (InternalErrorException e) {
                fullCoverage = false;
                missingMetadata.put(
                        job.getAppDataJob().getProduct().getProductName(),
                        "Cannot get sensing period: " + e.getMessage());
            }
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
    protected void customJobOrder(final JobGeneration<LevelSegmentDto> job) {
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
    protected void customJobDto(final JobGeneration<LevelSegmentDto> job,
            final LevelJobDto dto) {
        // NOTHING TO DO

    }

    protected void sortSegmentsPerStartDate(List<LevelSegmentMetadata> list) {
        list.sort((LevelSegmentMetadata s1, LevelSegmentMetadata s2) -> {
            DateTimeFormatter formatterProduct = SearchMetadata.DATE_FORMATTER;
            LocalDateTime startDate1 = LocalDateTime
                    .parse(s1.getValidityStart(), formatterProduct);
            LocalDateTime startDate2 = LocalDateTime
                    .parse(s2.getValidityStart(), formatterProduct);
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
            if ("BEGIN".equals(sortedSegments.get(0).getConsolidation())
                    && "END".equals(
                            sortedSegments.get(sortedSegments.size() - 1)
                                    .getConsolidation())) {
                DateTimeFormatter formatterProduct =
                        SearchMetadata.DATE_FORMATTER;
                LocalDateTime previousStopDate = LocalDateTime.parse(
                        sortedSegments.get(0).getValidityStop(),
                        formatterProduct);
                for (LevelSegmentMetadata segment : sortedSegments.subList(1,
                        sortedSegments.size())) {
                    LocalDateTime startDate = LocalDateTime.parse(
                            segment.getValidityStart(), formatterProduct);
                    if (startDate.isAfter(previousStopDate)) {
                        return false;
                    }
                    previousStopDate = LocalDateTime.parse(
                            segment.getValidityStop(), formatterProduct);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    protected Date getStartSensingDate(
            List<LevelSegmentMetadata> sortedSegments)
            throws InternalErrorException {
        if (CollectionUtils.isEmpty(sortedSegments)) {
            return null;
        }
        return DateUtils.convertWithSimpleDateFormat(
                sortedSegments.get(0).getValidityStart(),
                "yyyy-MM-dd'T'HH:mm:ss");
    }

    protected Date getStopSensingDate(List<LevelSegmentMetadata> sortedSegments)
            throws InternalErrorException {
        if (CollectionUtils.isEmpty(sortedSegments)) {
            return null;
        }
        return DateUtils.convertWithSimpleDateFormat(
                sortedSegments.get(sortedSegments.size() - 1).getValidityStop(),
                "yyyy-MM-dd'T'HH:mm:ss");
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

    /**
     * TODO: move in common lib
     * @param a
     * @param b
     * @return
     */
    protected Date least(Date a, Date b) {
        return a == null ? b : (b == null ? a : (a.before(b) ? a : b));
    }

    /**
     * TODO: move in common lib
     * @param a
     * @param b
     * @return
     */
    protected Date more(Date a, Date b) {
        return a == null ? b : (b == null ? a : (a.after(b) ? a : b));
    }

}

class MinimalL0SegmentComparable {

}
