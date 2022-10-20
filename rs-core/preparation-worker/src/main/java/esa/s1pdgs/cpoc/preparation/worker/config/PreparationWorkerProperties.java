package esa.s1pdgs.cpoc.preparation.worker.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.preparation.worker.model.ProductMode;

@Component
@Validated
@ConfigurationProperties(prefix = "worker")
public class PreparationWorkerProperties {
	
	/**
	 * Separator used to seperate the elements of a map in a string format
	 */
	protected static final String MAP_ELM_SEP = "\\|\\|";

	/**
	 * Separator used to separate the key and the value of a map element in a string
	 * format
	 */
	protected static final String MAP_KEY_VAL_SEP = ":";
	
	public static class InputWaitingConfig {
		private String processorNameRegexp = ".*";
		private String processorVersionRegexp = ".*";
		private String inputIdRegexp;
		private String timelinessRegexp;
		private long waitingFromDownlinkInSeconds;
		private long waitingFromIngestionInSeconds = 0;

		public String getProcessorNameRegexp() {
			return processorNameRegexp;
		}

		public void setProcessorNameRegexp(final String processorNameRegexp) {
			this.processorNameRegexp = processorNameRegexp;
		}

		public String getProcessorVersionRegexp() {
			return processorVersionRegexp;
		}

		public void setProcessorVersionRegexp(final String processorVersionRegexp) {
			this.processorVersionRegexp = processorVersionRegexp;
		}

		public String getInputIdRegexp() {
			return inputIdRegexp;
		}

		public void setInputIdRegexp(final String inputIdRegexp) {
			this.inputIdRegexp = inputIdRegexp;
		}

		public String getTimelinessRegexp() {
			return timelinessRegexp;
		}

		public void setTimelinessRegexp(final String timelinessRegexp) {
			this.timelinessRegexp = timelinessRegexp;
		}

		public long getWaitingFromDownlinkInSeconds() {
			return waitingFromDownlinkInSeconds;
		}

		public void setWaitingFromDownlinkInSeconds(final long waitingFromDownlinkInSeconds) {
			this.waitingFromDownlinkInSeconds = waitingFromDownlinkInSeconds;
		}

		public long getWaitingFromIngestionInSeconds() {
			return waitingFromIngestionInSeconds;
		}

		public void setWaitingFromIngestionInSeconds(final long waitingFromIngestionInSeconds) {
			this.waitingFromIngestionInSeconds = waitingFromIngestionInSeconds;
		}

		@Override
		public String toString() {
			return "InputWaitingConfig [processorNameRegexp=" + processorNameRegexp + ", processorVersionRegexp="
					+ processorVersionRegexp + ", inputIdRegexp=" + inputIdRegexp + ", timelinessRegexp="
					+ timelinessRegexp + ", waitingFromDownlinkInSeconds=" + waitingFromDownlinkInSeconds
					+ ", waitingFromIngestionInSeconds=" + waitingFromIngestionInSeconds + "]";
		}
	}

	public static class LatenessConfig {
		private long lateAfterMilliseconds;
		private String inputTopic;
		private String lateTopic;

		public long getLateAfterMilliseconds() {
			return lateAfterMilliseconds;
		}

		public void setLateAfterMilliseconds(final long lateAfterMilliseconds) {
			this.lateAfterMilliseconds = lateAfterMilliseconds;
		}

		public String getInputTopic() {
			return inputTopic;
		}

		public void setInputTopic(final String inputTopic) {
			this.inputTopic = inputTopic;
		}

		public String getLateTopic() {
			return lateTopic;
		}

		public void setLateTopic(final String lateTopic) {
			this.lateTopic = lateTopic;
		}

		@Override
		public String toString() {
			return "LatenessConfig [lateAfterMilliseconds=" + lateAfterMilliseconds + ", inputTopic=" + inputTopic
					+ ", lateTopic=" + lateTopic + "]";
		}
	}

	private Map<String, InputWaitingConfig> inputWaiting = new LinkedHashMap<>();

	private List<LatenessConfig> latenessConfig = new ArrayList<>();

	private Map<String, String> joborderTimelinessCategoryMapping = new LinkedHashMap<>();

	private boolean lateTopicActive = false;

	/**
	 * Maximal number of jobs waiting to be sent
	 */
	private int maxnumberofjobs;

	/**
	 * Location of task table XML files
	 */
	private String diroftasktables;

	/**
	 * Period (fixed rate)
	 */
	private int jobgenfixedrate;

	/**
	 * Maximum lifespan for the primary check before continuing
	 */
	private int primaryCheckMaxTimelifeS;
	
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
	private Map<String, ProductFamily> inputfamilies = new HashMap<>();

	/**
	 * Map between output product type and product family in string format.<br/>
	 * Format: {type_1}:{family_1}||{type_2}:{family_2}||...||{type_n}:{family_n}
	 */
	private String outputfamiliesstr;

	/**
	 * Map between output product type and product family
	 */
	private Map<String, ProductFamily> outputfamilies = new HashMap<>();

	/**
	 * Map of all the overlap for the different slice type
	 */
	private Map<String, Float> typeOverlap = new HashMap<>();

