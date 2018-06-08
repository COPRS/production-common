package fr.viveris.s1pdgs.jobgenerator.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import fr.viveris.s1pdgs.jobgenerator.model.ProductFamily;

/**
 * Extraction class of "tasktables" configuration properties
 * 
 * @author Cyrielle Gailliard
 *
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
	private String outputfamiliesstr;

	/**
	 * Map between output product type and product family
	 */
	private Map<String, ProductFamily> outputfamilies;

	/**
	 * Map of all the overlap for the different slice type.<br/>
	 * Format: {acquisition_1:overloap_1}||...||{acquisition_n:overloap_n}
	 */
	private String typeoverlapstr;

	/**
	 * Map of all the overlap for the different slice type
	 */
	private Map<String, Float> typeOverlap;

	/**
	 * Map of all the length for the different slice type.<br/>
	 * Format:
	 * {acquisition_1:slice_length_1}||...||{acquisition_n:slice_length_n}<br/>
	 * Format: acquisition in IW, EW, SM, EM
	 */
	private String typeslicelenstr;

	/**
	 * Map of all the length for the different slice type<br/>
	 * Format: acquisition in IW, EW, SM, EM
	 */
	private Map<String, Float> typeSliceLength;

	/**
	 * Map product type and corresponding metadata index in case of the product type
	 * in lowercase in not the metadata index (example: aux_resorb use aux_res)
	 * Format: {type_1}:{index_1}||{type_2}:{index_2}||...||{type_n}:{index_n}<br/>
	 * Format: acquisition in IW, EW, SM, EM
	 */
	private String mapTypeMetaStr;

	/**
	 * Map product type and corresponding metadata index in case of the product type
	 * in lowercase in not the metadata index (example: aux_resorb use aux_res)<br/>
	 * Format: acquisition in IW, EW, SM, EM
	 */
	private Map<String, String> mapTypeMeta;

	/**
	 * Default constructors
	 */
	public JobGeneratorSettings() {
		this.outputfamilies = new HashMap<>();
		this.typeOverlap = new HashMap<>();
		this.typeSliceLength = new HashMap<>();
		this.mapTypeMeta = new HashMap<>();
	}

	/**
	 * Initialization function:
	 * <li>Build maps by splitting the corresponding string (note: we cannot map
	 * configuration parameter directly in a map due to the use of K8S configuration
	 * map)</li>
	 */
	@PostConstruct
	public void initMaps() {
		extractMapProductTypeFamily();
		extractMapAcqOverload();
		extractMapAcqSliceLen();
		extractMapProductTypeMetaIndex();
	}

	/**
	 * Extract map product type family from the string
	 */
	private void extractMapProductTypeFamily() {
		if (StringUtils.isEmpty(outputfamiliesstr)) {
			return;
		}
		String[] paramsTmp = outputfamiliesstr.split(MAP_ELM_SEP);
		for (int i = 0; i < paramsTmp.length; i++) {
			String[] tmp = paramsTmp[i].split(MAP_KEY_VAL_SEP);
			if (tmp != null && tmp.length == 2) {
				String key = tmp[0];
				String valStr = tmp[1];
				outputfamilies.put(key, ProductFamily.fromValue(valStr));
			}
		}
	}

	/**
	 * Extract map product type family from the string
	 */
	private void extractMapAcqOverload() {
		if (StringUtils.isEmpty(typeoverlapstr)) {
			return;
		}
		String[] paramsTmp = typeoverlapstr.split(MAP_ELM_SEP);
		for (int i = 0; i < paramsTmp.length; i++) {
			String[] tmp = paramsTmp[i].split(MAP_KEY_VAL_SEP);
			if (tmp != null && tmp.length == 2) {
				String key = tmp[0];
				String valStr = tmp[1];
				typeOverlap.put(key, Float.valueOf(valStr));
			}
		}
	}

	/**
	 * Extract map product type family from the string
	 */
	private void extractMapAcqSliceLen() {
		if (StringUtils.isEmpty(typeslicelenstr)) {
			return;
		}
		String[] paramsTmp = typeslicelenstr.split(MAP_ELM_SEP);
		for (int i = 0; i < paramsTmp.length; i++) {
			String[] tmp = paramsTmp[i].split(MAP_KEY_VAL_SEP);
			if (tmp != null && tmp.length == 2) {
				String key = tmp[0];
				String valStr = tmp[1];
				typeSliceLength.put(key, Float.valueOf(valStr));
			}
		}
	}

	/**
	 * Extract map product type family from the string
	 */
	private void extractMapProductTypeMetaIndex() {
		if (StringUtils.isEmpty(mapTypeMetaStr)) {
			return;
		}
		String[] paramsTmp = mapTypeMetaStr.split(MAP_ELM_SEP);
		for (int i = 0; i < paramsTmp.length; i++) {
			String[] tmp = paramsTmp[i].split(MAP_KEY_VAL_SEP);
			if (tmp != null && tmp.length == 2) {
				String key = tmp[0];
				String val = tmp[1];
				mapTypeMeta.put(key, val);
			}
		}
	}

	/**
	 * Class of delay configuration
	 * 
	 * @author Cyrielle Gailliard
	 *
	 */
	public static class WaitTempo {
		/**
		 * Delay between 2 retries
		 */
		private int tempo;

		/**
		 * Number of maximal retries
		 */
		private int retries;
		
		/**
		 * Default constructor
		 */
		public WaitTempo() {
			this.tempo = 0;
			this.retries = 0;
		}

		/**
		 * Constructor using field
		 * @param tempo
		 * @param retries
		 */
		public WaitTempo(final int tempo, final int retries) {
			this.tempo = tempo;
			this.retries = retries;
		}

		/**
		 * @return the tempo
		 */
		public int getTempo() {
			return tempo;
		}

		/**
		 * @param tempo
		 *            the tempo to set
		 */
		public void setTempo(final int tempo) {
			this.tempo = tempo;
		}

		/**
		 * @return the retries
		 */
		public int getRetries() {
			return retries;
		}

		/**
		 * @param retries
		 *            the retries to set
		 */
		public void setRetries(final int retries) {
			this.retries = retries;
		}

	}

	/**
	 * @return the maxnboftasktable
	 */
	public int getMaxnboftasktable() {
		return maxnboftasktable;
	}

	/**
	 * @param maxnboftasktable
	 *            the maxnboftasktable to set
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
	 * @param waitprimarycheck
	 *            the waitprimarycheck to set
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
	 * @param waitmetadatainput
	 *            the waitmetadatainput to set
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
	 * @param diroftasktables
	 *            the diroftasktables to set
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
	 * @param maxnumberofjobs
	 *            the maxnumberofjobs to set
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
	 * @param jobgenfixedrate
	 *            the jobgenfixedrate to set
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
	 * @param defaultfamily
	 *            the defaultfamily to set
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
	 * @param outputfamiliesstr
	 *            the outputfamiliesstr to set
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
	 * @return the typeoverlapstr
	 */
	public String getTypeoverlapstr() {
		return typeoverlapstr;
	}

	/**
	 * @param typeoverlapstr
	 *            the typeoverlapstr to set
	 */
	public void setTypeoverlapstr(final String typeoverlapstr) {
		this.typeoverlapstr = typeoverlapstr;
	}

	/**
	 * @return the typeOverlap
	 */
	public Map<String, Float> getTypeOverlap() {
		return typeOverlap;
	}

	/**
	 * @return the typeslicelenstr
	 */
	public String getTypeslicelenstr() {
		return typeslicelenstr;
	}

	/**
	 * @param typeslicelenstr
	 *            the typeslicelenstr to set
	 */
	public void setTypeslicelenstr(final String typeslicelenstr) {
		this.typeslicelenstr = typeslicelenstr;
	}

	/**
	 * @return the typeSliceLength
	 */
	public Map<String, Float> getTypeSliceLength() {
		return typeSliceLength;
	}

	/**
	 * @return the mapTypeMetaStr
	 */
	public String getMapTypeMetaStr() {
		return mapTypeMetaStr;
	}

	/**
	 * @param mapTypeMetaStr
	 *            the mapTypeMetaStr to set
	 */
	public void setMapTypeMetaStr(final String mapTypeMetaStr) {
		this.mapTypeMetaStr = mapTypeMetaStr;
	}

	/**
	 * @return the mapTypeMeta
	 */
	public Map<String, String> getMapTypeMeta() {
		return mapTypeMeta;
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
				+ ", outputfamilies: " + outputfamilies + ", typeoverlapstr: " + typeoverlapstr + ", typeOverlap: "
				+ typeOverlap + ", typeslicelenstr: " + typeslicelenstr + ", typeSliceLength: " + typeSliceLength
				+ ", mapTypeMetaStr: " + mapTypeMetaStr + ", mapTypeMeta: "
				+ mapTypeMeta + "}";
	}

}
