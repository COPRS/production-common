package esa.s1pdgs.cpoc.mdc.trigger.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import esa.s1pdgs.cpoc.common.ProductCategory;


@ConfigurationProperties("trigger")
public class MdcTriggerConfigurationProperties {
	public static class CategoryConfig
	{
		private long fixedDelayMs = 500L;
		private long initDelayPolMs = 2000L;

		public long getFixedDelayMs() {
			return fixedDelayMs;
		}

		public void setFixedDelayMs(final long fixedDelayMs) {
			this.fixedDelayMs = fixedDelayMs;
		}

		public long getInitDelayPolMs() {
			return initDelayPolMs;
		}

		public void setInitDelayPolMs(final long initDelayPolMs) {
			this.initDelayPolMs = initDelayPolMs;
		}

		@Override
		public String toString() {
			return "CategoryConfig [fixedDelayMs=" + fixedDelayMs + ", initDelayPolMs=" + initDelayPolMs + "]";
		}
	}
	
	  
	private String fileWithManifestExt = ".safe";
	private String manifestFilename = "manifest.safe";  
	private Map<ProductCategory, CategoryConfig> productCategories = new LinkedHashMap<>();

	public Map<ProductCategory, CategoryConfig> getProductCategories() {
		return productCategories;
	}

	public void setProductCategories(final Map<ProductCategory, CategoryConfig> productCategories) {
		this.productCategories = productCategories;
	}

	public String getFileWithManifestExt() {
		return fileWithManifestExt;
	}

	public void setFileWithManifestExt(final String fileWithManifestExt) {
		this.fileWithManifestExt = fileWithManifestExt;
	}

	public String getManifestFilename() {
		return manifestFilename;
	}

	public void setManifestFilename(final String manifestFilename) {
		this.manifestFilename = manifestFilename;
	}
}