	/**
	 * Map of all the length for the different slice type<br/>
	 * Format: acquisition in IW, EW, SM, EM
	 */
	private Map<String, Float> typeSliceLength = new HashMap<>();

	/**
	 * Map product type and corresponding metadata index in case of the product type
	 * in lowercase in not the metadata index (example: aux_resorb use aux_res)<br/>
	 */
	private Map<String, String> mapTypeMeta = new HashMap<>();

	private List<ProductFamily> oqcCheck = new ArrayList<>();

	/**
	 * Mode of the process
	 */
	private ProductMode productMode = ProductMode.SLICING;
	
	/**
     * Maximal job age per status
     */
    private Map<String, Long> maxAgeJobMs = new HashMap<>();

    private String pathTaskTableXslt = "";
    
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
		final String[] paramsTmp = inputfamiliesstr.split(MAP_ELM_SEP);
		for (final String s : paramsTmp) {
			final String[] tmp = s.split(MAP_KEY_VAL_SEP);
			if (tmp.length == 2) {
				final String key = tmp[0];
				final String valStr = tmp[1];
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
		final String[] paramsTmp = outputfamiliesstr.split(MAP_ELM_SEP);
		for (final String s : paramsTmp) {
			final String[] tmp = s.split(MAP_KEY_VAL_SEP);
			if (tmp.length == 2) {
				final String key = tmp[0];
				final String valStr = tmp[1];
				outputfamilies.put(key, ProductFamily.valueOf(valStr));
			}
		}
	}

	public List<LatenessConfig> getLatenessConfig() {
		return latenessConfig;
	}

	public void setLatenessConfig(final List<LatenessConfig> latenessConfig) {
		this.latenessConfig = latenessConfig;
	}

	public Map<String, InputWaitingConfig> getInputWaiting() {
		return inputWaiting;
	}

	public void setInputWaiting(final Map<String, InputWaitingConfig> inputWaiting) {
		this.inputWaiting = inputWaiting;
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
	 * @return the maximum time a job should be in primary check
	 */
	public int getPrimaryCheckMaxTimelifeS() {
		return primaryCheckMaxTimelifeS;
	}

	/**
	 * @param primaryCheckMaxTimelifeS how long a job should be in primary check
	 */
	public void setPrimaryCheckMaxTimelifeS(int primaryCheckMaxTimelifeS) {
		this.primaryCheckMaxTimelifeS = primaryCheckMaxTimelifeS;
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
	public void setInputfamiliesstr(final String inputfamiliesstr) {
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

	public List<ProductFamily> getOqcCheck() {
		return oqcCheck;
	}

	public void setOqcCheck(final List<ProductFamily> oqcCheck) {
		this.oqcCheck = oqcCheck;
	}

	public ProductMode getProductMode() {
		return productMode;
	}

	public void setProductMode(final ProductMode productMode) {
		this.productMode = productMode;
	}

	public Map<String, String> getJoborderTimelinessCategoryMapping() {
		return joborderTimelinessCategoryMapping;
	}

	public void setJoborderTimelinessCategoryMapping(final Map<String, String> joborderTimelinessCategoryMapping) {
		this.joborderTimelinessCategoryMapping = joborderTimelinessCategoryMapping;
	}

	public boolean isLateTopicActive() {
		return lateTopicActive;
	}

	public void setLateTopicActive(final boolean lateTopicActive) {
		this.lateTopicActive = lateTopicActive;
	}
	
	public Map<String, Long> getMaxAgeJobMs() {
		return maxAgeJobMs;
	}

	public void setMaxAgeJobMs(Map<String, Long> maxAgeJobMs) {
		this.maxAgeJobMs = maxAgeJobMs;
	}

	public String getPathTaskTableXslt() {
		return pathTaskTableXslt;
	}

	public void setPathTaskTableXslt(String pathTaskTableXslt) {
		this.pathTaskTableXslt = pathTaskTableXslt;
	}

	/**
	 * Display object in JSON format
	 */
	@Override
	public String toString() {
		return "{maxnumberofjobs: " + maxnumberofjobs + "\", diroftasktables: \"" + diroftasktables
				+ "\", jobgenfixedrate: " + jobgenfixedrate + ", primaryCheckMaxTimelifeS:" + primaryCheckMaxTimelifeS
				+ ", defaultfamily: \"" + defaultfamily + "\", outputfamiliesstr: \"" + outputfamiliesstr
				+ "\", outputfamilies: \"" + outputfamilies + "\", typeOverlap: \"" + typeOverlap
				+ "\", typeSliceLength: \"" + typeSliceLength + "\", mapTypeMeta: \"" + mapTypeMeta + "\", oqcCheck: \""
				+ oqcCheck + "\", productMode: \"" + productMode + "\", inputWaiting: \"" + inputWaiting
				+ "\", joborderTimelinessCategoryMapping:" + joborderTimelinessCategoryMapping + "\", maxAgeJobMs:"
				+ maxAgeJobMs + "\", pathTaskTableXslt:" + pathTaskTableXslt + "\"}";
	}
}
