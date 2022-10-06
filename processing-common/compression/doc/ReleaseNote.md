# RS Core - Compression

COPRS Compression chain is responsible to perform a compression operation on a file or directory product.

## General

![overview](./media/overview.png "Overview")

In order to be published on the PRIP, it is required that the product that had been produced is zipped before. This is especially required as handling directory products is more difficult during downloads. The RS Core Compression chain takes action to convert an uncompressed product into a compressed one.

When a product is produced within the COPRS and added to the catalog a new catalog event will be raised. The compression chain will hook upon these events and invoke its workflow. A filter component will ensure that just product types will be compressed and published that should be published. E.g. to avoid that auxiliary or intermediate products will be published as well.

If an event passes the filter, the product referenced in the event will be downloaded into a local working directory and the configured compression command will be executed on the product. This results in a compressed product that will be uploaded into the Object Storage. For this the compression worker will use the same bucket as it would be used by the uncompressed one, followed by the suffix "-zip". After the compression was finished successfully a compression event is raised that will be consumed by the Distribution Chain in order to publish the zipped product into the PRIP index. Please note that compressed products will not be published on the Metadata Catalog as their uncompressed counterparts will be used in the processing chains.

The compression chain does handle three different priorities:

* High
* Medium
* Low

These can be used to honour the different requirements on timeliness. Each priority will have a filter that can be configured to determine the priority of the incoming event and decide which priority will be responsible for performing the processing. It is possible to scale the different compression worker priorities individually as it might be required to spawn more workers for the high priorities than for the lower ones.

For details, please see [Compression Chain Design](https://github.com/COPRS/reference-system-documentation/blob/develop/components/production%20common/Architecture%20Design%20Document/004%20-%20Software%20Component%20Design.md#compression-chain)

## Requirements

This software does have the following minimal requirements:

| Resource                    | Compression Worker* |
|-----------------------------|-------------|
| CPU                         | 300m        |
| Memory                      | 3500Mi      |
| Disk volume needed          | yes         |
| Disk access                 | ReadWriteOnce |
| Disk storage capacity       | 120Gi **    |
| Affinity between Pod / Node | N/A         |
|                             |             |

*These resource requirements are applicable for one worker. There may be many instances of an compression worker, see scaling up workers for more details.
** This amount had been used in previous operational S1 environment. The disk size might be lower depending on the products that are processed. This needs to be at least twice of the product size of the biggest product. An additional margin of 10% is recommended however.

## Compression Filter

The compression chain is using two different types of filters:

* A filter used as a gate to decide what products shall be processed (``message-filter``)
* Multiple filters that decides upon the priority of the event (``priority-filter-<high|medium|low>``)

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.message-filter.filter.function.expression``| A [SpEL](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html) expression that will be performed on the event to decide if the event is applicable for a compression. E.g. for Sentinel-1 the filter configuration using productFamily and keyObjectStorage name of the product could be like: ``((payload.productFamily matches '^((S\\d.*)|(AUX.*)|(L\\d.*))(?<!(ZIP|AUX|JOB|GRANULES|REPORT|ETAD|SAD|BLANK)$)$') && (!(payload.productFamily == AUXILIARY_FILE) || !(payload.keyObjectStorage matches 'S1__OPER_MSK_EW_SLC_.*\\.EOF')) && (!(payload.productFamily == L0_SEGMENT) || ((payload.keyObjectStorage matches 'S1._(GP|HK|RF).*_RAW.*\.SAFE)') && !(product.keyObjectStorage matches 'S1._RF_RAW__0.(HH|HV|VV|VH)_.*\.SAFE'))))``| 
|``app.priority-filter-high.filter.function.expression``| A [SpEL](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html) expression defining what request are supposed to be handled by the high priority chain. E.g. handling all S1 events with FAST24 timeliness: ``payload.timeliness == 'FAST24'``| 
|``app.priority-filter-medium.filter.function.expression``| A [SpEL](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html) expression defining what request are supposed to be handled by the medium priority chain. E.g. handling all S1 events with NRT timeliness. ``payload.timeliness == 'NRT'``| 
|``app.priority-filter-low.filter.function.expression``|  [SpEL](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html) expression defining what request are supposed to be handled by the low priority chain. E.g. handling all events that are not having a timeliness: ``payload.timeliness == null``| 

## Compression Worker

For each priority chain a separate configuration needs to be created. The configuration is however identically and applicable for:

* app.compression-worker-high
* app.compression-worker-medium
* app.compression-worker-low

The following description is just given for high priority workers:

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.compression-worker-high.compression-worker.compression-command.<mission>``| Defines the command that shall be used to perform the compression action depending on the ``mission``. The supported misson are ``s1``, ``s2`` or ``s3``. The command that shall be executed is a script contained in the base image and can be either ``/app/zip-compression.sh`` (for 7za zip compression with level 1 (deflate)), ``/app/zip-nocompression.sh`` (for 7za zip compression level 0 (no compression)) or ``/app/tar-compression`` (for generating a tarball). By default S1 is using zip compression, S2 tarballs and S3 zip without a compression.| 
|``app.compression-worker-high.compression-worker.workingDirectory`` | The local directory of the worker that shall be used as temporary working directory to perform the compression activity. This is set by default to ``/tmp/compression`` |
|``app.compression-worker-high.compression-worker.compressionTimeout`` | The timeout in seconds when the compression process will be terminated. If it takes more time than the configured value, it will be considered to be hanging. |
|``app.compression-worker-high.compression-worker.requestTimeout`` | The timeout in seconds when the compression process will be terminated. If it takes more time than the configured value, it will be considered to be hanging. |
|``app.compression-worker-high.compression-worker.hostname`` | The timeout of the overall request. If the request takes more seconds than configured, it is considered to be hanging. |


## Additional resources
In the scope of the COPRS it is necessary to be able to adjust the configuration of the commonly used kafka topics. As the SCDF server would create the kafka topics itself, when they aren't already present, it is necessary, that the kafka topic ``compression-event`` is already created, before the SCDF streams are started.

In case the default COPRS Infrastructure is used, this will be handled by the Strimzi Operator. On deployment of this RS core chain, the deployment script will firstly create the KafkaTopic object into the Kubernetes cluster, which will create the topics with the preferred configuration. The configuration can be found in the folder ``additional_resources`` in the files ``compression-event.yaml``.


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

Please note that it will be required to setup certain deployer properties like imagePullSecrets or hardware requirement for the different workers individually. The configuration items are the same as described above however.
