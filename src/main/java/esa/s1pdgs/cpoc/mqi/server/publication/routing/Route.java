package esa.s1pdgs.cpoc.mqi.server.publication.routing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Route to use for a given inputKey
 * 
 * @author Viveris Technologies
 */
@XmlRootElement(name = "route")
@XmlAccessorType(XmlAccessType.NONE)
public class Route {

    
    /**
     * Inputkey, represent the consumed topic
     */
    @XmlElement(name = "inputKey")
    private String inputKey;
    
    /**
     * OutputKey, represent the product family
     */
    @XmlElement(name = "output_key")
    private ProductFamily outputKey;
    
    /**
     * Routeto, where the message shall be sent
     */
    @XmlElement(name = "route_to")
    private RouteTo routeTo;
    
    /**
     * Default Constructor
     */
    public Route() {
        this.outputKey = ProductFamily.BLANK;
    }
    
    /**
     * Constructor
     */
    public Route(String inputKey, ProductFamily outputKey, RouteTo routeTo) {
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.routeTo = routeTo;
    }

    /**
     * @return the inpyutKey
     */
    public String getInputKey() {
        return inputKey;
    }

    /**
     * @param inpyutKey the inpyutKey to set
     */
    public void setInputKey(String inpyutKey) {
        this.inputKey = inpyutKey;
    }

    /**
     * @return the outputKey
     */
    public ProductFamily getOutputKey() {
        return outputKey;
    }

    /**
     * @param outputKey the outputKey to set
     */
    public void setOutputKey(ProductFamily outputKey) {
        this.outputKey = outputKey;
    }

    /**
     * @return the routeTo
     */
    public RouteTo getRouteTo() {
        return routeTo;
    }

    /**
     * @param routeTo the routeTo to set
     */
    public void setRouteTo(RouteTo routeTo) {
        this.routeTo = routeTo;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Route [inputKey=" + inputKey + ", outputKey=" + outputKey
                + ", routeTo=" + routeTo + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((inputKey == null) ? 0 : inputKey.hashCode());
        result = prime * result
                + ((outputKey == null) ? 0 : outputKey.hashCode());
        result = prime * result + ((routeTo == null) ? 0 : routeTo.hashCode());
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
        Route other = (Route) obj;
        if (inputKey == null) {
            if (other.inputKey != null)
                return false;
        } else if (!inputKey.equals(other.inputKey))
            return false;
        if (outputKey != other.outputKey)
            return false;
        if (routeTo == null) {
            if (other.routeTo != null)
                return false;
        } else if (!routeTo.equals(other.routeTo))
            return false;
        return true;
    }
    
    
    
}
