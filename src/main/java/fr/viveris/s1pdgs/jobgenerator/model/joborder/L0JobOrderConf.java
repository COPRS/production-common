package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

@XmlRootElement(name = "Ipf_Conf")
@XmlAccessorType(XmlAccessType.NONE)
public class L0JobOrderConf extends AbstractJobOrderConf {
	
	/**
	 * Dynamic processing parameters
	 */
	@XmlElementWrapper(name = "Dynamic_Processing_Parameters")
	@XmlElement(name = "Processing_Parameter")
	protected List<JobOrderProcParam> procParams;

	/**
	 * Number of processors. Automatically field
	 */
	@XmlPath("List_of_Dynamic_Processing_Parameters/@count")
	protected int nbProcParams;

	/**
	 * Default constructor
	 */
	public L0JobOrderConf() {
		super();
	}

	/**
	 * Clone
	 * 
	 * @param obj
	 */
	public L0JobOrderConf(AbstractJobOrderConf obj) {
		super(obj);
	}

	@Override
	public List<JobOrderProcParam> getProcParams() {
		return this.procParams;
	}

	@Override
	public int getNbProcParams() {
		return this.nbProcParams;
	}
	
	@Override
	public void addProcParam(JobOrderProcParam param) {
		this.procParams.add(param);
		this.nbProcParams ++;
	}

	/**
	 * @param procParams the procParams to set
	 */
	@Override
	public void setProcParams(List<JobOrderProcParam> procParams) {
		this.procParams = procParams;
		this.nbProcParams = procParams.size();
	}

}
