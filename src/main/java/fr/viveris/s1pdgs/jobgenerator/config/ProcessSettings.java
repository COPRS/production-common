package fr.viveris.s1pdgs.jobgenerator.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import fr.viveris.s1pdgs.jobgenerator.model.ProcessLevel;

/**
 * Extraction class of "process" configuration properties
 * 
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "process")
public class ProcessSettings {

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
	 * Process level
	 */
	private ProcessLevel level;

	/**
	 * Log level for the sdtout
	 */
	private String loglevelstdout;

	/**
	 * Log level for the stderr
	 */
	private String loglevelstderr;

	/**
	 * Processing station
	 */
	private String processingstation;

	/**
	 * Processing dynamic parameters<br/>
	 * Format: {name_1}:{dftval_1}||...||{name_n}:{dftval_n}
	 */
	private String paramstr;

	/**
	 * Processing dynamic parameters: key = parameter name, value = parameter value
	 */
	private final Map<String, String> params;

	/**
	 * Regular expressions per product type
	 */
	private String outputregexpstr;

	/**
	 * Regular expression: key = output file type, value = regular expression to use
	 * for file name.<br/>
	 * This is used to customize the way to match the outputs in the job
	 */
	private final Map<String, String> outputregexps;

	/**
	 * Default constructors
	 */
	public ProcessSettings() {
		this.params = new HashMap<>();
		this.outputregexps = new HashMap<>();
	}

	/**
	 * Initialize the maps according their value in string format
	 */
	@PostConstruct
	public void initMaps() {
		initMapParams();
		initMapOutputRegexps();
	}

	/**
	 * Init lmap of params
	 */
	private void initMapParams() {
		if (StringUtils.isEmpty(paramstr)) {
			return;
		}
		String[] paramsTmp = this.paramstr.split(MAP_ELM_SEP);
		for (int i = 0; i < paramsTmp.length; i++) {
			if (paramsTmp[i] != null) {
				String[] tmp = paramsTmp[i].split(MAP_KEY_VAL_SEP);
				if (tmp.length == 2) {
					this.params.put(tmp[0], tmp[1]);
				}
			}
		}

	}

	/**
	 * Init map of output regular expressions
	 */
	private void initMapOutputRegexps() {
		if (StringUtils.isEmpty(outputregexpstr)) {
			return;
		}
		String[] paramsTmp = this.outputregexpstr.split(MAP_ELM_SEP);
		for (int i = 0; i < paramsTmp.length; i++) {
			if (paramsTmp[i] != null) {
				String[] tmp = paramsTmp[i].split(MAP_KEY_VAL_SEP);
				if (tmp.length == 2) {
					this.outputregexps.put(tmp[0], tmp[1]);
				}
			}
		}

	}

	/**
	 * @return the level
	 */
	public ProcessLevel getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(final ProcessLevel level) {
		this.level = level;
	}

	/**
	 * @return the loglevelstdout
	 */
	public String getLoglevelstdout() {
		return loglevelstdout;
	}

	/**
	 * @param loglevelstdout
	 *            the loglevelstdout to set
	 */
	public void setLoglevelstdout(final String loglevelstdout) {
		this.loglevelstdout = loglevelstdout;
	}

	/**
	 * @return the loglevelstderr
	 */
	public String getLoglevelstderr() {
		return loglevelstderr;
	}

	/**
	 * @param loglevelstderr
	 *            the loglevelstderr to set
	 */
	public void setLoglevelstderr(final String loglevelstderr) {
		this.loglevelstderr = loglevelstderr;
	}

	/**
	 * @return the processingstation
	 */
	public String getProcessingstation() {
		return processingstation;
	}

	/**
	 * @param processingstation
	 *            the processingstation to set
	 */
	public void setProcessingstation(final String processingstation) {
		this.processingstation = processingstation;
	}

	/**
	 * @return the params
	 */
	public Map<String, String> getParams() {
		return params;
	}

	/**
	 * @return the outputregexps
	 */
	public Map<String, String> getOutputregexps() {
		return outputregexps;
	}

	/**
	 * @return the paramstr
	 */
	public String getParamstr() {
		return paramstr;
	}

	/**
	 * @param paramstr
	 *            the paramstr to set
	 */
	public void setParamstr(final String paramstr) {
		this.paramstr = paramstr;
	}

	/**
	 * @return the outputregexpstr
	 */
	public String getOutputregexpstr() {
		return outputregexpstr;
	}

	/**
	 * @param outputregexpstr
	 *            the outputregexpstr to set
	 */
	public void setOutputregexpstr(final String outputregexpstr) {
		this.outputregexpstr = outputregexpstr;
	}

}
