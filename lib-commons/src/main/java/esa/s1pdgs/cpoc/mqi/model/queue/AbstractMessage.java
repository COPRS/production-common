package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
	@JsonIgnore
	public static final String DEFAULT_HOSTNAME = System.getenv("HOSTNAME");
	
	@JsonIgnore
	public static final String NOT_DEFINED = "NOT_DEFINED";
		
	// use some sane defaults
	protected ProductFamily productFamily = ProductFamily.BLANK;
	protected String keyObjectStorage = NOT_DEFINED;
	
	/*
	 * WARNING: the fields below are just for informational purposes and will not be evaluated
	 * in any functional way.
	 */
	
	/* Most of the subsystems are not setting these
	 * values at the moment. Lets see if this automatic
	 * approach is working. 
	 */
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	protected Date creationDate = new Date();
	protected String hostname = DEFAULT_HOSTNAME;
	
	public AbstractMessage() {
	}
	
	public AbstractMessage(final ProductFamily productFamily, final String keyObjectStorage) {
		this.productFamily = productFamily;
		this.keyObjectStorage = keyObjectStorage;
	}

	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	public void setKeyObjectStorage(final String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(final ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(final Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}	
}
