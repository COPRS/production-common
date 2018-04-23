package fr.viveris.s1pdgs.level0.wrapper.model.kafka;

import java.io.File;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;

public class FileQueueMessage extends AbstractQueueMessage {
	
	private File file;

	public FileQueueMessage(ProductFamily family, String productName, File file) {
		super(family, productName);
		this.file = file;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileQueueMessage other = (FileQueueMessage) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return super.equals(obj);
	}

}
