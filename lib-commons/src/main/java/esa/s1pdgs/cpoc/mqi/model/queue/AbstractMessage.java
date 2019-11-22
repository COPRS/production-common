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
	private ProductFamily productFamily;
	
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
	
	public AbstractMessage(ProductFamily productFamily) {
		super();
		this.productFamily = productFamily;
	}

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + ((productFamily == null) ? 0 : productFamily.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractMessage other = (AbstractMessage) obj;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (productFamily != other.productFamily)
			return false;
		return true;
	}
	
	
}
