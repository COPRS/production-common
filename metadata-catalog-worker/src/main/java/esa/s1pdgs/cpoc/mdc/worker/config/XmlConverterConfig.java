package esa.s1pdgs.cpoc.mdc.worker.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;

@Configuration
public class XmlConverterConfig {
	/**
	 * XML converter
	 * @return
	 */
	@Bean
	public XmlConverter xmlConverter() {
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
		jaxb2Marshaller.setPackagesToScan("esa.s1pdgs.cpoc.mdc.worker.extraction.model");
		Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("jaxb.formatted.output", true);
		map.put("jaxb.encoding", "UTF-8");
		jaxb2Marshaller.setMarshallerProperties(map);
		return jaxb2Marshaller;
	}
}
