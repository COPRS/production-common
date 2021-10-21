package de.werum.coprs.nativeapi.config;

import java.io.File;

import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.operation.validator.validation.RequestValidator;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("openapi")
public class OpenApiRequestValidatorConfiguration {

	private String definitionFile;
	private String pathExclusionRegex;
	private Boolean disableValidation;
	
	@Bean
	public RequestValidator getRequestValidator() throws ResolutionException, ValidationException {
		final OpenApi3 openApi = new OpenApi3Parser().parse(new File(this.definitionFile), true);
		return new RequestValidator(openApi);
	}

	public Boolean getDisableValidation() {
		return this.disableValidation;
	}

	public void setDisableValidation(Boolean disableValidation) {
		this.disableValidation = disableValidation;
	}

	public String getPathExclusionRegex() {
		return this.pathExclusionRegex;
	}

	public void setPathExclusionRegex(String pathExclusionRegex) {
		this.pathExclusionRegex = pathExclusionRegex;
	}

	public String getDefinitionFile() {
		return this.definitionFile;
	}

	public void setDefinitionFile(String definitionFile) {
		this.definitionFile = definitionFile;
	}
}
