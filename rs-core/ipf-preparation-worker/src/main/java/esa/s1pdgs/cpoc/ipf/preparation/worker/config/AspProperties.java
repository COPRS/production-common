package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "l0asp")
public class AspProperties {
	
	private int waitingTimeHoursMinimalFast;
	private int waitingTimeHoursNominalFast;
	private int waitingTimeHoursMinimalNrtPt;
	private int waitingTimeHoursNominalNrtPt;
	
	private boolean disableTimeout;
	
	// --------------------------------------------------------------------------
	
	public int getWaitingTimeHoursMinimalFast() {
		return this.waitingTimeHoursMinimalFast;
	}
	
	public void setWaitingTimeHoursMinimalFast(int waitingTimeHoursMinimalFast) {
		this.waitingTimeHoursMinimalFast = waitingTimeHoursMinimalFast;
	}
	
	public int getWaitingTimeHoursNominalFast() {
		return this.waitingTimeHoursNominalFast;
	}
	
	public void setWaitingTimeHoursNominalFast(int waitingTimeHoursNominalFast) {
		this.waitingTimeHoursNominalFast = waitingTimeHoursNominalFast;
	}
	
	public int getWaitingTimeHoursMinimalNrtPt() {
		return this.waitingTimeHoursMinimalNrtPt;
	}
	
	public void setWaitingTimeHoursMinimalNrtPt(int waitingTimeHoursMinimalNrtPt) {
		this.waitingTimeHoursMinimalNrtPt = waitingTimeHoursMinimalNrtPt;
	}
	
	public int getWaitingTimeHoursNominalNrtPt() {
		return this.waitingTimeHoursNominalNrtPt;
	}
	
	public void setWaitingTimeHoursNominalNrtPt(int waitingTimeHoursNominalNrtPt) {
		this.waitingTimeHoursNominalNrtPt = waitingTimeHoursNominalNrtPt;
	}

	public boolean isDisableTimeout() {
		return this.disableTimeout;
	}

	public void setDisableTimeout(boolean disableTimeout) {
		this.disableTimeout = disableTimeout;
	}
	
}
