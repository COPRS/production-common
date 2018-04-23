package fr.viveris.s1pdgs.jobgenerator.model.tasktable;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableTestEnum;

/**
 * Class describing the content of a file taskTable.xml
 * @author Cyrielle
 *
 */
@XmlRootElement(name = "Ipf_Task_Table")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTable {
	
	/**
	 * Processor name
	 */
	@XmlElement(name = "Processor_Name")
	private String processorName;
	
	/**
	 * Version
	 */
	@XmlElement(name = "Version")
	private String version;

	/**
	 * Configuration files
	 */
	@XmlElementWrapper(name = "List_of_Cfg_Files")
	@XmlElement(name = "Cfg_Files")
	private List<TaskTableCfgFile> cfgFiles;

	/**
	 * Dynamic processors parameters
	 */
	@XmlElementWrapper(name = "List_of_Dyn_ProcParam")
	@XmlElement(name = "Dyn_ProcParam")
	private List<TaskTableDynProcParam> dynProcParams;

	/**
	 * Pools
	 */
	@XmlElementWrapper(name = "List_of_Pools")
	@XmlElement(name = "Pool")
	private List<TaskTablePool> pools;

	@XmlElement(name = "Test")
	private TaskTableTestEnum test;
	

	/**
	 * 
	 */
	public TaskTable() {
		super();
		this.dynProcParams = new ArrayList<>();
		this.cfgFiles = new ArrayList<>();
		this.pools = new ArrayList<>();
		this.test = TaskTableTestEnum.BLANK;
	}

	/**
	 * @return the processorName
	 */
	public String getProcessorName() {
		return processorName;
	}

	/**
	 * @param processorName the processorName to set
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
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the cfgFiles
	 */
	public List<TaskTableCfgFile> getCfgFiles() {
		return cfgFiles;
	}

	/**
	 * @param cfgFiles the cfgFiles to set
	 */
	public void setCfgFiles(List<TaskTableCfgFile> cfgFiles) {
		this.cfgFiles = cfgFiles;
	}

	/**
	 * @param cfgFiles the cfgFiles to set
	 */
	public void addCfgFile(TaskTableCfgFile cfgFile) {
		this.cfgFiles.add(cfgFile);
	}
	
	/**
	 * @return the dynProcParam
	 */
	public List<TaskTableDynProcParam> getDynProcParams() {
		return dynProcParams;
	}

	/**
	 * @param dynProcParam the dynProcParam to set
	 */
	public void addDynProcParam(TaskTableDynProcParam dynProcParam) {
		this.dynProcParams.add(dynProcParam);
	}

	/**
	 * @return the pools
	 */
	public List<TaskTablePool> getPools() {
		return pools;
	}

	/**
	 * @param pools the pools to set
	 */
	public void setPools(List<TaskTablePool> pools) {
		this.pools = pools;
	}

	/**
	 * @param pools the pools to set
	 */
	public void addPool(TaskTablePool pool) {
		this.pools.add(pool);
	}

	/**
	 * @return the test
	 */
	public TaskTableTestEnum getTest() {
		return test;
	}

	/**
	 * @param test the test to set
	 */
	public void setTest(TaskTableTestEnum test) {
		this.test = test;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TaskTable [processorName=" + processorName + ", version=" + version + ", cfgFiles=" + cfgFiles
				+ ", dynProcParams=" + dynProcParams + ", pools=" + pools + ", test=" + test + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cfgFiles == null) ? 0 : cfgFiles.hashCode());
		result = prime * result + ((dynProcParams == null) ? 0 : dynProcParams.hashCode());
		result = prime * result + ((pools == null) ? 0 : pools.hashCode());
		result = prime * result + ((processorName == null) ? 0 : processorName.hashCode());
		result = prime * result + ((test == null) ? 0 : test.hashCode());
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
		TaskTable other = (TaskTable) obj;
		if (cfgFiles == null) {
			if (other.cfgFiles != null)
				return false;
		} else if (!cfgFiles.equals(other.cfgFiles))
			return false;
		if (dynProcParams == null) {
			if (other.dynProcParams != null)
				return false;
		} else if (!dynProcParams.equals(other.dynProcParams))
			return false;
		if (pools == null) {
			if (other.pools != null)
				return false;
		} else if (!pools.equals(other.pools))
			return false;
		if (processorName == null) {
			if (other.processorName != null)
				return false;
		} else if (!processorName.equals(other.processorName))
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
