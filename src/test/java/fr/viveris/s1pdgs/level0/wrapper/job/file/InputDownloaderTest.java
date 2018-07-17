package fr.viveris.s1pdgs.level0.wrapper.job.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.common.ApplicationLevel;
import fr.viveris.s1pdgs.common.errors.AbstractCodedException;
import fr.viveris.s1pdgs.common.errors.InternalErrorException;
import fr.viveris.s1pdgs.common.errors.UnknownFamilyException;
import fr.viveris.s1pdgs.level0.wrapper.TestUtils;
import fr.viveris.s1pdgs.level0.wrapper.job.model.obs.S3DownloadFile;
import fr.viveris.s1pdgs.level0.wrapper.job.obs.ObsService;
import fr.viveris.s1pdgs.level0.wrapper.job.process.PoolExecutorCallable;
import fr.viveris.s1pdgs.level0.wrapper.utils.FileUtils;
import fr.viveris.s1pdgs.mqi.model.queue.LevelJobDto;

/**
 * Test the input downloader
 * 
 * @author Viveris Technologies
 */
public class InputDownloaderTest {

    /**
     * OBS service
     */
    @Mock
    private ObsService obsService;

    /**
     * Pool processor executable
     */
    @Mock
    private PoolExecutorCallable poolProcessorExecutor;

    private File workDirectory = new File(TestUtils.WORKDIR);
    private File ch1Directory = new File(TestUtils.WORKDIR + "ch01");
    private File ch2Directory = new File(TestUtils.WORKDIR + "ch02");
    private File jobOrder = new File(TestUtils.WORKDIR + "JobOrder.xml");
    private File statusFile = new File(TestUtils.WORKDIR + "Status.txt");
    private File blankFile = new File(TestUtils.WORKDIR + "blank.xml");

    private LevelJobDto dtol0 = TestUtils.buildL0LevelJobDto();
    private InputDownloader downloaderL0;

    private LevelJobDto dtol1 = TestUtils.buildL0LevelJobDto();
    private InputDownloader downloaderL1;

    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        doNothing().when(this.obsService).downloadFilesPerBatch(Mockito.any());
        doNothing().when(this.poolProcessorExecutor)
                .setActive(Mockito.anyBoolean());

        downloaderL0 = new InputDownloader(obsService, TestUtils.WORKDIR,
                dtol0.getInputs(), 5, "prefix-logs", this.poolProcessorExecutor,
                ApplicationLevel.L0);

        downloaderL1 = new InputDownloader(obsService, TestUtils.WORKDIR,
                dtol1.getInputs(), 5, "prefix-logs", this.poolProcessorExecutor,
                ApplicationLevel.L1);
    }

    /**
     * Cleaning
     * 
     * @throws IOException
     */
    @After
    public void clean() throws IOException {
        if ((new File(TestUtils.WORKDIR)).exists()) {
            FileUtils.delete(TestUtils.WORKDIR);
        }
    }

    /**
     * Test sort input
     * 
     * @throws AbstractCodedException
     * @throws IOException
     */
    @Test
    public void testSortInputs() throws AbstractCodedException, IOException {

        dtol0.addInput(TestUtils.buildBlankInputDto());

        List<S3DownloadFile> downloadToBatch = TestUtils.getL0DownloadFile();
        List<S3DownloadFile> result = downloaderL0.sortInputs();

        // Check work directory and subdirectories are created
        assertTrue(workDirectory.isDirectory());
        assertTrue(ch1Directory.exists() && ch1Directory.isDirectory());
        assertTrue(ch2Directory.exists() && ch2Directory.isDirectory());

        // Check the list of files to download is right
        assertEquals(downloadToBatch, result);

        // Check jobOrder.txt
        assertTrue(jobOrder.exists() && jobOrder.isFile());

        // Check blank file
        assertFalse(blankFile.exists());

    }

    /**
     * Test sort input when invalid family
     * 
     * @throws InternalErrorException
     * @throws UnknownFamilyException
     * @throws IOException
     */
    @Test(expected = UnknownFamilyException.class)
    public void testSortInputsWithInvalidFamily()
            throws InternalErrorException, UnknownFamilyException, IOException {
        dtol0.addInput(TestUtils.buildInvalidInputDto());
        downloaderL0.sortInputs();
    }

    @Test
    public void testProcessInputsL0()
            throws AbstractCodedException, IOException {

        downloaderL0.processInputs();

        List<S3DownloadFile> downloadToBatch = TestUtils.getL0DownloadFile();

        // Check work directory and subdirectories are created
        assertTrue(workDirectory.isDirectory());
        assertTrue(ch1Directory.exists() && ch1Directory.isDirectory());
        assertTrue(ch2Directory.exists() && ch2Directory.isDirectory());

        // We have one file per input + status.txt
        assertTrue(workDirectory.list().length == 4);
        verify(obsService, times(2)).downloadFilesPerBatch(Mockito.any());
        verify(obsService, times(1)).downloadFilesPerBatch(
                Mockito.eq(downloadToBatch.subList(0, 5)));
        verify(obsService, times(1)).downloadFilesPerBatch(
                Mockito.eq(downloadToBatch.subList(5, 8)));

        // Check jobOrder.txt
        assertTrue(jobOrder.exists() && jobOrder.isFile());
        // assertEquals("<xml>\\n<balise1></balise1>", readFile(jobOrder));

        // Check status.txt
        assertTrue(statusFile.exists() && statusFile.isFile());
        assertEquals("COMPLETED", FileUtils.readFile(statusFile));

        // Check blank file
        assertFalse(blankFile.exists());

        verify(poolProcessorExecutor, times(2)).setActive(Mockito.eq(true));

    }

    @Test
    public void testProcessInputsL1()
            throws AbstractCodedException, IOException {
        downloaderL1.processInputs();

        List<S3DownloadFile> downloadToBatch = TestUtils.getL0DownloadFile();

        // Check work directory and subdirectories are created
        assertTrue(workDirectory.isDirectory());
        assertTrue(ch1Directory.exists() && ch1Directory.isDirectory());
        assertTrue(ch2Directory.exists() && ch2Directory.isDirectory());

        // We have one file per input + status.txt
        assertTrue(workDirectory.list().length == 4);
        verify(this.obsService, times(2)).downloadFilesPerBatch(Mockito.any());
        verify(this.obsService, times(1)).downloadFilesPerBatch(
                Mockito.eq(downloadToBatch.subList(0, 5)));
        verify(this.obsService, times(1)).downloadFilesPerBatch(
                Mockito.eq(downloadToBatch.subList(5, 8)));

        // Check jobOrder.txt
        assertTrue(jobOrder.exists() && jobOrder.isFile());
        // assertEquals("<xml>\\n<balise1></balise1>", readFile(jobOrder));

        // Check status.txt
        assertTrue(statusFile.exists() && statusFile.isFile());
        assertEquals("COMPLETED", FileUtils.readFile(statusFile));

        verify(this.poolProcessorExecutor, times(1))
                .setActive(Mockito.eq(true));

    }
}
