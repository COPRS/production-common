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
 * Extraction class of "l0-process" configuration properties
 * 
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "process")
public class ProcessSettings {
	
	private ProcessLevel level;

	/**
	 * Lof level for the sdtout
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
	
	private String paramstr;

	/**
	 * Processing dynamic parameters: key = parameter name, value = parameter value
	 */
	private Map<String, String> params;
	
	private String outputregexpstr;

	/**
	 * Regular expression: key = output file type, value = regular expression to use
	 * for file name
	 */
	private Map<String, String> outputregexps;

	/**
	 * Default constructors
	 */
	public ProcessSettings() {
		this.params = new HashMap<>();
		this.outputregexps = new HashMap<>();
	}
	
	/**
	 * 
	 */
	@PostConstruct
	public void initMaps() {
		// Params
		if (!StringUtils.isEmpty(this.paramstr)) {
			String[] paramsTmp = this.paramstr.split("\\|\\|");
			for (int i=0; i<paramsTmp.length; i++) {
				if (!StringUtils.isEmpty(paramsTmp[i])) {
					String[] tmp = paramsTmp[i].split(":", 2);
					if (tmp.length == 2) {
						this.params.put(tmp[0], tmp[1]);
					}
				}
			}
		}
		//Regexp
		if (!StringUtils.isEmpty(this.outputregexpstr)) {
			String[] paramsTmp = this.outputregexpstr.split("\\|\\|");
			for (int i=0; i<paramsTmp.length; i++) {
				if (!StringUtils.isEmpty(paramsTmp[i])) {
					String[] tmp = paramsTmp[i].split(":", 2);
					if (tmp.length == 2) {
						this.outputregexps.put(tmp[0], tmp[1]);
					}
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
	 * @param level the level to set
	 */
	public void setLevel(ProcessLevel level) {
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
	public void setLoglevelstdout(String loglevelstdout) {
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
	public void setLoglevelstderr(String loglevelstderr) {
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
	public void setProcessingstation(String processingstation) {
		this.processingstation = processingstation;
	}

	/**
	 * @return the params
	 */
	public Map<String, String> getParams() {
		return params;
	}

	/**
	 * @param params
	 *            the params to set
	 */
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	/**
	 * @return the outputregexps
	 */
	public Map<String, String> getOutputregexps() {
		return outputregexps;
	}

	/**
	 * @param outputregexps
	 *            the outputregexps to set
	 */
	public void setOutputregexps(Map<String, String> outputregexps) {
		this.outputregexps = outputregexps;
	}

	/**
	 * @return the paramstr
	 */
	public String getParamstr() {
		return paramstr;
	}

	/**
	 * @param paramstr the paramstr to set
	 */
	public void setParamstr(String paramstr) {
		this.paramstr = paramstr;
	}

	/**
	 * @return the outputregexpstr
	 */
	public String getOutputregexpstr() {
		return outputregexpstr;
	}

	/**
	 * @param outputregexpstr the outputregexpstr to set
	 */
	public void setOutputregexpstr(String outputregexpstr) {
		this.outputregexpstr = outputregexpstr;
	}

}
