package esa.s1pdgs.cpoc.mqi.model.queue;

import java.time.LocalDateTime;
import java.util.Date;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * This is supposed to be the basic element that is used in all other
 * job and event messages. It is containing all data that is shared
 * accross all of them.
 * 
 * @author florian_sievert
 *
 */
public abstract class AbstractMessage {
	
    private LocalDateTime creationDate;
    private String hostname;

	public AbstractMessage() {
		/* Most of the subsystems are not setting these
		 * values at the moment. Lets see if this automatic
		 * approach is working. 
		 */
		creationDate = LocalDateTime.now();
		hostname = System.getenv("HOSTNAME");
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
}
