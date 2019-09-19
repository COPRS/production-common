package esa.s1pdgs.cpoc.jobgenerator.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.jobgenerator.tasks.JobsGeneratorFactory;
import esa.s1pdgs.cpoc.jobgenerator.tasks.l0app.L0AppConsumer;
import esa.s1pdgs.cpoc.jobgenerator.tasks.l0app.L0AppJobDispatcher;
import esa.s1pdgs.cpoc.jobgenerator.tasks.l0segmentapp.L0SegmentAppConsumer;
import esa.s1pdgs.cpoc.jobgenerator.tasks.l0segmentapp.L0SegmentAppJobDispatcher;
import esa.s1pdgs.cpoc.jobgenerator.tasks.levelproducts.LevelProductsJobDispatcher;
import esa.s1pdgs.cpoc.jobgenerator.tasks.levelproducts.LevelProductsMessageConsumer;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Extraction class of "tasktables" configuration properties
 * 
 * @author Cyrielle Gailliard
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "job-generator")
public class JobGeneratorSettings {

	/**
	 * Separator use to seperate the elements of a map in a string format
	 */
	protected static final String MAP_ELM_SEP = "\\|\\|";

	/**
	 * Separator use to separate the key and the value of a map element in a string
	 * format
	 */
	protected static final String MAP_KEY_VAL_SEP = ":";

	/**
	 * Maximal number of task tables
	 */
	private int maxnboftasktable;

	/**
	 * Maximal number of jobs waiting to be sent
	 */
	private int maxnumberofjobs;

	/**
	 * Delay configuration between 2 check of raw metadata presence for a session
	 */
	private WaitTempo waitprimarycheck;

	/**
	 * Delay configuration between 2 check of inputs metadata search for a job
	 */
	private WaitTempo waitmetadatainput;

	/**
	 * Location of task table XML files
	 */
	private String diroftasktables;

	/**
	 * Period (fixed rate)
	 */
	private int jobgenfixedrate;

	/**
	 * Default family of products
	 */
	private String defaultfamily;

	/**
	 * Map between output product type and product family in string format.<br/>
	 * Format: {type_1}:{family_1}||{type_2}:{family_2}||...||{type_n}:{family_n}
	 */
	private String inputfamiliesstr;

	/**
	 * Map between output product type and product family
	 */
	private Map<String, ProductFamily> inputfamilies;

	/**
	 * Map between output product type and product family in string format.<br/>
	 * Format: {type_1}:{family_1}||{type_2}:{family_2}||...||{type_n}:{family_n}
	 */
	private String outputfamiliesstr;

	/**
	 * Map between output product type and product family
	 */
	private Map<String, ProductFamily> outputfamilies;

	/**
	 * Map of all the overlap for the different slice type
	 */
	private Map<String, Float> typeOverlap;

	/**
	 * Map of all the length for the different slice type<br/>
	 * Format: acquisition in IW, EW, SM, EM
	 */
	private Map<String, Float> typeSliceLength;

	/**
	 * Map product type and corresponding metadata index in case of the product type
	 * in lowercase in not the metadata index (example: aux_resorb use aux_res)<br/>
	 */
	private Map<String, String> mapTypeMeta;

//    private ProcessSettings processSettings;

	/**
	 * Default constructors
	 */
	public JobGeneratorSettings() {
//        this.processSettings = processSettings;
		this.inputfamilies = new HashMap<>();
		this.outputfamilies = new HashMap<>();

	}

	/**
	 * Initialization function:
	 * <li>Build maps by splitting the corresponding string (note: we cannot map
	 * configuration parameter directly in a map due to the use of K8S configuration
	 * map)</li>
	 */
	@PostConstruct
	public void initMaps() {
		extractMapProductTypeFamilyInput();
		extractMapProductTypeFamilyOutput();
	}

