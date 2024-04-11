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

package esa.s1pdgs.cpoc.metadata.model;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = AuxMetadataSerializer.class)
@JsonDeserialize(using = AuxMetadataDeserializer.class)
public class AuxMetadata extends AbstractMetadata {
    private final Map<String, String> additionalProperties;

    public AuxMetadata(final String productName,
                       final String productType,
                       final String keyObjectStorage,
                       final String validityStart,
                       final String validityStop,
                       final String missionId,
                       final String satelliteId,
                       final String stationCode,
                       final Map<String, String> additionalProperties) {
        super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
        this.additionalProperties = additionalProperties;
    }

    public  boolean has(final String additionalProperty) {
        return additionalProperties.containsKey(additionalProperty);
    }

    public String get(final String additionalProperty) {
        return additionalProperties.get(additionalProperty);
    }

    @Override
	public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void ifPresent(final String additionalProperty, final Consumer<String> consumer) {
        if(has(additionalProperty)) {
            consumer.accept(get(additionalProperty));
        }
    }

    @Override
    public String toString() {
        return "AuxMetadata [" +
                "additionalProperties=" + additionalProperties +
                ", productName='" + productName + '\'' +
                ", productType='" + productType + '\'' +
                ", keyObjectStorage='" + keyObjectStorage + '\'' +
                ", validityStart='" + validityStart + '\'' +
                ", validityStop='" + validityStop + '\'' +
                ", missionId='" + missionId + '\'' +
                ", satelliteId='" + satelliteId + '\'' +
                ", stationCode='" + stationCode + '\'' +
                ']';
    }

	@Override
	public int hashCode() {
		return Objects.hash(
				additionalProperties,
				keyObjectStorage,
				missionId,
				productName,
				productType,
				satelliteId,
				stationCode,
				swathtype,
				validityStart,
				validityStop);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AuxMetadata other = (AuxMetadata) obj;
		return Objects.equals(additionalProperties, other.additionalProperties)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) 
				&& Objects.equals(productName, other.productName)
				&& Objects.equals(productType, other.productType) 
				&& Objects.equals(satelliteId, other.satelliteId)
				&& Objects.equals(stationCode, other.stationCode) 
				&& Objects.equals(swathtype, other.swathtype)
				&& Objects.equals(validityStart, other.validityStart)
				&& Objects.equals(validityStop, other.validityStop);
	}    
}
