package fr.viveris.s1pdgs.jobgenerator.tasks.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings.WaitTempo;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.controller.JobsProducer;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;
import fr.viveris.s1pdgs.jobgenerator.exception.MetadataException;
import fr.viveris.s1pdgs.jobgenerator.exception.InputsMissingException;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFileRaw;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.ProcessLevel;
import fr.viveris.s1pdgs.jobgenerator.model.ProductFamily;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.EdrsSessionMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadataQuery;
import fr.viveris.s1pdgs.jobgenerator.model.product.EdrsSessionProduct;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTable;
import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;
import fr.viveris.s1pdgs.jobgenerator.service.metadata.MetadataService;
import fr.viveris.s1pdgs.jobgenerator.utils.TestL0Utils;

/**
 * 
 * @author Cyrielle
 *
 */
public class EdrsSessionJobsGeneratorTest {

	/**
	 * XML converter
	 */
	@Mock
	private XmlConverter xmlConverter;

	@Mock
	private MetadataService metadataService;

	@Mock
	private ProcessSettings l0ProcessSettings;

	@Mock
	private JobGeneratorSettings jobGeneratorSettings;

	@Mock
	private JobsProducer kafkaJobsSender;

	private TaskTable expectedTaskTable;
	private EdrsSessionJobsGenerator generator;

	/**
	 * Test set up
	 * 
	 * @throws Exception
	 */
	@Before
	public void init() throws Exception {

		// Retrieve task table from the XML converter
		expectedTaskTable = TestL0Utils.buildTaskTableAIOP();

		// Mcokito
		MockitoAnnotations.initMocks(this);
		this.mockProcessSettings();
		this.mockJobGeneratorSettings();
		this.mockXmlConverter();
		this.mockMetadataService();

		JobsGeneratorFactory factory = new JobsGeneratorFactory(l0ProcessSettings, jobGeneratorSettings, xmlConverter,
				metadataService, kafkaJobsSender);
		generator = (EdrsSessionJobsGenerator) factory.createJobGeneratorForEdrsSession(
				new File("./test/data/generic_config/task_tables/TaskTable.AIOP.xml"));
	}

	private void mockProcessSettings() {
		Mockito.doAnswer(i -> {
			Map<String, String> r = new HashMap<String, String>(2);
			return r;
		}).when(l0ProcessSettings).getParams();
		Mockito.doAnswer(i -> {
			Map<String, String> r = new HashMap<String, String>(5);
			r.put("SM_RAW__0S", "^S1[A-B]_S[1-6]_RAW__0S.*$");
			r.put("AN_RAW__0S", "^S1[A-B]_N[1-6]_RAW__0S.*$");
			r.put("ZS_RAW__0S", "^S1[A-B]_N[1-6]_RAW__0S.*$");
			r.put("REP_L0PSA_", "^S1[A|B|_]_OPER_REP_ACQ.*$");
			r.put("REP_EFEP_", "^S1[A|B|_]_OPER_REP_PASS.*.EOF$");
			return r;
		}).when(l0ProcessSettings).getOutputregexps();
		Mockito.doAnswer(i -> {
			return ProcessLevel.L0;
		}).when(l0ProcessSettings).getLevel();
	}

	private void mockJobGeneratorSettings() {
		Mockito.doAnswer(i -> {
			Map<String, ProductFamily> r = new HashMap<>();
			r.put("", ProductFamily.L0_REPORT);
			r.put("", ProductFamily.L0_ACN);
			return r;
		}).when(jobGeneratorSettings).getOutputfamilies();
		Mockito.doAnswer(i -> {
			return "L0_PRODUCT";
		}).when(jobGeneratorSettings).getDefaultfamily();
		Mockito.doAnswer(i -> {
			return 2;
		}).when(jobGeneratorSettings).getMaxnumberofjobs();
		Mockito.doAnswer(i -> {
			return new WaitTempo(2000, 3);
		}).when(jobGeneratorSettings).getWaitprimarycheck();
		Mockito.doAnswer(i -> {
			return new WaitTempo(10000, 3);
		}).when(jobGeneratorSettings).getWaitmetadatainput();
	}

	private void mockXmlConverter() {
		try {
			Mockito.when(xmlConverter.convertFromXMLToObject(Mockito.anyString())).thenReturn(expectedTaskTable);
			Mockito.when(xmlConverter.convertFromObjectToXMLString(Mockito.any())).thenReturn(null);
		} catch (IOException | JAXBException e1) {
			fail("BuildTaskTableException raised: " + e1.getMessage());
		}
	}

