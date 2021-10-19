package esa.s1pdgs.cpoc.mqi.server.publication.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class RouteToTest {

    /**
     * Test constructor / getters / setters
     */
    @Test
    public void test() {
        RouteTo obj = new RouteTo();
        assertEquals("", obj.getTopic());

        obj.setTopic("topic-name");
        assertEquals("topic-name", obj.getTopic());
    }

    /**
     * Test the toString function
     */
    @Test
    public void testToString() {
        RouteTo obj = new RouteTo();
        obj.setTopic("topic-name");

        String str = obj.toString();
        assertTrue(str.contains("topic: topic-name"));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(RouteTo.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
