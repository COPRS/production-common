package esa.s1pdgs.cpoc.mdcatalog.status.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class StatusPerCategoryDto
 * 
 * @author Viveris Technologies
 */
public class GlobalStatusDtoTest {

	/**
	 * Test default cosntructor and getters
	 */
	@Test
	public void testConstructor() {
		GlobalStatusDto dto = new GlobalStatusDto(AppState.PROCESSING);
		assertEquals(AppState.PROCESSING, dto.getGlobalStatus());
		assertEquals(0, dto.getStatusPerCategory().size());

		StatusPerCategoryDto status1 = new StatusPerCategoryDto(AppState.PROCESSING, 123456, 8,
				ProductCategory.EDRS_SESSIONS);
		StatusPerCategoryDto status2 = new StatusPerCategoryDto(AppState.WAITING, 12, 0, ProductCategory.LEVEL_JOBS);
		dto.addStatusPerCategory(status1);
		dto.addStatusPerCategory(status2);

		assertEquals(2, dto.getStatusPerCategory().size());
		assertEquals(status1, dto.getStatusPerCategory().get(ProductCategory.EDRS_SESSIONS));
		assertEquals(status2, dto.getStatusPerCategory().get(ProductCategory.LEVEL_JOBS));
	}

	/**
	 * Test toString methods and setters
	 */
	@Test
	public void testToStringAndSetters() {
		GlobalStatusDto dto = new GlobalStatusDto();
		dto.setGlobalStatus(AppState.FATALERROR);
		StatusPerCategoryDto status1 = new StatusPerCategoryDto(AppState.FATALERROR, 123456, 8,
				ProductCategory.EDRS_SESSIONS);
		StatusPerCategoryDto status2 = new StatusPerCategoryDto(AppState.WAITING, 12, 0, ProductCategory.LEVEL_JOBS);
		dto.addStatusPerCategory(status1);
		dto.addStatusPerCategory(status2);

		String str = dto.toString();
		assertTrue(str.contains("globalStatus: FATALERROR"));
		assertTrue(str.contains("statusPerCategory: " + dto.getStatusPerCategory().toString()));
	}

	/**
	 * Test equals and hashcode
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(GlobalStatusDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
