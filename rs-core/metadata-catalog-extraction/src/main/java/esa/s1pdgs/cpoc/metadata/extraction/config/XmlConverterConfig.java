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

package esa.s1pdgs.cpoc.metadata.extraction.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;

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
                jaxb2Marshaller.setPackagesToScan("esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model");
                Map<String, Object> map = new ConcurrentHashMap<String, Object>();
                map.put("jaxb.formatted.output", true);
                map.put("jaxb.encoding", "UTF-8");
                jaxb2Marshaller.setMarshallerProperties(map);
                return jaxb2Marshaller;
        }
}
