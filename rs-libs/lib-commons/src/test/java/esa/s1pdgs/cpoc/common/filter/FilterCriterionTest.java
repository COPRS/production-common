package esa.s1pdgs.cpoc.common.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * @author Viveris Technologies
 */
public class FilterCriterionTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        FilterCriterion obj = new FilterCriterion("key-filter", 125L);
        assertEquals("key-filter", obj.getKey());
        assertEquals(125L, obj.getValue());
        assertEquals(FilterOperator.EQ, obj.getOperator());

        FilterCriterion obj2 = new FilterCriterion("key-filter2",
                new Date(1534247665000L), FilterOperator.LTE);
        assertEquals("key-filter2", obj2.getKey());
        assertEquals(new Date(1534247665000L), obj2.getValue());
        assertEquals(FilterOperator.LTE, obj2.getOperator());
    }

    /**
     * Test setters
     */
    @Test
    public void testSetters() {
        FilterCriterion obj = new FilterCriterion("key-filter", 125L);
        obj.setKey("key2");
        obj.setValue(new Date(1534247665000L));
        obj.setOperator(FilterOperator.GT);
        assertEquals("key2", obj.getKey());
        assertEquals(new Date(1534247665000L), obj.getValue());
        assertEquals(FilterOperator.GT, obj.getOperator());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        FilterCriterion obj =
                new FilterCriterion("key-filter2", 1458, FilterOperator.LTE);
        String str = obj.toString();
        assertTrue(str.contains("key: key-filter2"));
        assertTrue(str.contains("value: 1458"));
        assertTrue(str.contains("operator: LTE"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(FilterCriterion.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