	private void mockMetadataService() {
		try {
			Mockito.doAnswer(i -> {
				String productName = i.getArgument(1);
				Calendar start = Calendar.getInstance();
				start.set(2017, Calendar.DECEMBER, 5, 20, 3, 9);
				Calendar stop = Calendar.getInstance();
				stop.set(2017, Calendar.DECEMBER, 15, 20, 3, 9);
				if (productName.contains("ch1")) {
					return new EdrsSessionMetadata(productName, "RAW",
							"S1A/L20171109175634707000125/ch01/" + productName, null, null);
				} else {
					return new EdrsSessionMetadata(productName, "RAW",
							"S1A/L20171109175634707000125/ch02/" + productName, null, null);
				}
			}).when(this.metadataService).getEdrsSession(Mockito.anyString(), Mockito.anyString());
			Mockito.doAnswer(i -> {
				SearchMetadataQuery query = i.getArgument(0);
				if ("MPL_ORBPRE".equalsIgnoreCase(query.getProductType())) {
					return new SearchMetadata("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF",
							"MPL_ORBPRE", "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF",
							"2017-12-05T20:03:09", "2017-12-15T20:03:09");
				} else if ("MPL_ORBSCT".equalsIgnoreCase(query.getProductType())) {
					return new SearchMetadata("S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF",
							"MPL_ORBSCT", "S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF",
							"2014-04-03T22:46:09", "9999-12-31T23:59:59");
				} else if ("AUX_OBMEMC".equalsIgnoreCase(query.getProductType())) {
					return new SearchMetadata("S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml", "AUX_OBMEMC",
							"S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml", "2014-02-01T00:00:00",
							"9999-12-31T23:59:59");
				}
				return null;
			}).when(this.metadataService).search(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString(),
					Mockito.anyInt());
		} catch (MetadataException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testPreSearch() {
		EdrsSessionProduct session = TestL0Utils.buildEdrsSessionProduct(true);
		EdrsSessionProduct sessionComplete = TestL0Utils.buildEdrsSessionProduct(false);
		Job<EdrsSession> job = new Job<EdrsSession>(session);

		try {
			generator.preSearch(job);
			for (int i = 0; i < sessionComplete.getObject().getChannel1().getRawNames().size(); i++) {
				assertEquals(sessionComplete.getObject().getChannel1().getRawNames().get(i).getObjectStorageKey(),
						session.getObject().getChannel1().getRawNames().get(i).getObjectStorageKey());
			}
			for (int i = 0; i < sessionComplete.getObject().getChannel2().getRawNames().size(); i++) {
				assertEquals(sessionComplete.getObject().getChannel2().getRawNames().get(i).getObjectStorageKey(),
						session.getObject().getChannel2().getRawNames().get(i).getObjectStorageKey());
			}
		} catch (InputsMissingException e) {
			fail("MetadataMissingException raised: " + e.getMessage());
		}
	}

	@Test
	public void testPreSearchMissingRaw() throws MetadataException {
		Mockito.doAnswer(i -> {
			return null;
		}).when(this.metadataService).getEdrsSession(Mockito.anyString(), Mockito.anyString());

		EdrsSessionProduct session = TestL0Utils.buildEdrsSessionProduct(true);
		Job<EdrsSession> job = new Job<EdrsSession>(session);
		try {
			generator.preSearch(job);
			fail("MetadataMissingException shall be raised");
		} catch (InputsMissingException e) {
			assertTrue(e.getMissingMetadata().containsKey("DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw"));
			assertTrue(e.getMissingMetadata().containsKey("DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw"));
		}
	}

	@Test
	public void testCustomDto() {
		EdrsSessionProduct sessionComplete = TestL0Utils.buildEdrsSessionProduct(false);
		Job<EdrsSession> job = new Job<EdrsSession>(sessionComplete);
		job.setJobOrder(TestL0Utils.buildJobOrderL20171109175634707000125());
		JobDto dto = new JobDto(sessionComplete.getIdentifier(), "/data/test/workdir/",
				"/data/test/workdir/JobOrder.xml");

		generator.customJobDto(job, dto);
		int nbChannel1 = sessionComplete.getObject().getChannel1().getRawNames().size();
		int nbChannel2 = sessionComplete.getObject().getChannel2().getRawNames().size();
		assertTrue(dto.getInputs().size() == nbChannel1 + nbChannel2);
		for (int i = 0; i < nbChannel1; i++) {
			EdrsSessionFileRaw raw1 = sessionComplete.getObject().getChannel1().getRawNames().get(i);
			EdrsSessionFileRaw raw2 = sessionComplete.getObject().getChannel2().getRawNames().get(i);
			int indexRaw1 = i * 2;
			int indexRaw2 = i * 2 + 1;
			assertEquals(raw1.getObjectStorageKey(), dto.getInputs().get(indexRaw1).getContentRef());
			assertEquals(ProductFamily.RAW.name(), dto.getInputs().get(indexRaw1).getFamily());
			assertEquals("/data/test/workdir/ch01/" + raw1.getFileName(),
					dto.getInputs().get(indexRaw1).getLocalPath());
			assertEquals(raw2.getObjectStorageKey(), dto.getInputs().get(indexRaw2).getContentRef());
			assertEquals(ProductFamily.RAW.name(), dto.getInputs().get(indexRaw2).getFamily());
			assertEquals("/data/test/workdir/ch02/" + raw2.getFileName(),
					dto.getInputs().get(indexRaw2).getLocalPath());
		}
	}

	@Test
	public void testCustomJobOrder() {
		EdrsSessionProduct sessionComplete = TestL0Utils.buildEdrsSessionProduct(false);
		Job<EdrsSession> job = new Job<>(sessionComplete);
		job.setJobOrder(TestL0Utils.buildJobOrderL20171109175634707000125());
		generator.customJobOrder(job);
		job.getJobOrder().getConf().getProcParams().forEach(param -> {
			if ("Mission_Id".equals(param.getName())) {
				assertEquals("S1A", param.getValue());
			}
		});

		EdrsSessionProduct sessionComplete1 = TestL0Utils.buildEdrsSessionProduct(false);
		sessionComplete1.setMissionId("S2");
		Job<EdrsSession> job1 = new Job<EdrsSession>(sessionComplete1);
		job1.setJobOrder(TestL0Utils.buildJobOrderL20171109175634707000125());
		generator.customJobOrder(job1);
		job1.getJobOrder().getConf().getProcParams().forEach(param -> {
			if ("Mission_Id".equals(param.getName())) {
				assertEquals("S2A", param.getValue());
			}
		});
	}
}
