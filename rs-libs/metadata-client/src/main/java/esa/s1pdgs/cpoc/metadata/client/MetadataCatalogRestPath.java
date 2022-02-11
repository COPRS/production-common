package esa.s1pdgs.cpoc.metadata.client;

public enum MetadataCatalogRestPath {

	EDRS_SESSION("edrsSession"), METADATA("metadata"), L0_SLICE("l0Slice"), L1_SLICE("l1Slice"), L1_ACN("l1Acn"), LEVEL_SEGMENT("level_segment"),
	S3_METADATA("s3metadata");

	private final String path;

	private MetadataCatalogRestPath(String path) {
		this.path = path;
	}

	public String path() {
		return this.path;
	}
}
