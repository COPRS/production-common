package fr.viveris.s1pdgs.mqi.server.publication.routing;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import fr.viveris.s1pdgs.common.ProductFamily;

/**
 * Default route to use for a given family
 * 
 * @author Viveris Technologies
 */
@XmlRootElement(name = "default_route")
@XmlAccessorType(XmlAccessType.NONE)
public class DefaultRoute {

    /**
     * Product family
     */
    @XmlElement(name = "family")
    private ProductFamily family;

    /**
     * Route to
     */
    @XmlElement(name = "route_to")
    private RouteTo routeTo;

    /**
     * Default constructor
     */
    public DefaultRoute() {
        this.family = ProductFamily.BLANK;
    }

    /**
     * @return the family
     */
    public ProductFamily getFamily() {
        return family;
    }

    /**
     * @param family
     *            the family to set
     */
    public void setFamily(final ProductFamily family) {
        this.family = family;
    }

    /**
     * @return the routeTo
     */
    public RouteTo getRouteTo() {
        return routeTo;
    }

    /**
     * @param routeTo
     *            the routeTo to set
     */
    public void setRouteTo(final RouteTo routeTo) {
        this.routeTo = routeTo;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{family: %s, routeTo: %s}", family, routeTo);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(family, routeTo);
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
            DefaultRoute other = (DefaultRoute) obj;
            ret = Objects.equals(family, other.family)
                    && Objects.equals(routeTo, other.routeTo);
        }
        return ret;
    }
}
