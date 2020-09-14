package esa.s1pdgs.cpoc.metadata.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class AuxMetadataTest {

    @Test
    public void testSerialize() throws IOException {

        Map<String, String> additional = new HashMap<>();
        additional.put("one", "two");
        additional.put("three", "four");

        AuxMetadata metadata = new AuxMetadata(
                "productName",
                "type",
                "key",
                "start",
                "stop",
                "mission",
                "s1a",
                "code",
                additional);

        StringWriter string = new StringWriter();
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter("  ").createInstance();
        ObjectWriter writer = new ObjectMapper().writer(printer);
        writer.writeValue(string, metadata);
        System.out.println(string.toString());

        ObjectReader reader = new ObjectMapper().reader().forType(AuxMetadata.class);
        AuxMetadata parsed = reader.readValue(string.toString());

        writer.writeValue(System.out, parsed);

        assertThat(parsed, is(equalTo(metadata)));
    }

}