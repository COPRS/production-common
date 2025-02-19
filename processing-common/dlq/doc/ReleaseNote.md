# RS Core - DLQ

The RS Core component Dead Letter Queue is responsible for automated restarting of failed processings.

## Overview

![overview](media/overview.png "Overview of the DLQ")

The DLQ Manager polls the configured dead letter queue topic for failed processing messages. The messages are then routed according to a set of routing rules. A rule consists of the following attributes.

**ErrorTitle**: Title of the error type.

**ErrorID**: Regex to identify an error.

**ActionType**:
- Restart: Republish message in error message while MaxRetry is not reached, else move it to the Parking Lot
- Drop: Ignore the message (the error will be dropped)
- NoAction: Disable rule without deleting the error message

**TargetTopic**: The target topic for restart. If not set, the original topic is used (optional).

**MaxRetry**: Maximum retry for error.

**Comment**: Description and further notes about an error (optional).

**Priority**: For the case when several rules match the same errorID, the rule with the highest priority is applied (min 0 ; max 2147483647).

Example rule table configuration:
```Bash
app.dlq-manager.dlq-manager.routing.es.errorTitle=Elasticsearch issues
app.dlq-manager.dlq-manager.routing.es.errorID=.*Elasticsearch.*
app.dlq-manager.dlq-manager.routing.es.actionType=Restart
app.dlq-manager.dlq-manager.routing.es.targetTopic=topic-example
app.dlq-manager.dlq-manager.routing.es.maxRetry=1
app.dlq-manager.dlq-manager.routing.es.priority=100
app.dlq-manager.dlq-manager.routing.es.comment=Explaination of Elasticsearch issues
app.dlq-manager.dlq-manager.routing.timeout.errorTitle=Any Timeout
app.dlq-manager.dlq-manager.routing.timeout.errorID=.*(?i:timeout).*
app.dlq-manager.dlq-manager.routing.timeout.actionType=Restart
#app.dlq-manager.dlq-manager.routing.timeout.targetTopic=
app.dlq-manager.dlq-manager.routing.timeout.maxRetry=1
app.dlq-manager.dlq-manager.routing.timeout.priority=50
#app.dlq-manager.dlq-manager.routing.timeout.comment=
```

## Requirements

This software does have the following minimal requirements:

| Resource                    | DLQ Manager |
|-----------------------------|-------------|
| Memory request              |    512Mi    |
| CPU request                 |    500m     |
| Memory limit                |    4000Mi   |
| CPU limit                   |    1500m    |
| Disk volume needed          |    no       |
| Disk access                 |    n/a      |
| Disk storage capacity       |    n/a      |
| Volume Mount                |    n/a      |
| Affinity between Pod / Node |    no       |

## Additional resources
In the scope of the COPRS it is necessary to be able to adjust the configuration of the commonly used kafka topics. As the SCDF server would create the kafka topics itself, when they aren't already present, it is necessary, that the kafka topics ``error-warning`` and ``parking-lot`` are already created, before the SCDF streams are started.

In case the default COPRS Infrastructure is used, this will be handled by the Strimzi Operator. On deployment of this RS core chain, the deployment script will firstly create the two KafkaTopic objects into the Kubernetes cluster, which will create the topics with the preferred configuration. The configuration can be found in the folder ``additional_resources`` in the files ``error-warning.yaml`` and ``parking-lot.yaml``.

## Deployment Prerequisite

Following components of the COPRS shall be installed and running
- [COPRS Infrastructure](https://github.com/COPRS/infrastructure)
Kubernetes Secrets shall be created.
- See [COPRS Kubernetes Secret](/processing-common/doc/secret.md)

Additionally the DLQ system needs a persistence in order to store failed processings arriving at the parking lot Kafka topic. Thus it is required to have a MongoDB instance available and setup. For further general information regarding the creation of a secret for the  MongoDB instance, please see [COPRS MongoDB](/processing-common/doc/secret.md)

The default configuration provided in the RS Core Component is expecting a secret "mongodlq" in the namespace "processing" containing a field for PASSWORD and USERNAME that can be used in order to authenticate at the MongoDB.

Please note that further initialization might be required. For the DLQ component please execute the following commands in the MongoDB in order to create the credentials for the secret:
``
db.createUser({user: "<USER>", pwd: "<PASSWORD>", roles: [{role: "readWrite", db: "coprs"}]})
``

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
| `deployer.<application-name>.kubernetes.environmentVariables` | Can be used to pass additional environmental variables into the application.<br> This can be used for example to set JVM specific arguments to use 512m. The example given shows how the XMX argument can be set: JAVA_TOOL_OPTIONS=-Xmx512m <br> For further information, please consult [this](https://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#_environment_variables) page. |
