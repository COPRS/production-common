package esa.s1pdgs.cpoc.mqi.model.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the LevelJobsMessageDto
 * 
 * @author Viveris Technologies
 */
public class LevelJobsMessageDtoTest {

    /**
     * Test getters, setters and constructors
     */
    @Test
    public void testGettersSettersConstructors() {
        LevelJobDto body = new LevelJobDto(ProductFamily.L0_JOB,
                "testEqualsFunction", "NRT", "/data/localWD/123456",
                "/data/localWD/123456/JobOrder.xml");
        GenericMessageDto<LevelJobDto> dto = new GenericMessageDto<LevelJobDto>(123, "input-key", body);

        assertEquals(123, dto.getId());
        assertEquals(body, dto.getBody());
        assertEquals("input-key", dto.getInputKey());

        dto = new GenericMessageDto<LevelJobDto>();
        dto.setId(321);
        dto.setBody(body);
        dto.setInputKey("othey-input");
        assertEquals(321, dto.getId());
        assertEquals(body, dto.getBody());
        assertEquals("othey-input", dto.getInputKey());
    }

    /**
     * Test the toString function
     */
    @Test
    public void testToString() {
        LevelJobDto body = new LevelJobDto(ProductFamily.L0_JOB,
                "testEqualsFunction", "NRT", "/data/localWD/123456",
                "/data/localWD/123456/JobOrder.xml");
        GenericMessageDto<LevelJobDto> dto =
                new GenericMessageDto<LevelJobDto>(123, "input-key", body);
        String str = dto.toString();
        assertTrue("toString should contain the identifier",
                str.contains("identifier: 123"));
        assertTrue("toString should contain the body",
                str.contains("body: " + body.toString()));
        assertTrue("toString should contain the input key",
                str.contains("inputKey: input-key"));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(GenericMessageDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
