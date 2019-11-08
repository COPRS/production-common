// Commented out because UUT has been replaced with AppStatusDto

//package esa.s1pdgs.cpoc.mdcatalog.status.dto;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import org.junit.Test;
//
//import esa.s1pdgs.cpoc.appstatus.dto.StatusPerCategoryDto;
//import esa.s1pdgs.cpoc.common.AppState;
//import esa.s1pdgs.cpoc.common.ProductCategory;
//import nl.jqno.equalsverifier.EqualsVerifier;
//import nl.jqno.equalsverifier.Warning;
//
///**
// * Test the class StatusPerCategoryDto
// * 
// * @author Viveris Technologies
// */
//public class StatusPerCategoryDtoTest {
//
//    /**
//     * Test default cosntructor and getters
//     */
//    @Test
//    public void testConstructor() {
//        StatusPerCategoryDto dto =
//                new StatusPerCategoryDto(AppState.PROCESSING, 123456, 8, ProductCategory.EDRS_SESSIONS);
//        assertEquals(AppState.PROCESSING, dto.getStatus());
//        assertEquals(123456, dto.getTimeSinceLastChange());
//        assertEquals(8, dto.getErrorCounter());
//        assertEquals(ProductCategory.EDRS_SESSIONS, dto.getCategory());
//    }
//
//    /**
//     * Test toString methods and setters
//     */
//    @Test
//    public void testToStringAndSetters() {
//        StatusPerCategoryDto dto = new StatusPerCategoryDto();
//        dto.setStatus(AppState.FATALERROR);
//        dto.setTimeSinceLastChange(953620);
//        dto.setErrorCounter(4);
//        dto.setCategory(ProductCategory.AUXILIARY_FILES);
//        String str = dto.toString();
//        assertTrue(str.contains("status: FATALERROR"));
//        assertTrue(str.contains("timeSinceLastChange: 953620"));
//        assertTrue(str.contains("errorCounter: 4"));
//        assertTrue(str.contains("category: AUXILIARY_FILES"));
//    }
//
//    /**
//     * Test equals and hashcode
//     */
//    @Test
//    public void equalsDto() {
//        EqualsVerifier.forClass(StatusPerCategoryDto.class).usingGetClass()
//                .suppress(Warning.NONFINAL_FIELDS).verify();
//    }
//}
