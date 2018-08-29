package esa.s1pdgs.cpoc.common.filter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test the enumeration FilterOperator
 * 
 * @author Viveris Technologies
 */
public class FilterOperatorTest {

    /**
     * Test default method of enumeration
     */
    @Test
    public void testValueOf() {
        assertEquals(5, FilterOperator.values().length);
        assertEquals(FilterOperator.EQ, FilterOperator.valueOf("EQ"));
        assertEquals(FilterOperator.LT, FilterOperator.valueOf("LT"));
        assertEquals(FilterOperator.LTE, FilterOperator.valueOf("LTE"));
        assertEquals(FilterOperator.GT, FilterOperator.valueOf("GT"));
        assertEquals(FilterOperator.GTE, FilterOperator.valueOf("GTE"));
    }
}
