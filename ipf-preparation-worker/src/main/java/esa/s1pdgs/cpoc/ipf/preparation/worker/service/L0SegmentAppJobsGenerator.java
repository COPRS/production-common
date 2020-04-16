package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGeneration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * Customization of the job generator for L0 slice products
 * 
 * @author Cyrielle Gailliard
 */
public class L0SegmentAppJobsGenerator extends AbstractJobsGenerator {
	
    public L0SegmentAppJobsGenerator(
    		final XmlConverter xmlConverter,
            final MetadataClient metadataClient,
            final ProcessSettings l0ProcessSettings,
            final IpfPreparationWorkerSettings taskTablesSettings,
            final AppCatalogJobClient<CatalogEvent> appDataService,
            final ProcessConfiguration processConfiguration,
            final MqiClient mqiClient,
            final InputTimeoutChecker timeoutChecker,
			final String taskTableXmlName,
			final TaskTable taskTable,
			final ProductMode mode
    ) {
        super(
        		xmlConverter,
        		metadataClient, 
        		l0ProcessSettings,
                taskTablesSettings, 
                appDataService, 
                processConfiguration,
                mqiClient,
                timeoutChecker,
        		taskTableXmlName,
        		taskTable,
        		mode    
        );
    }

    @Override
    protected void preSearch(final JobGeneration job)
            throws IpfPrepWorkerInputsMissingException {
        boolean fullCoverage = false;

        // Retrieve the segments
        final Map<String, String> missingMetadata = new HashMap<>();
        final List<String> pols = new ArrayList<>();
        final Map<String, List<LevelSegmentMetadata>> segmentsGroupByPol =
                new HashMap<>();
        String lastName = "";
        try {
        	@SuppressWarnings("unchecked")
			final AppDataJob<CatalogEvent> appDataJob = job.getAppDataJob();

            for (final GenericMessageDto<CatalogEvent> message : new ArrayList<>(appDataJob.getMessages())) {
                final CatalogEvent dto = message.getBody();
                lastName = dto.getKeyObjectStorage();
                final LevelSegmentMetadata metadata = metadataClient
                        .getLevelSegment(dto.getProductFamily(), dto.getKeyObjectStorage());
                if (metadata == null) {
                	LOGGER.debug("== preSearch: metadata is null for {}",dto.getKeyObjectStorage());
                    missingMetadata.put(dto.getKeyObjectStorage(), "Missing segment");
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
        } catch (final MetadataQueryException e) {
        	LOGGER.debug("== preSearch: Exception- Missing segment for lastname{}",lastName);
            missingMetadata.put(lastName, "Missing segment: " + e.getMessage());
        }

        // If missing one segment
        if (!missingMetadata.isEmpty()) {
        	LOGGER.debug("== preSearch: Missing other segment for lastname{}",lastName);
            throw new IpfPrepWorkerInputsMissingException(missingMetadata);
        }

        LOGGER.debug("== preSearch00 -segment  {}",segmentsGroupByPol);
        // Check polarisation right
        String sensingStart = null;
        String sensingStop = null;
        if (pols.size() <= 0 || pols.size() > 2) {
            missingMetadata.put(
                    job.getAppDataJob().getProduct().getProductName(),
                    "Invalid number of polarisation " + pols.size());
        } else if (pols.size() == 1) {
            // Sort segments
            final String polA = pols.get(0);
            final List<LevelSegmentMetadata> segmentsA = segmentsGroupByPol.get(polA);
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
                                    + extractProductSensingConsolidation(segmentsA));
                }
            } else {
                fullCoverage = false;
                missingMetadata.put(
                        job.getAppDataJob().getProduct().getProductName(),
                        "Missing the other polarisation of " + polA);
            }
            // Get sensing start and stop
            sensingStart = getStartSensingDate(segmentsA,
                    AppDataJobProduct.TIME_FORMATTER);
            sensingStop = getStopSensingDate(segmentsA,
                    AppDataJobProduct.TIME_FORMATTER);

        } else {
            final String polA = pols.get(0);
            final String polB = pols.get(1);
            // Sort segments
            final List<LevelSegmentMetadata> segmentsA = segmentsGroupByPol.get(polA);
            final List<LevelSegmentMetadata> segmentsB = segmentsGroupByPol.get(polB);
            // Check coverage ok
            if (isDoublePolarisation(polA, polB)) {
                final boolean fullCoverageA;
                sortSegmentsPerStartDate(segmentsA);
                if (isCovered(segmentsA)) {
                    fullCoverageA = true;
                } else {
                    fullCoverageA = false;
                    missingMetadata.put(
                            job.getAppDataJob().getProduct().getProductName(),
                            "Missing segments for the coverage of polarisation "
                                    + polA + ": "
                                    + extractProductSensingConsolidation(segmentsA));
                }
                final boolean fullCoverageB;
                sortSegmentsPerStartDate(segmentsB);
                if (isCovered(segmentsB)) {
                    fullCoverageB = true;
                } else {
                    fullCoverageB = false;
                    missingMetadata.put(
                            job.getAppDataJob().getProduct().getProductName(),
                            "Missing segments for the coverage of polarisation "
                                    + polB + ": "
                                    + extractProductSensingConsolidation(segmentsB));
                }
                fullCoverage = fullCoverageA && fullCoverageB;
            } else {
                fullCoverage = false;
                missingMetadata.put(
                        job.getAppDataJob().getProduct().getProductName(),
                        "Invalid double polarisation " + polA + " - " + polB);
            }
            // Get sensing start and stop
            final DateTimeFormatter formatter = AppDataJobProduct.TIME_FORMATTER;
            sensingStart = least(getStartSensingDate(segmentsA, formatter),
                    getStartSensingDate(segmentsB, formatter), formatter);
            sensingStop = more(getStopSensingDate(segmentsA, formatter),
                    getStopSensingDate(segmentsB, formatter), formatter);
        }

