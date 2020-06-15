package esa.s1pdgs.cpoc.obs_sdk;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import esa.s1pdgs.cpoc.common.ProductFamily;

// allows specifying via environmental 'obsBackend' which backend is used, e.g. aws-s3, swift
@PropertySource({"classpath:obs-${obsBackend:aws-s3}.properties"})
@EnableConfigurationProperties
@ConfigurationProperties
@Configuration
public class ObsConfigurationProperties {	
	public static final String UNDEFINED = "NOT_DEFINED";
	
	@Value("${obsBackend:aws-s3}") 
	private String backend;
	
	@Value("${user.id}") 
	private String userId;
	
	@Value("${user.secret}")
	private String userSecret;
	
	@Value("${endpoint}")
	private String endpoint;
	
	@Value("${endpoint.region}")
	private String endpointRegion;
	
	@Value("${tenant.id:NOT_DEFINED}")
	private String tenantId;
	
	@Value("${tenant.name:NOT_DEFINED}")
	private String tenantName;
	
	@Value("${endpoint.auth:KEYSTONE}")
	private String authMethod;
	
	@Value("${transfer.manager.multipart-upload-threshold-mb:3072}")
	private long multipartUploadThreshold;
	
	@Value("${transfer.manager.minimum-upload-part-size-mb:100}")
	private long minUploadPartSize;
	
	@Value("${retry-policy.condition.max-retries:3}")
	private int maxRetries;
	
	@Value("${storage_retry_obs_max-retries:10}")
	private int maxObsRetries;
	
	@Value("${retry-policy.backoff.base-delay-ms:1000}")
	private int backoffBaseDelay;
	
	@Value("${retry-policy.backoff.throttled-base-delay-ms:500 }")
	private int backoffThrottledBaseDelay;
	
	@Value("${retry-policy.backoff.max-backoff-ms:20000}")
	private int backoffMaxDelay;
	
	@Value("${timeout-s.shutdown:10}")
	private int timeoutShutdown;
	
	@Value("${timeout-s.down-exec:15}")
	private int timeoutDownExec;
	
	@Value("${timeout-s.up-exec:20}")
	private int timeoutUpExec;
	
	@Value("${disable-chunked-encoding:false}")
	private boolean disableChunkedEncoding;

	@Value("${max-input-stream-buffer-size-mb:1024}")
	private int maxInputStreamBufferSizeMb;

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

	@Override
	public String toString() {
		return "ObsConfigurationProperties [backend=" + backend + ", userId=" + userId + ", userSecret=" + userSecret
				+ ", endpoint=" + endpoint + ", endpointRegion=" + endpointRegion + ", tenantId=" + tenantId
				+ ", tenantName=" + tenantName + ", authMethod=" + authMethod + ", multipartUploadThreshold="
				+ multipartUploadThreshold + ", minUploadPartSize=" + minUploadPartSize + ", maxRetries=" + maxRetries+ ", maxObsRetries=" + maxObsRetries
				+ ", backoffBaseDelay=" + backoffBaseDelay + ", backoffThrottledBaseDelay=" + backoffThrottledBaseDelay
				+ ", backoffMaxDelay=" + backoffMaxDelay + ", timeoutShutdown=" + timeoutShutdown + ", timeoutDownExec="
				+ timeoutDownExec + ", timeoutUpExec=" + timeoutUpExec + ", disableChunkedEncoding=" + disableChunkedEncoding + ", bucket=" + bucket + "]";
	}
}