	/**
	 * Extract map product type family from the string
	 */
	private void extractMapProductTypeFamilyInput() {
		if (StringUtils.isEmpty(inputfamiliesstr)) {
			return;
		}
		String[] paramsTmp = inputfamiliesstr.split(MAP_ELM_SEP);
		for (int i = 0; i < paramsTmp.length; i++) {
			String[] tmp = paramsTmp[i].split(MAP_KEY_VAL_SEP);
			if (tmp != null && tmp.length == 2) {
				String key = tmp[0];
				String valStr = tmp[1];
				inputfamilies.put(key, ProductFamily.valueOf(valStr));
			}
		}
	}

	/**
	 * Extract map product type family from the string
	 */
	private void extractMapProductTypeFamilyOutput() {
		if (StringUtils.isEmpty(outputfamiliesstr)) {
			return;
		}
		String[] paramsTmp = outputfamiliesstr.split(MAP_ELM_SEP);
		for (int i = 0; i < paramsTmp.length; i++) {
			String[] tmp = paramsTmp[i].split(MAP_KEY_VAL_SEP);
			if (tmp != null && tmp.length == 2) {
				String key = tmp[0];
				String valStr = tmp[1];
				outputfamilies.put(key, ProductFamily.valueOf(valStr));
			}
		}
	}

	/**
	 * Class of delay configuration
	 * 
	 * @author Cyrielle Gailliard
	 */
	public static class WaitTempo {
		/**
		 * Delay between 2 retries
		 */
		private int tempo;

		/**
		 * Number of maximal retries
		 */
		private int maxTimelifeS;

		/**
		 * Default constructor
		 */
		public WaitTempo() {
			this.tempo = 0;
			this.maxTimelifeS = 0;
		}

		/**
		 * Constructor using field
		 * 
		 * @param tempo
		 * @param retries
		 */
		public WaitTempo(final int tempo, final int retries) {
			this.tempo = tempo;
			this.maxTimelifeS = retries;
		}

		/**
		 * @return the tempo
		 */
		public int getTempo() {
			return tempo;
		}

		/**
		 * @param tempo the tempo to set
		 */
		public void setTempo(final int tempo) {
			this.tempo = tempo;
		}

		/**
		 * @return the retries
		 */
		public int getMaxTimelifeS() {
			return maxTimelifeS;
		}

		/**
		 * @param retries the retries to set
		 */
		public void setMaxTimelifeS(final int maxTimelifeS) {
			this.maxTimelifeS = maxTimelifeS;
		}

	}

	/**
	 * @return the maxnboftasktable
	 */
	public int getMaxnboftasktable() {
		return maxnboftasktable;
	}

	/**
	 * @param maxnboftasktable the maxnboftasktable to set
	 */
	public void setMaxnboftasktable(final int maxnboftasktable) {
		this.maxnboftasktable = maxnboftasktable;
	}

	/**
	 * @return the waitprimarycheck
	 */
	public WaitTempo getWaitprimarycheck() {
		return waitprimarycheck;
	}

	/**
	 * @param waitprimarycheck the waitprimarycheck to set
	 */
	public void setWaitprimarycheck(final WaitTempo waitprimarycheck) {
		this.waitprimarycheck = waitprimarycheck;
	}

	/**
	 * @return the waitmetadatainput
	 */
	public WaitTempo getWaitmetadatainput() {
		return waitmetadatainput;
	}

	/**
	 * @param waitmetadatainput the waitmetadatainput to set
	 */
	public void setWaitmetadatainput(final WaitTempo waitmetadatainput) {
		this.waitmetadatainput = waitmetadatainput;
	}

	/**
	 * @return the diroftasktables
	 */
	public String getDiroftasktables() {
		return diroftasktables;
	}

	/**
	 * @param diroftasktables the diroftasktables to set
	 */
	public void setDiroftasktables(final String diroftasktables) {
		this.diroftasktables = diroftasktables;
	}

