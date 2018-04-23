package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * Class of job order configuration
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "Ipf_Conf")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderConf {

	/**
	 * Processor name
	 */
	@XmlElement(name = "Processor_Name")
	private String processorName;

	/**
	 * Processor version
	 */
	@XmlElement(name = "Version")
	private String version;

	/**
	 * Stdout log level
	 */
	@XmlElement(name = "Stdout_Log_Level")
	private String stdoutLogLevel;

	/**
	 * Stderr log level
	 */
	@XmlElement(name = "Stderr_Log_Level")
	private String stderrLogLevel;

	/**
	 * Indicates if the environment is a test environment or not
	 */
	@XmlElement(name = "Test")
	private boolean test;

	/**
	 * Global activation of the breakpoints
	 */
	@XmlElement(name = "Breakpoint_Enable")
	private boolean breakPointEnable;

	/**
	 * Processing station
	 */
	@XmlElement(name = "Processing_Station")
	private String processingStation;

	/**
	 * Sensing time of the concerned session
	 */
	@XmlElement(name = "Sensing_Time")
	private JobOrderSensingTime sensingTime;
	
	/**
	 * Configuration files
	 */
	@XmlElementWrapper(name = "Config_Files")
	@XmlElement(name = "Conf_File_Name")
	private List<String> configFiles;
	
	/**
	 * Dynamic processing parameters
	 */
	@XmlElementWrapper(name = "List_of_Dynamic_Processing_Parameters")
	@XmlElement(name = "Dynamic_Processing_Parameter")
	private List<JobOrderProcParam> procParams;

	/**
	 * Number of processors. Automatically field
	 */
	@XmlPath("List_of_Dynamic_Processing_Parameters/@count")
	private int nbProcParams;


	/**
	 * Default constructor
	 */
	public JobOrderConf() {
		super();
		this.test = false;
		this.breakPointEnable = false;
		this.configFiles = new ArrayList<>();
		this.procParams = new ArrayList<>();
		this.nbProcParams = 0;
	}

	/**
	 * Clone
	 * 
	 * @param obj
	 */
	public JobOrderConf(JobOrderConf obj) {
		this();
		this.processorName = obj.getProcessorName();
		this.version = obj.getVersion();
		this.stdoutLogLevel = obj.getStdoutLogLevel();
		this.stderrLogLevel = obj.getStderrLogLevel();
		this.test = obj.isTest();
		this.processingStation = obj.getProcessingStation();
		this.procParams = obj.getProcParams();
		this.nbProcParams = obj.getProcParams().size();
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
	public void setProcessorName(String processorName) {
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
	public void setVersion(String version) {
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
	public void setStdoutLogLevel(String stdoutLogLevel) {
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
	public void setStderrLogLevel(String stderrLogLevel) {
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
	public void setTest(boolean test) {
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
	public void setBreakPointEnable(boolean breakPointEnable) {
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
	public void setSensingTime(JobOrderSensingTime sensingTime) {
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
	public void setProcessingStation(String processingStation) {
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
	public void addConfigFiles(List<String> confFiles) {
		if (confFiles != null) {
			this.configFiles.addAll(confFiles);
		}
	}
	
	/**
	 * @param confFiles
	 *            the confFiles to set
	 */
	public void addConfigFile(String configFile) {
		this.configFiles.add(configFile);
	}

	/**
	 * @param configFiles the configFiles to set
	 */
	public void setConfigFiles(List<String> configFiles) {
		this.configFiles = configFiles;
	}

	/**
	 * @return the procParams
	 */
	public List<JobOrderProcParam> getProcParams() {
		return procParams;
	}
	

	public void addProcParam(JobOrderProcParam param) {
		this.procParams.add(param);
		this.nbProcParams ++;
	}

	/**
	 * @param procParams the procParams to set
	 */
	public void setProcParams(List<JobOrderProcParam> procParams) {
		this.procParams = procParams;
		this.nbProcParams = procParams.size();
	}

	/**
	 * @return the nbProcParams
	 */
	public int getNbProcParams() {
		return nbProcParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobOrderConf [processorName=" + processorName + ", version=" + version + ", stdoutLogLevel="
				+ stdoutLogLevel + ", stderrLogLevel=" + stderrLogLevel + ", test=" + test + ", breakPointEnable="
				+ breakPointEnable + ", processingStation=" + processingStation + ", sensingTime=" + sensingTime
				+ ", configFiles=" + configFiles + ", procParams=" + procParams + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (breakPointEnable ? 1231 : 1237);
		result = prime * result + ((configFiles == null) ? 0 : configFiles.hashCode());
		result = prime * result + ((procParams == null) ? 0 : procParams.hashCode());
		result = prime * result + ((processingStation == null) ? 0 : processingStation.hashCode());
		result = prime * result + ((processorName == null) ? 0 : processorName.hashCode());
		result = prime * result + ((sensingTime == null) ? 0 : sensingTime.hashCode());
		result = prime * result + ((stderrLogLevel == null) ? 0 : stderrLogLevel.hashCode());
		result = prime * result + ((stdoutLogLevel == null) ? 0 : stdoutLogLevel.hashCode());
		result = prime * result + (test ? 1231 : 1237);
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobOrderConf other = (JobOrderConf) obj;
		if (breakPointEnable != other.breakPointEnable)
			return false;
		if (configFiles == null) {
			if (other.configFiles != null)
				return false;
		} else if (!configFiles.equals(other.configFiles))
			return false;
		if (procParams == null) {
			if (other.procParams != null)
				return false;
		} else if (!procParams.equals(other.procParams))
			return false;
		if (processingStation == null) {
			if (other.processingStation != null)
				return false;
		} else if (!processingStation.equals(other.processingStation))
			return false;
		if (processorName == null) {
			if (other.processorName != null)
				return false;
		} else if (!processorName.equals(other.processorName))
			return false;
		if (sensingTime == null) {
			if (other.sensingTime != null)
				return false;
		} else if (!sensingTime.equals(other.sensingTime))
			return false;
		if (stderrLogLevel == null) {
			if (other.stderrLogLevel != null)
				return false;
		} else if (!stderrLogLevel.equals(other.stderrLogLevel))
			return false;
		if (stdoutLogLevel == null) {
			if (other.stdoutLogLevel != null)
				return false;
		} else if (!stdoutLogLevel.equals(other.stdoutLogLevel))
			return false;
		if (test != other.test)
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}
