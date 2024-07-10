/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	private String uncompressionCommand = "/app/uncompression.sh";
	
	private String workingDirectory;
	
    /**
     * Timeout (in seconds) of the compression job
     */
    private long compressionTimeout;

    /**
     * Timeout (in seconds) of the overall request
     */
    private long requestTimeout;

    /**
     * Flag whether or not to skip uncompression
     */
    private boolean skipUncompression = false;
    
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

	public boolean isSkipUncompression() {
		return skipUncompression;
	}

	public void setSkipUncompression(boolean skipUncompression) {
		this.skipUncompression = skipUncompression;
	}

}
