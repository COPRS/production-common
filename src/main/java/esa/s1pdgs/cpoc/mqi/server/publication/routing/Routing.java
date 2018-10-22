package esa.s1pdgs.cpoc.mqi.server.publication.routing;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Routing for publishing messages
 * @author Viveris Technologies
 *
 */
@XmlRootElement(name = "routing")
@XmlAccessorType(XmlAccessType.NONE)
public class Routing {

    /**
     * List of configured routes
     */
    @XmlElement(name = "default_route")
    private List<DefaultRoute> defaultRoutes;
    
    /**
     * List of specific routes
     */
    @XmlElement(name = "route")
    private List<Route> routes;
    
    
    /**
     * Default constructor
     */
    public Routing() {
        this.defaultRoutes = new ArrayList<>();
        this.routes = new ArrayList<>();
    }

    /**
     * @return the defaultRoutes
     */
    public List<DefaultRoute> getDefaultRoutes() {
        return defaultRoutes;
    }

    /**
     * @param defaultRoutes
     *            the defaultRoutes to set
     */
    public void setDefaultRoutes(final List<DefaultRoute> routes) {
        this.defaultRoutes = routes;
    }

    /**
     * @param defaultRoutes
     *            the routes to set
     */
    public void addRoute(final DefaultRoute route) {
        this.defaultRoutes.add(route);
    }
    
    /**
     * Get the default route for given family
     */
    public DefaultRoute getDefaultRoute(final ProductFamily family) {
        DefaultRoute result = null;
        for (DefaultRoute dft : this.defaultRoutes) {
            if (dft.getFamily().equals(family)) {
                result = dft;
            }
        }
        return result;
    }

    /**
     * @return the routes
     */
    public List<Route> getRoutes() {
        return routes;
    }

    /**
     * @param routes the routes to set
     */
    public void setRoutes(final List<Route> routes) {
        this.routes = routes;
    }
    
    /**
     * Add a route to the list of route
     *
     * @param route
     */
    public void addRoute(final Route route) {
        this.routes.add(route);
    }
    
    /**
     * @param inputKey
     * 
     * @return the route
     * 
     */
    public Route getRoute(final String inputKey) {
        Route result = null;
        for(Route rte : this.routes) {
            if(rte.getInputKey().equals(inputKey)) {
                result = rte;
            }
        }
        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{defaultRoutes: %s, routes: %s}", defaultRoutes, routes);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((defaultRoutes == null) ? 0 : defaultRoutes.hashCode());
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
        Routing other = (Routing) obj;
        if (defaultRoutes == null) {
            if (other.defaultRoutes != null)
                return false;
        } else if (!defaultRoutes.equals(other.defaultRoutes))
            return false;
        if (routes == null) {
            if (other.routes != null)
                return false;
        } else if (!routes.equals(other.routes))
            return false;
        return true;
    }
}
