S1-PDGS Cloud POC - Message Queue Interface server
==================================================

The MQI server is in charge of:
* consuming KAFKA messages (which includes managing KAFKA offsets and rebalance):
  * managing KAFKA offsets and rebalance
  * distributing messages to the application of the module with a priority strategy
  * acknowledging their processing and eventually publishing processing errors
* publishing processing outputs in KAFKA topics
* publishing errors in KAFKA topics

A MQI server has no sense if it is not associated to a processing application.

To be robust to KAFKA rebalance and untimely reboots, the messages are stored in the applicative database.

<div style="text-align:center"><img alt="Design" src="build/design_mqi_server.png" align="center"/></div>

Some use-cases are available in [this file](./build/use-cases.xlsx)

### Sources

The module has 3 main controllers:
* Message consumption (internal)
* Message distribution (REST server)
* Message publication (REST server)

<div style="text-align:center"><img alt="Architecture" src="build/architecture_mqi_server.png" align="center"/></div>

### Builds

This project is a maven, java and spring project.

##### IDE

You can use STS (Spring Tools Suite) or Eclipse.
Required java version is >= 1.8

##### Internal dependencies

This project depends on:
* [commons](https://conf.geohub.space/wo7/lib-commons) library
* [app-catalog-client](https://conf.geohub.space/wo7/obs-sdk) library

Please install these dependencies in your local repository before building project

##### External dependency
This project depends on:
* spring-boot
* spring-boot-starter-web
* spring-boot-starter-jetty
* spring-log4j2
* spring-kafka
* kafka-lag-based-assignor
	
### Configuration

##### application.yml
Below the parameters to configure for the production

Parameter                                        | Description
------------------------------------------------ | ------------- 
kafka.bootstrap-servers								| host:port to use for establishing the initial connection to the Kafka cluster
kafka.hostname										| Hostname
kafka.client-id										| ID to pass to the server when making requests. Used for server-side logging
kafka.error-topic										| Topic name for the errors
kafka.consumer.group-id				              | Unique string that identifies the consumer group to which this consumer belongs
kafka.consumer.max-poll-records				       | Maximum number of records returned in a single call to poll().
kafka.consumer.max-poll-interval-ms				|
kafka.consumer.heartbeat-intv-ms				    | Expected time between heartbeats to the consumer coordinator.
kafka.consumer.session-timeout-ms				    |
kafka.consumer.auto-offset-reset				    | What to do when there is no initial offset in Kafka or if the current offset does not exist any more on the server (e.g. because that data has been deleted): earliest: automatically reset the offset to the earliest offset, latest: automatically reset the offset to the latest offset, none: throw exception to the consumer if no previous offset is found for the consumer's group,anything else: throw exception to the consumer.
kafka.consumer.offset-dft-mode				       | Default offset seek mode when rebalance: -2: let the consumer, -1: start to the beginning offset, -0: start to the end offset
kafka.listener.poll-timeout-ms				       | Timeout to use when polling the consumer.
kafka.producer.max-retries				           | When greater than zero, enables retrying of failed sends.
application.hostname				                  | Hostname
application.max-error-counter				       | 
application.stop-fixed-delay-ms				       |
application.wait-next-ms			               	| Time to wait before getting next message when API is called
application.product-categories.auxiliary-files.consumption.enable						| True if the category is enable for the service
application.product-categories.auxiliary-files.consumption.topicswithprioritystr		| List of topics with priority
application.product-categories.auxiliary-files.publication.enable						| True if the category is enable for the service
application.product-categories.auxiliary-files.publication.routing-file				| Location of the routing file
server.port				                         |
persistence.host-uri-catalog				       |
persistence.port-uri-other-app				       |
persistence.max-retries				              |
persistence.tempo-retry-ms				          |
persistence.other-app.suffix-uri				    |
