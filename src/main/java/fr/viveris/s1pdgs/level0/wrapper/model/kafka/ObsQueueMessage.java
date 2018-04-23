package fr.viveris.s1pdgs.level0.wrapper.model.kafka;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;

public class ObsQueueMessage extends AbstractQueueMessage {
	
	private String keyObs;

	public ObsQueueMessage(ProductFamily family, String productName, String keyObs) {
		super(family, productName);
		this.keyObs = keyObs;
	}

	/**
	 * @return the keyObs
	 */
	public String getKeyObs() {
		return keyObs;
	}

	/**
	 * @param keyObs the keyObs to set
	 */
	public void setKeyObs(String keyObs) {
		this.keyObs = keyObs;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyObs == null) ? 0 : keyObs.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObsQueueMessage other = (ObsQueueMessage) obj;
		if (keyObs == null) {
			if (other.keyObs != null)
				return false;
		} else if (!keyObs.equals(other.keyObs))
			return false;
		return super.equals(obj);
	}

}
