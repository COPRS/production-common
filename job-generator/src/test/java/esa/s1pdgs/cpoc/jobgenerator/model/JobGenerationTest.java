package esa.s1pdgs.cpoc.jobgenerator.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadataQuery;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadataResult;
import esa.s1pdgs.cpoc.jobgenerator.utils.TestL0Utils;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object Job
 * 
 * @author Cyrielle Gailliard
 */
public class JobGenerationTest {
    
    /**
     * 
     */
    public void testUpdateDataJob() {
        
    }

    /**
     * Test toString
     */
    @Test
    public void testToString() {
        AppDataJobDto appDataJob = TestL0Utils.buildAppDataEdrsSession(true);
        AppDataJobGenerationDto gen1 = new AppDataJobGenerationDto();
        gen1.setTaskTable("TaskTable.AIOP.1.xml");
        gen1.setState(AppDataJobGenerationDtoState.INITIAL);
        AppDataJobGenerationDto gen2 = new AppDataJobGenerationDto();
        gen2.setTaskTable("TaskTable.AIOP.2.xml");
        gen2.setState(AppDataJobGenerationDtoState.READY);
        AppDataJobGenerationDto gen3 = new AppDataJobGenerationDto();
        gen3.setTaskTable("TaskTable.AIOP.3.xml");
        gen3.setState(AppDataJobGenerationDtoState.PRIMARY_CHECK);
        appDataJob.setGenerations(Arrays.asList(gen1, gen2, gen3));

        JobGeneration job = new JobGeneration(appDataJob, "TaskTable.AIOP.2.xml");
        assertEquals(appDataJob, job.getAppDataJob());
        assertTrue(job.getMetadataQueries().size() == 0);
        assertEquals(gen2, job.getGeneration());

        JobOrder order = new JobOrder();
        SearchMetadataQuery query1 = new SearchMetadataQuery();
        query1.setIdentifier(2);
        SearchMetadataQuery query2 = new SearchMetadataQuery();
        query2.setIdentifier(15);
        SearchMetadataResult result1 = new SearchMetadataResult(query1);
        SearchMetadataResult result2 = new SearchMetadataResult(query2);
        Map<Integer, SearchMetadataResult> metadata = new HashMap<>();
        metadata.put(2, result1);
        metadata.put(15, result2);

        job.setJobOrder(order);
        job.setMetadataQueries(metadata);

        String str = job.toString();
        assertTrue(str.contains("appDataJob: " + appDataJob.toString()));
        assertTrue(str.contains("jobOrder: " + order.toString()));
        assertTrue(str.contains("generation: " + gen2.toString()));
        assertTrue(str.contains("metadataQueries: " + metadata.toString()));

        job = new JobGeneration(appDataJob, "TaskTable.AIOP.3.xml");
        assertEquals(gen3, job.getGeneration());

        appDataJob.setGenerations(new ArrayList<>());
        job.updateAppDataJob(appDataJob, "TaskTable.AIOP.3.xml");
        assertNotEquals(gen3, job.getGeneration());
        assertEquals("TaskTable.AIOP.3.xml", job.getGeneration().getTaskTable());
        assertEquals(AppDataJobGenerationDtoState.INITIAL, job.getGeneration().getState());
        
        job = new JobGeneration(appDataJob, "TaskTable.AIOP.3.xml");
        assertEquals("TaskTable.AIOP.3.xml",
                job.getGeneration().getTaskTable());
        assertEquals(AppDataJobGenerationDtoState.INITIAL,
                job.getGeneration().getState());
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void equalsDto() {
        EqualsVerifier.forClass(JobGeneration.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
