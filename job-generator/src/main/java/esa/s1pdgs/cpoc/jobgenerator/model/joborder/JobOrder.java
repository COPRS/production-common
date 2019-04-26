package esa.s1pdgs.cpoc.jobgenerator.model.joborder;

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

import esa.s1pdgs.cpoc.common.ApplicationLevel;

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
	public JobOrder(final JobOrder obj, final ApplicationLevel applicationLevel) {
		this();
		this.conf = applicationLevel == ApplicationLevel.L0 ? new L0JobOrderConf(obj.getConf()) : new L1JobOrderConf(obj.getConf());
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
	public void setConf(final AbstractJobOrderConf conf) {
		this.conf = conf;
	}

	/**
	 * @return the procs
	 */
	public List<JobOrderProc> getProcs() {
		return procs;
	}

	/**
	 * 
	 * @param proc
	 */
	public void addProc(final JobOrderProc proc) {
		this.procs.add(proc);
		this.nbProcs++;
	}

	/**
	 * 
	 * @param procs
	 */
	public void addProcs(final List<JobOrderProc> procs) {
		if (procs != null) {
			this.procs.addAll(procs);
			this.nbProcs += procs.size();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{conf: %s, procs: %s, nbProcs: %s}", conf, procs, nbProcs);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(conf, procs, nbProcs);
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
			JobOrder other = (JobOrder) obj;
			ret = Objects.equals(conf, other.conf) && Objects.equals(procs, other.procs) && nbProcs == other.nbProcs;
		}
		return ret;
	}

}
