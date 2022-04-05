package esa.s1pdgs.cpoc.compression.worker.test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import org.mockito.Mock;
import org.mockito.Mockito;

import esa.s1pdgs.cpoc.appstatus.Status;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.worker.config.CompressionWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.compression.worker.status.AppStatusImpl;

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
     * Application status
     */
    @Mock
    protected AppStatusImpl appStatus;

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

    /**
     * Mock default status
     * 
     * @throws AbstractCodedException
     */
    protected void mockDefaultStatus() throws AbstractCodedException {
        mockStatus((new Status(3, 30)), false);
    }

    /**
     * Mock status
     * 
     * @param state
     * @param maxErrorCounter
     * @param shallBeStopped
     */
    protected void mockStatus(final Status status, final boolean shallBeStopped) {

        doNothing().when(appStatus).setWaiting();
        doNothing().when(appStatus).setProcessing(Mockito.anyLong());
        doNothing().when(appStatus).setStopping();
        doNothing().when(appStatus).setError(Mockito.anyString());
        doNothing().when(appStatus).setShallBeStopped(Mockito.anyBoolean());
        doNothing().when(appStatus).forceStopping();

        doReturn(status).when(appStatus).getStatus();
        doReturn(shallBeStopped).when(appStatus).isShallBeStopped();
    }
}
