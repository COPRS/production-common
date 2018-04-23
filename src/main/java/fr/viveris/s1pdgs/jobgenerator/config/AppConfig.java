package fr.viveris.s1pdgs.jobgenerator.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;

/**
 * General application configuration
 * @author Cyrielle Gailliard
 *
 */
@Configuration
public class AppConfig {

	/**
	 * XML converter
	 * @return
	 */
	@Bean
	XmlConverter xmlConverter() {
		XmlConverter xmlConverter = new XmlConverter();
		xmlConverter.setMarshaller(jaxb2Marshaller());
		xmlConverter.setUnmarshaller(jaxb2Marshaller());
		
		return xmlConverter;
	}

	/**
	 * JAXb2 marshaller
	 * @return
	 */
	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setPackagesToScan("fr.viveris.s1pdgs.jobgenerator.model");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("jaxb.formatted.output", true);
		map.put("jaxb.encoding", "UTF-8");
		jaxb2Marshaller.setMarshallerProperties(map);
		return jaxb2Marshaller;
	}
}