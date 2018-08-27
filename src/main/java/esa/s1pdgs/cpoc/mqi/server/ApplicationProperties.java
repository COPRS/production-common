package esa.s1pdgs.cpoc.mqi.server;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Configuration of the application: product cagtegories
 * 
 * @author Viveris Technologies
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    /**
     * Hostname.
     */
    private String hostname;
    
    /**
     * Time to wait before getting next message when API is called
     */
    private int waitNextMs;

    /**
     * Properties per product categories
     */
    private Map<ProductCategory, ProductCategoryProperties> productCategories;

    /**
     * Constructor
     */
    public ApplicationProperties() {
        super();
        this.productCategories = new HashMap<>();
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the waitNextMs
     */
    public int getWaitNextMs() {
        return waitNextMs;
    }

    /**
     * @param waitNextMs the waitNextMs to set
     */
    public void setWaitNextMs(final int waitNextMs) {
        this.waitNextMs = waitNextMs;
    }

    /**
     * @return the productCategories
     */
    public Map<ProductCategory, ProductCategoryProperties> getProductCategories() {
        return productCategories;
    }

    /**
     * @param productCategories
     *            the productCategories to set
     */
    public void setProductCategories(
            final Map<ProductCategory, ProductCategoryProperties> productCategories) {
        this.productCategories = productCategories;
    }

    /**
     * Properties of a product category
     * 
     * @author Viveris Technologies
     */
    public static class ProductCategoryProperties {

        /**
         * Consumption properties
         */
        private ProductCategoryConsumptionProperties consumption;

        /**
         * Publication properties
         */
        private ProductCategoryPublicationProperties publication;

        /**
         * Constructor
         */
        public ProductCategoryProperties() {
            super();
            this.consumption = new ProductCategoryConsumptionProperties();
            this.publication = new ProductCategoryPublicationProperties();
        }

        /**
         * Constructor
         */
        public ProductCategoryProperties(
                final ProductCategoryConsumptionProperties consumption,
                final ProductCategoryPublicationProperties publication) {
            super();
            this.consumption = consumption;
            this.publication = publication;
        }

        /**
         * @return the consumption
         */
        public ProductCategoryConsumptionProperties getConsumption() {
            return consumption;
        }

        /**
         * @param consumption
         *            the consumption to set
         */
        public void setConsumption(
                final ProductCategoryConsumptionProperties consumption) {
            this.consumption = consumption;
        }

        /**
         * @return the publication
         */
        public ProductCategoryPublicationProperties getPublication() {
            return publication;
        }

        /**
         * @param publication
         *            the publication to set
         */
        public void setPublication(
                final ProductCategoryPublicationProperties publication) {
            this.publication = publication;
        }
    }

    /**
     * Consumption properties for a product category
     * 
     * @author Viveris Technologies
     */
    public static class ProductCategoryConsumptionProperties {

        /**
         * True if the category is enable for the service
         */
        private boolean enable;

        /**
         * The topic to consume for this category
         */
        private Map<String, Integer> topicsWithPriority;

        /**
         * Constructor
         */
        public ProductCategoryConsumptionProperties() {
            super();
            this.enable = false;
        }

        /**
         * Constructor
         */
        public ProductCategoryConsumptionProperties(final boolean enable,
                final Map<String, Integer> topicsWithPriority) {
            super();
            this.enable = enable;
            this.topicsWithPriority = topicsWithPriority;
        }

        /**
         * @return the enable
         */
        public boolean isEnable() {
            return enable;
        }

        /**
         * @param enable
         *            the enable to set
         */
        public void setEnable(final boolean enable) {
            this.enable = enable;
        }

        /**
         * @return the topic
         */
        public Map<String, Integer> getTopicsWithPriority() {
            return topicsWithPriority;
        }

        /**
         * @param topics
         *            the topics to set
         */
        public void setTopicsWithPriority(final Map<String, Integer> topicsWithPriority) {
            this.topicsWithPriority = topicsWithPriority;
        }
    }

    /**
     * Publication properties for a product category
     * 
     * @author Viveris Technologies
     */
    public static class ProductCategoryPublicationProperties {

        /**
         * True if the category is enable for the service
         */
        private boolean enable;

        /**
         * The path of the routing file
         */
        private String routingFile;

        /**
         * Constructor
         */
        public ProductCategoryPublicationProperties() {
            super();
            this.enable = false;
        }

        /**
         * Constructor
         */
        public ProductCategoryPublicationProperties(final boolean enable,
                final String routingFile) {
            super();
            this.enable = enable;
            this.routingFile = routingFile;
        }

        /**
         * @return the enable
         */
        public boolean isEnable() {
            return enable;
        }

        /**
         * @param enable
         *            the enable to set
         */
        public void setEnable(final boolean enable) {
            this.enable = enable;
        }

        /**
         * @return the routingFile
         */
        public String getRoutingFile() {
            return routingFile;
        }

        /**
         * @param routingFile
         *            the routingFile to set
         */
        public void setRoutingFile(final String routingFile) {
            this.routingFile = routingFile;
        }
    }
}
