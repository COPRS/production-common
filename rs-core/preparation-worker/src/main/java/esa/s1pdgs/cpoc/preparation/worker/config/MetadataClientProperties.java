package esa.s1pdgs.cpoc.preparation.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "metadata")
public class MetadataClientProperties {
	/**
	 * Host URI for the applicative catalog server
	 */
	private String metadataHostname;

	/**
	 * Maximal number of retries when query fails
	 */
	private int nbretry;

	/**
	 * Temporisation in ms between 2 retries
	 */
	private int temporetryms;

	public String getMetadataHostname() {
		return metadataHostname;
	}

	public void setMetadataHostname(String metadataHostname) {
		this.metadataHostname = metadataHostname;
	}

	public int getNbretry() {
		return nbretry;
	}

	public void setNbretry(int nbretry) {
		this.nbretry = nbretry;
	}

	public int getTemporetryms() {
		return temporetryms;
	}

	public void setTemporetryms(int temporetryms) {
		this.temporetryms = temporetryms;
	}
}
