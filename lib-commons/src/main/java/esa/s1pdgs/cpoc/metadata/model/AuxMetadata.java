package esa.s1pdgs.cpoc.metadata.model;

import java.util.Map;
import java.util.function.Consumer;

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

    public void ifPresent(String additionalProperty, Consumer<String> consumer) {
        if(has(additionalProperty)) {
            consumer.accept(get(additionalProperty));
        }
    }
}
