package de.werum.coprs.nativeapi.service.mapping;

import java.io.FileReader;
import java.net.URI;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.werum.coprs.nativeapi.rest.model.stac.StacItemCollection;

public class TestPripToStacMapper {

	@Test
	public void test() throws Exception {
		FileReader reader = new FileReader(Paths.get("src/test/resources/testdata.json").toFile());
		
		final JsonReader jsonReader = Json.createReader(reader);
	    final JsonObject jsonObject = jsonReader.readObject();
	    System.out.println(jsonObject);

	    StacItemCollection result = PripToStacMapper.mapFromPripOdataJson(jsonObject, new URI("localhost"), true);
	    ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, result);
	}
}
