package esa.s1pdgs.cpoc.mqi.server.publication.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class RoutingTest {

    /**
     * Test constructor / getters / setters
     */
    @Test
    public void test() {
        Routing obj = new Routing();
        assertEquals(0, obj.getDefaultRoutes().size());

        RouteTo routeTo1 = new RouteTo();
        routeTo1.setTopic("topic1");
        DefaultRoute route1 = new DefaultRoute();
        route1.setFamily(ProductFamily.L0_ACN);
        route1.setRouteTo(routeTo1);
        obj.addRoute(route1);

        RouteTo routeTo2 = new RouteTo();
        routeTo2.setTopic("topic2");
        DefaultRoute route2 = new DefaultRoute();
        route2.setFamily(ProductFamily.EDRS_SESSION);
        route2.setRouteTo(routeTo2);
        obj.addRoute(route2);
        
        Route rte = new Route();
        RouteTo rte1 = new RouteTo();
        rte1.setTopic("topic-l1-slices");
        rte.setInputKey("input-Key");
        rte.setOutputKey(ProductFamily.L1_SLICE);
        rte.setRouteTo(rte1);
        obj.addRoute(rte);
                
        assertEquals(2, obj.getDefaultRoutes().size());
        assertEquals(1, obj.getRoutes().size());

        assertEquals(route2, obj.getDefaultRoute(ProductFamily.EDRS_SESSION));
        assertEquals(route1, obj.getDefaultRoute(ProductFamily.L0_ACN));
        assertNull(obj.getDefaultRoute(ProductFamily.AUXILIARY_FILE));
        assertEquals(rte, obj.getRoute("input-Key"));
        assertNull(obj.getRoute("inputKey"));
    }

    /**
     * Test the toString function
     */
    @Test
    public void testToString() {
        Routing obj = new Routing();
        List<DefaultRoute> dftRoutes = new ArrayList<>();
        List<Route> routes = new ArrayList<>();

        RouteTo routeTo1 = new RouteTo();
        routeTo1.setTopic("topic1");
        DefaultRoute route1 = new DefaultRoute();
        route1.setFamily(ProductFamily.L0_ACN);
        route1.setRouteTo(routeTo1);
        dftRoutes.add(route1);

        RouteTo routeTo2 = new RouteTo();
        routeTo2.setTopic("topic2");
        DefaultRoute route2 = new DefaultRoute();
        route2.setFamily(ProductFamily.EDRS_SESSION);
        route2.setRouteTo(routeTo2);
        dftRoutes.add(route2);

        Route rte = new Route();
        RouteTo rte1 = new RouteTo();
        rte1.setTopic("topic-l1-slices");
        rte.setInputKey("input-Key");
        rte.setOutputKey(ProductFamily.L1_SLICE);
        rte.setRouteTo(rte1);
        routes.add(rte);
        
        obj.setDefaultRoutes(dftRoutes);
        obj.setRoutes(routes);

        String str = obj.toString();
        assertTrue(str.contains("defaultRoutes: " + dftRoutes.toString()));
        assertTrue(str.contains("routes: " + routes.toString()));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(Routing.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
