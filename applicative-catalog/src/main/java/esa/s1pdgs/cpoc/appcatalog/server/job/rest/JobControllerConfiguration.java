package esa.s1pdgs.cpoc.appcatalog.server.job.rest;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ProductCategory;

@ConfigurationProperties(ignoreInvalidFields = true, prefix = "")
public class JobControllerConfiguration {
	static class Generations {
		private int maxErrorsInitial=50;
		private int maxErrorsPrimaryCheck=300;
		
		public int getMaxErrorsInitial() {
			return maxErrorsInitial;
		}
		public void setMaxErrorsInitial(int maxErrorsInitial) {
			this.maxErrorsInitial = maxErrorsInitial;
		}
		public int getMaxErrorsPrimaryCheck() {
			return maxErrorsPrimaryCheck;
		}
		public void setMaxErrorsPrimaryCheck(int maxErrorsPrimaryCheck) {
			this.maxErrorsPrimaryCheck = maxErrorsPrimaryCheck;
		}
		
		@Override
		public String toString() {
			return "Generations [maxErrorsInitial=" + maxErrorsInitial + ", maxErrorsPrimaryCheck="
					+ maxErrorsPrimaryCheck + "]";
		}
	}
	
	// sorry, couldn't get it mapped properly to Map<ProductCategory,Generations> (even with converter)
	private Map<String,Map<String,Generations>> jobs;

	public Map<String,Map<String,Generations>> getJobs() {
		return jobs;
	}

	public void setJobs(Map<String, Map<String,Generations>> jobs) {
		this.jobs = jobs;
	}
	
	public final Generations getFor(final ProductCategory category) {
		final String catString = category.toString().toLowerCase().replace('_', '-');		
		return jobs.get(catString).get("generations");
	}

	@Override
	public String toString() {
		return "JobControllerConfiguration [jobs=" + jobs + "]";
	}
}
