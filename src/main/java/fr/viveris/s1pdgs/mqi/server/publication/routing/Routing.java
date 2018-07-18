package fr.viveris.s1pdgs.mqi.server.publication.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
     * Default constructor
     */
    public Routing() {
        this.defaultRoutes = new ArrayList<>();
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{defaultRoutes: %s}", defaultRoutes);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(defaultRoutes);
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
            Routing other = (Routing) obj;
            ret = Objects.equals(defaultRoutes, other.defaultRoutes);
        }
        return ret;
    }
}
