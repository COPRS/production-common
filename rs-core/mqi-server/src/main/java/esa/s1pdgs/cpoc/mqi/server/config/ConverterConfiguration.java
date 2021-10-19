package esa.s1pdgs.cpoc.mqi.server.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import esa.s1pdgs.cpoc.mqi.server.converter.XmlConverter;

/**
 * Configuration for conversion
 * @author Viveris Technologies
 *
 */
@Configuration
public class ConverterConfiguration {

	/**
	 * XML converter
	 */
	@Bean
	public XmlConverter xmlConverter() {
		return new XmlConverter(jaxb2Marshaller());
	}

	/**
	 * JAXb2 marshaller
	 */
	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setPackagesToScan("esa.s1pdgs.cpoc.mqi.server.publication.routing");
		Map<String, Object> map = new ConcurrentHashMap<>();
		map.put("jaxb.formatted.output", true);
		map.put("jaxb.encoding", "UTF-8");
		jaxb2Marshaller.setMarshallerProperties(map);
		return jaxb2Marshaller;
	}
}