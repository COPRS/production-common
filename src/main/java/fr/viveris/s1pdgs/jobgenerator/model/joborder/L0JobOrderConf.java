package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.springframework.util.CollectionUtils;

/**
 * 
 */
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
		this.procParams = new ArrayList<>();
		this.nbProcParams = 0;
	}

	/**
	 * Clone
	 * 
	 * @param obj
	 */
	public L0JobOrderConf(final AbstractJobOrderConf obj) {
		super(obj);
		this.procParams = new ArrayList<>();
		this.nbProcParams = 0;
		if (!CollectionUtils.isEmpty(obj.getProcParams())) {
			this.procParams.addAll(obj.getProcParams().stream().filter(item -> item != null)
					.map(item -> new JobOrderProcParam(item)).collect(Collectors.toList()));
			this.nbProcParams = this.procParams.size();
		}
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
	public void addProcParam(final JobOrderProcParam param) {
		this.procParams.add(param);
		this.nbProcParams++;
	}

	/**
	 * @param procParams
	 *            the procParams to set
	 */
	@Override
	public void setProcParams(final List<JobOrderProcParam> procParams) {
		this.procParams = procParams;
		this.nbProcParams = procParams.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String superToString = super.toAbstractString();
		return String.format("{%s, procParams: %s, nbProcParams: %s}", superToString, procParams,
				nbProcParams);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int superHash = super.hashCode();
		return Objects.hash(procParams, nbProcParams, superHash);
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
			L0JobOrderConf other = (L0JobOrderConf) obj;
			ret = super.equals(other) && Objects.equals(procParams, other.procParams)
					&& nbProcParams == other.nbProcParams;
		}
		return ret;
	}

}
