package fr.viveris.s1pdgs.scaler.k8s;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "wrapper")
public class WrapperProperties {

	// -------------------------
	// Nodes configuration
	// -------------------------
	private LabelKubernetes labelWrapperConfig;
	private LabelKubernetes labelWrapperStateUsed;
	private LabelKubernetes labelWrapperStateUnused;

	// -------------------------
	// Pods configuration
	// nbPoolingPods shall be a multiple of nbPodsPerServer
	// (nbMinServers * nbPodsPerServer) / nbPoolingPods shall be int
	// (nbMaxServers * nbPodsPerServer) / nbPoolingPods shall be int
	// nbMinServers * nbPodsPerServer >= nbPoolingPods
	// -------------------------
	private LabelKubernetes labelWrapperApp;
	private int nbPoolingPods;
	private int nbMinServers;
	private int nbMaxServers;
	private int nbPodsPerServer;
	private String podTemplateFile;

	// -------------------------
	// Pods configuration
	// -------------------------
	private TimeProperties executionTime;
	private long tempoPoolingMs;
	private long tempoScalingS;
	private long tempoDeleteResourcesS;
	private long waitPodDeletionMs;

	// -------------------------
	// REST API configuration
	// -------------------------
	private RestApiProperties restApi;

	public WrapperProperties() {

	}

	/**
	 * @return the labelWrapperConfig
	 */
	public LabelKubernetes getLabelWrapperConfig() {
		return labelWrapperConfig;
	}

	/**
	 * @param labelWrapperConfig
	 *            the labelWrapperConfig to set
	 */
	public void setLabelWrapperConfig(LabelKubernetes labelWrapperConfig) {
		this.labelWrapperConfig = labelWrapperConfig;
	}

	/**
	 * @return the labelWrapperStateUsed
	 */
	public LabelKubernetes getLabelWrapperStateUsed() {
		return labelWrapperStateUsed;
	}

	/**
	 * @param labelWrapperStateUsed
	 *            the labelWrapperStateUsed to set
	 */
	public void setLabelWrapperStateUsed(LabelKubernetes labelWrapperStateUsed) {
		this.labelWrapperStateUsed = labelWrapperStateUsed;
	}

	/**
	 * @return the labelWrapperStateUnused
	 */
	public LabelKubernetes getLabelWrapperStateUnused() {
		return labelWrapperStateUnused;
	}

	/**
	 * @param labelWrapperStateUnused
	 *            the labelWrapperStateUnused to set
	 */
	public void setLabelWrapperStateUnused(LabelKubernetes labelWrapperStateUnused) {
		this.labelWrapperStateUnused = labelWrapperStateUnused;
	}

	/**
	 * @return the labelWrapperApp
	 */
	public LabelKubernetes getLabelWrapperApp() {
		return labelWrapperApp;
	}

	/**
	 * @param labelWrapperApp
	 *            the labelWrapperApp to set
	 */
	public void setLabelWrapperApp(LabelKubernetes labelWrapperApp) {
		this.labelWrapperApp = labelWrapperApp;
	}

	/**
	 * @return the nbPoolingPods
	 */
	public int getNbPoolingPods() {
		return nbPoolingPods;
	}

	/**
	 * @param nbPoolingPods the nbPoolingPods to set
	 */
	public void setNbPoolingPods(int nbPoolingPods) {
		this.nbPoolingPods = nbPoolingPods;
	}

	/**
	 * @return the nbMinServers
	 */
	public int getNbMinServers() {
		return nbMinServers;
	}

	/**
	 * @param nbMinServers the nbMinServers to set
	 */
	public void setNbMinServers(int nbMinServers) {
		this.nbMinServers = nbMinServers;
	}

	/**
	 * @return the nbMaxServers
	 */
	public int getNbMaxServers() {
		return nbMaxServers;
	}

	/**
	 * @param nbMaxServers the nbMaxServers to set
	 */
	public void setNbMaxServers(int nbMaxServers) {
		this.nbMaxServers = nbMaxServers;
	}

	/**
	 * @return the nbPodsPerServer
	 */
	public int getNbPodsPerServer() {
		return nbPodsPerServer;
	}

