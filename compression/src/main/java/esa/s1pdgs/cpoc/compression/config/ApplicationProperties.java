package esa.s1pdgs.cpoc.compression.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "compression")
public class ApplicationProperties {
	/**
	 * The command that is performed to invoke the compression process
	 */
	private String command;
	
	private String workingDirectory;
	
    /**
     * Timeout (in seconds) for waiting end of several tasks
     */
    private long tmProcAllTasksS;

    /**
     * Timeout (in seconds) for waiting end of one task
     */
    private long tmProcOneTaskS;

    /**
     * Timeout (in seconds) for waiting ending processing in case of forced stop
     */
    private long tmProcStopS;

    /**
     * Timeout (in seconds) for waiting stop ok
     */
    private long tmProcCheckStopS;
    
    /**
     * Maximal number of loop when waiting for inputs downloading
     */
    private int wapNbMaxLoop;
    
    /**
     * Batch size for upload
     */
    private int sizeBatchUpload;

    /**
     * Batch size for download
     */
    private int sizeBatchDownload;

    /**
     * Tempo between two loops when waiting for inputs downloading
     */
    private long wapTempoS;
    
    private String hostname;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public long getTmProcAllTasksS() {
		return tmProcAllTasksS;
	}

	public void setTmProcAllTasksS(long tmProcAllTasksS) {
		this.tmProcAllTasksS = tmProcAllTasksS;
	}

	public long getTmProcOneTaskS() {
		return tmProcOneTaskS;
	}

	public void setTmProcOneTaskS(long tmProcOneTaskS) {
		this.tmProcOneTaskS = tmProcOneTaskS;
	}

	public long getTmProcStopS() {
		return tmProcStopS;
	}

	public void setTmProcStopS(long tmProcStopS) {
		this.tmProcStopS = tmProcStopS;
	}

	public long getTmProcCheckStopS() {
		return tmProcCheckStopS;
	}

	public void setTmProcCheckStopS(long tmProcCheckStopS) {
		this.tmProcCheckStopS = tmProcCheckStopS;
	}

	public int getWapNbMaxLoop() {
		return wapNbMaxLoop;
	}

	public void setWapNbMaxLoop(int wapNbMaxLoop) {
		this.wapNbMaxLoop = wapNbMaxLoop;
	}

	public long getWapTempoS() {
		return wapTempoS;
	}

	public void setWapTempoS(long wapTempoS) {
		this.wapTempoS = wapTempoS;
	}

	public int getSizeBatchUpload() {
		return sizeBatchUpload;
	}

	public void setSizeBatchUpload(int sizeBatchUpload) {
		this.sizeBatchUpload = sizeBatchUpload;
	}

	public int getSizeBatchDownload() {
		return sizeBatchDownload;
	}

	public void setSizeBatchDownload(int sizeBatchDownload) {
		this.sizeBatchDownload = sizeBatchDownload;
	}
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

}
