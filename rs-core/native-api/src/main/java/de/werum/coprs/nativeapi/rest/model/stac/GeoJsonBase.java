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
import org.geojson.Crs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(property = "type", use = Id.NAME)
@JsonPropertyOrder({ "type", "bbox", "crs" })
@JsonSubTypes({ /* @Type(StacItem.class), */ @Type(StacItemCollection.class) })
public abstract class GeoJsonBase implements Serializable {

	private static final long serialVersionUID = -2152365274928331743L;

	public enum GeoJsonType {
		FeatureCollection,
		Feature,
		Point,
		LineString,
		MultiPoint,
		Polygon,
		MultiLineString,
		MultiPolygon,
		GeometryCollection
	}

	private Crs crs;

	private double[] bbox;

	public Crs getCrs() {
		return this.crs;
	}

	public void setCrs(Crs crs) {
		this.crs = crs;
	}

	public double[] getBbox() {
		return this.bbox;
	}

	public void setBbox(double[] bbox) {
		this.bbox = bbox;
	}

}
