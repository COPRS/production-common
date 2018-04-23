package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import fr.viveris.s1pdgs.jobgenerator.model.ProcessLevel;

/**
 * Class describing the content of a file jobOrder.xml
 * 
 * @author Cyrielle
 *
 */
@XmlRootElement(name = "Ipf_Job_Order")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrder {

	/**
	 * Global configuration
	 */
	@XmlElement(name = "Ipf_Conf")
	private AbstractJobOrderConf conf;

	/**
	 * Processors
	 */
	@XmlElementWrapper(name = "List_of_Ipf_Procs")
	@XmlElement(name = "Ipf_Proc")
	private List<JobOrderProc> procs;

	/**
	 * Number of processors. Automatically field
	 */
	@XmlPath("List_of_Ipf_Procs/@count")
	private int nbProcs;

	/**
	 * Default constructor
	 */
	public JobOrder() {
		super();
		this.procs = new ArrayList<>();
		this.nbProcs = 0;
	}

	/**
	 * Clone
	 * 
	 * @param obj
	 */
	public JobOrder(JobOrder obj, ProcessLevel level) {
		this();
		if (obj.getConf() != null) {
			switch (level) {
			case L0:
				this.conf = new L0JobOrderConf(obj.getConf());
				break;
			case L1:
				this.conf = new L1JobOrderConf(obj.getConf());
				break;
			}
		}
		this.procs.addAll(obj.getProcs().stream().filter(item -> item != null).map(item -> new JobOrderProc(item))
				.collect(Collectors.toList()));
		this.nbProcs = this.procs.size();
	}

	/**
	 * @return the conf
	 */
	public AbstractJobOrderConf getConf() {
		return conf;
	}

	/**
	 * @param conf
	 *            the conf to set
	 */
	public void setConf(AbstractJobOrderConf conf) {
		this.conf = conf;
	}

	/**
	 * @return the procs
	 */
	public List<JobOrderProc> getProcs() {
		return procs;
	}

	public void addProc(JobOrderProc proc) {
		this.procs.add(proc);
		this.nbProcs++;
	}

	public void addProcs(List<JobOrderProc> procs) {
		if (procs != null) {
			this.procs.addAll(procs);
			this.nbProcs += procs.size();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobOrder [conf=" + conf + ", procs=" + procs + ", nbProcs=" + nbProcs + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conf == null) ? 0 : conf.hashCode());
		result = prime * result + nbProcs;
		result = prime * result + ((procs == null) ? 0 : procs.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		JobOrder other = (JobOrder) obj;
		if (nbProcs != other.nbProcs)
			return false;
		if (conf == null) {
			if (other.conf != null)
				return false;
		} else if (!conf.equals(other.conf))
			return false;
		if (procs == null) {
			if (other.procs != null)
				return false;
		} else if (!procs.equals(other.procs))
			return false;
		return true;
	}

}
