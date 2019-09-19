package esa.s1pdgs.cpoc.metadata.client;

public enum MetadataCatalogRestPath {

	EDRS_SESSION("edrsSession"), METADATA("metadata"), L0_SLICE("l0Slice"), LEVEL_SEGMENT("level_segment");

	private final String path;

	private MetadataCatalogRestPath(String path) {
		this.path = path;
	}

	public String path() {
		return this.path;
	}
}
