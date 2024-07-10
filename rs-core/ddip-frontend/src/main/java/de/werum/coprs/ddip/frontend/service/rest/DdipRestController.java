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

package de.werum.coprs.ddip.frontend.service.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.werum.coprs.ddip.frontend.service.DdipService;
import de.werum.coprs.ddip.frontend.service.rest.model.PingResponse;

@CrossOrigin
@RestController
public class DdipRestController {

	public static final Logger LOGGER = LoggerFactory.getLogger(DdipRestController.class);

	public final DdipService ddipService;

	@Autowired
	public DdipRestController(final DdipService ddipService) {
		this.ddipService = ddipService;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/app/ping", produces = MediaType.APPLICATION_JSON_VALUE)
	public PingResponse ping() {
		LOGGER.debug("Received ping request");
		final String version = this.ddipService.getDdipVersion();
		return new PingResponse(null != version && !version.isEmpty() ? version : "UNKNOWN");
	}

}
