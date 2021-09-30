package de.werum.csgrs.nativeapi.rest.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.geojson.GeoJsonObject;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProductMetadata")
public class PripMetadataResponse {

	@JsonProperty("Id")
	@Schema(name = "Id", example = "c02e4932-1631-4ca1-a497-6a7b4775b2a9", description = "A universally unique identifier (UUID) as technical identifier for the product instance within PRIP, assigned by the production system")
	private String id;

	@JsonProperty("Name")
	@Schema(name = "Name", example = "S1A_IW_GRDH_1SSH_20190504T020120_20190504T020145_027071_030CC0_AE1B.SAFE", description = "The name of the product file")
	private String name;

	@JsonProperty("ContentType")
	@Schema(name = "ContentType", example = "application/zip", description = "The Mime type of the product")
	private String contentType;

	@JsonProperty("ContentLength")
	@Schema(name = "ContentLength", example = "4737286945", description = "The actual size of the product file in bytes (B)")
	private long contentLength;

	@JsonProperty("PublicationDate")
	@Schema(name = "PublicationDate", example = "2021-09-09T14:46:03.788Z", description = "The date and time (UTC) of the product file at which it becomes visible to the user", pattern = "YYYY-MM-DDThh:mm:ss.sssZ")
	private String publicationDate;

	@JsonProperty("EvictionDate")
	@Schema(name = "EvictionDate", example = "2021-09-16T14:46:03.788Z", description = "The date and time (UTC) from which the product file is foreseen for removal from the storage", pattern = "YYYY-MM-DDThh:mm:ss.sssZ")
	private String evictionDate;

	@JsonProperty("ProductionType")
	@Schema(name = "ProductionType", example = "systematic_production", description = "Describes how the product file was produced")
	private String productionType;

	@JsonProperty("Checksum")
	@Schema(name = "Checksum", type = "array", description = "Represents the known checksums for the product file, providing a value for supporting download integrity checks")
	private List<Checksum> checksum;

	@JsonProperty("ContentDate")
	@Schema(name = "ContentDate", type = "object", description = "The sensing range period with start and end times in UTC (format YYYY-MM-DDThh:mm:ss.sssZ)")
	private ContentDate contentDate;

	@JsonProperty("Footprint")
	@Schema(name = "Footprint", type = "object", description = "Geographic footprint of the product")
	private GeoJsonObject footprint;

	@JsonProperty("Attributes")
	@Schema(name = "Attributes", type = "object", description = "Additional attributes")
	private Map<String, Object> attributes;

	@JsonProperty("Links")
	@Schema(name = "Links", type = "object", description = "Links representing possible actions on the data")
	private Map<String, String> links;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getContentLength() {
		return this.contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public String getPublicationDate() {
		return this.publicationDate;
	}

	public void setPublicationDate(String publicationDate) {
		this.publicationDate = publicationDate;
	}

	public String getEvictionDate() {
		return this.evictionDate;
	}

	public void setEvictionDate(String evictionDate) {
		this.evictionDate = evictionDate;
	}

	public String getProductionType() {
		return this.productionType;
	}

	public void setProductionType(String productionType) {
		this.productionType = productionType;
	}

	public List<Checksum> getChecksum() {
		return this.checksum;
	}

	public void setChecksum(List<Checksum> checksum) {
		this.checksum = checksum;
	}

	public ContentDate getContentDate() {
		return this.contentDate;
	}

	public void setContentDate(ContentDate contentDate) {
		this.contentDate = contentDate;
	}

	public GeoJsonObject getFootprint() {
		return this.footprint;
	}

	public void setFootprint(GeoJsonObject footprint) {
		this.footprint = footprint;
	}

	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public Map<String, String> getLinks() {
		return this.links;
	}

	public void setLinks(Map<String, String> links) {
		this.links = links;
	}

}
