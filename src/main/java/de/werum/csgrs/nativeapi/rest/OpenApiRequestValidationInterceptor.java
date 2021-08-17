package de.werum.csgrs.nativeapi.rest;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.operation.validator.adapters.server.servlet.ServletRequest;
import org.openapi4j.operation.validator.model.Request;
import org.openapi4j.operation.validator.validation.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import de.werum.csgrs.nativeapi.config.OpenApiRequestValidatorConfiguration;

@Component
public class OpenApiRequestValidationInterceptor implements HandlerInterceptor, WebMvcConfigurer {

	static final Logger LOG = LogManager.getLogger(OpenApiRequestValidationInterceptor.class);

	private final RequestValidator requestValidator;
	private final boolean disableValidation;
	private final Pattern pathExclusionPattern;

	@Autowired
	public OpenApiRequestValidationInterceptor(final OpenApiRequestValidatorConfiguration openapiConfig, final RequestValidator requestValidator) {
		this.requestValidator = requestValidator;
		this.disableValidation = openapiConfig.getDisableValidation();
		this.pathExclusionPattern = Pattern.compile(openapiConfig.getPathExclusionRegex());
	}

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(this);
	}

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
		final String path = request.getContextPath() + request.getServletPath();

		if (this.disableValidation) {
			LOG.trace("Skipping OpenAPI validation");
		} else if (this.pathExclusionPattern.matcher(path).matches()) {
			LOG.debug(String.format("Skipping OpenAPI validation for path: %s", path));
		} else {
			final Request servletRequest = ServletRequest.of(request);

			try {
				this.requestValidator.validate(servletRequest);
				LOG.debug(String.format("Check against OpenAPI definition successful. Valid request: %s", request));
			} catch (final ValidationException e) {
				LOG.debug(String.format("Check against OpenAPI definition failed for: %s Reason: %s", request, e.getMessage()));
				response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
				return false;
			}
		}

		return true;
	}
}
