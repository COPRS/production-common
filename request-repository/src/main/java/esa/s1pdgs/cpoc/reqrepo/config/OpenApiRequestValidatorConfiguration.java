package esa.s1pdgs.cpoc.reqrepo.config;

import java.io.File;

import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.operation.validator.validation.RequestValidator;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiRequestValidatorConfiguration {

	public final static String OPENAPI_SPEC_FILE_PATHNAME = "src/main/resources/s1pro-api_openapi.yaml";
	
	@Bean
	public RequestValidator getRequestValidator() throws ResolutionException, ValidationException {
		final OpenApi3 openApi = new OpenApi3Parser().parse(new File(OPENAPI_SPEC_FILE_PATHNAME), false);
		final RequestValidator requestValidator = new RequestValidator(openApi);
		return requestValidator;
	}
}
