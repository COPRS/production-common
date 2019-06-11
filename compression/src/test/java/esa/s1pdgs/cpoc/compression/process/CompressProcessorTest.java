package esa.s1pdgs.cpoc.compression.process;

import java.io.File;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.TestUtils;
import esa.s1pdgs.cpoc.compression.obs.ObsService;
import esa.s1pdgs.cpoc.compression.test.MockPropertiesTest;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class CompressProcessorTest extends MockPropertiesTest {
	/**
     * Output processsor
     */
    @Mock
    private ObsService obsService;

    /**
     * MQI service
     */
    @Mock
    private GenericMqiService<LevelJobDto> mqiService;

    /**
     * Job to process
     */
    private GenericMessageDto<LevelJobDto> inputMessage;

    /**
     * Processor to test
     */
    private CompressProcessor processor;
    
    /**
     * Working directory
     */
    private File workingDir;
    
    @Mock
    private ExecutorService procExecutorSrv;

    @Mock
    private ExecutorCompletionService<Boolean> procCompletionSrv;

    @Mock
    private PoolExecutorCallable procExecutor;
    
    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        mockDefaultAppProperties();
        mockDefaultDevProperties();
        mockDefaultStatus();

        inputMessage = new GenericMessageDto<LevelJobDto>(123, "",
                TestUtils.buildL0LevelJobDto());
        workingDir = new File(inputMessage.getBody().getWorkDirectory());
        if (!workingDir.exists()) {
            workingDir.mkdir();
        }
        processor = new CompressProcessor(appStatus, properties,
                obsService,mqiService, mqiStatusService);
        
        procExecutorSrv = Executors.newSingleThreadExecutor();
        procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);
    }

    /**
     * Clean
     */
    @After
    public void clean() {
        if (workingDir.exists()) {
            workingDir.delete();
        }
    }
    
    @Test
    public void test() {
    	
    }
    
}
