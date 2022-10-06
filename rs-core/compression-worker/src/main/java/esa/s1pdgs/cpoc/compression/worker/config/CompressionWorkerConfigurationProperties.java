package esa.s1pdgs.cpoc.compression.worker.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "compression-worker")
public class CompressionWorkerConfigurationProperties {
	/**
	 * The command that is performed to invoke the compression process
	 */
	private Map<String, String> compressionCommand = new LinkedHashMap<>();
	
	/**
	 * The command that is performed to invoke the uncompression process
	 */
	private String uncompressionCommand;
	
	private String workingDirectory;
	
    /**
     * Timeout (in seconds) of the compression job
     */
    private long compressionTimeout;

    /**
     * Timeout (in seconds) of the overall request
     */
    private long requestTimeout;

    
    private String hostname;
    
	public Map<String, String> getCompressionCommand() {
		return compressionCommand;
	}
	
	public void setCompressionCommand(Map<String, String> compressionCommand) {
		this.compressionCommand = compressionCommand;
	}

	public String getUncompressionCommand() {
		return uncompressionCommand;
	}

	public void setUncompressionCommand(String uncompressionCommand) {
		this.uncompressionCommand = uncompressionCommand;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public long getCompressionTimeout() {
		return compressionTimeout;
	}

	public void setCompressionTimeout(long compressionTimeout) {
		this.compressionTimeout = compressionTimeout;
	}

	public long getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(long requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

}
