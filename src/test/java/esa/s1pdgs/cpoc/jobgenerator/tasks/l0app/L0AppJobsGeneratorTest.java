package esa.s1pdgs.cpoc.jobgenerator.tasks.l0app;

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
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobFileDto;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings.WaitTempo;
import esa.s1pdgs.cpoc.jobgenerator.model.JobGeneration;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadata;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadataQuery;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.metadata.MetadataService;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.jobgenerator.tasks.JobsGeneratorFactory;
import esa.s1pdgs.cpoc.jobgenerator.tasks.l0app.L0AppJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.utils.TestL0Utils;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;

/**
 * 
 * @author Cyrielle
 *
 */
public class L0AppJobsGeneratorTest {

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
	private OutputProducerFactory JobsSender;
	
	@Mock
	private AbstractAppCatalogJobService<EdrsSessionDto> appDataService;

	private TaskTable expectedTaskTable;
	private L0AppJobsGenerator generator;

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
				metadataService, JobsSender);
		generator = (L0AppJobsGenerator) factory.createJobGeneratorForEdrsSession(
				new File("./test/data/generic_config/task_tables/TaskTable.AIOP.xml"), appDataService);
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
			return ApplicationLevel.L0;
		}).when(l0ProcessSettings).getLevel();
        Mockito.doAnswer(i -> {
            return "hostname";
        }).when(l0ProcessSettings).getHostname();
	}

	private void mockJobGeneratorSettings() {
        Mockito.doAnswer(i -> {
            Map<String, ProductFamily> r =
                    new HashMap<String, ProductFamily>(20);
            String families =
                    "MPL_ORBPRE:AUXILIARY_FILE||MPL_ORBSCT:AUXILIARY_FILE||AUX_OBMEMC:AUXILIARY_FILE||AUX_CAL:AUXILIARY_FILE||AUX_PP1:AUXILIARY_FILE||AUX_INS:AUXILIARY_FILE||AUX_RESORB:AUXILIARY_FILE||AUX_RES:AUXILIARY_FILE";
            if (!StringUtils.isEmpty(families)) {
                String[] paramsTmp = families.split("\\|\\|");
                for (int k = 0; k < paramsTmp.length; k++) {
                    if (!StringUtils.isEmpty(paramsTmp[k])) {
                        String[] tmp = paramsTmp[k].split(":", 2);
                        if (tmp.length == 2) {
                            r.put(tmp[0], ProductFamily.fromValue(tmp[1]));
                        }
                    }
                }
            }
            return r;
        }).when(jobGeneratorSettings).getInputfamilies();
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
		} catch (JobGenMetadataException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testPreSearch() {
	    AppDataJobDto<EdrsSessionDto> appDataJob = TestL0Utils.buildAppDataEdrsSession(true);
        AppDataJobDto<EdrsSessionDto> appDataJobComplete = TestL0Utils.buildAppDataEdrsSession(false);
		JobGeneration<EdrsSessionDto> job = new JobGeneration<>(appDataJob, "TaskTable.AIOP.xml");

		try {
			generator.preSearch(job);
			for (int i = 0; i < appDataJobComplete.getProduct().getRaws1().size(); i++) {
				assertEquals(appDataJobComplete.getProduct().getRaws1().get(i).getKeyObs(),
				        appDataJob.getProduct().getRaws1().get(i).getKeyObs());
			}
			for (int i = 0; i < appDataJobComplete.getProduct().getRaws2().size(); i++) {
				assertEquals(appDataJobComplete.getProduct().getRaws2().get(i).getKeyObs(),
				        appDataJob.getProduct().getRaws2().get(i).getKeyObs());
			}
		} catch (JobGenInputsMissingException e) {
			fail("MetadataMissingException raised: " + e.getMessage());
		}
	}

	@Test
	public void testPreSearchMissingRaw() throws JobGenMetadataException {
		Mockito.doAnswer(i -> {
			return null;
		}).when(this.metadataService).getEdrsSession(Mockito.anyString(), Mockito.anyString());

        AppDataJobDto<EdrsSessionDto> appDataJob = TestL0Utils.buildAppDataEdrsSession(true);
        JobGeneration<EdrsSessionDto> job = new JobGeneration<>(appDataJob, "TaskTable.AIOP.xml");
		try {
			generator.preSearch(job);
			fail("MetadataMissingException shall be raised");
		} catch (JobGenInputsMissingException e) {
			assertTrue(e.getMissingMetadata().containsKey("DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw"));
			assertTrue(e.getMissingMetadata().containsKey("DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw"));
		}
	}

	@Test
	public void testCustomDto() {
        AppDataJobDto<EdrsSessionDto> appDataJob = TestL0Utils.buildAppDataEdrsSession(false);
        JobGeneration<EdrsSessionDto> job = new JobGeneration<>(appDataJob, "TaskTable.AIOP.xml");
		job.setJobOrder(TestL0Utils.buildJobOrderL20171109175634707000125());
		ProductFamily family = ProductFamily.EDRS_SESSION;
		LevelJobDto dto = new LevelJobDto(family, appDataJob.getProduct().getProductName(), "/data/test/workdir/",
				"/data/test/workdir/JobOrder.xml");

		generator.customJobDto(job, dto);
		int nbChannel1 = appDataJob.getProduct().getRaws1().size();
		int nbChannel2 = appDataJob.getProduct().getRaws2().size();
		assertTrue(dto.getInputs().size() == nbChannel1 + nbChannel2);
		for (int i = 0; i < nbChannel1; i++) {
			AppDataJobFileDto raw1 = appDataJob.getProduct().getRaws1().get(i);
			AppDataJobFileDto raw2 = appDataJob.getProduct().getRaws2().get(i);
			int indexRaw1 = i * 2;
			int indexRaw2 = i * 2 + 1;
			assertEquals(raw1.getKeyObs(), dto.getInputs().get(indexRaw1).getContentRef());
			assertEquals(ProductFamily.EDRS_SESSION.name(), dto.getInputs().get(indexRaw1).getFamily());
			assertEquals("/data/test/workdir/ch01/" + raw1.getFilename(),
					dto.getInputs().get(indexRaw1).getLocalPath());
			assertEquals(raw2.getKeyObs(), dto.getInputs().get(indexRaw2).getContentRef());
			assertEquals(ProductFamily.EDRS_SESSION.name(), dto.getInputs().get(indexRaw2).getFamily());
			assertEquals("/data/test/workdir/ch02/" + raw2.getFilename(),
					dto.getInputs().get(indexRaw2).getLocalPath());
		}
	}

	@Test
	public void testCustomJobOrder() {
        AppDataJobDto<EdrsSessionDto> appDataJob = TestL0Utils.buildAppDataEdrsSession(false);
        JobGeneration<EdrsSessionDto> job = new JobGeneration<>(appDataJob, "TaskTable.AIOP.xml");
		job.setJobOrder(TestL0Utils.buildJobOrderL20171109175634707000125());
		generator.customJobOrder(job);
		job.getJobOrder().getConf().getProcParams().forEach(param -> {
			if ("Mission_Id".equals(param.getName())) {
				assertEquals("S1A", param.getValue());
			}
		});

        AppDataJobDto<EdrsSessionDto> appDataJob1 = TestL0Utils.buildAppDataEdrsSession(false, "S2", true, true);
        JobGeneration<EdrsSessionDto> job1 = new JobGeneration<>(appDataJob1, "TaskTable.AIOP.xml");
		job1.setJobOrder(TestL0Utils.buildJobOrderL20171109175634707000125());
		generator.customJobOrder(job1);
		job1.getJobOrder().getConf().getProcParams().forEach(param -> {
			if ("Mission_Id".equals(param.getName())) {
				assertEquals("S2A", param.getValue());
			}
		});
	}
}
