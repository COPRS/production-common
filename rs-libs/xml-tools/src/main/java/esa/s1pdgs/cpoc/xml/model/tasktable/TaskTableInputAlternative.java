package esa.s1pdgs.cpoc.xml.model.tasktable;

import java.util.Comparator;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableFileNameType;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputOrigin;

/**
 * 
 */
@XmlRootElement(name = "Alternative")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableInputAlternative {

	public static final Comparator<TaskTableInputAlternative> ORDER = Comparator.comparingInt(TaskTableInputAlternative::getOrder);
	
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
	
	@XmlElement(name = "customClass", required = false)
	private String customClass;

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
	 * 
	 */
	public TaskTableInputAlternative() {
		super();
		this.origin = TaskTableInputOrigin.BLANK;
		this.order = 1;
		this.deltaTime0 = 0.0;
		this.deltaTime1 = 0.0;
		this.fileNameType = TaskTableFileNameType.BLANK;
	}

	/**
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
	
	
	// for comparator test
	public TaskTableInputAlternative(final int order) {
		this.order = order;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @return the origin
	 */
	public TaskTableInputOrigin getOrigin() {
		return origin;
	}

	/**
	 * @return the retrievalMode
	 */
	public String getRetrievalMode() {
		return retrievalMode;
	}
	
	public void setRetrievalMode(String retrievalMode) {
		this.retrievalMode = retrievalMode;
	}
	
	
	/**
	 * @return the custom class or null
	 */
	public String getCustomClass() {
		return customClass;
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
	 * @return the fileNameType
	 */
	public TaskTableFileNameType getFileNameType() {
		return fileNameType;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(order, origin, retrievalMode, deltaTime0, deltaTime1, fileType, fileNameType);
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
			final TaskTableInputAlternative other = (TaskTableInputAlternative) obj;
			ret = order == other.order && Objects.equals(origin, other.origin)
					&& Objects.equals(retrievalMode, other.retrievalMode)
					&& Objects.equals(customClass, other.customClass)
					&& Objects.equals(deltaTime0, other.deltaTime0) && Objects.equals(deltaTime1, other.deltaTime1)
					&& Objects.equals(fileType, other.fileType) && Objects.equals(fileNameType, other.fileNameType);
		}
		return ret;
	}
	
	

	@Override
	public String toString() {
		return "TaskTableInputAlternative [order=" + order + ", origin=" + origin + ", retrievalMode=" + retrievalMode
				+ ", deltaTime0=" + deltaTime0 + ", deltaTime1=" + deltaTime1 + ", fileType=" + fileType
				+ ", fileNameType=" + fileNameType + "]";
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

		@Override
		public String toString() {
			return "TaskTableInputAltKey{" +
					"retrievalMode='" + retrievalMode + '\'' +
					", deltaTime0=" + deltaTime0 +
					", deltaTime1=" + deltaTime1 +
					", fileType='" + fileType + '\'' +
					'}';
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
				final TaskTableInputAltKey other = (TaskTableInputAltKey) obj;
				ret = Objects.equals(retrievalMode, other.retrievalMode) && Objects.equals(deltaTime0, other.deltaTime0)
						&& Objects.equals(deltaTime1, other.deltaTime1) && Objects.equals(fileType, other.fileType);
			}
			return ret;
		}
	}
}
