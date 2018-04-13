package fr.viveris.s1pdgs.scaler.openstack.model;

public class VolumeDesc {

	private String id;

	private String name;

	private String imageRef;

	private String volumeType;

	private String zone;

	private int size;

	private boolean bootable;
	
	private String description;

	public VolumeDesc() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the imageRef
	 */
	public String getImageRef() {
		return imageRef;
	}

	/**
	 * @param imageRef
	 *            the imageRef to set
	 */
	public void setImageRef(String imageRef) {
		this.imageRef = imageRef;
	}

	/**
	 * @return the volumeType
	 */
	public String getVolumeType() {
		return volumeType;
	}

	/**
	 * @param volumeType
	 *            the volumeType to set
	 */
	public void setVolumeType(String volumeType) {
		this.volumeType = volumeType;
	}

	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}

	/**
	 * @param zone
	 *            the zone to set
	 */
	public void setZone(String zone) {
		this.zone = zone;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the bootable
	 */
	public boolean isBootable() {
		return bootable;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param bootable
	 *            the bootable to set
	 */
	public void setBootable(boolean bootable) {
		this.bootable = bootable;
	}

	/**
	 * @return the Volume Builder
	 */
	public static VolumeDescBuilder builder() {
		return new VolumeDescBuilder();
	}

	public static class VolumeDescBuilder {
		private VolumeDesc m;

		VolumeDescBuilder() {
			this(new VolumeDesc());
		}

		VolumeDescBuilder(VolumeDesc m) {
			this.m = m;
		}

		public VolumeDescBuilder id(String id) {
			this.m.id = id;
			return this;
		}

		public VolumeDescBuilder name(String name) {
			this.m.name = name;
			return this;
		}

		public VolumeDescBuilder imageRef(String imageRef) {
			this.m.imageRef = imageRef;
			return this;
		}

		public VolumeDescBuilder volumeType(String volumeType) {
			this.m.volumeType = volumeType;
			return this;
		}

		public VolumeDescBuilder description(String description) {
			this.m.description = description;
			return this;
		}

		public VolumeDescBuilder zone(String zone) {
			this.m.zone = zone;
			return this;
		}

		public VolumeDescBuilder size(int size) {
			this.m.size = size;
			return this;
		}

		public VolumeDescBuilder bootable(boolean bootable) {
			this.m.bootable = bootable;
			return this;
		}
		
		public VolumeDesc build() {
			return m;
		}
	}
}
