package esa.s1pdgs.cpoc.mqi.server.distribution;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.MessageConsumptionController;
import esa.s1pdgs.cpoc.mqi.server.publication.MessagePublicationController;
import esa.s1pdgs.cpoc.mqi.server.test.RestControllerTest;

/**
 * Test the controller ErrorsController
 * 
 * @author Viveris Technologies
 */
public class ErrorsControllerTest extends RestControllerTest {

	/**
     * Mock the controller of consumed messages
     */
    @Mock
    private MessageConsumptionController messages;

    /**
     * Mock the controller of published messages
     */
    @Mock
    private MessagePublicationController publication;

    /**
     * Mock the application properties
     */
    @Mock
    private ApplicationProperties properties;

    /**
     * The controller to test
     */
    private ErrorsController controller;

    /**
     * Initialization
     * 
     * @throws MqiCategoryNotAvailable
     */
    @Before
    public void init() throws MqiCategoryNotAvailable {
        MockitoAnnotations.initMocks(this);

        controller = new ErrorsController(messages,
                publication, properties);

        this.initMockMvc(this.controller);
    }

    /**
     * Test the URI of the next message API
     * 
     * @throws Exception
     */
    @Test
    public void testPublishOK() throws Exception {
        doReturn(true).when(publication).publishError(Mockito.anyString());
        request(post("/errors/publish").content("this is the message"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(publication, only())
                .publishError(Mockito.eq("this is the message"));
    }

    /**
     * Test the URI of the next message API
     * 
     * @throws Exception
     */
    @Test
    public void testPublishKO() throws Exception {
        doReturn(false).when(publication).publishError(Mockito.anyString());
        request(post("/errors/publish").content("this is the message"))
                .andExpect(MockMvcResultMatchers.status().isGatewayTimeout());
        verify(publication, only())
                .publishError(Mockito.eq("this is the message"));
    }
}
