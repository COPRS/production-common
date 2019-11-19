package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableTestEnum;

/**
 * Class describing the content of a file taskTable.xml
 * 
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

	/**
	 * 
	 */
	@XmlElement(name = "Test")
	private TaskTableTestEnum test;

	/**
	 * 
	 */
	private ApplicationLevel level;

	/**
	 * 
	 */
	public TaskTable() {
		super();
		this.dynProcParams = new ArrayList<>();
		this.cfgFiles = new ArrayList<>();
		this.pools = new ArrayList<>();
		this.test = TaskTableTestEnum.BLANK;
		this.level = ApplicationLevel.L0;
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
	 * @return the cfgFiles
	 */
	public List<TaskTableCfgFile> getCfgFiles() {
		return cfgFiles;
	}

	/**
	 * @param cfgFiles
	 *            the cfgFiles to set
	 */
	public void setCfgFiles(final List<TaskTableCfgFile> cfgFiles) {
		this.cfgFiles = cfgFiles;
	}

	/**
	 * @param cfgFiles
	 *            the cfgFiles to set
	 */
	public void addCfgFile(final TaskTableCfgFile cfgFile) {
		this.cfgFiles.add(cfgFile);
	}

	/**
	 * @return the dynProcParam
	 */
	public List<TaskTableDynProcParam> getDynProcParams() {
		return dynProcParams;
	}

	/**
	 * @param dynProcParam
	 *            the dynProcParam to set
	 */
	public void addDynProcParam(final TaskTableDynProcParam dynProcParam) {
		this.dynProcParams.add(dynProcParam);
	}

	/**
	 * @return the pools
	 */
	public List<TaskTablePool> getPools() {
		return pools;
	}

	/**
	 * @param pools
	 *            the pools to set
	 */
	public void setPools(final List<TaskTablePool> pools) {
		this.pools = pools;
	}

	/**
	 * @param pools
	 *            the pools to set
	 */
	public void addPool(final TaskTablePool pool) {
		this.pools.add(pool);
	}

	/**
	 * @return the test
	 */
	public TaskTableTestEnum getTest() {
		return test;
	}

	/**
	 * @param test
	 *            the test to set
	 */
	public void setTest(final TaskTableTestEnum test) {
		this.test = test;
	}

	/**
	 * @return the level
	 */
	public ApplicationLevel getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(final ApplicationLevel level) {
		this.level = level;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(processorName, version, cfgFiles, dynProcParams, pools, test, level);
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
			TaskTable other = (TaskTable) obj;
			ret = Objects.equals(processorName, other.processorName) && Objects.equals(version, other.version)
					&& Objects.equals(cfgFiles, other.cfgFiles) && Objects.equals(dynProcParams, other.dynProcParams)
					&& Objects.equals(pools, other.pools) && Objects.equals(test, other.test)
					&& Objects.equals(level, other.level);
		}
		return ret;
	}
}
