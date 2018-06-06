package fr.viveris.s1pdgs.jobgenerator.tasks.generator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.viveris.s1pdgs.jobgenerator.config.AppConfig;
import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings.WaitTempo;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.controller.JobsProducer;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;
import fr.viveris.s1pdgs.jobgenerator.exception.MaxNumberCachedJobsReachException;
import fr.viveris.s1pdgs.jobgenerator.exception.MetadataException;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException;
import fr.viveris.s1pdgs.jobgenerator.exception.BuildTaskTableException;
import fr.viveris.s1pdgs.jobgenerator.exception.InputsMissingException;
import fr.viveris.s1pdgs.jobgenerator.model.GenerationStatusEnum;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.ProcessLevel;
import fr.viveris.s1pdgs.jobgenerator.model.ProductFamily;
import fr.viveris.s1pdgs.jobgenerator.model.ProductMode;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadataQuery;
import fr.viveris.s1pdgs.jobgenerator.model.product.AbstractProduct;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTable;
import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;
import fr.viveris.s1pdgs.jobgenerator.service.metadata.MetadataService;
import fr.viveris.s1pdgs.jobgenerator.utils.TestGenericUtils;

public class AbstractJobsGeneratorTest {

	/**
	 * To check the raised custom exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private XmlConverter xmlConverter;

	@Mock
	private MetadataService metadataService;

	@Mock
	private ProcessSettings processSettings;

	@Mock
	private JobGeneratorSettings jobGeneratorSettings;

	@Mock
	private JobsProducer kafkaJobsSender;

	private int nbLoopMetadata;

	private AbstractJobsGeneratorImpl generator;
	

	private TaskTable expectedTaskTable;

	/**
	 * Test set up
	 * 
	 * @throws Exception
	 */
	@Before
	public void init() throws Exception {
		this.nbLoopMetadata = 0;
		expectedTaskTable = TestGenericUtils.buildTaskTableIW();

		// Mcokito
		MockitoAnnotations.initMocks(this);

		// Mock process settings
		this.mockProcessSettings();

		// Mock job generator settings
		this.mockJobGeneratorSettings();

		// Mock XML converter
		this.mockXmlConverter(expectedTaskTable);

		// Mock metadata service
		this.mockMetadataService(0, 0);

		this.mockKafkaSender();

		generator = new AbstractJobsGeneratorImpl(xmlConverter, metadataService, processSettings, jobGeneratorSettings,
				kafkaJobsSender);
		generator.initialize(new File("./test/data/generic_config/task_tables/IW_RAW__0_GRDH_1.xml"));
		generator.setMode(ProductMode.SLICING);
	}

	private void mockProcessSettings() {
		Mockito.doAnswer(i -> {
			Map<String, String> r = new HashMap<String, String>(2);
			return r;
		}).when(processSettings).getParams();
		Mockito.doAnswer(i -> {
			Map<String, String> r = new HashMap<String, String>(5);
			r.put("SM_RAW__0S", "^S1[A-B]_S[1-6]_RAW__0S.*$");
			r.put("AN_RAW__0S", "^S1[A-B]_N[1-6]_RAW__0S.*$");
			r.put("ZS_RAW__0S", "^S1[A-B]_N[1-6]_RAW__0S.*$");
			r.put("REP_L0PSA_", "^S1[A|B|_]_OPER_REP_ACQ.*$");
			r.put("REP_EFEP_", "^S1[A|B|_]_OPER_REP_PASS.*.EOF$");
			return r;
		}).when(processSettings).getOutputregexps();
		Mockito.doAnswer(i -> {
			return ProcessLevel.L0;
		}).when(processSettings).getLevel();
	}

