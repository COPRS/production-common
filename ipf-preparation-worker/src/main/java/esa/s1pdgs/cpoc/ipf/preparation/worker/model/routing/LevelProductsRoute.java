package esa.s1pdgs.cpoc.ipf.preparation.worker.model.routing;

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
@XmlRootElement(name = "level_products_route")
@XmlAccessorType(XmlAccessType.NONE)
public class LevelProductsRoute {

	/**
	 * Condition of the routing
	 */
	@XmlElement(name = "level_products_from")
	private LevelProductsRouteFrom routeFrom;

	/**
	 * List of wanted task table
	 */
	@XmlElement(name = "level_products_to")
	private LevelProductsRouteTo routeTo;

	/**
	 * Default constructor
	 */
	public LevelProductsRoute() {
		super();
	}

	/**
	 * Constructor using fields
	 * 
	 * @param from
	 * @param to
	 */
	public LevelProductsRoute(final LevelProductsRouteFrom routeFrom, final LevelProductsRouteTo routeTo) {
		this();
		this.routeFrom = routeFrom;
		this.routeTo = routeTo;
	}

	/**
	 * @return the routeFrom
	 */
	public LevelProductsRouteFrom getRouteFrom() {
		return routeFrom;
	}

	/**
	 * @param routeFrom
	 *            the routeFrom to set
	 */
	public void setRouteFrom(final LevelProductsRouteFrom routeFrom) {
		this.routeFrom = routeFrom;
	}

	/**
	 * @return the routeTo
	 */
	public LevelProductsRouteTo getRouteTo() {
		return routeTo;
	}

	/**
	 * @param routeTo
	 *            the routeTo to set
	 */
	public void setRouteTo(final LevelProductsRouteTo routeTo) {
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
			LevelProductsRoute other = (LevelProductsRoute) obj;
			ret = Objects.equals(routeFrom, other.routeFrom) && Objects.equals(routeTo, other.routeTo);
		}
		return ret;
	}

}
