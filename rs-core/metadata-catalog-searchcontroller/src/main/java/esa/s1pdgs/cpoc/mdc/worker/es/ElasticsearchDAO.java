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

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElasticsearchDAO {

	/**
	 * Elasticsearch client
	 */
	@Autowired
	private RestHighLevelClient restHighLevelClient;
	
	
	public GetResponse get (GetRequest getRequest) throws IOException {
		return this.restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
	}
	
	public DeleteResponse delete(DeleteRequest deleteRequest) throws IOException {
		return this.restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
	}
	
	public IndexResponse index (IndexRequest request) throws IOException {
		return this.restHighLevelClient.index(request, RequestOptions.DEFAULT);
	}
	
	public SearchResponse search (SearchRequest searchRequest) throws IOException {		
		return this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
	}

	public RefreshResponse refresh(RefreshRequest refreshRequest) throws IOException {
		return this.restHighLevelClient.indices().refresh(refreshRequest, RequestOptions.DEFAULT);
	}
	
	

}
