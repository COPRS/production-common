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
