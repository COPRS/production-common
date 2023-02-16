package esa.s1pdgs.cpoc.ipf.execution.worker.test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.DevProperties;

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
        final Map<String, Boolean> activations = new HashMap<>();
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
        mockOverwriteShell(false);
    }

    /**
     * Mock timeouts of the application properties
     * 
     * @param tmProcAllTasksS
     * @param tmProcOneTaskS
     * @param tmProcStopS
     * @param tmProcCheckStopS
     */
    protected void mockTmAppProperties(final long tmProcAllTasksS,
            final long tmProcOneTaskS, final long tmProcStopS, final long tmProcCheckStopS) {
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
    protected void mockSizeAppProperties(final int sizeBatchUpload,
            final int sizeBatchDownload) {
        doReturn(sizeBatchUpload).when(properties).getSizeBatchUpload();
        doReturn(sizeBatchDownload).when(properties).getSizeBatchDownload();
    }

    /**
     * Mock WAP of the application properties
     * 
     * @param wapNbMaxLoop
     * @param wapTempoS
     */
    protected void mockWapAppProperties(final int wapNbMaxLoop, final long wapTempoS) {
        doReturn(wapNbMaxLoop).when(properties).getWapNbMaxLoop();
        doReturn(wapTempoS).when(properties).getWapTempoS();
    }
    
    protected void mockWorkingdirProperties(final Path workingdir) {
    	doReturn(workingdir.toString()).when(properties).getWorkingDir();
    }
    
    protected void mockOverwriteShell(final boolean overwriteShell) {
    	doReturn(overwriteShell).when(properties).getOverwriteShell();
    }

    /**
     * Mock status
     * 
     * @param state
     * @param maxErrorCounter
     * @param shallBeStopped
     */
    protected void mockStatus(final Status state,
            final int maxErrorCounter, final boolean shallBeStopped) {

        doNothing().when(appStatus).setWaiting();
        doNothing().when(appStatus).setProcessing(Mockito.anyLong());
        doNothing().when(appStatus).setStopping();
        doNothing().when(appStatus).setError(Mockito.anyString());
        doNothing().when(appStatus).setShallBeStopped(Mockito.anyBoolean());
        doNothing().when(appStatus).forceStopping();

        doReturn(state).when(appStatus).getStatus();
        doReturn(shallBeStopped).when(appStatus).isShallBeStopped();
    }
}
