# RS Core - Distribution

COPRS Distribution chain is responsible for publishing products on the PRIP index.

# Overview

![overview](./media/overview.png "Overview")

The Distribution chain is publishing products that had been produced within the COPRS on the PRIP index so the products become available for the end user via the User Web Client or the Native API. In order to publish a product via the PRIP it is required to be zipped first. So usually the products will be processed by the Compression Chain first. So the Distribution Chain will listen on the Compression Events for new products.

It is a sink and no further processing occurs. 

For details, please see [Metadata Chain Design](https://github.com/COPRS/reference-system-documentation/blob/pro_V1.1/components/production%20common/Architecture%20Design%20Document/004%20-%20Software%20Component%20Design.md#metadata-extraction)

# Resource Requirements

This software does have the following minimal requirements:

| Resource                    |  Distribution Worker* | 
|-----------------------------|---------------|
| Memory request              |     326Mi    |
| CPU request                 |     100m      |
| Memory limit                |     1302Mi    |
| CPU limit                   |     500m     |
| Disk volume needed          |   no  |
| Disk access                 |     n/a       |
| Disk storage                |  n/a         |
| Volume Mount                |  n/a  |         
| Affinity between Pod / Node |     no       |

 *These resource requirements are applicable for one worker. There may be many instances of an extraction worker, see [scaling up workers](/processing-common/doc/scaling.md) for more details.


# Deployment Prerequisite
Following components of the COPRS shall be installed and running
- [COPRS Infrastructure](https://github.com/COPRS/infrastructure)


# Configuration
## Application properties
| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.*.spring.kafka.bootstrap-servers``| It is a pair of host and port where kafka brokers are running. A Kafka client connects to these servers to bootstrap the application. Comma separated values are provided for multiple enteries.Example : ``kafka-headless:9092``|
|``app.*.main.banner-mode``| Disable Spring Boot Banner Using banner-mode at System Console.Default : ``off``|
|``app.*.management.endpoint.health.show-details``| Spring Boot provides a health stats for the application. Default : ``always``|
|``app.*.logging.config``| Path to the file that describes logging configuration for the application.Default : ``log/log4j2.yml``

### Elasticsearch (ES)

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.*.elasticsearch.host``|Elasticsearch host name running in the Kubernetes cluster. Default: ``elasticsearch-master.monitoring``| 
|``app.*.elasticsearch.port``| Elasticsearch port name running in the Kubernetes cluster.Default: ``9200``| 
|``app.*.elasticsearch.connect-timeout-ms``| Timeout for a period in which this client should establish a connection Elasticsearch Service.Example: ``2000``| 
|``app.*.elasticsearch.socket-timeout-ms``| A maximum time of inactivity between two data packets when exchanging data with a ES server.Example: ``10000``| 


###  Distribution Worker
| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.distribution-worker.distribution-worker.hostname``| The hostname of the distribution worker Default: ``$[HOSTNAME]}``|
|``app.distribution-worker.distribution-worker.metadata-unavailable-retries-number``| Amount of retries how often the worker will attempt to fetch the metadata from the catalog. Default:
 ``10``|
|``app.distribution-worker.distribution-worker.metadata-unavailable-retries-interval-ms``| Amount of ms the worker will wait before retrying to fetch the metadata from the catalog again. Default: ``5000``|
|``app.distribution-worker.distribution-worker.metadata-insertion-retries-number``| Amount of retries how often the worker will attempt to add a new entry into the PRIP index Default: ``3``|
|``app.distribution-worker.distribution-worker.metadata-insertion-retries-interval-ms``| Amount of ms the worker will wait before retrying to add an entry in the PRIP index. Default: ``1000``|

TBD Describe the attribute mapping

## Deployer properties

The following table only contains a few properties used by the factory default configuration. For more information please refer to the [official documentation](https://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#configuration-kubernetes-deployer) or COPRS-ICD-ADST-001139201 - ICD RS core.
  
| Property | Details |
|-|-|
| `deployer.<application-name>.kubernetes.namespace` | Namespace to use | 
| `deployer.<application-name>.kubernetes.livenessProbeDelay` | Delay in seconds when the Kubernetes liveness check of the app container should start checking its health status. | 
| `deployer.<application-name>.kubernetes.livenessProbePeriod` | Period in seconds for performing the Kubernetes liveness check of the app container. | 
| `deployer.<application-name>.kubernetes.livenessProbeTimeout` | Timeout in seconds for the Kubernetes liveness check of the app container. If the health check takes longer than this value to return it is assumed as 'unavailable'. | 
| `deployer.<application-name>.kubernetes.livenessProbePath` | Path that app container has to respond to for liveness check. | 
| `deployer.<application-name>.kubernetes.livenessProbePort` | Port that app container has to respond on for liveness check. | 
| `deployer.<application-name>.kubernetes.readinessProbeDelay` | Delay in seconds when the readiness check of the app container should start checking if the module is fully up and running. | 
| `deployer.<application-name>.kubernetes.readinessProbePeriod` | Period in seconds to perform the readiness check of the app container. | 
| `deployer.<application-name>.kubernetes.readinessProbeTimeout` | Timeout in seconds that the app container has to respond to its health status during the readiness check. | 
| `deployer.<application-name>.kubernetes.readinessProbePath` | Path that app container has to respond to for readiness check. | 
| `deployer.<application-name>.kubernetes.readinessProbePort` | Port that app container has to respond on for readiness check. | 
| `deployer.<application-name>.kubernetes.limits.memory` | The memory limit, maximum needed value to allocate a pod, Default unit is mebibytes, 'M' and 'G" suffixes supported | 
| `deployer.<application-name>.kubernetes.limits.cpu` | The CPU limit, maximum needed value to allocate a pod | 
| `deployer.<application-name>.kubernetes.requests.memory` | The memory request, guaranteed needed value to allocate a pod. | 
| `deployer.<application-name>.kubernetes.requests.cpu` | The CPU request, guaranteed needed value to allocate a pod. | 
| `deployer.<application-name>.kubernetes.maxTerminatedErrorRestarts` | Maximum allowed restarts for app that fails due to an error or excessive resource use. | 

