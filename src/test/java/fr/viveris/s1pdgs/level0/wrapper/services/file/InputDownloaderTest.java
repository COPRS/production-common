package fr.viveris.s1pdgs.level0.wrapper.services.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.level0.wrapper.TestUtils;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.model.ApplicationLevel;
import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3DownloadFile;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.S3Factory;
import fr.viveris.s1pdgs.level0.wrapper.services.task.PoolExecutorCallable;

public class InputDownloaderTest {

	@Mock
	private S3Factory s3Factory;

	@Mock
	private PoolExecutorCallable poolProcessorExecutor;

	@Before
	public void init() throws CodedException {
		MockitoAnnotations.initMocks(this);
		doNothing().when(this.s3Factory).downloadFilesPerBatch(Mockito.any());
		doNothing().when(this.poolProcessorExecutor).setActive(Mockito.anyBoolean());
	}

	private String readFile(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
	}

	private void delete(String path) throws IOException {
		Path p = Paths.get(path);
		Files.walk(p, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
				.peek(System.out::println).forEach(File::delete);
	}

	@Test
	public void testProcessInputsL0() throws IOException {
		File workDirectory = new File(TestUtils.WORKDIR);
		File ch1Directory = new File(TestUtils.WORKDIR + "ch01");
		File ch2Directory = new File(TestUtils.WORKDIR + "ch02");
		File statusFile = new File(TestUtils.WORKDIR + "Status.txt");
		File jobOrder = new File(TestUtils.WORKDIR + "JobOrder.xml");
		try {
			JobDto dto = TestUtils.buildL0JobDto();

			List<S3DownloadFile> downloadToBatch = new ArrayList<>();
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(0).getFamily()),
					dto.getInputs().get(0).getContentRef(), dto.getInputs().get(0).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(1).getFamily()),
					dto.getInputs().get(1).getContentRef(), dto.getInputs().get(1).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(2).getFamily()),
					dto.getInputs().get(2).getContentRef(), dto.getInputs().get(2).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(4).getFamily()),
					dto.getInputs().get(4).getContentRef(), dto.getInputs().get(4).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(5).getFamily()),
					dto.getInputs().get(5).getContentRef(), dto.getInputs().get(5).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(6).getFamily()),
					dto.getInputs().get(6).getContentRef(), dto.getInputs().get(6).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(7).getFamily()),
					dto.getInputs().get(7).getContentRef(), dto.getInputs().get(7).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(8).getFamily()),
					dto.getInputs().get(8).getContentRef(), dto.getInputs().get(8).getLocalPath()));

			InputDownloader d = new InputDownloader(s3Factory, TestUtils.WORKDIR, dto.getInputs(), 5,
					this.poolProcessorExecutor, ApplicationLevel.L0);

			try {
				d.processInputs();
			} catch (CodedException e) {
				fail("Exception occurred: " + e.getMessage());
			}

			// Check work directory and subdirectories are created
			assertTrue(workDirectory.isDirectory());
			assertTrue(ch1Directory.exists() && ch1Directory.isDirectory());
			assertTrue(ch2Directory.exists() && ch2Directory.isDirectory());

			// We have one file per input + status.txt
			assertTrue(workDirectory.list().length == 4);
			verify(this.s3Factory, times(2)).downloadFilesPerBatch(Mockito.any());
			verify(this.s3Factory, times(1)).downloadFilesPerBatch(Mockito.eq(downloadToBatch.subList(0, 5)));
			verify(this.s3Factory, times(1)).downloadFilesPerBatch(Mockito.eq(downloadToBatch.subList(5, 8)));

			// Check jobOrder.txt
			assertTrue(jobOrder.exists() && jobOrder.isFile());
			// assertEquals("<xml>\\n<balise1></balise1>", readFile(jobOrder));

			// Check status.txt
			assertTrue(statusFile.exists() && statusFile.isFile());
			assertEquals("COMPLETED", readFile(statusFile));
			
			verify(this.poolProcessorExecutor, times(2)).setActive(Mockito.eq(true));

		} catch (Exception e) {
			fail("Exception " + e.getMessage());
		} finally {
			this.delete(TestUtils.WORKDIR);
			assertTrue(!workDirectory.exists());
		}

	}

	@Test
	public void testProcessInputsL1() throws IOException {
		File workDirectory = new File(TestUtils.WORKDIR);
		File ch1Directory = new File(TestUtils.WORKDIR + "ch01");
		File ch2Directory = new File(TestUtils.WORKDIR + "ch02");
		File statusFile = new File(TestUtils.WORKDIR + "Status.txt");
		File jobOrder = new File(TestUtils.WORKDIR + "JobOrder.xml");
		try {
			JobDto dto = TestUtils.buildL0JobDto();

			List<S3DownloadFile> downloadToBatch = new ArrayList<>();
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(0).getFamily()),
					dto.getInputs().get(0).getContentRef(), dto.getInputs().get(0).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(1).getFamily()),
					dto.getInputs().get(1).getContentRef(), dto.getInputs().get(1).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(2).getFamily()),
					dto.getInputs().get(2).getContentRef(), dto.getInputs().get(2).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(4).getFamily()),
					dto.getInputs().get(4).getContentRef(), dto.getInputs().get(4).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(5).getFamily()),
					dto.getInputs().get(5).getContentRef(), dto.getInputs().get(5).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(6).getFamily()),
					dto.getInputs().get(6).getContentRef(), dto.getInputs().get(6).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(7).getFamily()),
					dto.getInputs().get(7).getContentRef(), dto.getInputs().get(7).getLocalPath()));
			downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(8).getFamily()),
					dto.getInputs().get(8).getContentRef(), dto.getInputs().get(8).getLocalPath()));

			InputDownloader d = new InputDownloader(s3Factory, TestUtils.WORKDIR, dto.getInputs(), 5,
					this.poolProcessorExecutor, ApplicationLevel.L1);

			try {
				d.processInputs();
			} catch (CodedException e) {
				fail("Exception occurred: " + e.getMessage());
			}

			// Check work directory and subdirectories are created
			assertTrue(workDirectory.isDirectory());
			assertTrue(ch1Directory.exists() && ch1Directory.isDirectory());
			assertTrue(ch2Directory.exists() && ch2Directory.isDirectory());

			// We have one file per input + status.txt
			assertTrue(workDirectory.list().length == 4);
			verify(this.s3Factory, times(2)).downloadFilesPerBatch(Mockito.any());
			verify(this.s3Factory, times(1)).downloadFilesPerBatch(Mockito.eq(downloadToBatch.subList(0, 5)));
			verify(this.s3Factory, times(1)).downloadFilesPerBatch(Mockito.eq(downloadToBatch.subList(5, 8)));

			// Check jobOrder.txt
			assertTrue(jobOrder.exists() && jobOrder.isFile());
			// assertEquals("<xml>\\n<balise1></balise1>", readFile(jobOrder));

			// Check status.txt
			assertTrue(statusFile.exists() && statusFile.isFile());
			assertEquals("COMPLETED", readFile(statusFile));
			
			verify(this.poolProcessorExecutor, times(1)).setActive(Mockito.eq(true));

		} catch (Exception e) {
			fail("Exception " + e.getMessage());
		} finally {
			this.delete(TestUtils.WORKDIR);
			assertTrue(!workDirectory.exists());
		}

	}
}
