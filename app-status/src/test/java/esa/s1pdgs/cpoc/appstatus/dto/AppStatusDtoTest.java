package esa.s1pdgs.cpoc.appstatus.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.AppState;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class WrapperStatusDto
 * 
 * @author Viveris Technologies
 */
public class AppStatusDtoTest {

    /**
     * Test default constructor and getters
     */
    @Test
    public void testConstructor() {
        AppStatusDto dto =
                new AppStatusDto(AppState.PROCESSING, 123456, 8);
        assertEquals(AppState.PROCESSING, dto.getStatus());
        assertEquals(123456, dto.getTimeSinceLastChange());
        assertEquals(8, dto.getErrorCounter());
    }

    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSetters() {
    	AppStatusDto dto = new AppStatusDto();
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
        EqualsVerifier.forClass(AppStatusDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
