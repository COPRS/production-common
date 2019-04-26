package esa.s1pdgs.cpoc.mqi.server.publication.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class DefaultRouteTest {
    
    /**
     * Test constructor / getters / setters
     */
    @Test
    public void test() {
        DefaultRoute obj = new DefaultRoute();
        assertEquals(ProductFamily.BLANK, obj.getFamily());
        assertNull(obj.getRouteTo());
        
        RouteTo routeTo = new RouteTo();
        obj.setFamily(ProductFamily.L0_ACN);
        obj.setRouteTo(routeTo);
        assertEquals(ProductFamily.L0_ACN, obj.getFamily());
        assertEquals(routeTo, obj.getRouteTo());
    }
    

    /**
     * Test the toString function
     */
    @Test
    public void testToString() {
        RouteTo routeTo = new RouteTo();
        DefaultRoute obj = new DefaultRoute();
        obj.setFamily(ProductFamily.L0_ACN);
        obj.setRouteTo(routeTo);
        
        String str = obj.toString();
        assertTrue(str.contains("family: L0_ACN"));
        assertTrue(str.contains("routeTo: " + routeTo.toString()));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(DefaultRoute.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
