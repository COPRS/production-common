package de.werum.coprs.nativeapi.rest.model.stac;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * https://github.com/radiantearth/stac-spec/blob/master/collection-spec/collection-spec.md#temporal-extent-object
 */
@JsonTypeName("TemporalExtent")
@JsonPropertyOrder({"interval"})
public class StacTemporalExtent implements Serializable {

	private static final long serialVersionUID = 5908483672446855450L;
	
	private List<String> interval = new ArrayList<>();

	public List<String> getInterval() {
		return interval;
	}

	public void setInterval(List<String> interval) {
		this.interval = interval;
	}
}
