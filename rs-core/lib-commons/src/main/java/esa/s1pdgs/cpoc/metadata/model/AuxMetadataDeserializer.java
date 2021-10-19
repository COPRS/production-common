package esa.s1pdgs.cpoc.metadata.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class AuxMetadataDeserializer extends JsonDeserializer<AuxMetadata> {
    @Override
    public AuxMetadata deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        Map<String, String> values = new HashMap<>();
        String name;
        while ((name = jsonParser.nextFieldName()) != null) {
            values.put(name, jsonParser.nextTextValue());
        }

        final List<String> nonAdditionalProperties = Arrays.asList(
                "productName",
                "productType",
                "keyObjectStorage",
                "validityStart",
                "validityStop",
                "missionId",
                "satelliteId",
                "stationCode"
        );

        return new AuxMetadata(
                values.getOrDefault("productName", "UNDEFINED"),
                values.getOrDefault("productType", "UNDEFINED"),
                values.getOrDefault("keyObjectStorage", "UNDEFINED"),
                values.getOrDefault("validityStart", "UNDEFINED"),
                values.getOrDefault("validityStop", "UNDEFINED"),
                values.getOrDefault("missionId", "UNDEFINED"),
                values.getOrDefault("satelliteId", "UNDEFINED"),
                values.getOrDefault("stationCode", "UNDEFINED"),
                values.entrySet().stream()
                        .filter(e -> !nonAdditionalProperties.contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }


}
