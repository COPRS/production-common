package esa.s1pdgs.cpoc.compression.worker.test;

import static org.mockito.Mockito.doReturn;

import org.mockito.Mock;

import esa.s1pdgs.cpoc.compression.worker.config.CompressionWorkerConfigurationProperties;

/**
 * Test class with properties mocked
 * 
 * @author Viveris Technologies
 */
public class MockPropertiesTest {

    /**
     * Topic
     */
    protected static final String TOPIC_NAME = "topic-name";

    /**
     * Application properties
     */
    @Mock
    protected CompressionWorkerConfigurationProperties properties;

    /**
     * Default mock of application properties
     */
    protected void mockDefaultAppProperties() {
        mockTmAppProperties(1800, 600, 300, 60);
    }

    /**
     * Mock timeouts of the application properties
     * 
     * @param tmProcAllTasksS
     * @param tmProcOneTaskS
     * @param tmProcStopS
     * @param tmProcCheckStopS
     */
    protected void mockTmAppProperties(long tmProcAllTasksS,
            long tmProcOneTaskS, long tmProcStopS, long tmProcCheckStopS) {
        doReturn(tmProcAllTasksS).when(properties).getCompressionTimeout();
        doReturn(tmProcStopS).when(properties).getRequestTimeout();
    }

}
