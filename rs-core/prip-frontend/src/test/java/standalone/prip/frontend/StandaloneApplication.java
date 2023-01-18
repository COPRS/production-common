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
    	 * 1. Download and unpack https://www.elastic.co/de/downloads/past-releases/elasticsearch-7-7-0
    	 * 		--> better use open source version: https://www.elastic.co/de/downloads/past-releases/elasticsearch-oss-7-7-0
    	 * 
    	 * 2. Execute bin/elasticsearch
    	 * 
    	 * 3. Create an index for PRIP metadata:
    	 *    $ curl -XPUT "http://localhost:9200/prip" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"id":{"type":"keyword"},"obsKey":{"type":"keyword"},"name":{"type":"keyword"},"productFamily":{"type":"keyword"},"contentType":{"type":"keyword"},"contentLength":{"type":"long"},"contentDateStart":{"type":"date"},"contentDateEnd":{"type":"date"},"creationDate":{"type":"date"},"evictionDate":{"type":"date"},"checksum":{"type":"nested","properties":{"algorithm":{"type":"keyword"},"value":{"type":"keyword"},"checksum_date":{"type":"date"}}},"footprint":{"type":"geo_shape"},"browseKeys":{"type":"keyword"},"online":{"type":"boolean"}}}},"settings":{"analysis":{"analyzer":{"default":{"type":"keyword"}}}}}'
    	 *    
    	 * 4. Create one ...
    	 *    $ curl -XPOST "http://localhost:9200/prip/_doc" -H 'Content-Type: application/json' -d '{"id":"00000000-0000-0000-0000-000000000001","productFamily":"L1_SLICE_ZIP","attr_relativeOrbitNumber_long":57,"attr_processingDate_date":"2022-11-08T18:58:50.000Z","attr_orbitDirection_string":"ASCENDING","productionType":null,"evictionDate":"2022-11-15T18:58:54.308Z","attr_beginningDateTime_date":"2020-01-20T12:48:36.000Z","attr_platformShortName_string":"dummy-text","attr_productType_string":"IW_SLC__1S","contentDateStart":"2020-01-20T12:48:36.000Z","checksum":[{"checksum_date":"2022-11-08T18:58:54.000Z","value":"d3ae5a6534ad83aeb69279b6bd089ebf","algorithm":"MD5"}],"attr_completionTimeFromAscendingNode_double":7654321,"attr_endingDateTime_date":"2020-01-20T12:49:08.400Z","contentType":"application/octet-stream","attr_processingCenter_string":"Lueneburg","attr_orbitNumber_long":385,"attr_productClass_string":"S","attr_cycleNumber_long":1,"contentDateEnd":"2020-01-20T12:49:08.400Z","attr_instrumentConfigurationID_string":"-1","creationDate":"2022-11-08T18:58:54.308Z","browseKeys":["S1A_IW_SLC__1SDV_20200120T124836_20200120T124908_030884_038B5E_BA07.SAFE_bwi.png"],"footprint":{"coordinates":[[[-38.832,-11.3875],[-29.191,-11.3875],[-29.191,-14.5215],[-38.832,-14.6084],[-38.832,-11.3875]]],"type":"polygon"},"attr_processorVersion_string":"1.0","attr_coordinates_string":"-11.3875,-29.191 -14.5215,-29.191 -14.6084,-38.832 -11.3875,-38.832 -11.3875,-29.191","attr_missionDatatakeID_long":232286,"attr_processorName_string":"ACQ-WERUM-Processor","attr_polarisationChannels_string":"","name":"S1A_IW_SLC__1SDV_20200120T124836_20200120T124908_030884_038B5E_BA07.SAFE.zip","contentLength":841324,"attr_platformSerialIdentifier_string":"dummy-text","obsKey":"S1A_IW_SLC__1SDV_20200120T124836_20200120T124908_030884_038B5E_BA07.SAFE.zip","attr_startTimeFromAscendingNode_double":1234567}'
    	 *    
    	 *    ... or more records
    	 *    	 (adjust path to bulk data file (see src/test/resources/prip-testdata.json)
    	 *    $ curl -XPOST "http://localhost:9200/_bulk" -H 'Content-Type: application/json' --data-binary @prip-testdata.json
    	 *    
    	 * 5. Retrieve them over the PRIP Frontend
    	 *    $ curl http://localhost:8080/odata/v1/Products
    	 */
    	
        SpringApplication.run(StandaloneApplication.class, args);
    }
}
