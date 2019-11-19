package esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.enums.JobOrderDestination;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.enums.JobOrderFileNameType;

public class JobOrderEnumsTest {

    @Test
    public void testEnumJobOrderFileNameType() {
        assertEquals(4, JobOrderFileNameType.values().length);
        assertEquals(JobOrderFileNameType.PHYSICAL,
                JobOrderFileNameType.valueOf("PHYSICAL"));
        assertEquals(JobOrderFileNameType.DIRECTORY,
                JobOrderFileNameType.valueOf("DIRECTORY"));
        assertEquals(JobOrderFileNameType.REGEXP,
                JobOrderFileNameType.valueOf("REGEXP"));
        assertEquals(JobOrderFileNameType.BLANK,
                JobOrderFileNameType.valueOf("BLANK"));

        assertEquals("", JobOrderFileNameType.BLANK.getValue());
        assertEquals("Physical", JobOrderFileNameType.PHYSICAL.getValue());
        assertEquals("Directory", JobOrderFileNameType.DIRECTORY.getValue());
        assertEquals("Regexp", JobOrderFileNameType.REGEXP.getValue());
    }

    @Test
    public void testEnumJobOrderDestination() {
        assertEquals(2, JobOrderDestination.values().length);
        assertEquals(JobOrderDestination.DB, JobOrderDestination.valueOf("DB"));
        assertEquals(JobOrderDestination.PROC,
                JobOrderDestination.valueOf("PROC"));
    }
}
