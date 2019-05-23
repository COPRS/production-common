package esa.s1pdgs.cpoc.jobgenerator.model.l2routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Routing<br/>
 * Used for mapping the file routing.xml in java objects
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "l2_routing")
@XmlAccessorType(XmlAccessType.NONE)
public class L2Routing {

	/**
	 * List of configured routes
	 */
	@XmlElement(name = "l2_route")
	private List<L2Route> routes;

	/**
	 * Default constructor
	 */
	public L2Routing() {
		this.routes = new ArrayList<>();
	}

	/**
	 * @return the routes
	 */
	public List<L2Route> getRoutes() {
		return routes;
	}

	/**
	 * @param routes
	 *            the routes to set
	 */
	public void setRoutes(final List<L2Route> routes) {
		this.routes = routes;
	}

	/**
	 * @param routes
	 *            the routes to set
	 */
	public void addRoute(final L2Route route) {
		this.routes.add(route);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{routes: %s}", routes);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(routes);
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
			L2Routing other = (L2Routing) obj;
			ret = Objects.equals(routes, other.routes);
		}
		return ret;
	}

}
