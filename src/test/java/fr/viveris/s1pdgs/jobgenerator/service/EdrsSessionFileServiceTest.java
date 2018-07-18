package fr.viveris.s1pdgs.jobgenerator.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.util.FileCopyUtils;

import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException;
import fr.viveris.s1pdgs.jobgenerator.exception.InternalErrorException;
import fr.viveris.s1pdgs.jobgenerator.exception.InvalidFormatProduct;
import fr.viveris.s1pdgs.jobgenerator.exception.ObjectStorageException;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFile;
import fr.viveris.s1pdgs.common.ProductFamily;
import fr.viveris.s1pdgs.jobgenerator.service.s3.ObsService;
import fr.viveris.s1pdgs.jobgenerator.utils.TestL0Utils;

public class EdrsSessionFileServiceTest {

	/**
	 * S3 service
	 */
	@Mock
	private ObsService obsService;

	/**
	 * XML converter
	 */
	@Mock
	private XmlConverter xmlConverter;

	private EdrsSessionFileService service;

	private File fileCh1;

	private File fileCh2;

	private EdrsSessionFile session1;

	private EdrsSessionFile session2;

	/**
	 * Test set up
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {

		// Mcokito
		MockitoAnnotations.initMocks(this);

		(new File("./tmp")).mkdirs();

		service = new EdrsSessionFileService(obsService, xmlConverter, "./tmp/");
		fileCh1 = new File("./tmp/DCS_02_L20171109175634707000125_ch1_DSIB.xml");
		fileCh1.createNewFile();
		fileCh2 = new File("./tmp/DCS_02_L20171109175634707000125_ch2_DSIB.xml");
		fileCh2.createNewFile();
		FileCopyUtils.copy(new File("./test/data/DCS_02_L20171109175634707000125_ch1_DSIB.xml"), fileCh1);
		FileCopyUtils.copy(new File("./test/data/DCS_02_L20171109175634707000125_ch2_DSIB.xml"), fileCh2);
		session1 = TestL0Utils.createEdrsSessionFileChannel1(true);
		session2 = TestL0Utils.createEdrsSessionFileChannel2(true);

		// Mock the dispatcher
		Mockito.doAnswer(i -> {
			return fileCh1;
		}).when(obsService).downloadFile(Mockito.any(), Mockito.eq("S1A/SESSION_1/ch1/KEY_OBS_SESSION_1_1.xml"),
				Mockito.anyString());
		Mockito.doAnswer(i -> {
			return fileCh2;
		}).when(obsService).downloadFile(Mockito.any(), Mockito.eq("S1A/SESSION_1/ch2/KEY_OBS_SESSION_1_2.xml"),
				Mockito.anyString());
		Mockito.doAnswer(i -> {
			return fileCh2;
		}).when(obsService).downloadFile(Mockito.any(), Mockito.eq("KEY_OBS_SESSION_1_2.xml"), Mockito.anyString());

		// Mock the XML converter
		Mockito.doAnswer(i -> {
			return session1;
		}).when(xmlConverter).convertFromXMLToObject(Mockito.eq(fileCh1.getAbsolutePath()));
		Mockito.doAnswer(i -> {
			return session2;
		}).when(xmlConverter).convertFromXMLToObject(Mockito.eq(fileCh2.getAbsolutePath()));

	}

	@Test
	public void testCreateSessionFile() throws AbstractCodedException {
		try {
			EdrsSessionFile r1 = service.createSessionFile("S1A/SESSION_1/ch1/KEY_OBS_SESSION_1_1.xml");
			Mockito.verify(obsService, times(1)).downloadFile(Mockito.eq(ProductFamily.EDRS_SESSION),
					Mockito.eq("S1A/SESSION_1/ch1/KEY_OBS_SESSION_1_1.xml"),

					Mockito.eq("./tmp/"));
			Mockito.verify(xmlConverter, times(1)).convertFromXMLToObject(Mockito.eq(fileCh1.getAbsolutePath()));
			assertEquals(session1, r1);

			EdrsSessionFile r2 = service.createSessionFile("S1A/SESSION_1/ch2/KEY_OBS_SESSION_1_2.xml");
			Mockito.verify(obsService, times(1)).downloadFile(Mockito.eq(ProductFamily.EDRS_SESSION),
					Mockito.eq("S1A/SESSION_1/ch2/KEY_OBS_SESSION_1_2.xml"),

					Mockito.eq("./tmp/"));
			Mockito.verify(xmlConverter, times(1)).convertFromXMLToObject(Mockito.eq(fileCh2.getAbsolutePath()));
			assertEquals(session2, r2);

			EdrsSessionFile r3 = service.createSessionFile("KEY_OBS_SESSION_1_2.xml");
			Mockito.verify(obsService, times(1)).downloadFile(Mockito.eq(ProductFamily.EDRS_SESSION),
					Mockito.eq("KEY_OBS_SESSION_1_2.xml"),

					Mockito.eq("./tmp/"));
			Mockito.verify(xmlConverter, times(2)).convertFromXMLToObject(Mockito.eq(fileCh2.getAbsolutePath()));
			assertEquals(session2, r3);

		} catch (ObjectStorageException | InvalidFormatProduct | IOException | JAXBException e) {
			fail("Invalid exception raised " + e.getMessage());
		}
	}

	@Test
	public void testCreateSessionFileXMLConversionError() throws AbstractCodedException, IOException, JAXBException {
		Mockito.doThrow(JAXBException.class).when(xmlConverter)
				.convertFromXMLToObject(Mockito.eq(fileCh1.getAbsolutePath()));
		try {
			service.createSessionFile("S1A/SESSION_1/ch1/KEY_OBS_SESSION_1_1.xml");
			fail("JAXBException should be raised");
		} catch (InternalErrorException exc) {
			Mockito.verify(xmlConverter, times(1)).convertFromXMLToObject(Mockito.eq(fileCh1.getAbsolutePath()));
			assertTrue(exc.getMessage().startsWith("Cannot convert"));
		}

		Mockito.doThrow(IOException.class).when(xmlConverter)
				.convertFromXMLToObject(Mockito.eq(fileCh1.getAbsolutePath()));
		try {
			service.createSessionFile("S1A/SESSION_1/ch1/KEY_OBS_SESSION_1_1.xml");
			fail("JAXBException should be raised");
		} catch (InternalErrorException exc) {
			Mockito.verify(xmlConverter, times(2)).convertFromXMLToObject(Mockito.eq(fileCh1.getAbsolutePath()));
			assertTrue(exc.getMessage().startsWith("Cannot convert"));
		}
	}
}
