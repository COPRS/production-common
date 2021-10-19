package esa.s1pdgs.cpoc.metadata.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class AuxMetadataSerializer extends JsonSerializer<AuxMetadata> {
    @Override
    public void serialize(AuxMetadata auxMetadata, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("productName", auxMetadata.productName);
        jsonGenerator.writeStringField("productType", auxMetadata.productType);
        jsonGenerator.writeStringField("keyObjectStorage", auxMetadata.keyObjectStorage);
        jsonGenerator.writeStringField("validityStart", auxMetadata.validityStart);
        jsonGenerator.writeStringField("validityStop", auxMetadata.validityStop);
        jsonGenerator.writeStringField("missionId", auxMetadata.missionId);
        jsonGenerator.writeStringField("satelliteId", auxMetadata.satelliteId);
        jsonGenerator.writeStringField("stationCode", auxMetadata.stationCode);
        auxMetadata.getAdditionalProperties().forEach(
                (name, value) -> writeStringField(jsonGenerator, name, value));
        jsonGenerator.writeEndObject();
    }

    private void writeStringField(final JsonGenerator generator, final String key, final String value) {
        try {
            generator.writeStringField(key, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
