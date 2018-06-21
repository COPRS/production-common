package fr.viveris.s1pdgs.level0.wrapper.test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;

import fr.viveris.s1pdgs.level0.wrapper.AppStatus;
import fr.viveris.s1pdgs.level0.wrapper.AppStatus.WrapperStatus;
import fr.viveris.s1pdgs.level0.wrapper.config.ApplicationProperties;
import fr.viveris.s1pdgs.level0.wrapper.config.DevProperties;

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
     * Development properties
     */
    @Mock
    protected DevProperties devProperties;

    /**
     * Application properties
     */
    @Mock
    protected ApplicationProperties properties;

    /**
     * Application status
     */
    @Mock
    protected AppStatus appStatus;

    /**
     * Mock the default development properties (all step activation at true)
     */
    protected void mockDefaultDevProperties() {
        this.mockDevProperties(true, true, true, true);
    }

    /**
     * Mock the development properties
     * 
     * @param download
     * @param execution
     * @param upload
     * @param erasing
     */
    protected void mockDevProperties(final boolean download,
            final boolean execution, final boolean upload,
            final boolean erasing) {
        Map<String, Boolean> activations = new HashMap<>();
        activations.put("download", Boolean.valueOf(download));
        activations.put("execution", Boolean.valueOf(execution));
        activations.put("upload", Boolean.valueOf(upload));
        activations.put("erasing", Boolean.valueOf(erasing));
        doReturn(activations).when(devProperties).getStepsActivation();
    }

    /**
     * Default mock of application properties
     */
    protected void mockDefaultAppProperties() {
        mockTmAppProperties(1800, 600, 300, 60);
        mockSizeAppProperties(20, 5);
        mockWapAppProperties(12, 1);
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
        doReturn(tmProcAllTasksS).when(properties).getTmProcAllTasksS();
        doReturn(tmProcOneTaskS).when(properties).getTmProcOneTaskS();
        doReturn(tmProcStopS).when(properties).getTmProcStopS();
        doReturn(tmProcCheckStopS).when(properties).getTmProcCheckStopS();
    }

    /**
     * Mock batch sizes of the application properties
     * 
     * @param sizeBatchUpload
     * @param sizeBatchDownload
     */
    protected void mockSizeAppProperties(int sizeBatchUpload,
            int sizeBatchDownload) {
        doReturn(sizeBatchUpload).when(properties).getSizeBatchUpload();
        doReturn(sizeBatchDownload).when(properties).getSizeBatchDownload();
    }

    /**
     * Mock WAP of the application properties
     * 
     * @param wapNbMaxLoop
     * @param wapTempoS
     */
    protected void mockWapAppProperties(int wapNbMaxLoop, long wapTempoS) {
        doReturn(wapNbMaxLoop).when(properties).getWapNbMaxLoop();
        doReturn(wapTempoS).when(properties).getWapTempoS();
    }

    /**
     * Mock default status
     */
    protected void mockDefaultStatus() {
        mockStatus((new AppStatus(3)).getStatus(), 3, false);
    }

    /**
     * Mock status
     * @param state
     * @param maxErrorCounter
     * @param shallBeStopped
     */
    protected void mockStatus(final WrapperStatus state,
            final int maxErrorCounter, final boolean shallBeStopped) {
        
        doNothing().when(appStatus).setWaiting();
        doNothing().when(appStatus).setProcessing();
        doNothing().when(appStatus).setStopping();
        doNothing().when(appStatus).setError();
        doNothing().when(appStatus).setShallBeStopped(Mockito.anyBoolean());
        doNothing().when(appStatus).forceStopping();

        doReturn(state).when(appStatus).getStatus();
        doReturn(shallBeStopped).when(appStatus).isShallBeStopped();
    }
}
