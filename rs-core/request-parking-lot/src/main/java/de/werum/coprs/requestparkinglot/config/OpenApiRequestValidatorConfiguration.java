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

package de.werum.coprs.requestparkinglot.config;

import java.io.File;

import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.operation.validator.validation.RequestValidator;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiRequestValidatorConfiguration {

	@Value("${openapi.spec-file}")
    private String openapiSpecFile;
	
	@Bean
	public RequestValidator getRequestValidator() throws ResolutionException, ValidationException {
		final OpenApi3 openApi = new OpenApi3Parser().parse(new File(openapiSpecFile), false);
		final RequestValidator requestValidator = new RequestValidator(openApi);
		return requestValidator;
	}
}
