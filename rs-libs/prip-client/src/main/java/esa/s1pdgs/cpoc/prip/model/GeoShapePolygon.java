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

package esa.s1pdgs.cpoc.prip.model;

import java.util.List;

import org.elasticsearch.common.geo.GeoShapeType;

/**
 * Java representation of the elasticsearch geo_shape polygon type.
 */
public class GeoShapePolygon extends PripGeoShape {
	
	public GeoShapePolygon(List<PripGeoCoordinate> coordinates) {
		super(GeoShapeType.POLYGON, coordinates);
	}
	
	public GeoShapePolygon(List<PripGeoCoordinate> coordinates, String orientation) {
		super(GeoShapeType.POLYGON, coordinates, orientation);
	}

}
