package esa.s1pdgs.cpoc.mqi.server.converter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * Configuration for conversion
 * @author Viveris Technologies
 *
 */
@Configuration
public class ConverterConfiguration {

	/**
	 * XML converter
	 * @return
	 */
	@Bean
	public XmlConverter xmlConverter() {
		return new XmlConverter(jaxb2Marshaller());
	}

	/**
	 * JAXb2 marshaller
	 * @return
	 */
	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setPackagesToScan("fr.viveris.s1pdgs.mqi.server.publication.routing");
		Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("jaxb.formatted.output", true);
		map.put("jaxb.encoding", "UTF-8");
		jaxb2Marshaller.setMarshallerProperties(map);
		return jaxb2Marshaller;
	}
}