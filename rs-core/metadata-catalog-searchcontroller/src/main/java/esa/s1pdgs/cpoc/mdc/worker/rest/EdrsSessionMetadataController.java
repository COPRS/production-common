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

package esa.s1pdgs.cpoc.mdc.worker.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;

@RestController
@RequestMapping(path = "/edrsSession")
public class EdrsSessionMetadataController extends AbstractMetadataController<EdrsSessionMetadata> {

	@Autowired
	public EdrsSessionMetadataController(final EsServices esServices) {
		super(EdrsSessionMetadata.class, esServices);
	}

//	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{productType}/{productName:.+}")
//	public ResponseEntity<EdrsSessionMetadata> get(
//			@PathVariable(name = "productType") String productType,
//			@PathVariable(name = "productName") String productName
//	) {
//		return getResponse(productName, ProductFamily.EDRS_SESSION, () -> esServices.getEdrsSession(productType, productName));
//	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/sessionId/{sessionId}")
	public ResponseEntity<List<EdrsSessionMetadata>> get(@PathVariable(name = "sessionId") final String sessionId) {
		return getResponse(
				"files for session " + sessionId, 
				ProductFamily.EDRS_SESSION, 
				() -> esServices.getEdrsSessionsFor(sessionId)
		);
	}
			
}
