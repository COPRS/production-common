package fr.viveris.s1pdgs.jobgenerator.model.l1routing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "l1_route")
@XmlAccessorType(XmlAccessType.NONE)
public class L1Route {
	
	@XmlElement(name = "l1_from")
	private L1RouteFrom from;
	
	@XmlElement(name = "l1_to")
	private L1RouteTo to;

	public L1Route() {
		
	}

	public L1Route(L1RouteFrom from, L1RouteTo to) {
		this();
		this.from = from;
		this.to= to;
	}

	/**
	 * @return the from
	 */
	public L1RouteFrom getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(L1RouteFrom from) {
		this.from = from;
	}

	/**
	 * @return the to
	 */
	public L1RouteTo getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(L1RouteTo to) {
		this.to = to;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "L1Route [from=" + from + ", to=" + to + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		L1Route other = (L1Route) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

}