	private void mockJobGeneratorSettings() {
		Mockito.doAnswer(i -> {
			Map<String, ProductFamily> r = new HashMap<>();
			r.put("IW_GRDH_1S", ProductFamily.L1_PRODUCT);
			r.put("IW_GRDH_1A", ProductFamily.L1_ACN);
			r.put("IW_RAW__0S", ProductFamily.L0_PRODUCT);
			r.put("IW_RAW__0A", ProductFamily.L0_ACN);
			r.put("IW_RAW__0C", ProductFamily.L0_ACN);
			r.put("IW_RAW__0N", ProductFamily.L0_ACN);
			return r;
		}).when(jobGeneratorSettings).getOutputfamilies();
		Mockito.doAnswer(i -> {
			return ProductFamily.CONFIG.name();
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

	private void mockXmlConverter(TaskTable table) throws IOException, JAXBException {
		Mockito.when(xmlConverter.convertFromXMLToObject(Mockito.anyString())).thenReturn(table);
		Mockito.doAnswer(i -> {
			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
			ctx.register(AppConfig.class);
			ctx.refresh();
			XmlConverter xmlConverter = ctx.getBean(XmlConverter.class);
			String r = xmlConverter.convertFromObjectToXMLString(i.getArgument(0));
			ctx.close();
			return r;
		}).when(xmlConverter).convertFromObjectToXMLString(Mockito.any());
	}

	private void mockMetadataService(int maxLoop1, int maxLoop2) {
		try {
			Mockito.doAnswer(i -> {
				return null;
			}).when(this.metadataService).getEdrsSession(Mockito.anyString(), Mockito.anyString());
			Mockito.doAnswer(i -> {
				if (this.nbLoopMetadata >= maxLoop2) {
					this.nbLoopMetadata = 0;
					SearchMetadataQuery query = i.getArgument(0);
					if ("IW_RAW__0S".equalsIgnoreCase(query.getProductType())) {
						return new SearchMetadata(
								"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
								"IW_RAW__0S",
								"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
								"2017-12-13T12:16:23", "2017-12-13T12:16:56");
					} else if ("IW_RAW__0A".equalsIgnoreCase(query.getProductType())) {
						return new SearchMetadata(
								"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
								"IW_RAW__0A",
								"S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
								"2017-12-13T12:11:23", "2017-12-13T12:19:47");
					} else if ("IW_RAW__0C".equalsIgnoreCase(query.getProductType())) {
						return new SearchMetadata(
								"S1A_IW_RAW__0CDV_20171213T121123_20171213T121947_019684_021735_E131.SAFE",
								"IW_RAW__0C",
								"S1A_IW_RAW__0CDV_20171213T121123_20171213T121947_019684_021735_E131.SAFE",
								"2017-12-13T12:11:23", "2017-12-13T12:19:47");
					} else if ("IW_RAW__0N".equalsIgnoreCase(query.getProductType())) {
						return new SearchMetadata(
								"S1A_IW_RAW__0NDV_20171213T121123_20171213T121947_019684_021735_87D4.SAFE",
								"IW_RAW__0N",
								"S1A_IW_RAW__0NDV_20171213T121123_20171213T121947_019684_021735_87D4.SAFE",
								"2017-12-13T12:11:23", "2017-12-13T12:19:47");
					} else if ("AUX_CAL".equalsIgnoreCase(query.getProductType())) {
						return new SearchMetadata("S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE", "AUX_CAL",
								"S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE", "2017-10-17T08:00:00",
								"9999-12-31T23:59:59");
					} else if ("AUX_INS".equalsIgnoreCase(query.getProductType())) {
						return new SearchMetadata("S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE", "AUX_INS",
								"S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE", "2017-10-17T08:00:00",
								"9999-12-31T23:59:59");
					} else if ("AUX_PP1".equalsIgnoreCase(query.getProductType())) {
						return new SearchMetadata("S1A_AUX_PP1_V20171017T080000_G20171013T101236.SAFE", "AUX_PP1",
								"S1A_AUX_PP1_V20171017T080000_G20171013T101236.SAFE", "2017-10-17T08:00:00",
								"9999-12-31T23:59:59");
					} else if ("AUX_RES".equalsIgnoreCase(query.getProductType())) {
						return new SearchMetadata(
								"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
								"AUX_OBMEMC",
								"S1A_OPER_AUX_RESORB_OPOD_20171213T143838_V20171213T102737_20171213T134507.EOF",
								"2017-12-13T10:27:37", "2017-12-13T13:45:07");
					}
				}
				return null;
			}).when(this.metadataService).search(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString(),
					Mockito.anyInt());
		} catch (MetadataException e) {
			fail(e.getMessage());
		}
	}

	private void mockKafkaSender() throws AbstractCodedException {
		Mockito.doAnswer(i -> {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(new File("./tmp/jobDtoGeneric.json"), i.getArgument(0));
			return null;
		}).when(this.kafkaJobsSender).send(Mockito.any());
	}

	// ---------------------------------------------------------
	// INITIALIZATION
	// @see JobsGeneratorFactoryTest
	// ---------------------------------------------------------

	// ---------------------------------------------------------
	// CACHED JOBS
	// ---------------------------------------------------------

	@Test
	public void testAddJobs() {
		AstractProductImpl p1 = new AstractProductImpl("SESSION_1", "A", "S1A", new Date(), new Date(), "product1");
		Job<String> job1 = new Job<String>(p1);
		AstractProductImpl p2 = new AstractProductImpl("SESSION_2", "A", "S1A", new Date(), new Date(), "product2");
		Job<String> job2 = new Job<String>(p2);

		try {
			generator.addJob(job1);
			assertEquals(1, generator.cachedJobs.size());
			assertTrue(generator.cachedJobs.containsKey("SESSION_1"));
			Job<String> r1 = generator.cachedJobs.get("SESSION_1");
			assertEquals(generator.taskTableXmlName, r1.getTaskTableName());
			assertEquals(generator.jobOrderTemplate, r1.getJobOrder());
			assertEquals(generator.metadataSearchQueries.size(), r1.getMetadataQueries().size());
			Integer oneKey = generator.metadataSearchQueries.keySet().iterator().next();
			assertEquals(generator.metadataSearchQueries.get(oneKey), r1.getMetadataQueries().get(oneKey).getQuery());

			generator.addJob(job2);
			assertEquals(2, generator.cachedJobs.size());
			assertTrue(generator.cachedJobs.containsKey("SESSION_2"));
		} catch (MaxNumberCachedJobsReachException e) {
			fail("SessionProcessingException raised: " + e.getMessage());
		}
	}

	@Test(expected = MaxNumberCachedJobsReachException.class)
	public void testAddJobsMaxNumber() throws MaxNumberCachedJobsReachException {
		AstractProductImpl p1 = new AstractProductImpl("SESSION_1", "A", "S1A", new Date(), new Date(), "product1");
		Job<String> job1 = new Job<String>(p1);
		AstractProductImpl p2 = new AstractProductImpl("SESSION_2", "A", "S1A", new Date(), new Date(), "product2");
		Job<String> job2 = new Job<String>(p2);
		AstractProductImpl p3 = new AstractProductImpl("SESSION_3", "A", "S1A", new Date(), new Date(), "product3");
		Job<String> job3 = new Job<String>(p3);

		try {
			generator.addJob(job1);
			assertEquals(1, generator.cachedJobs.size());
			assertTrue(generator.cachedJobs.containsKey("SESSION_1"));
			generator.addJob(job2);
		} catch (MaxNumberCachedJobsReachException e) {
			fail("SessionProcessingException raised: " + e.getMessage());
		}

		generator.addJob(job3);
	}

	@Test
	public void testAddSessionExist() {
		AstractProductImpl p1 = new AstractProductImpl("SESSION_1", "A", "S1A", new Date(), new Date(), "product1");
		Job<String> job1 = new Job<String>(p1);
		AstractProductImpl p2 = new AstractProductImpl("SESSION_1", "A", "S1A", new Date(), new Date(), "product2");
		Job<String> job2 = new Job<String>(p2);

		try {
			generator.addJob(job1);
			generator.addJob(job2);
			assertEquals(1, generator.cachedJobs.size());
			assertTrue(generator.cachedJobs.containsKey("SESSION_1"));
		} catch (MaxNumberCachedJobsReachException e) {
			fail("SessionProcessingException raised: " + e.getMessage());
		}
	}

	// ---------------------------------------------------------
	// CACHED RUN
	// ---------------------------------------------------------

	@Test
	public void testRun() {
		try {
			AstractProductImpl p1 = new AstractProductImpl("SESSION_1", "A", "S1A", new Date(), new Date(), "product1");
			Job<String> job1 = new Job<String>(p1);

			generator.addJob(job1);
			generator.run();

			Mockito.verify(kafkaJobsSender).send(Mockito.any());

			// As job is not removed from cached if no another run is launch we can it
			assertFalse(generator.cachedJobs.containsKey("SESSION_1"));
			// TODO to improve to check dto ok (do manually by reading the file
			// ./tmp/jobDto.txt)
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	// ---------------------------------------------------------
	// Test initialize
	// ---------------------------------------------------------
	
	@Test
	public void testInitializeWhenTaskTableIOException() throws IOException, JAXBException, BuildTaskTableException {
		doThrow(new IOException("IO exception raised")).when(xmlConverter).convertFromXMLToObject(Mockito.anyString());
		AbstractJobsGeneratorImpl gen = new AbstractJobsGeneratorImpl(xmlConverter, metadataService, processSettings, jobGeneratorSettings,
				kafkaJobsSender);
		generator.setMode(ProductMode.SLICING);
		
		thrown.expect(BuildTaskTableException.class);
		thrown.expect(hasProperty("taskTable", is("IW_RAW__0_GRDH_1.xml")));
		thrown.expectMessage("IO exception raised");
		thrown.expectCause(isA(IOException.class));
		gen.initialize(new File("./test/data/generic_config/task_tables/IW_RAW__0_GRDH_1.xml"));
	}
	
	@Test
	public void testInitializeWhenTaskTableJAXBException() throws IOException, JAXBException, BuildTaskTableException {
		doThrow(new JAXBException("JAXB exception raised")).when(xmlConverter).convertFromXMLToObject(Mockito.anyString());
		AbstractJobsGeneratorImpl gen = new AbstractJobsGeneratorImpl(xmlConverter, metadataService, processSettings, jobGeneratorSettings,
				kafkaJobsSender);
		generator.setMode(ProductMode.SLICING);
		
		thrown.expect(BuildTaskTableException.class);
		thrown.expect(hasProperty("taskTable", is("IW_RAW__0_GRDH_1.xml")));
		thrown.expectMessage("JAXB exception raised");
		thrown.expectCause(isA(JAXBException.class));
		gen.initialize(new File("./test/data/generic_config/task_tables/IW_RAW__0_GRDH_1.xml"));
	}

	// ---------------------------------------------------------
	// Test remove entries
	// ---------------------------------------------------------
	
	public void testRemoveJobNotReadyForTooLong() {
		doReturn(new WaitTempo(100, 2)).when(jobGeneratorSettings).getWaitmetadatainput();
		doReturn(new WaitTempo(100, 1)).when(jobGeneratorSettings).getWaitprimarycheck();
		
		AstractProductImpl p1 = new AstractProductImpl("SESSION_1", "A", "S1A", new Date(), new Date(), "product1");
		Job<String> job1 = new Job<String>(p1);
		generator.cachedJobs.put("test-job", job1);
		
		// here nbretries = 1
		job1.getStatus().updateStatus(GenerationStatusEnum.NOT_READY);
		generator.removeNotReadyJobsForToolLong();
		assertTrue(job1.getStatus().getNbRetries() == 1);
		assertTrue(generator.cachedJobs.size() == 1);
		
		// here nbretries = 2
		job1.getStatus().updateStatus(GenerationStatusEnum.NOT_READY);
		generator.removeNotReadyJobsForToolLong();
		assertTrue(generator.cachedJobs.size() == 0);
		
	}
	
	public void testRemoveJobPrimaryCheckForTooLong() {
		doReturn(new WaitTempo(100, 1)).when(jobGeneratorSettings).getWaitmetadatainput();
		doReturn(new WaitTempo(100, 2)).when(jobGeneratorSettings).getWaitprimarycheck();
		
		AstractProductImpl p1 = new AstractProductImpl("SESSION_1", "A", "S1A", new Date(), new Date(), "product1");
		Job<String> job1 = new Job<String>(p1);
		generator.cachedJobs.put("test-job", job1);
		
		// here nbretries = 0
		job1.getStatus().updateStatus(GenerationStatusEnum.PRIMARY_CHECK);
		generator.removeNotReadyJobsForToolLong();
		assertTrue(job1.getStatus().getNbRetries() == 0);
		assertTrue(generator.cachedJobs.size() == 1);
		
		// here nbretries = 1
		job1.getStatus().updateStatus(GenerationStatusEnum.PRIMARY_CHECK);
		generator.removeNotReadyJobsForToolLong();
		assertTrue(job1.getStatus().getNbRetries() == 1);
		assertTrue(generator.cachedJobs.size() == 1);
		
		// here nbretries = 2
		job1.getStatus().updateStatus(GenerationStatusEnum.PRIMARY_CHECK);
		generator.removeNotReadyJobsForToolLong();
		assertTrue(generator.cachedJobs.size() == 0);
		
	}
}

class AbstractJobsGeneratorImpl extends AbstractJobsGenerator<String> {

	private int counterPreSearch;
	private int counterCustomJobOrder;
	private int counterJobDto;

	public AbstractJobsGeneratorImpl(XmlConverter xmlConverter, MetadataService metadataService,
			ProcessSettings l0ProcessSettings, JobGeneratorSettings taskTablesSettings, JobsProducer kafkaJobsSender) {
		super(xmlConverter, metadataService, l0ProcessSettings, taskTablesSettings, kafkaJobsSender);
		counterPreSearch = 0;
		counterCustomJobOrder = 0;
		counterJobDto = 0;
	}

	@Override
	protected void preSearch(Job<String> job) throws InputsMissingException {
		counterPreSearch++;

	}

	@Override
	protected void customJobOrder(Job<String> job) {
		counterCustomJobOrder++;

	}

	@Override
	protected void customJobDto(Job<String> job, JobDto dto) {
		counterJobDto++;

	}

	/**
	 * @return the counterPreSearch
	 */
	public int getCounterPreSearch() {
		return counterPreSearch;
	}

	/**
	 * @return the counterCustomJobOrder
	 */
	public int getCounterCustomJobOrder() {
		return counterCustomJobOrder;
	}

	/**
	 * @return the counterJobDto
	 */
	public int getCounterJobDto() {
		return counterJobDto;
	}

}

class AstractProductImpl extends AbstractProduct<String> {

	public AstractProductImpl(String identifier, String satelliteId, String missionId, Date startTime, Date stopTime,
			String object) {
		super(identifier, satelliteId, missionId, startTime, stopTime, object, "");
	}

}