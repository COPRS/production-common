package esa.s1pdgs.cpoc.appcatalog.common.rest.model.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AppDataJobGenerationDtoTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        AppDataJobGenerationDto obj = new AppDataJobGenerationDto();

        // check default constructor
        assertEquals(AppDataJobGenerationDtoState.INITIAL, obj.getState());
        assertNull(obj.getCreationDate());
        assertNull(obj.getLastUpdateDate());

        obj.setTaskTable("task-table-1");
        obj.setState(AppDataJobGenerationDtoState.INITIAL);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());

        // check setters
        assertEquals(AppDataJobGenerationDtoState.INITIAL, obj.getState());
        assertNotNull(obj.getCreationDate());
        assertNotNull(obj.getLastUpdateDate());
        assertEquals("task-table-1", obj.getTaskTable());

        // check toString
        String str = obj.toString();
        assertTrue(str.contains("state: INITIAL"));
        assertTrue(str.contains("taskTable: task-table-1"));
        assertTrue(str.contains("creationDate: "));
        assertTrue(str.contains("lastUpdateDate: "));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJobGenerationDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
