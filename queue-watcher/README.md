S1-PDGS Cloud POC - Queue-Watcher
====================================

It is a server which extracts metadata of available products and offers a set of REST API for querying these metadata.

The metadata catalog server is composed of the hereafter sub components:
* Metadata extraction
* A REST API used to query the Elasticsearch cluster
* A REST API used to manage its status


<div style="text-align:center"><img alt="tut" src="build/design_metadata_catalog.jpg" align="center"/></div>


### Sources

The metadata catalog is a Spring Boot application configured with annotations.


### Builds

This project is a maven, java and spring project.

##### IDE

You can use STS (Spring Tools Suite) or Eclipse.
Required java version is >= 1.8

##### Internal dependencies

This project depends on:
* [commons](https://conf.geohub.space/wo7/lib-commons) library
* [obs-client](https://conf.geohub.space/wo7/obs-sdk) library
* [mqi-client](https://conf.geohub.space/wo7/mqi-client) library

Please install these dependencies in your local repository before building project

##### External dependency
This project depends on:
* spring-boot
* spring-boot-starter-web
* spring-boot-starter-jetty
* spring-log4j2
* elasticsearch and elasticsearch-rest-high-level-client
	
### Configuration

##### obs-aws-s3.properties
See [obs-client](https://conf.geohub.space/wo7/obs-sdk) project

##### application.yml
Below the parameters to configure for the production

Parameter                                        | Description
------------------------------------------------ | ------------- 
elasticsearch.host                               | Host of Elasticsearch cluster
elasticsearch.port                               | Port of Elasticsearch cluster
elasticsearch.index-type                         | Elasticsearch index type used for metadata
elasticsearch.connect-timeout-ms                 | Timeout in milliseconds of connection to the cluster
elasticsearch.socket-timeout-ms                  | Timeout in milliseconds of the socket to the cluster
elasticsearch.max-retry-timeout-ms               | Timeout in milliseconds of connection to the cluster
file.file-with-manifest-ext                      | Extension of the SAFE files
file.manifest-filename                           | Name of the manifest (with extension)
file.mqi.host-uri                                | the host and port for querying MQI server
file.mqi.max-retries                             | the maximal number of consecutive retries following a MQI request error 
file.mqi.tempo-retry-ms                          | 
file.product-categories.auxiliary-files.local-directory			| Local directory of file downloading (Must contains the last /)
file.product-categories.auxiliary-files.fixed-delay-ms			| (fixed delay) Period in milliseconds between 2 polls of next message
file.product-categories.auxiliary-files.init-delay-poll-ms		| Initial delay in milliseconds before starting consuming message
server.port                                      | port used for publishing REST API around status
mdextractor.type-overlap                         | !!!! Overlap per acquisition
mdextractor.type-slice-length                    | !!!!! Slice length per acquisition
mdextractor.xslt-directory                       | Directory where the XSLT are locatede (Must contains the last /)
status.delete-fixed-delay-ms                     | (fixed delay) period in milliseconds between 2 check if application shall be stopped or not
status.max-error-counter-processing              | the number of consecutive processing errors leading to the state FATALERROR
status.max-error-counter-mqi                     | the number of consecutive MQI errors leading to the state FATALERROR