        // Check if we add the coverage
        if (!fullCoverage) {
            final Date currentDate = new Date();
            if (job.getGeneration().getCreationDate()
                    .getTime() < currentDate.getTime() - ipfPreparationWorkerSettings
                            .getWaitprimarycheck().getMaxTimelifeS() * 1000) {
                LOGGER.warn("Continue generation of {} {} even if sensing gaps",
                        job.getAppDataJob().getProduct().getProductName(),
                        job.getGeneration());
                job.getAppDataJob().getProduct().setStartTime(sensingStart);
                job.getAppDataJob().getProduct().setStopTime(sensingStop);
            } else {
                throw new IpfPrepWorkerInputsMissingException(missingMetadata);
            }
        } else {
            job.getAppDataJob().getProduct().setStartTime(sensingStart);
            job.getAppDataJob().getProduct().setStopTime(sensingStop);
        }
        LOGGER.debug("== preSearch: performed lastName: {},fullCoverage= {} ",lastName,fullCoverage);
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
        for (final JobOrderProcParam param : jobOrder.getConf().getProcParams()) {
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
            final IpfExecutionJob dto) {
        // NOTHING TO DO

    }

    protected void sortSegmentsPerStartDate(final List<LevelSegmentMetadata> list) {
        list.sort((final LevelSegmentMetadata s1, final LevelSegmentMetadata s2) -> {
            final LocalDateTime startDate1 = LocalDateTime
                    .parse(s1.getValidityStart(), AbstractMetadata.METADATA_DATE_FORMATTER);
            final LocalDateTime startDate2 = LocalDateTime
                    .parse(s2.getValidityStart(), AbstractMetadata.METADATA_DATE_FORMATTER);
            return startDate1.compareTo(startDate2);
        });
    }

    protected boolean isSinglePolarisation(final String polA) {
        return "SH".equals(polA) || "SV".equals(polA);
    }

    protected boolean isDoublePolarisation(final String polA, final String polB) {
        if (("VH".equals(polA) && "VV".equals(polB))
                || ("VV".equals(polA) && "VH".equals(polB))) {
            return true;
        } else return ("HH".equals(polA) && "HV".equals(polB))
                || ("HV".equals(polA) && "HH".equals(polB));
    }

    protected boolean isCovered(final List<LevelSegmentMetadata> sortedSegments) {
        if (CollectionUtils.isEmpty(sortedSegments)) {
            return false;
        } else if (sortedSegments.size() == 1) {
            return "FULL".equals(sortedSegments.get(0).getConsolidation());
        } else {
            // Check consolidation first
        	//S1PRO-1135 BEGIN instead of START
            if ("BEGIN".equals(sortedSegments.get(0).getProductSensingConsolidation())
                    && "END".equals(
                            sortedSegments.get(sortedSegments.size() - 1)
                                    .getProductSensingConsolidation())) { //S1PRO-1333
                LocalDateTime previousStopDate = LocalDateTime.parse(
                        sortedSegments.get(0).getValidityStop(),
                        AbstractMetadata.METADATA_DATE_FORMATTER);
                for (final LevelSegmentMetadata segment : sortedSegments.subList(1,
                        sortedSegments.size())) {
                    final LocalDateTime startDate = LocalDateTime.parse(
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
            final List<LevelSegmentMetadata> sortedSegments,
            final DateTimeFormatter outFormatter) {
        if (CollectionUtils.isEmpty(sortedSegments)) {
            return null;
        }
        final LevelSegmentMetadata segment = sortedSegments.get(0);
        return DateUtils.convertToAnotherFormat(segment.getValidityStart(),
        		AbstractMetadata.METADATA_DATE_FORMATTER, outFormatter);
    }

    protected String getStopSensingDate(
            final List<LevelSegmentMetadata> sortedSegments,
            final DateTimeFormatter outFormatter) {
        if (CollectionUtils.isEmpty(sortedSegments)) {
            return null;
        }
        final LevelSegmentMetadata segment =
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
    protected String least(final String a, final String b, final DateTimeFormatter formatter) {
        final LocalDateTime timeA = LocalDateTime.parse(a, formatter);
        final LocalDateTime timeB = LocalDateTime.parse(b, formatter);
        return timeA.isBefore(timeB) ? a : b;
    }

    /**
     * TODO: move in common lib
     * 
     * @param a
     * @param b
     * @return
     */
    protected String more(final String a, final String b, final DateTimeFormatter formatter) {
        final LocalDateTime timeA = LocalDateTime.parse(a, formatter);
        final LocalDateTime timeB = LocalDateTime.parse(b, formatter);
        return timeA.isAfter(timeB) ? a : b;
    }

    protected String extractProductSensingConsolidation(
            final List<LevelSegmentMetadata> sortedSegments) {
        StringBuilder ret = new StringBuilder();
        for (final LevelSegmentMetadata segment : sortedSegments) {
            ret.append(segment.getProductSensingConsolidation());
            ret.append(" ");
            ret.append(segment.getValidityStart());
            ret.append(" ");
            ret.append(segment.getValidityStop());
            ret.append(" | ");
        }
        return ret.toString();
    }

}

class MinimalL0SegmentComparable {

}
