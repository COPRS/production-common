/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.server.status.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.AppState;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test class for the Dto of Applicative catalog status
 * 
 * @author Viveris Technologies
 */
public class AppCatalogStatusDtoTest {
    
    /**
     * Test default cosntructor and getters
     */
    @Test
    public void testConstructor() {
        AppCatalogStatusDto dto =
                new AppCatalogStatusDto(AppState.PROCESSING, 123456, 8);
        assertEquals(AppState.PROCESSING, dto.getStatus());
        assertEquals(123456, dto.getTimeSinceLastChange());
        assertEquals(8, dto.getErrorCounter());
    }

    /**
     * Test toString methods and setters
     */
    @Test
    public void testToStringAndSetters() {
        AppCatalogStatusDto dto = new AppCatalogStatusDto();
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
        EqualsVerifier.forClass(AppCatalogStatusDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
