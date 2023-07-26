package esa.s1pdgs.cpoc.obs_sdk;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ProductFamily;

@EnableConfigurationProperties
@ConfigurationProperties(prefix = "obs")
@Configuration
public class ObsConfigurationProperties {	
	public static final String UNDEFINED = "NOT_DEFINED";
	
	private String backend = "aws-s3";
	
	private String userId;
	
	private String userSecret;
	
	private String endpoint;
	
	private String endpointRegion;
	
	// In old versions http was enforced, so we keep it as default
	private boolean enforceHttp = true;
	
	private String tenantId = UNDEFINED;
	
	private String tenantName = UNDEFINED;
	
	private String authMethod = "KEYSTONE";
	
	private long multipartUploadThreshold = 3072;
	
	private long minUploadPartSize = 100;
	
	private int maxRetries = 3;
	
	private int maxObsRetries = 10;
	
	private int backoffBaseDelay = 1000;
	
	private int backoffThrottledBaseDelay = 6000;
	
	private int backoffMaxDelay = 20000;
	
	private int timeoutShutdown = 10;
	
	private int timeoutDownExec = 15;
	
	private int timeoutUpExec = 20;
	
	private boolean disableChunkedEncoding = false;

	private int maxInputStreamBufferSizeMb = 1024;

	private String uploadCacheLocation = "/tmp";

	private Map<ProductFamily, String> bucket = new HashMap<>();
	
	public String getBucketFor(final ProductFamily family) {
		return bucket.get(family);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserSecret() {
		return userSecret;
	}

	public void setUserSecret(String userSecret) {
		this.userSecret = userSecret;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getEndpointRegion() {
		return endpointRegion;
	}

	public void setEndpointRegion(String endpointRegion) {
		this.endpointRegion = endpointRegion;
	}
	
	public boolean isEnforceHttp() {
		return enforceHttp;
	}

	public void setEnforceHttp(boolean enforceHttp) {
		this.enforceHttp = enforceHttp;
	}

	public long getMultipartUploadThreshold() {
		return multipartUploadThreshold;
	}

	public void setMultipartUploadThreshold(long multipartUploadThreshold) {
		this.multipartUploadThreshold = multipartUploadThreshold;
	}

	public long getMinUploadPartSize() {
		return minUploadPartSize;
	}

	public void setMinUploadPartSize(long minUploadPartSize) {
		this.minUploadPartSize = minUploadPartSize;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public int getMaxObsRetries() {
		return maxObsRetries;
	}

	public void setMaxObsRetries(int maxObsRetries) {
		this.maxObsRetries = maxObsRetries;
	}

	public int getBackoffBaseDelay() {
		return backoffBaseDelay;
	}

	public void setBackoffBaseDelay(int backoffBaseDelay) {
		this.backoffBaseDelay = backoffBaseDelay;
	}

	public int getBackoffThrottledBaseDelay() {
		return backoffThrottledBaseDelay;
	}

	public void setBackoffThrottledBaseDelay(int backoffThrottledBaseDelay) {
		this.backoffThrottledBaseDelay = backoffThrottledBaseDelay;
	}

	public int getBackoffMaxDelay() {
		return backoffMaxDelay;
	}

	public void setBackoffMaxDelay(int backoffMaxDelay) {
		this.backoffMaxDelay = backoffMaxDelay;
	}
	
	public int getTimeoutShutdown() {
		return timeoutShutdown;
	}

	public void setTimeoutShutdown(int timeoutShutdown) {
		this.timeoutShutdown = timeoutShutdown;
	}

	public int getTimeoutDownExec() {
		return timeoutDownExec;
	}

	public void setTimeoutDownExec(int timeoutDownExec) {
		this.timeoutDownExec = timeoutDownExec;
	}

	public int getTimeoutUpExec() {
		return timeoutUpExec;
	}

	public void setTimeoutUpExec(int timeoutUpExec) {
		this.timeoutUpExec = timeoutUpExec;
	}

	public Map<ProductFamily, String> getBucket() {
		return bucket;
	}

	public void setBucket(Map<ProductFamily, String> buckets) {
		this.bucket = buckets;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getAuthMethod() {
		return authMethod;
	}

	public void setAuthMethod(String authMethod) {
		this.authMethod = authMethod;
	}

	public String getBackend() {
		return backend;
	}

	public void setBackend(String backend) {
		this.backend = backend;
	}

	public boolean getDisableChunkedEncoding() {
		return disableChunkedEncoding;
	}

	public void setDisableChunkedEncoding(boolean disableChunkedEncoding) {
		this.disableChunkedEncoding = disableChunkedEncoding;
	}

	public int getMaxInputStreamBufferSize() {
		return maxInputStreamBufferSizeMb * 1024 * 1024;
	}

	public Path getUploadCacheLocation() {
		return Paths.get(uploadCacheLocation);
	}

	@Override
	public String toString() {
		return "ObsConfigurationProperties [backend=" + backend + ", userId=" + userId + ", userSecret=" + userSecret
				+ ", endpoint=" + endpoint + ", endpointRegion=" + endpointRegion + ", tenantId=" + tenantId
				+ ", tenantName=" + tenantName + ", authMethod=" + authMethod + ", multipartUploadThreshold="
				+ multipartUploadThreshold + ", minUploadPartSize=" + minUploadPartSize + ", maxRetries="
				+ maxRetries+ ", maxObsRetries=" + maxObsRetries + ", backoffBaseDelay=" + backoffBaseDelay
				+ ", backoffThrottledBaseDelay=" + backoffThrottledBaseDelay + ", backoffMaxDelay=" + backoffMaxDelay
				+ ", timeoutShutdown=" + timeoutShutdown + ", timeoutDownExec=" + timeoutDownExec + ", timeoutUpExec="
				+ timeoutUpExec + ", disableChunkedEncoding=" + disableChunkedEncoding + ", bucket=" + bucket
				+ ", uploadCacheLocation=" + uploadCacheLocation + "]";
	}
}
