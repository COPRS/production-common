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

package de.werum.coprs.requestparkinglot.rest;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.operation.validator.adapters.server.servlet.ServletRequest;
import org.openapi4j.operation.validator.model.Request;
import org.openapi4j.operation.validator.model.impl.RequestParameters;
import org.openapi4j.operation.validator.validation.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class OpenApiRequestValidationInterceptor implements HandlerInterceptor, WebMvcConfigurer {

	static final Logger LOGGER = LogManager.getLogger(OpenApiRequestValidationInterceptor.class);

	private final RequestValidator requestValidator;
	private final boolean disableValidation;
    private final Pattern pathExclusionPattern;
	
	@Autowired
	public OpenApiRequestValidationInterceptor(
		final RequestValidator requestValidator,
		@Value("${openapi.disable-validation:false}") final boolean disableValidation,
		@Value("${openapi.path-exclusion-regex:}") final String pathExclusionRegex)
	{
		this.requestValidator = requestValidator;
		this.disableValidation = disableValidation;
		pathExclusionPattern = Pattern.compile(pathExclusionRegex);
	}
	
	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(this);
	}
	
	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {		
		final String path = request.getContextPath() + request.getServletPath();
		if (disableValidation) {
			LOGGER.trace("Skipping OpenAPI specification validation");
		} else if (pathExclusionPattern.matcher(path).matches()) {
			LOGGER.debug(String.format("Skipping OpenAPI specification validation for path: %s", path));
		} else {
			final Request servletRequest = ServletRequest.of(request);
			try {
				@SuppressWarnings("unused")
				final RequestParameters requestParameters = requestValidator.validate(servletRequest);
				LOGGER.debug(String.format("Check against OpenAPI specification successful. Valid request: %s", request));
			} catch (final ValidationException e) {
				LOGGER.debug(String.format("Check against OpenAPI specification failed. Invalid request: %s", request));
		        response.sendError(HttpStatus.BAD_REQUEST.value());
				return false;
			}
		}
		return true;
	}
}