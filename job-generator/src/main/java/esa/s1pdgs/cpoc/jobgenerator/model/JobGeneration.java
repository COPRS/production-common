package esa.s1pdgs.cpoc.jobgenerator.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadataResult;

/**
 * Describe a job. Used during generation to keep a status of each in progress
 * job.<br/>
 * This class is generic and depends on the processing level.
 * <li>L0: T is EdrsSession object</li>
 * <li>L1: T is L0Slice object</li>
 * 
 * @author Cyrielle Gailliard
 * @param <T>
 */
public class JobGeneration {

    /**
     * Applicative data job
     */
    private AppDataJobDto appDataJob;

    /**
     * Job generation
     */
    private AppDataJobGenerationDto generation;

    /**
     * Job order to send to the wrapper (object used to map a job order in the
     * XML file)
     */
    private JobOrder jobOrder;

    /**
     * Distinct metadata queries needed for searching inputs of the task table
     */
    private Map<Integer, SearchMetadataResult> metadataQueries;

    /**
     * Constructor from product
     * 
     * @param identifier
     * @param startTime
     * @param stopTime
     * @param product
     */
    public JobGeneration(final AppDataJobDto appDataJob, final String taskTable) {
        this.appDataJob = appDataJob;
        this.metadataQueries = new HashMap<>();
        List<AppDataJobGenerationDto> generations = appDataJob.getGenerations();
        if (CollectionUtils.isEmpty(generations)) {
            generation = new AppDataJobGenerationDto();
            generation.setTaskTable(taskTable);
        } else {
            for (AppDataJobGenerationDto jobGen : generations) {
                if (taskTable.equals(jobGen.getTaskTable())) {
                    generation = jobGen;
                    break;
                }
            }
        }
    }
    
    public void updateAppDataJob(final AppDataJobDto appDataJob, final String taskTable) {
        this.appDataJob = appDataJob;
        List<AppDataJobGenerationDto> generations = appDataJob.getGenerations();
        if (CollectionUtils.isEmpty(generations)) {
            generation = new AppDataJobGenerationDto();
            generation.setTaskTable(taskTable);
        } else {
            for (AppDataJobGenerationDto jobGen : generations) {
                if (taskTable.equals(jobGen.getTaskTable())) {
                    generation = jobGen;
                    break;
                }
            }
        }
    }

    /**
     * @return the appDataJob
     */
    public AppDataJobDto getAppDataJob() {
        return appDataJob;
    }

    /**
     * @param appDataJob
     *            the appDataJob to set
     */
    public void setAppDataJob(final AppDataJobDto appDataJob) {
        this.appDataJob = appDataJob;
    }

    /**
     * @return the generation
     */
    public AppDataJobGenerationDto getGeneration() {
        return generation;
    }

    /**
     * @param generation
     *            the generation to set
     */
    public void setGeneration(final AppDataJobGenerationDto generation) {
        this.generation = generation;
    }

    /**
     * @return the jobOrder
     */
    public JobOrder getJobOrder() {
        return jobOrder;
    }

    /**
     * @param jobOrder
     *            the jobOrder to set
     */
    public void setJobOrder(final JobOrder jobOrder) {
        this.jobOrder = jobOrder;
    }

    /**
     * @return the metadataQueries
     */
    public Map<Integer, SearchMetadataResult> getMetadataQueries() {
        return metadataQueries;
    }

    /**
     * @param metadataQueries
     *            the metadataQueries to set
     */
    public void setMetadataQueries(
            final Map<Integer, SearchMetadataResult> metadataQueries) {
        this.metadataQueries = metadataQueries;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{appDataJob: %s, generation: %s, jobOrder: %s, metadataQueries: %s}",
                appDataJob, generation, jobOrder, metadataQueries);
    }

    /**
     * hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(appDataJob, generation, jobOrder, metadataQueries);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            JobGeneration other = (JobGeneration) obj;
            ret = Objects.equals(appDataJob, other.appDataJob)
                    && Objects.equals(generation, other.generation)
                    && Objects.equals(jobOrder, other.jobOrder)
                    && Objects.equals(metadataQueries, other.metadataQueries);
        }
        return ret;
    }
}
