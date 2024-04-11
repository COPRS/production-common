/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.werum.coprs.nativeapi.rest.model.stac;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * https://github.com/radiantearth/stac-spec/blob/master/collection-spec/collection-spec.md#extent-object
 */
@JsonTypeName("Extent")
@JsonPropertyOrder({"spatial", "temporal"})
public class StacExtent implements Serializable {
	
	private static final long serialVersionUID = -1435134305439608842L;

	private StacSpatialExtent spatial;
	private StacTemporalExtent temporal;

	public StacSpatialExtent getSpatial() {
		return spatial;
	}

	public void setSpatial(StacSpatialExtent spatial) {
		this.spatial = spatial;
	}

	public StacTemporalExtent getTemporal() {
		return temporal;
	}

	public void setTemporal(StacTemporalExtent temporal) {
		this.temporal = temporal;
	}
}