	/**
	 * @param nbPodsPerServer the nbPodsPerServer to set
	 */
	public void setNbPodsPerServer(int nbPodsPerServer) {
		this.nbPodsPerServer = nbPodsPerServer;
	}

	/**
	 * @return the podTemplateFile
	 */
	public String getPodTemplateFile() {
		return podTemplateFile;
	}

	/**
	 * @param podTemplateFile the podTemplateFile to set
	 */
	public void setPodTemplateFile(String podTemplateFile) {
		this.podTemplateFile = podTemplateFile;
	}

	/**
	 * @return the executionTime
	 */
	public TimeProperties getExecutionTime() {
		return executionTime;
	}

	/**
	 * @param executionTime
	 *            the executionTime to set
	 */
	public void setExecutionTime(TimeProperties executionTime) {
		this.executionTime = executionTime;
	}

	/**
	 * @return the tempoPoolingMs
	 */
	public long getTempoPoolingMs() {
		return tempoPoolingMs;
	}

	/**
	 * @param tempoPoolingMs the tempoPoolingMs to set
	 */
	public void setTempoPoolingMs(long tempoPoolingMs) {
		this.tempoPoolingMs = tempoPoolingMs;
	}

	/**
	 * @return the tempoScalingS
	 */
	public long getTempoScalingS() {
		return tempoScalingS;
	}

	/**
	 * @param tempoScalingS the tempoScalingS to set
	 */
	public void setTempoScalingS(long tempoScalingS) {
		this.tempoScalingS = tempoScalingS;
	}

	/**
	 * @return the tempoDeleteResourcesS
	 */
	public long getTempoDeleteResourcesS() {
		return tempoDeleteResourcesS;
	}

	/**
	 * @param tempoDeleteResourcesS the tempoDeleteResourcesS to set
	 */
	public void setTempoDeleteResourcesS(long tempoDeleteResourcesS) {
		this.tempoDeleteResourcesS = tempoDeleteResourcesS;
	}

	/**
	 * @return the waitPodDeletionMs
	 */
	public long getWaitPodDeletionMs() {
		return waitPodDeletionMs;
	}

	/**
	 * @param waitPodDeletionMs the waitPodDeletionMs to set
	 */
	public void setWaitPodDeletionMs(long waitPodDeletionMs) {
		this.waitPodDeletionMs = waitPodDeletionMs;
	}

	/**
	 * @return the restApi
	 */
	public RestApiProperties getRestApi() {
		return restApi;
	}

	/**
	 * @param restApi
	 *            the restApi to set
	 */
	public void setRestApi(RestApiProperties restApi) {
		this.restApi = restApi;
	}

	public static class LabelKubernetes {
		private String label;
		private String value;

		public LabelKubernetes() {

		}

		public LabelKubernetes(String label, String value) {
			this.label = label;
			this.value = value;
		}

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * @param label
		 *            the label to set
		 */
		public void setLabel(String label) {
			this.label = label;
		}

		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @param value
		 *            the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}

	}

	public static class RestApiProperties {
		private int port;

		public RestApiProperties() {

		}

		/**
		 * @return the port
		 */
		public int getPort() {
			return port;
		}

		/**
		 * @param port
		 *            the port to set
		 */
		public void setPort(int port) {
			this.port = port;
		}
	}

	public static class TimeProperties {
		private long averageS;
		private double minThresholdS;
		private double maxThresholdS;

		public TimeProperties() {

		}

		/**
		 * @return the averageS
		 */
		public long getAverageS() {
			return averageS;
		}

		/**
		 * @param averageS the averageS to set
		 */
		public void setAverageS(long averageS) {
			this.averageS = averageS;
		}

		/**
		 * @return the minThresholdS
		 */
		public double getMinThresholdS() {
			return minThresholdS;
		}

		/**
		 * @param minThresholdS the minThresholdS to set
		 */
		public void setMinThresholdS(double minThresholdS) {
			this.minThresholdS = minThresholdS;
		}

		/**
		 * @return the maxThresholdS
		 */
		public double getMaxThresholdS() {
			return maxThresholdS;
		}

		/**
		 * @param maxThresholdS the maxThresholdS to set
		 */
		public void setMaxThresholdS(double maxThresholdS) {
			this.maxThresholdS = maxThresholdS;
		}

	}
}
