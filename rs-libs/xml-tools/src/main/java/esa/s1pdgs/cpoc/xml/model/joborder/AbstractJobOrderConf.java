package esa.s1pdgs.cpoc.xml.model.joborder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class of job order configuration
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "Ipf_Conf")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractJobOrderConf {

	/**
	 * Processor name
	 */
	@XmlElement(name = "Processor_Name")
	protected String processorName;

	/**
	 * Processor version
	 */
	@XmlElement(name = "Version")
	protected String version;

	/**
	 * Stdout log level
	 */
	@XmlElement(name = "Stdout_Log_Level")
	protected String stdoutLogLevel;

	/**
	 * Stderr log level
	 */
	@XmlElement(name = "Stderr_Log_Level")
	protected String stderrLogLevel;

	/**
	 * Indicates if the environment is a test environment or not
	 */
	@XmlElement(name = "Test")
	protected boolean test;

	/**
	 * Global activation of the breakpoints
	 */
	@XmlElement(name = "Breakpoint_Enable")
	protected boolean breakPointEnable;

	/**
	 * Processing station
	 */
	@XmlElement(name = "Processing_Station")
	protected String processingStation;

	/**
	 * Sensing time of the concerned session
	 */
	@XmlElement(name = "Sensing_Time")
	protected JobOrderSensingTime sensingTime;

	/**
	 * Configuration files
	 */
	@XmlElementWrapper(name = "Config_Files")
	@XmlElement(name = "Conf_File_Name")
	protected List<String> configFiles;

	/**
	 * Default constructor
	 */
	public AbstractJobOrderConf() {
		super();
		this.test = false;
		this.breakPointEnable = false;
		this.configFiles = new ArrayList<>();
	}

	/**
	 * Clone
	 * 
	 * @param obj
	 */
	public AbstractJobOrderConf(final AbstractJobOrderConf obj) {
		this();
		this.processorName = obj.getProcessorName();
		this.version = obj.getVersion();
		this.stdoutLogLevel = obj.getStdoutLogLevel();
		this.stderrLogLevel = obj.getStderrLogLevel();
		this.test = obj.isTest();
		this.processingStation = obj.getProcessingStation();
		this.configFiles = obj.getConfigFiles();
		if (obj.getSensingTime() != null) {
			this.sensingTime = new JobOrderSensingTime(obj.getSensingTime());
		}
	}

	/**
	 * @return the processorName
	 */
	public String getProcessorName() {
		return processorName;
	}

	/**
	 * @param processorName
	 *            the processorName to set
	 */
	public void setProcessorName(final String processorName) {
		this.processorName = processorName;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(final String version) {
		this.version = version;
	}

	/**
	 * @return the stdoutLogLevel
	 */
	public String getStdoutLogLevel() {
		return stdoutLogLevel;
	}

	/**
	 * @param stdoutLogLevel
	 *            the stdoutLogLevel to set
	 */
	public void setStdoutLogLevel(final String stdoutLogLevel) {
		this.stdoutLogLevel = stdoutLogLevel;
	}

	/**
	 * @return the stderrLogLevel
	 */
	public String getStderrLogLevel() {
		return stderrLogLevel;
	}

	/**
	 * @param stderrLogLevel
	 *            the stderrLogLevel to set
	 */
	public void setStderrLogLevel(final String stderrLogLevel) {
		this.stderrLogLevel = stderrLogLevel;
	}

	/**
	 * @return the test
	 */
	public boolean isTest() {
		return test;
	}

	/**
	 * @param test
	 *            the test to set
	 */
	public void setTest(final boolean test) {
		this.test = test;
	}

	/**
	 * @return the breakPointEnable
	 */
	public boolean isBreakPointEnable() {
		return breakPointEnable;
	}

	/**
	 * @param breakPointEnable
	 *            the breakPointEnable to set
	 */
	public void setBreakPointEnable(final boolean breakPointEnable) {
		this.breakPointEnable = breakPointEnable;
	}

	/**
	 * @return the sensingTime
	 */
	public JobOrderSensingTime getSensingTime() {
		return sensingTime;
	}

	/**
	 * @param sensingTime
	 *            the sensingTime to set
	 */
	public void setSensingTime(final JobOrderSensingTime sensingTime) {
		this.sensingTime = sensingTime;
	}

	/**
	 * @return the processingStation
	 */
	public String getProcessingStation() {
		return processingStation;
	}

	/**
	 * @param processingStation
	 *            the processingStation to set
	 */
	public void setProcessingStation(final String processingStation) {
		this.processingStation = processingStation;
	}

	/**
	 * @return the configFiles
	 */
	public List<String> getConfigFiles() {
		return configFiles;
	}

	/**
	 * @param confFiles
	 *            the confFiles to set
	 */
	public void addConfigFiles(final List<String> confFiles) {
		if (confFiles != null) {
			this.configFiles.addAll(confFiles);
		}
	}

	/**
	 * @param confFiles
	 *            the confFiles to set
	 */
	public void addConfigFile(final String configFile) {
		this.configFiles.add(configFile);
	}

	/**
	 * @param configFiles
	 *            the configFiles to set
	 */
	public void setConfigFiles(final List<String> configFiles) {
		this.configFiles = configFiles;
	}

	/**
	 * @return the procParams
	 */
	public abstract List<JobOrderProcParam> getProcParams();

	/**
	 * 
	 * @param param
	 */
	public abstract void addProcParam(final JobOrderProcParam param);

	/**
	 * @param procParams
	 *            the procParams to set
	 */
	public abstract void setProcParams(final List<JobOrderProcParam> procParams);

	/**
	 * @return the nbProcParams
	 */
	public abstract int getNbProcParams();

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toAbstractString() {
		return String.format(
				"processorName: %s, version: %s, stdoutLogLevel: %s, stderrLogLevel: %s, test: %s, breakPointEnable: %s, processingStation: %s, sensingTime: %s, configFiles: %s",
				processorName, version, stdoutLogLevel, stderrLogLevel, test, breakPointEnable, processingStation,
				sensingTime, configFiles);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(processorName, version, stdoutLogLevel, stderrLogLevel, test, breakPointEnable,
				processingStation, sensingTime, configFiles);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			AbstractJobOrderConf other = (AbstractJobOrderConf) obj;
			ret = Objects.equals(processorName, other.processorName) && Objects.equals(version, other.version)
					&& Objects.equals(stdoutLogLevel, other.stdoutLogLevel)
					&& Objects.equals(stderrLogLevel, other.stderrLogLevel) && test == other.test
					&& breakPointEnable == other.breakPointEnable
					&& Objects.equals(processingStation, other.processingStation)
					&& Objects.equals(sensingTime, other.sensingTime) && Objects.equals(configFiles, other.configFiles);
		}
		return ret;
	}

}
