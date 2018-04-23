package fr.viveris.s1pdgs.jobgenerator.model.tasktable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableFileNameType;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableInputOrigin;

@XmlRootElement(name = "Alternative")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableInputAlternative {
	
	@XmlElement(name = "Order")
	private int order;
	
	@XmlElement(name = "Origin")
	private TaskTableInputOrigin origin;
	
	@XmlElement(name = "Retrieval_Mode")
	private String retrievalMode;
	
	@XmlElement(name = "T0")
	private double deltaTime0;
	
	@XmlElement(name = "T1")
	private double deltaTime1;
	
	@XmlElement(name = "File_Type")
	private String fileType;
	
	@XmlElement(name = "File_Name_Type")
	private TaskTableFileNameType fileNameType;
	
	private int idSearchMetadataQuery;

	/**
	 * 
	 */
	public TaskTableInputAlternative() {
		super();
		this.origin = TaskTableInputOrigin.BLANK;
		this.order = 1;
		this.deltaTime0 = 0.0;
		this.deltaTime1 = 0.0;
		this.fileNameType = TaskTableFileNameType.BLANK;
		this.idSearchMetadataQuery = 0;
	}

	/**
	 * @param order
	 * @param origin
	 * @param retrievalMode
	 * @param deltaTime0
	 * @param deltaTime1
	 * @param fileType
	 * @param fileNameType
	 */
	public TaskTableInputAlternative(int order, TaskTableInputOrigin origin, String retrievalMode, double deltaTime0,
			double deltaTime1, String fileType, TaskTableFileNameType fileNameType) {
		this();
		this.order = order;
		this.origin = origin;
		this.retrievalMode = retrievalMode;
		this.deltaTime0 = deltaTime0;
		this.deltaTime1 = deltaTime1;
		this.fileType = fileType;
		this.fileNameType = fileNameType;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * @return the origin
	 */
	public TaskTableInputOrigin getOrigin() {
		return origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(TaskTableInputOrigin origin) {
		this.origin = origin;
	}

	/**
	 * @return the retrievalMode
	 */
	public String getRetrievalMode() {
		return retrievalMode;
	}

	/**
	 * @param retrievalMode the retrievalMode to set
	 */
	public void setRetrievalMode(String retrievalMode) {
		this.retrievalMode = retrievalMode;
	}

	/**
	 * @return the deltaTime0
	 */
	public double getDeltaTime0() {
		return deltaTime0;
	}

	/**
	 * @param deltaTime0 the deltaTime0 to set
	 */
	public void setDeltaTime0(int deltaTime0) {
		this.deltaTime0 = deltaTime0;
	}

	/**
	 * @return the deltaTime1
	 */
	public double getDeltaTime1() {
		return deltaTime1;
	}

	/**
	 * @param deltaTime1 the deltaTime1 to set
	 */
	public void setDeltaTime1(int deltaTime1) {
		this.deltaTime1 = deltaTime1;
	}

	/**
	 * @return the fileType
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return the fileNameType
	 */
	public TaskTableFileNameType getFileNameType() {
		return fileNameType;
	}

	/**
	 * @param fileNameType the fileNameType to set
	 */
	public void setFileNameType(TaskTableFileNameType fileNameType) {
		this.fileNameType = fileNameType;
	}

	/**
	 * @return the idSearchMetadataQuery
	 */
	public int getIdSearchMetadataQuery() {
		return idSearchMetadataQuery;
	}

	/**
	 * @param idSearchMetadataQuery the idSearchMetadataQuery to set
	 */
	public void setIdSearchMetadataQuery(int idSearchMetadataQuery) {
		this.idSearchMetadataQuery = idSearchMetadataQuery;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileNameType == null) ? 0 : fileNameType.hashCode());
		result = prime * result + ((fileType == null) ? 0 : fileType.hashCode());
		result = prime * result + idSearchMetadataQuery;
		result = prime * result + order;
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + ((retrievalMode == null) ? 0 : retrievalMode.hashCode());
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
		TaskTableInputAlternative other = (TaskTableInputAlternative) obj;
		if (deltaTime0 != other.deltaTime0)
			return false;
		if (deltaTime1 != other.deltaTime1)
			return false;
		if (fileNameType != other.fileNameType)
			return false;
		if (fileType == null) {
			if (other.fileType != null)
				return false;
		} else if (!fileType.equals(other.fileType))
			return false;
		if (idSearchMetadataQuery != other.idSearchMetadataQuery)
			return false;
		if (order != other.order)
			return false;
		if (origin != other.origin)
			return false;
		if (retrievalMode == null) {
			if (other.retrievalMode != null)
				return false;
		} else if (!retrievalMode.equals(other.retrievalMode))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TaskTableInputAlternative [order=" + order + ", origin=" + origin + ", retrievalMode=" + retrievalMode
				+ ", deltaTime0=" + deltaTime0 + ", deltaTime1=" + deltaTime1 + ", fileType=" + fileType
				+ ", fileNameType=" + fileNameType + "]";
	}
	
	public TaskTableInputAltKey getTaskTableInputAltKey() {
		return new TaskTableInputAltKey(retrievalMode, deltaTime0, deltaTime1, fileType);
	}

	public static class TaskTableInputAltKey {
		
		private String retrievalMode;
		private double deltaTime0;
		private double deltaTime1;
		private String fileType;
		
		public TaskTableInputAltKey(String retrievalMode, double deltaTime0, double deltaTime1, String fileType) {
			this.retrievalMode = retrievalMode;
			this.deltaTime0 = deltaTime0;
			this.deltaTime1 = deltaTime1;
			this.fileType = fileType;
		}

		/**
		 * @return the retrievalMode
		 */
		public String getRetrievalMode() {
			return retrievalMode;
		}

		/**
		 * @return the deltaTime0
		 */
		public double getDeltaTime0() {
			return deltaTime0;
		}

		/**
		 * @return the deltaTime1
		 */
		public double getDeltaTime1() {
			return deltaTime1;
		}

		/**
		 * @return the fileType
		 */
		public String getFileType() {
			return fileType;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fileType == null) ? 0 : fileType.hashCode());
			result = prime * result + ((retrievalMode == null) ? 0 : retrievalMode.hashCode());
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
			TaskTableInputAltKey other = (TaskTableInputAltKey) obj;
			if (deltaTime0 != other.deltaTime0)
				return false;
			if (deltaTime1 != other.deltaTime1)
				return false;
			if (fileType == null) {
				if (other.fileType != null)
					return false;
			} else if (!fileType.equals(other.fileType))
				return false;
			if (retrievalMode == null) {
				if (other.retrievalMode != null)
					return false;
			} else if (!retrievalMode.equals(other.retrievalMode))
				return false;
			return true;
		}
	}
}