	/**
	 * @return the maxnumberofjobs
	 */
	public int getMaxnumberofjobs() {
		return maxnumberofjobs;
	}

	/**
	 * @param maxnumberofjobs the maxnumberofjobs to set
	 */
	public void setMaxnumberofjobs(final int maxnumberofjobs) {
		this.maxnumberofjobs = maxnumberofjobs;
	}

	/**
	 * @return the jobgenfixedrate
	 */
	public int getJobgenfixedrate() {
		return jobgenfixedrate;
	}

	/**
	 * @param jobgenfixedrate the jobgenfixedrate to set
	 */
	public void setJobgenfixedrate(final int jobgenfixedrate) {
		this.jobgenfixedrate = jobgenfixedrate;
	}

	/**
	 * @return the defaultfamily
	 */
	public String getDefaultfamily() {
		return defaultfamily;
	}

	/**
	 * @param defaultfamily the defaultfamily to set
	 */
	public void setDefaultfamily(final String defaultfamily) {
		this.defaultfamily = defaultfamily;
	}

	/**
	 * @return the outputfamiliesstr
	 */
	public String getOutputfamiliesstr() {
		return outputfamiliesstr;
	}

	/**
	 * @param outputfamiliesstr the outputfamiliesstr to set
	 */
	public void setOutputfamiliesstr(final String outputfamiliesstr) {
		this.outputfamiliesstr = outputfamiliesstr;
	}

	/**
	 * @return the outputfamilies
	 */
	public Map<String, ProductFamily> getOutputfamilies() {
		return outputfamilies;
	}

	/**
	 * @return the inputfamiliesstr
	 */
	public String getInputfamiliesstr() {
		return inputfamiliesstr;
	}

	/**
	 * @param inputfamiliesstr the inputfamiliesstr to set
	 */
	public void setInputfamiliesstr(String inputfamiliesstr) {
		this.inputfamiliesstr = inputfamiliesstr;
	}

	/**
	 * @return the inputfamilies
	 */
	public Map<String, ProductFamily> getInputfamilies() {
		return inputfamilies;
	}

	/**
	 * @return the typeOverlap
	 */
	public Map<String, Float> getTypeOverlap() {
		return typeOverlap;
	}

	/**
	 * @return the typeSliceLength
	 */
	public Map<String, Float> getTypeSliceLength() {
		return typeSliceLength;
	}

	/**
	 * @return the mapTypeMeta
	 */
	public Map<String, String> getMapTypeMeta() {
		return mapTypeMeta;
	}

	/**
	 * 
	 * @param typeOverlap
	 */
	public void setTypeOverlap(final Map<String, Float> typeOverlap) {
		this.typeOverlap = typeOverlap;
	}

	/**
	 * 
	 * @param typeSliceLength
	 */
	public void setTypeSliceLength(final Map<String, Float> typeSliceLength) {
		this.typeSliceLength = typeSliceLength;
	}

	/**
	 * 
	 * @param mapTypeMeta
	 */
	public void setMapTypeMeta(final Map<String, String> mapTypeMeta) {
		this.mapTypeMeta = mapTypeMeta;
	}

	/**
	 * Display object in JSON format
	 */
	@Override
	public String toString() {
		return "{maxnboftasktable: " + maxnboftasktable + ", maxnumberofjobs: " + maxnumberofjobs
				+ ", waitprimarycheck: " + waitprimarycheck + ", waitmetadatainput: " + waitmetadatainput
				+ ", diroftasktables: " + diroftasktables + ", jobgenfixedrate: " + jobgenfixedrate
				+ ", defaultfamily: " + defaultfamily + ", outputfamiliesstr: " + outputfamiliesstr
				+ ", outputfamilies: " + outputfamilies + ", typeOverlap: " + typeOverlap + ", typeSliceLength: "
				+ typeSliceLength + ", mapTypeMeta: " + mapTypeMeta + "}";
	}

