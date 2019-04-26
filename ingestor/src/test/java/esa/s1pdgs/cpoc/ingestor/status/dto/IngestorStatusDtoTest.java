/**
 * 
 */
package esa.s1pdgs.cpoc.ingestor.status.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.AppState;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 *
 *
 * @author Viveris Technologies
 */
public class IngestorStatusDtoTest {

    /**
     * Test default cosntructor and getters
     */
    @Test
    public void testConstructor() {
        IngestorStatusDto dto =
                new IngestorStatusDto(AppState.PROCESSING, 123456, 8);
        assertEquals(AppState.PROCESSING, dto.getStatus());
        assertEquals(123456, dto.getTimeSinceLastChange());
        assertEquals(8, dto.getErrorCounter());
    }

    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSetters() {
        IngestorStatusDto dto = new IngestorStatusDto();
        dto.setStatus(AppState.FATALERROR);
        dto.setTimeSinceLastChange(953620);
        dto.setErrorCounter(4);
        String str = dto.toString();
        assertTrue(str.contains("status: FATALERROR"));
        assertTrue(str.contains("timeSinceLastChange: 953620"));
        assertTrue(str.contains("errorCounter: 4"));
    }

    /**
     * Test equals and hashcode
     */
    @Test
    public void equalsDto() {
        EqualsVerifier.forClass(IngestorStatusDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }
    
}
