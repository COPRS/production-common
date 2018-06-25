package fr.viveris.s1pdgs.scaler.k8s.model.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.scaler.k8s.model.dto.AppState;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class WrapperStatusDto
 * 
 * @author Viveris Technologies
 */
public class WrapperStatusDtoTest {

    /**
     * Test default cosntructor and getters
     */
    @Test
    public void testConstructor() {
        WrapperStatusDto dto =
                new WrapperStatusDto(AppState.PROCESSING, 123456, 8);
        assertEquals(AppState.PROCESSING, dto.getStatus());
        assertEquals(123456, dto.getTimeSinceLastChange());
        assertEquals(8, dto.getErrorCounter());
    }

    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSetters() {
        WrapperStatusDto dto = new WrapperStatusDto();
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
        EqualsVerifier.forClass(WrapperStatusDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
