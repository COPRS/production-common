package fr.viveris.s1pdgs.jobgenerator.model.l1routing;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "l1_routing")
@XmlAccessorType(XmlAccessType.NONE)
public class L1Routing {
	
	@XmlElement(name = "l1_route")
	private List<L1Route> routes;

	public L1Routing() {
		this.routes = new ArrayList<>();
	}

	/**
	 * @return the routes
	 */
	public List<L1Route> getRoutes() {
		return routes;
	}

	/**
	 * @param routes the routes to set
	 */
	public void addRoute(L1Route route) {
		this.routes.add(route);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "L1Routing [routes=" + routes + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((routes == null) ? 0 : routes.hashCode());
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
		L1Routing other = (L1Routing) obj;
		if (routes == null) {
			if (other.routes != null)
				return false;
		} else if (!routes.equals(other.routes))
			return false;
		return true;
	}

}
