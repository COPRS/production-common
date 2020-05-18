package esa.s1pdgs.cpoc.datalifecycle.worker.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ProductCategory;

@Configuration
@ConfigurationProperties("data-lifecycle-worker")
public class DataLifecycleWorkerConfigurationProperties {

    private final Map<ProductCategory, CategoryConfig> productCategories = new LinkedHashMap<>();

    public Map<ProductCategory, CategoryConfig> getProductCategories() {
        return productCategories;
    }

    @Override
    public String toString() {
        return String.format("DataLifecycleWorkerConfigurationProperties [productCategories=%s]", productCategories);
    }

    public static class CategoryConfig {
        private final long fixedDelayMs = 500L;
        private final long initDelayPolMs = 2000L;

        public long getFixedDelayMs() {
            return fixedDelayMs;
        }

        public long getInitDelayPolMs() {
            return initDelayPolMs;
        }

        @Override
        public String toString() {
            return "CategoryConfig [fixedDelayMs=" + fixedDelayMs + ", initDelayPolMs=" + initDelayPolMs + "]";
        }
    }
}
