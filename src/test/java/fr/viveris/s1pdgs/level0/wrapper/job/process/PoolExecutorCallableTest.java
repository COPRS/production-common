package fr.viveris.s1pdgs.level0.wrapper.job.process;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.common.ProductFamily;
import fr.viveris.s1pdgs.common.errors.InternalErrorException;
import fr.viveris.s1pdgs.common.errors.processing.WrapperProcessTimeoutException;
import fr.viveris.s1pdgs.level0.wrapper.test.MockPropertiesTest;
import fr.viveris.s1pdgs.level0.wrapper.test.SystemUtils;
import fr.viveris.s1pdgs.mqi.model.queue.LevelJobDto;
import fr.viveris.s1pdgs.mqi.model.queue.LevelJobPoolDto;
import fr.viveris.s1pdgs.mqi.model.queue.LevelJobTaskDto;

public class PoolExecutorCallableTest extends MockPropertiesTest {

    private LevelJobDto job;

    private PoolExecutorCallable callable;

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockDefaultAppProperties();
        mockWapAppProperties(3, 1);

        job = new LevelJobDto(ProductFamily.L0_JOB, "id", ".", "3");
        job.addPool(new LevelJobPoolDto());
        job.getPools().get(0)
                .addTask(new LevelJobTaskDto(SystemUtils.getCmdMkdir()));
        job.addPool(new LevelJobPoolDto());
        job.getPools().get(1)
                .addTask(new LevelJobTaskDto(SystemUtils.getCmdLs()));
        job.getPools().get(1)
                .addTask(new LevelJobTaskDto(SystemUtils.getCmdTrue()));
        job.getPools().get(1)
                .addTask(new LevelJobTaskDto(SystemUtils.getCmdSleep()));
        job.addPool(new LevelJobPoolDto());
        job.getPools().get(2)
                .addTask(new LevelJobTaskDto(SystemUtils.getCmdRmdir()));

        callable = new PoolExecutorCallable(properties, job, "log");
    }

    @Test
    public void testInit() {
        assertEquals(3, callable.processors.size());
        assertFalse(callable.isActive());
    }

    @Test
    public void testInterruptedDuringWaiting()
            throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        CompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(service);
        completionService.submit(callable);
        Thread.sleep(1500);
        service.shutdownNow();

        thrown.expect(ExecutionException.class);
        thrown.expectCause(isA(InternalErrorException.class));
        completionService.take().get();
    }

    @Test
    public void testInterruptedDuringProcessing()
            throws InterruptedException, ExecutionException {
        callable.setActive(true);
        ExecutorService service = Executors.newSingleThreadExecutor();
        CompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(service);
        completionService.submit(callable);
        Thread.sleep(500);
        service.shutdownNow();

        thrown.expect(ExecutionException.class);
        thrown.expectCause(isA(InternalErrorException.class));
        completionService.take().get();
    }

    @Test
    public void testWaitForActiveTooLong()
            throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        CompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(service);
        completionService.submit(callable);

        thrown.expect(ExecutionException.class);
        thrown.expectCause(isA(WrapperProcessTimeoutException.class));
        completionService.take().get();
    }

    @Test
    public void testProcess() throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        CompletionService<Boolean> completionService =
                new ExecutorCompletionService<>(service);
        completionService.submit(callable);
        Thread.sleep(500);
        callable.setActive(true);

        boolean ret = completionService.take().get();
        assertTrue(ret);
    }
}
