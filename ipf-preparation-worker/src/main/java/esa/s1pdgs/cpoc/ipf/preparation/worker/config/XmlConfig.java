package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import esa.s1pdgs.cpoc.ipf.preparation.worker.service.XmlConverter;

@Configuration
public class XmlConfig {
	/**
	 * XML converter
	 * @return
	 */
	@Bean
	public XmlConverter xmlConverter() {
		final XmlConverter xmlConverter = new XmlConverter();
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
		final Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setPackagesToScan("esa.s1pdgs.cpoc.ipf.preparation.worker.model");
		final Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("jaxb.formatted.output", true);
		map.put("jaxb.encoding", "UTF-8");
		jaxb2Marshaller.setMarshallerProperties(map);
		return jaxb2Marshaller;
	}
}
