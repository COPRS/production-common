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

package esa.s1pdgs.cpoc.mdc.worker.es;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.junit.Ignore;
import org.junit.Test;

public class ElasticsearchDAOTest {
	
	private RestHighLevelClient restHighLevelClient;
	
	
	public ElasticsearchDAOTest() {
		
		restHighLevelClient = new RestHighLevelClient(RestClient.builder( new HttpHost("localhost", 9200, "http")));
	}
	
	
	@Test
	@Ignore
	public void testCoordinateType_1() throws IOException {
		
		// [ -1, 52.6289 ], [ -1.4946, 54.5767 ], [ -5.2745, 54.2683 ], [ -4.6105, 52.3285 ], [ -1, 52.6289 ] 
		extractCoordinates("D12yKXMBJTQ2djjArsD8");

	}
	
	@Test
	@Ignore
	public void testCoordinateType_2() throws IOException {
		
		// [ -1.0, 52.6289 ], [ -1.4946, 54.5767 ], [ -5.2745, 54.2683 ], [ -4.6105, 52.3285 ], [ -1.0, 52.6289 ]
		extractCoordinates("EV2zKXMBJTQ2djjAd8A0");

	}
	
	@Test
	@Ignore
	public void testCoordinateType_3() throws IOException {
		
		//  [ -1.001, 52.6289 ], [ -1.4946, 54.5767 ], [ -5.2745, 54.2683 ], [ -4.6105, 52.3285 ], [ -1.001, 52.6289 ]
		extractCoordinates("EF2zKXMBJTQ2djjAPsCZ");

	}
	
	@Test
	@Ignore
	public void testCoordinateType_4() throws IOException {
		
		// [ -1.0000, 52.6289 ], [ -1.4946, 54.5767 ], [ -5.2745, 54.2683 ], [ -4.6105, 52.3285 ], [ -1.0000, 52.6289 ] 
		extractCoordinates("Dl2yKXMBJTQ2djjAHMDT");

	}
	
	
	private void extractCoordinates(String id) throws IOException {
		
		GetResponse getResponse = restHighLevelClient.get(new GetRequest("l0_slice", id), RequestOptions.DEFAULT);
		
		final Map<String, Object> sliceCoordinates = (Map<String, Object>) getResponse.getSourceAsMap()
				.get("sliceCoordinates");
		
		final List<Object> firstArray = (List<Object>) sliceCoordinates.get("coordinates");
		final List<Object> secondArray = (List<Object>) firstArray.get(0);
		
		final CoordinatesBuilder coordBuilder = new CoordinatesBuilder();

		for (final Object arr : secondArray) {
			final List<Number> coords = (List<Number>) arr;
			final double lon = coords.get(0).doubleValue();
		    final double lat = coords.get(1).doubleValue();
			coordBuilder.coordinate(lon, lat);
		}
		
		System.out.println(coordBuilder.build());
		
	}
	

}
