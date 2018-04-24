package fr.viveris.s1pdgs.level0.wrapper.services.s3;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.level0.wrapper.TestUtils;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObsS3Exception;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.UnknownFamilyException;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3DownloadFile;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3UploadFile;

public class S3FactoryTest {

	@Mock
	private ConfigFilesS3Services configFilesS3Services;

	@Mock
	private SessionFilesS3Services sessionFilesS3Services;

	@Mock
	private L0SlicesS3Services l0SlicesS3Services;

	@Mock
	private L0AcnsS3Services l0AcnsS3Services;

	@Mock
	private L1SlicesS3Services l1SlicesS3Services;

	@Mock
	private L1AcnsS3Services l1AcnsS3Services;

	private S3Factory s3Factory;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	private void mockForDownload() throws ObsS3Exception {
		doNothing().when(this.configFilesS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(true).when(this.configFilesS3Services).exist(Mockito.anyString());
		doReturn(6).when(this.configFilesS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		doNothing().when(this.sessionFilesS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(true).when(this.sessionFilesS3Services).exist(Mockito.anyString());
		doReturn(5).when(this.sessionFilesS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		doNothing().when(this.l0SlicesS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(true).when(this.l0SlicesS3Services).exist(Mockito.anyString());
		doReturn(4).when(this.l0SlicesS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		doNothing().when(this.l0AcnsS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(true).when(this.l0AcnsS3Services).exist(Mockito.anyString());
		doReturn(3).when(this.l0AcnsS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		doNothing().when(this.l1SlicesS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(true).when(this.l1SlicesS3Services).exist(Mockito.anyString());
		doReturn(2).when(this.l1SlicesS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		doNothing().when(this.l1AcnsS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(true).when(this.l1AcnsS3Services).exist(Mockito.anyString());
		doReturn(1).when(this.l1AcnsS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		this.s3Factory = new S3Factory(sessionFilesS3Services, configFilesS3Services, l0SlicesS3Services,
				l0AcnsS3Services, l1SlicesS3Services, l1AcnsS3Services);
	}
	
	private void mockForUpload() throws ObsS3Exception {
		doReturn(0).when(this.configFilesS3Services).uploadDirectory(Mockito.anyString(), Mockito.any());
		doNothing().when(this.configFilesS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(false).when(this.configFilesS3Services).exist(Mockito.anyString());
		doReturn(6).when(this.configFilesS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		doReturn(0).when(this.sessionFilesS3Services).uploadDirectory(Mockito.anyString(), Mockito.any());
		doNothing().when(this.sessionFilesS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(false).when(this.sessionFilesS3Services).exist(Mockito.anyString());
		doReturn(5).when(this.sessionFilesS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		doCallRealMethod().when(this.l0SlicesS3Services).uploadDirectory(Mockito.anyString(), Mockito.any());
		doNothing().when(this.l0SlicesS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(false).when(this.l0SlicesS3Services).exist(Mockito.anyString());
		doReturn(4).when(this.l0SlicesS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		doReturn(0).when(this.l0AcnsS3Services).uploadDirectory(Mockito.anyString(), Mockito.any());
		doNothing().when(this.l0AcnsS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(false).when(this.l0AcnsS3Services).exist(Mockito.anyString());
		doReturn(3).when(this.l0AcnsS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		doReturn(2).when(this.l1SlicesS3Services).uploadDirectory(Mockito.anyString(), Mockito.any());
		doNothing().when(this.l1SlicesS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(false).when(this.l1SlicesS3Services).exist(Mockito.anyString());
		doReturn(2).when(this.l1SlicesS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		doReturn(0).when(this.l1AcnsS3Services).uploadDirectory(Mockito.anyString(), Mockito.any());
		doNothing().when(this.l1AcnsS3Services).uploadFile(Mockito.anyString(), Mockito.any());
		doReturn(false).when(this.l1AcnsS3Services).exist(Mockito.anyString());
		doReturn(1).when(this.l1AcnsS3Services).downloadFiles(Mockito.anyString(),
				Mockito.anyString());

		this.s3Factory = new S3Factory(sessionFilesS3Services, configFilesS3Services, l0SlicesS3Services,
				l0AcnsS3Services, l1SlicesS3Services, l1AcnsS3Services);
	}

	@Test
	public void testDownloadBatch() throws CodedException {
		this.mockForDownload();
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

		this.s3Factory.downloadFilesPerBatch(downloadToBatch);
		verify(this.sessionFilesS3Services, times(5)).downloadFiles(Mockito.anyString(), Mockito.anyString());
		verify(this.configFilesS3Services, times(3)).downloadFiles(Mockito.anyString(), Mockito.anyString());
		verify(this.sessionFilesS3Services, times(1)).downloadFiles(Mockito.eq(dto.getInputs().get(6).getContentRef()),
				Mockito.eq(TestUtils.getAbsolutePath(dto.getInputs().get(6).getLocalPath())));
		verify(this.configFilesS3Services, times(1)).downloadFiles(Mockito.eq(dto.getInputs().get(0).getContentRef()),
				Mockito.eq(TestUtils.getAbsolutePath(dto.getInputs().get(0).getLocalPath())));
		verify(this.sessionFilesS3Services, never()).downloadFiles(Mockito.eq(dto.getInputs().get(0).getContentRef()),
				Mockito.eq(TestUtils.getAbsolutePath(dto.getInputs().get(0).getLocalPath())));
	}

	@Test(expected = UnknownFamilyException.class)
	public void testDownloadInvalidFamily() throws CodedException {
		this.mockForDownload();
		JobDto dto = TestUtils.buildL0JobDto();
		List<S3DownloadFile> downloadToBatch = new ArrayList<>();
		downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(0).getFamily()),
				dto.getInputs().get(0).getContentRef(), dto.getInputs().get(0).getLocalPath()));
		downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(1).getFamily()),
				dto.getInputs().get(1).getContentRef(), dto.getInputs().get(1).getLocalPath()));
		downloadToBatch.add(new S3DownloadFile(ProductFamily.L1_PRODUCT, dto.getInputs().get(2).getContentRef(),
				dto.getInputs().get(2).getLocalPath()));
		downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(4).getFamily()),
				dto.getInputs().get(4).getContentRef(), dto.getInputs().get(4).getLocalPath()));
		downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(5).getFamily()),
				dto.getInputs().get(5).getContentRef(), dto.getInputs().get(5).getLocalPath()));
		downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(6).getFamily()),
				dto.getInputs().get(6).getContentRef(), dto.getInputs().get(6).getLocalPath()));
		downloadToBatch.add(new S3DownloadFile(ProductFamily.fromValue(dto.getInputs().get(7).getFamily()),
				dto.getInputs().get(7).getContentRef(), dto.getInputs().get(7).getLocalPath()));
		this.s3Factory.downloadFilesPerBatch(downloadToBatch);
	}

	@Test
	public void testUploadProduct() throws CodedException, IOException {
		this.mockForUpload();
		// Create a temp dir for test
		File f = new File("./key3");
		f.mkdirs();
		File f1 = new File("./key3/file1");
		f1.createNewFile();
		File f2 = new File("./key3/file2");
		f2.createNewFile();
		List<S3UploadFile> upload = new ArrayList<>();
		upload.add(new S3UploadFile(ProductFamily.L0_ACN, "key1", new File("key1")));
		upload.add(new S3UploadFile(ProductFamily.L0_ACN, "key2", new File("key2")));
		upload.add(new S3UploadFile(ProductFamily.L0_PRODUCT, "key3", f));
		upload.add(new S3UploadFile(ProductFamily.L0_ACN, "key4", new File("key4")));
		upload.add(new S3UploadFile(ProductFamily.L0_PRODUCT, "key5", new File("key5")));
		upload.add(new S3UploadFile(ProductFamily.L0_PRODUCT, "key6", new File("key6")));
		upload.add(new S3UploadFile(ProductFamily.L0_ACN, "key7", new File("key7")));
		this.s3Factory.uploadFilesPerBatch(upload);
		verify(this.l0SlicesS3Services, times(4)).uploadFile(Mockito.anyString(), Mockito.any());
		verify(this.l0SlicesS3Services, times(1)).uploadFile(Mockito.eq("key3/file2"), Mockito.eq(f2));
		verify(this.l0SlicesS3Services, times(1)).uploadFile(Mockito.eq("key5"), Mockito.eq(new File("key5")));
		verify(this.l0SlicesS3Services, times(1)).uploadDirectory(Mockito.anyString(), Mockito.any());
		verify(this.l0SlicesS3Services, times(1)).uploadDirectory(Mockito.eq("key3"), Mockito.eq(f));
		verify(this.l0AcnsS3Services, times(4)).uploadFile(Mockito.anyString(), Mockito.any());
		verify(this.l0AcnsS3Services, times(1)).uploadFile(Mockito.eq("key2"), Mockito.eq(new File("key2")));
	}

	@Test(expected = UnknownFamilyException.class)
	public void testUploadInvalidFamily() throws CodedException {
		this.mockForUpload();
		List<S3UploadFile> upload = new ArrayList<>();
		upload.add(new S3UploadFile(ProductFamily.L0_ACN, "key1", new File("key1")));
		upload.add(new S3UploadFile(ProductFamily.L0_ACN, "key2", new File("key2")));
		upload.add(new S3UploadFile(ProductFamily.RAW, "key1", new File("key1")));
		upload.add(new S3UploadFile(ProductFamily.L0_PRODUCT, "key3", new File("key3")));
		this.s3Factory.uploadFilesPerBatch(upload);
	}

}
