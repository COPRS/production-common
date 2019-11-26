package esa.s1pdgs.cpoc.mqi.server.converter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.server.converter.ConverterConfiguration;
import esa.s1pdgs.cpoc.mqi.server.converter.XmlConverter;
import esa.s1pdgs.cpoc.mqi.server.publication.routing.DefaultRoute;
import esa.s1pdgs.cpoc.mqi.server.publication.routing.RouteTo;
import esa.s1pdgs.cpoc.mqi.server.publication.routing.Routing;

/**
 * Test XML conversion and mapping
 * 
 * @author Viveris Technologies
 */
public class XmlConverterTest {

    /**
     * Spring annotation context
     */
    private AnnotationConfigApplicationContext ctx;

    /**
     * XML converter
     */
    private XmlConverter xmlConverter;

    /**
     * Initialize the spring context
     */
    @Before
    public void init() {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(ConverterConfiguration.class);
        ctx.refresh();
        xmlConverter = ctx.getBean(XmlConverter.class);

    }

    /**
     * Close the spring context
     */
    @After
    public void close() {
        ctx.close();
    }
    
    /**
     * Test marshalling of routing file
     * @throws JAXBException 
     * @throws IOException 
     */
    @Test
    public void testUnmarshalingRouting() throws IOException, JAXBException {
        Routing routing = (Routing) xmlConverter.convertFromXMLToObject("./src/test/resources/routing-files/level-jobs.xml");
        
        DefaultRoute route0 = new DefaultRoute();
        RouteTo routeTo0 = new RouteTo();
        routeTo0.setTopic("t-pdgs-aio-execution-jobs");
        route0.setFamily(ProductFamily.L0_JOB);
        route0.setRouteTo(routeTo0);

        DefaultRoute route1 = new DefaultRoute();
        RouteTo routeTo1 = new RouteTo();
        routeTo1.setTopic("t-pdgs-l1-execution-jobs-nrt");
        route1.setFamily(ProductFamily.L1_JOB);
        route1.setRouteTo(routeTo1);
       
        DefaultRoute route2 = new DefaultRoute();
        RouteTo routeTo2 = new RouteTo();
        routeTo2.setTopic("t-pdgs-l2-execution-jobs-fast");
        route2.setFamily(ProductFamily.L2_JOB);
        route2.setRouteTo(routeTo2);
        
        assertEquals(3, routing.getDefaultRoutes().size());
        
        assertEquals(route0, routing.getDefaultRoute(ProductFamily.L0_JOB));
        assertEquals(route1, routing.getDefaultRoute(ProductFamily.L1_JOB));
        assertEquals(route2, routing.getDefaultRoute(ProductFamily.L2_JOB));
    }
}
