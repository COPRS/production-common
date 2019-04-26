package esa.s1pdgs.cpoc.jobgenerator.model.l1routing;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class used to route a L0 slice to one or several tasktables<br/>
 * Used for mapping the file routing.xml in java objects
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "l1_route")
@XmlAccessorType(XmlAccessType.NONE)
public class L1Route {

	/**
	 * Condition of the routing
	 */
	@XmlElement(name = "l1_from")
	private L1RouteFrom routeFrom;

	/**
	 * List of wanted task table
	 */
	@XmlElement(name = "l1_to")
	private L1RouteTo routeTo;

	/**
	 * Default constructor
	 */
	public L1Route() {
		super();
	}

	/**
	 * Constructor using fields
	 * 
	 * @param from
	 * @param to
	 */
	public L1Route(final L1RouteFrom routeFrom, final L1RouteTo routeTo) {
		this();
		this.routeFrom = routeFrom;
		this.routeTo = routeTo;
	}

	/**
	 * @return the routeFrom
	 */
	public L1RouteFrom getRouteFrom() {
		return routeFrom;
	}

	/**
	 * @param routeFrom
	 *            the routeFrom to set
	 */
	public void setRouteFrom(final L1RouteFrom routeFrom) {
		this.routeFrom = routeFrom;
	}

	/**
	 * @return the routeTo
	 */
	public L1RouteTo getRouteTo() {
		return routeTo;
	}

	/**
	 * @param routeTo
	 *            the routeTo to set
	 */
	public void setRouteTo(final L1RouteTo routeTo) {
		this.routeTo = routeTo;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{routeFrom: %s, routeTo: %s}", routeFrom, routeTo);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(routeFrom, routeTo);
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
			L1Route other = (L1Route) obj;
			ret = Objects.equals(routeFrom, other.routeFrom) && Objects.equals(routeTo, other.routeTo);
		}
		return ret;
	}

}
