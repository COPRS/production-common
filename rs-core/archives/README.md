S1-PDGS Cloud POC - Archives
============================

Once L0 and L1 processes are finished, products have to be made available from outside the processing chain, such as L0 /,L1 slices and reports. As the input of the system is FTP, the outputs are too.
This module goal is to distribute the outputs of the processes of the chain.

The archive component is composed by:
* An output FTP server: proposes a FTP interface for external systems to download L0/L1 products and reports
* An archive: download from OBS to FTP directory the L0/L1 product or reports when they are available (put in message queue system)

<div style="text-align:center"><img alt="tut" src="build/design_archives.png" align="center"/></div>


### Sources

The module has to type of Kafka consumer:
* Slices consumer
* Reports consumer

##### Slices Consumer
This consumer is plugged to t-pdgs-l0-slices and t-pdgs-l1-slices.
When it receives a message, it will download the slice from the right bucket of the object storage directly on the shared volume, so it will be available through FTP as soon as possible.

##### Reports Consumer
This consumer is plugged to t-pdgs-aio-production-report-events and t-pdgs-l1-production-report-events.
The message in this queue contains the payload (i.e. the report itself), so when the message is read, it is written on the shared volume without accessing the object storage.

### Builds

This project is a maven, java and spring project.

##### IDE

You can use STS (Spring Tools Suite) or Eclipse.
Required java version is >= 1.8

##### Internal dependencies

This project depends on:
* [commons](https://conf.geohub.space/wo7/lib-commons) library
* [obs-client](https://conf.geohub.space/wo7/obs-sdk) library

Please install these dependencies in your local repository before building project

##### External dependency
This project depends on:
* spring-boot
* spring-log4j2
* spring-kafka
* kafka-log4j-appender
	
### Configuration

##### obs-aws-s3.properties
See [obs-client](https://conf.geohub.space/wo7/obs-sdk) project

##### application.yml
Below the parameters to configure for the production

Parameter                                        | Description
------------------------------------------------ | ------------- 
kafka.bootstrap-servers                          | the bootstrap servers for KAFKA
kafka.group-id                                   | the group identifier to use for KAFKA consumers
kafka.poll-timeout                               | the bootstrap servers for KAFKA (example: kafka-svc:9092)
kafka.max-pool-records                           | the maximal number of messages to get per poll
kafka.session-timeout-ms                         | the timeout to acknowledge a message
kafka.topics.slices                              | the list of topics of level products (separated by comma)
kafka.topics.reports                             | the list of topics of level reports (separated by comma)
kafka.enable-consumer.slice                      | true if the archive shall consume level products, false else
kafka.enable-consumer.report                     | true if the archive shall consume level reports, false else
status.delete-fixed-delay-ms                     | (fixed delay) period in milliseconds between 2 check if application shall be stopped or not
status.max-error-counter-slices                  | the number of consecutive processing errors for slices leading to the state FATALERROR
status.max-error-counter-reports                 | the number of consecutive processing errors for reports leading to the state FATALERROR
file.slices.local-directory                      | the directory of the FTP server for level products (MUST CONTAIN THE LAST '/' CHARACTER)
file.reports.poll-fixed-delay                    | the directory of the FTP server for level reports (MUST CONTAIN THE LAST '/' CHARACTER)
dev.activations.download-all     					| false if the archive shall download only manifest.safe file for level products, if true dowload all files of the product