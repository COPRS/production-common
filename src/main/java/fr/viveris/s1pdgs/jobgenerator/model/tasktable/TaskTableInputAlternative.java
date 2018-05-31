package fr.viveris.s1pdgs.jobgenerator.model.tasktable;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableFileNameType;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableInputOrigin;

/**
 * 
 */
@XmlRootElement(name = "Alternative")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableInputAlternative {

	/**
	 * 
	 */
	@XmlElement(name = "Order")
	private int order;

	/**
	 * 
	 */
	@XmlElement(name = "Origin")
	private TaskTableInputOrigin origin;

	/**
	 * 
	 */
	@XmlElement(name = "Retrieval_Mode")
	private String retrievalMode;

	/**
	 * 
	 */
	@XmlElement(name = "T0")
	private double deltaTime0;

	/**
	 * 
	 */
	@XmlElement(name = "T1")
	private double deltaTime1;

	/**
	 * 
	 */
	@XmlElement(name = "File_Type")
	private String fileType;

	/**
	 * 
	 */
	@XmlElement(name = "File_Name_Type")
	private TaskTableFileNameType fileNameType;

	/**
	 * Used to distinct queries according the natural key
	 */
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
	public TaskTableInputAlternative(final int order, final TaskTableInputOrigin origin, final String retrievalMode,
			final double deltaTime0, final double deltaTime1, final String fileType,
			final TaskTableFileNameType fileNameType) {
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
	 * @param order
	 *            the order to set
	 */
	public void setOrder(final int order) {
		this.order = order;
	}

	/**
	 * @return the origin
	 */
	public TaskTableInputOrigin getOrigin() {
		return origin;
	}

	/**
	 * @param origin
	 *            the origin to set
	 */
	public void setOrigin(final TaskTableInputOrigin origin) {
		this.origin = origin;
	}

	/**
	 * @return the retrievalMode
	 */
	public String getRetrievalMode() {
		return retrievalMode;
	}

	/**
	 * @param retrievalMode
	 *            the retrievalMode to set
	 */
	public void setRetrievalMode(final String retrievalMode) {
		this.retrievalMode = retrievalMode;
	}

	/**
	 * @return the deltaTime0
	 */
	public double getDeltaTime0() {
		return deltaTime0;
	}

	/**
	 * @param deltaTime0
	 *            the deltaTime0 to set
	 */
	public void setDeltaTime0(final int deltaTime0) {
		this.deltaTime0 = deltaTime0;
	}

	/**
	 * @return the deltaTime1
	 */
	public double getDeltaTime1() {
		return deltaTime1;
	}

	/**
	 * @param deltaTime1
	 *            the deltaTime1 to set
	 */
	public void setDeltaTime1(final int deltaTime1) {
		this.deltaTime1 = deltaTime1;
	}

	/**
	 * @return the fileType
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * @param fileType
	 *            the fileType to set
	 */
	public void setFileType(final String fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return the fileNameType
	 */
	public TaskTableFileNameType getFileNameType() {
		return fileNameType;
	}

	/**
	 * @param fileNameType
	 *            the fileNameType to set
	 */
	public void setFileNameType(final TaskTableFileNameType fileNameType) {
		this.fileNameType = fileNameType;
	}

	/**
	 * @return the idSearchMetadataQuery
	 */
	public int getIdSearchMetadataQuery() {
		return idSearchMetadataQuery;
	}

	/**
	 * @param idSearchMetadataQuery
	 *            the idSearchMetadataQuery to set
	 */
	public void setIdSearchMetadataQuery(final int idSearchMetadataQuery) {
		this.idSearchMetadataQuery = idSearchMetadataQuery;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(order, origin, retrievalMode, deltaTime0, deltaTime1, fileType, fileNameType,
				idSearchMetadataQuery);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			TaskTableInputAlternative other = (TaskTableInputAlternative) obj;
			ret = order == other.order && Objects.equals(origin, other.origin)
					&& Objects.equals(retrievalMode, other.retrievalMode)
					&& Objects.equals(deltaTime0, other.deltaTime0) && Objects.equals(deltaTime1, other.deltaTime1)
					&& Objects.equals(fileType, other.fileType) && Objects.equals(fileNameType, other.fileNameType)
					&& idSearchMetadataQuery == other.idSearchMetadataQuery;
		}
		return ret;
	}

	public TaskTableInputAltKey getTaskTableInputAltKey() {
		return new TaskTableInputAltKey(retrievalMode, deltaTime0, deltaTime1, fileType);
	}

	/**
	 * Natural identifier of a task input
	 */
	public static class TaskTableInputAltKey {

		/**
		 * 
		 */
		private final String retrievalMode;

		/**
		 * 
		 */
		private final double deltaTime0;

		/**
		 * 
		 */
		private final double deltaTime1;

		/**
		 * 
		 */
		private final String fileType;

		/**
		 * 
		 * @param retrievalMode
		 * @param deltaTime0
		 * @param deltaTime1
		 * @param fileType
		 */
		public TaskTableInputAltKey(final String retrievalMode, final double deltaTime0, final double deltaTime1,
				final String fileType) {
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

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(retrievalMode, deltaTime0, deltaTime1, fileType);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj) {
			boolean ret;
			if (this == obj) {
				ret = true;
			} else if (obj == null || getClass() != obj.getClass()) {
				ret = false;
			} else {
				TaskTableInputAltKey other = (TaskTableInputAltKey) obj;
				ret = Objects.equals(retrievalMode, other.retrievalMode) && Objects.equals(deltaTime0, other.deltaTime0)
						&& Objects.equals(deltaTime1, other.deltaTime1) && Objects.equals(fileType, other.fileType);
			}
			return ret;
		}
	}
}
