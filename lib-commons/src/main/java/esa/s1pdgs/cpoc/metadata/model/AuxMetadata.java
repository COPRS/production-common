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

    public AuxMetadata(String productName,
                       String productType,
                       String keyObjectStorage,
                       String validityStart,
                       String validityStop,
                       String missionId,
                       String satelliteId,
                       String stationCode,
                       Map<String, String> additionalProperties) {
        super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
        this.additionalProperties = additionalProperties;
    }

    public  boolean has(String additionalProperty) {
        return additionalProperties.containsKey(additionalProperty);
    }

    public String get(String additionalProperty) {
        return additionalProperties.get(additionalProperty);
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void ifPresent(String additionalProperty, Consumer<String> consumer) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AuxMetadata that = (AuxMetadata) o;
        return Objects.equals(additionalProperties, that.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), additionalProperties);
    }
}
