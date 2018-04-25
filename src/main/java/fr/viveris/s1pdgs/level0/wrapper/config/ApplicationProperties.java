package fr.viveris.s1pdgs.level0.wrapper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import fr.viveris.s1pdgs.level0.wrapper.model.ApplicationLevel;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "process")
public class ApplicationProperties {

	private ApplicationLevel level;
	
	private long timeoutProcessAllTasksS;
	private long timeoutProcessOneTaskS;
	private long timeoutProcessStopS;
	private long timeoutProcessCheckStopS;
	private long timeoutBatchS3UploadS;
	private long timeoutBatchS3DownloadS;
	private int sizeBatchS3Upload;
	private int sizeBatchS3Download;
	
	private int waitActiveProcessNbMaxLoop;
	private long waitActiveProcessTempoS;
	
	public ApplicationProperties() {
	}

	/**
	 * @return the level
	 */
	public ApplicationLevel getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(ApplicationLevel level) {
		this.level = level;
	}
	

	/**
	 * @return the timeoutProcessAllTasksS
	 */
	public long getTimeoutProcessAllTasksS() {
		return timeoutProcessAllTasksS;
	}

	/**
	 * @param timeoutProcessAllTasksS the timeoutProcessAllTasksS to set
	 */
	public void setTimeoutProcessAllTasksS(long timeoutProcessAllTasksS) {
		this.timeoutProcessAllTasksS = timeoutProcessAllTasksS;
	}

	/**
	 * @return the timeoutProcessOneTaskS
	 */
	public long getTimeoutProcessOneTaskS() {
		return timeoutProcessOneTaskS;
	}

	/**
	 * @param timeoutProcessOneTaskS the timeoutProcessOneTaskS to set
	 */
	public void setTimeoutProcessOneTaskS(long timeoutProcessOneTaskS) {
		this.timeoutProcessOneTaskS = timeoutProcessOneTaskS;
	}

	/**
	 * @return the timeoutProcessStopS
	 */
	public long getTimeoutProcessStopS() {
		return timeoutProcessStopS;
	}

	/**
	 * @param timeoutProcessStopS the timeoutProcessStopS to set
	 */
	public void setTimeoutProcessStopS(long timeoutProcessStopS) {
		this.timeoutProcessStopS = timeoutProcessStopS;
	}

	/**
	 * @return the timeoutProcessCheckStopS
	 */
	public long getTimeoutProcessCheckStopS() {
		return timeoutProcessCheckStopS;
	}

	/**
	 * @param timeoutProcessCheckStopS the timeoutProcessCheckStopS to set
	 */
	public void setTimeoutProcessCheckStopS(long timeoutProcessCheckStopS) {
		this.timeoutProcessCheckStopS = timeoutProcessCheckStopS;
	}

	/**
	 * @return the timeoutBatchS3UploadS
	 */
	public long getTimeoutBatchS3UploadS() {
		return timeoutBatchS3UploadS;
	}

	/**
	 * @param timeoutBatchS3UploadS the timeoutBatchS3UploadS to set
	 */
	public void setTimeoutBatchS3UploadS(long timeoutBatchS3UploadS) {
		this.timeoutBatchS3UploadS = timeoutBatchS3UploadS;
	}

	/**
	 * @return the timeoutBatchS3DownloadS
	 */
	public long getTimeoutBatchS3DownloadS() {
		return timeoutBatchS3DownloadS;
	}

	/**
	 * @param timeoutBatchS3DownloadS the timeoutBatchS3DownloadS to set
	 */
	public void setTimeoutBatchS3DownloadS(long timeoutBatchS3DownloadS) {
		this.timeoutBatchS3DownloadS = timeoutBatchS3DownloadS;
	}

	/**
	 * @return the sizeBatchS3Upload
	 */
	public int getSizeBatchS3Upload() {
		return sizeBatchS3Upload;
	}

	/**
	 * @param sizeBatchS3Upload the sizeBatchS3Upload to set
	 */
	public void setSizeBatchS3Upload(int sizeBatchS3Upload) {
		this.sizeBatchS3Upload = sizeBatchS3Upload;
	}

	/**
	 * @return the sizeBatchS3Download
	 */
	public int getSizeBatchS3Download() {
		return sizeBatchS3Download;
	}

	/**
	 * @param sizeBatchS3Download the sizeBatchS3Download to set
	 */
	public void setSizeBatchS3Download(int sizeBatchS3Download) {
		this.sizeBatchS3Download = sizeBatchS3Download;
	}

	/**
	 * @return the waitActiveProcessNbMaxLoop
	 */
	public int getWaitActiveProcessNbMaxLoop() {
		return waitActiveProcessNbMaxLoop;
	}

	/**
	 * @param waitActiveProcessNbMaxLoop the waitActiveProcessNbMaxLoop to set
	 */
	public void setWaitActiveProcessNbMaxLoop(int waitActiveProcessNbMaxLoop) {
		this.waitActiveProcessNbMaxLoop = waitActiveProcessNbMaxLoop;
	}

	/**
	 * @return the waitActiveProcessTempoS
	 */
	public long getWaitActiveProcessTempoS() {
		return waitActiveProcessTempoS;
	}

	/**
	 * @param waitActiveProcessTempoS the waitActiveProcessTempoS to set
	 */
	public void setWaitActiveProcessTempoS(long waitActiveProcessTempoS) {
		this.waitActiveProcessTempoS = waitActiveProcessTempoS;
	}

}
