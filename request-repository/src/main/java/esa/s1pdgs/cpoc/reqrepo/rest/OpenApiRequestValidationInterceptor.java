package esa.s1pdgs.cpoc.reqrepo.rest;

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
	
	@Autowired
	public OpenApiRequestValidationInterceptor(
		final RequestValidator requestValidator,
		@Value("${openapi.disable-validation:false}") final boolean disableValidation)
	{
		this.requestValidator = requestValidator;
		this.disableValidation = disableValidation;
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this);
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (disableValidation) {
			LOGGER.debug("Skipping OpenAPI specification validation");
		} else {			
			Request servletRequest = ServletRequest.of(request);
			try {
				@SuppressWarnings("unused")
				RequestParameters requestParameters = requestValidator.validate(servletRequest);
				LOGGER.debug(String.format("Check against OpenAPI specification successful. Valid request: %s", request));
			} catch (ValidationException e) {
				LOGGER.debug(String.format("Check against OpenAPI specification failed. Invalid request: %s", request));
		        response.sendError(HttpStatus.BAD_REQUEST.value());
				return false;
			}
		}
		return true;
	}
}