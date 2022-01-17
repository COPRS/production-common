package de.werum.coprs.nativeapi.rest.model.stac;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * https://github.com/radiantearth/stac-api-spec/blob/master/stac-spec/item-spec/item-spec.md#link-object
 */
@JsonTypeName("Link")
@JsonPropertyOrder({ "rel", "href", "type", "title" })
public class StacLink implements Serializable {

	private static final long serialVersionUID = 1596516246914267573L;

	private String rel;
	private String href;
	private String type;
	private String title;

	public String getHref() {
		return this.href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getRel() {
		return this.rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


}