	@Bean
	@Autowired
	public AbstractJobsDispatcher<? extends AbstractDto> jobsDispatcher(final ProcessSettings processSettings,
			final JobsGeneratorFactory factory, final ThreadPoolTaskScheduler taskScheduler,
			final XmlConverter xmlConverter,
			@Value("${level-products.pathroutingxmlfile}") final String pathRoutingXmlFile,
			@Qualifier("appCatalogServiceForLevelProducts") final AppCatalogJobClient appDataServiceLevelProducts,
			@Qualifier("appCatalogServiceForEdrsSessions") final AppCatalogJobClient appDataServiceErdsSettions,
			@Qualifier("appCatalogServiceForLevelSegments") final AppCatalogJobClient appDataServiceLevelSegments) {

		AbstractJobsDispatcher<? extends AbstractDto> jobsDispatcher;

		switch (processSettings.getLevel()) {
		case L0:
			jobsDispatcher = new L0AppJobDispatcher(this, processSettings, factory, taskScheduler,
					appDataServiceErdsSettions);
			break;
		case L0_SEGMENT:
			jobsDispatcher = new L0SegmentAppJobDispatcher(this, processSettings, factory, taskScheduler,
					appDataServiceLevelSegments);
			break;
		case L1:
		case L2:
			jobsDispatcher = new LevelProductsJobDispatcher(this, processSettings, factory, taskScheduler, xmlConverter,
					pathRoutingXmlFile, appDataServiceLevelProducts);
			break;
		default:
			throw new IllegalArgumentException("Unsupported Application Level");
		}
		jobsDispatcher.setTaskForFunctionalLog(String.format("%sJobGeneration", processSettings.getLevel().name()));
		return jobsDispatcher;

	}

	@SuppressWarnings("unchecked")
	@Bean
	@Autowired
	public AbstractGenericConsumer<? extends AbstractDto> productMessageConsumer(
			final AbstractJobsDispatcher<? extends AbstractDto> jobsDispatcher,
			final L0SegmentAppProperties appProperties, final L0SlicePatternSettings patternSettings,
			final ProcessSettings processSettings, final GenericMqiClient mqiService,
			final StatusService mqiStatusService,
			@Qualifier("appCatalogServiceForLevelProducts") final AppCatalogJobClient appDataServiceLevelProducts,
			@Qualifier("appCatalogServiceForEdrsSessions") final AppCatalogJobClient appDataServiceErdsSettions,
			@Qualifier("appCatalogServiceForLevelSegments") final AppCatalogJobClient appDataServiceLevelSegments,
			final ErrorRepoAppender errorRepoAppender, final AppStatus appStatus, final MetadataClient metadataClient) {

		AbstractGenericConsumer<? extends AbstractDto> messageConsumer;

		switch (processSettings.getLevel()) {
		case L0:
			messageConsumer = new L0AppConsumer((AbstractJobsDispatcher<EdrsSessionDto>) jobsDispatcher,
					processSettings, mqiService, mqiStatusService, appDataServiceErdsSettions,
					errorRepoAppender, appStatus, metadataClient);
			break;
		case L0_SEGMENT:
			messageConsumer = new L0SegmentAppConsumer((AbstractJobsDispatcher<ProductDto>) jobsDispatcher,
					appProperties, processSettings, mqiService, mqiStatusService, appDataServiceLevelSegments,
					errorRepoAppender, appStatus);
			break;
		case L1:
		case L2:
			messageConsumer = new LevelProductsMessageConsumer((AbstractJobsDispatcher<ProductDto>) jobsDispatcher,
					patternSettings, processSettings, mqiService, mqiStatusService, appDataServiceLevelProducts,
					errorRepoAppender, appStatus, metadataClient);
			break;
		default:
			throw new IllegalArgumentException("Unsupported Application Level");
		}
		messageConsumer.setTaskForFunctionalLog(String.format("%sJobGeneration", processSettings.getLevel().name()));
		return messageConsumer;
	}

}
