package standalone.prip.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("esa.s1pdgs.cpoc.prip")
public class StandaloneApplication {
    public static void main(String[] args) {

    	/*
    	 * PRIP Standalone Application
    	 * 
    	 * How to get it running:
    	 * 
    	 * 1. Download and unpack https://www.elastic.co/de/downloads/past-releases/elasticsearch-6-1-3
    	 * 
    	 * 2. Execute bin/elasticsearch
    	 * 
    	 * 3. Create an index for PRIP metadata:
    	 *    $ curl -XPUT "http://localhost:9200/prip" -H 'Content-Type: application/json' -d '{"mappings":{"metadata":{"properties":{"id":{"type":"text"},"obsKey":{"type":"text"},"name":{"type":"text"},"productFamily":{"type":"text"},"contentType":{"type":"text"},"contentLength":{"type":"long"},"creationDate":{"type":"date"},"evictionDate":{"type":"date"},"checksum":{"type":"nested","properties":{"algorithm":{"type":"text"},"value":{"type":"text"}}}}}}}'
    	 *    
    	 * 4. Create one ore more records
    	 *    $ curl -XPOST "http://localhost:9200/prip" -H 'Content-Type: application/json' -d '{"id":"00000000-0000-0000-0000-000000000001","obsKey":"S1B_GP_RAW__0____20181001T101010_20181001T151653_012957________0001.SAFE.zip","name":"S1B_GP_RAW__0____20181001T101010_20181001T151653_012957________0001.SAFE.zip","productFamily":"L0_SEGMENT_ZIP","contentType":"application/zip","contentLength":"3600003","creationDate":"2020-04-04T16:47:32.944000Z","evictionDate":"2020-04-11T16:47:32.944000Z","checksum":[{"algorithm":"MD5","value":"4d21b35de4619315e8ba36dfa596eb44"}]}'
    	 *    
    	 * 5. Retrieve them over the PRIP Frontend
    	 *    $ curl http://localhost:8080/odata/v1/Products
    	 */
    	
        SpringApplication.run(StandaloneApplication.class, args);
    }
}
