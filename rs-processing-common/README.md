# rs-processing-common

This directory contains a set of helm charts for these services of the RS that will be used as infrastructure and running independently from the SCDF applications. They are not invoked by a request, but running rather as some kind of service for SCDF applications or providing access to the data hold by the RS.

The helm charts are supposed to be deployed into the RS cluster via Ansible or by using helm directly.

These services are:
- Metadata Search Controller
- Eviction Manager
- Native API
- RSAPI Frontend (PRIP / DDIP Frontend)
- Request Parking Lot

Additionally the User Web Client that can be used as web frontend for the COPRS can be deployed also as a standalone service. It can be found in this [repository](https://github.com/COPRS/user-web-client).

# General

Please note that for the first version of the V1.1 delivery just the Metadata Search Controller is available and should be used. All components are packaged as Helm Charts and will be pushed into the Artifactory repository. In order to retrieve them, please add the repository into your environment by using the following command:

``helm repo add rs-helm http://artifactory.coprs.esa-copernicus.eu/artifactory/api/helm/rs-helm``

Please verify that the helm was added successfully using:

``helm repo list``

The individual components can be deployed via charts and are described more in detail using the following sections:

## Global configuration

The following global parameters exist per instance and can be used when deploying the Helm Chart:

| Name                              | Description                                              | Default |
| ----------------------------------|----------------------------------------------------------|---------|
| `replicaCount` | The amount of instances that shall be spawned as replica | `1`|
| `service.port` | The port that shall be exposed by the deployed service | `8080`|
| `service.name` | The name of the service when it is deployed | e.g. `rs-core-metadata-catalog-searchcontroller` |
| `logLevel` | Defines the threadhold level of the logging | e.g. `DEBUG` or `INFO` |
| `logConfig` | Defines the config that shall be used for log4j. By default all logs will be written in json format, the debug config allows to print it out in an human readable format | by default `log/log4j2.yml` is used for json output. Use `log/log4j2_debug.yml` for a human readable format.
| `processing.namespace` | The namespace into that the chart shall be deployed | `processing` |
| `image.registry` | The registry from that the image shall be pulled | `artifactory.coprs.esa-copernicus.eu` |
| `image.repository` | The path within the directory from that the image shall be pulled | `rs-docker` |
| `image.tag` | The tag of the docker image that shall be pulled | `develop` |
| `image.imagePullSecrets` | The secret that will be use to authentificate against the registry | `artifactory` |
| `resources.cpu.request` | Specifies the amount of CPU that is requested. More information can be found [here](https://kubernetes.io/docs/tasks/configure-pod-container/assign-cpu-resource/]) | Instance specific |
| `resources.cpu.limit` | Specifies the maximum amount of CPU that is requested. More information can be found [here](https://kubernetes.io/docs/tasks/configure-pod-container/assign-cpu-resource/]) | Instance specific | Instance specific |
| `resources.ram.request` | Specifies the amount of RAM that is requested. More information can be found [here](https://kubernetes.io/docs/tasks/configure-pod-container/assign-cpu-resource/]) | Instance specific | Instance specific |
| `resources.ram.limit` | Specifies the maximum amount of RAM that is requested. More information can be found [here](https://kubernetes.io/docs/tasks/configure-pod-container/assign-cpu-resource/]) | Instance specific | Instance specific | Instance specific |
| `resources.javaOpts.xms` | Specifies the amount of memory used when starting the Java JVM | Instance specific |
| `resources.javaOpts.xmx` | Specifies the maximum memory allocation pool for Java JVM | Instance specific |

## DDIP

This component provides an DDIP interface for the User Web Client and other external systems and is operated as a frontend before the PRIP interface.

The latest version of it can be deployed using the following command line:
``helm install rs-helm/rs-ddip-frontend``

| Name                              | Description                                              | Default |
| ----------------------------------|----------------------------------------------------------|---------|
| `ddip.dispatch.prip.protocol` | The protocol used to connect to the PRIP | `http`|
| `ddip.dispatch.prip.host` | The hostname or IP used to connect to the PRIP | `rs-prip-frontend-svc.processing.svc.cluster.local` |
| `ddip.dispatch.prip.port` | The port on which the PRIP listens for requests | `8080` |
| `ddip.dispatch.collections` | value (collection name) and key (ODATA expression) pairs to define collections that can be searched via DDIP feature (filter=Collection/Name eq 'Sentinel1')  | Sentinel1: startswith(Name,'S1')</br> Sentinel3: startswith(Name,'S3') |
| `update.maxSurge` | maximum number of Pods that can be created over the desired number of Pods | `100%` |
| `update.maxUnavailable` | optional field that specifies the maximum number of Pods that can be unavailable during the update process | `50%` |

## PRIP

This component provides an PRIP interface. It is not supposed to be used directly by the end-user. Instead the DDIP is operated as frontend. The PRIP service is running in the backend to perform the queries on the elastic search query and providing an OData interface.

The latest version can be deployed by using the following command line:
``helm install rs-helm/rs-prip-frontend``

| Name                              | Description                                              | Default |
| ----------------------------------|----------------------------------------------------------|---------|
| `elasticsearch.host` | The hostname of the elastic search server that shall be used as backend and contains the catalog | `elasticsearch-master`|
| `elasticsearch.port` | The port of the elastic search server that shall be used as backend and contains the catalog | `9200` |
| `elasticsearch.connect-timeout-ms` | Timeout in milliseconds of connection to the cluster | `2000` |
| `elasticsearch.socket-timeout-ms` | Timeout in milliseconds of the socket to the cluster | `10000` |
| `prip-frontend.debug-support` | Adding additional debug information on the Odata interface. Don't use this in an operational setup | `false` |
| `update.maxSurge` | maximum number of Pods that can be created over the desired number of Pods | `100%` |
| `update.maxUnavailable` | optional field that specifies the maximum number of Pods that can be unavailable during the update process | `50%` |

## Native API

This component provides a STAC API that can be used to query products on the DDIP.

The latest version can be deployed by using the following command line:
``helm install rs-helm/rs-native-api``

| Name                              | Description                                              | Default |
| ----------------------------------|----------------------------------------------------------|---------|
| `nativeapi.prip.protocol` | The protocol that shall be used to contact the PRIP backend | `http`|
| `nativeapi.prip.host` | The host that shall be used to contact the PRIP backend| `s1pro-prip-frontend-svc.processing.svc.cluster.local` |
| `nativeapi.prip.port` | The port that shall be used to contact the PRIP backend | `8080` |
| `nativeapi.external.protocol` | The protocol used to externally connect to the PRIP/DDIP frontend | `http` |
| `nativeapi.external.host` | The externally reachable hostname or IP used to connect to the PRIP/DDIP frontend | `coprs.werum.de/prip/odata/v1/` |
| `nativeapi.external.port` | The port on which the PRIP/DDIP frontend listens externally for requests | `80` |
| `nativeapi.defaultLimit` | The amount of products that are returned as limit from a query. This will be added to the OData query by using $top | `100` |
| `nativeapi.maxLimit` | The maximal limit that is accepted. If a higher limit is requested, it will be truncated to this value. | `100` |
| `update.maxSurge` | maximum number of Pods that can be created over the desired number of Pods | `100%` |
| `update.maxUnavailable` | optional field that specifies the maximum number of Pods that can be unavailable during the update process | `50%` |
| `nativeapi.collections` | Allows to configure a set of collections that shall be exposed by the stac endpoint. It contains the information about the collections that are available. | See the section below for a detailed description of the configuration | 

### Collection configuration
The attribute `nativeapi.collection` contains a list of collections that shall be exposed by the stac native API. A typical configuration looks like this:
```
    collections:
        s1:
            title: Collection of Sentinel-1
            description: This collection holds all published Sentinel-1 products of the Copernicus Reference System
            license: proprietary
```
The collection starts with its id used as key (e.g. `s1`) and will be used within URLs. The level below this structure are giving more a more descriptive information about the collection and used within the actual stac collections returned.

`title` contains a long name of the the collection.
`description` contains a more verbose description of the collection
`license` defines the licence that shall be shown next to the collection

`nativeapi.collection` can contain multiple definitions of collections that shall be made available via the endpoint `/stac/collections`.

### Look up Table (LuT) configuration

When a query is added to the native API endpoint a set of GET parameters will be provided. In order to translate these parameters into a valid OData query a lookup table is used that defines how the parameter shall be translated. These elements can be defined under `nativeapi.lutConfigs`. The following section contains an example configuration:
```
  lutConfigs:
    "[bbox={value}]":
      - "OData.CSC.Intersects(location=Footprint,area=geography'SRID=4326;POLYGON(({value})))"
    "[productname={value}]": 
      - "contains(Name,'{value}')"
    "[collections={producttype}]":
      - "Attributes/OData.CSC.StringAttribute/any(att:att/Name eq 'productType' and att/OData.CSC.StringAttribute/Value eq ‘{producttype}"
    "[datetime={start}/{stop}]": 
      - "ContentDate/Start gt {start}"
      - "ContentDate/End lt {stop}"
```
The key of a statement will be defined e.g. as `[productname={value}]`. Please note that the whole term is defined by a key. As the equal character might cause issues with YAML parsing it is required to put the whole key into round brackets to escape it properly and avoid misunderstanding. This brackets are not evaluated by the backend itself. The left side of the equal character is the actual name of the parameter that is provided to the STAC endpoint, e.g. `search?productname=myproduct`. If a parameter that is provided maps to a statement it will be applied.

The right side of the equal character defines the generic type of the statement and is either a single value that will be substituted in the actual OData term or is a range, e.g. if there is a start and stop value. The names of the values can be defined as wanted, but it needs to be used in the OData term. The productname is an example of a single value statement that will use the content of the provided parameter and put it into the configured statement. For example the result here would be `contains(Name,'myproduct')`.

The `datetime` is an example for a ranged type. The value of the parameter contains two variables. Please note that open queries are possible by leaving out a definition or using ".." instead. So the following time query will be open ended "2010-10-18T14:33:00.000Z/" and query for all products after "2010-10-18T14:33:00.000Z". This statement is equal to "2010-10-18T14:33:00.000Z/.."

When having a ranged query it is not sufficient enough to provide a single statement as the value cannot be mapped as empty string or null to provide a valid OData query. Thus if one of these ranges are open ended the associated OData statement needs to be ignored. To allow this in a generic manner, the statements needs to be listed independently. E.g.
```
    "[publicationdate={start}/{stop}]":
      - "CreationDate gt {start}"
      - "CreationDate lt {stop}"  
```
If start and stop are provided, both statements will be concated using an and operator like:
`PublicationDate gt 2010-10-18T14:33:00.000Z and PublicationDate lt 2023-02-06T14:33:00.000Z`

If just start is provided and end is undefined this will result in:
`PublicationDate gt 2010-10-18T14:33:00.000Z`

The key contains the parameter name that is provided to the endpoint and . Please note that in order to avoid misunderstanding during the parsing the key needs to be surrounded by round brackets. These brackets are ignored and required to escape the term for YAML.

If multiple parameters are provided to the STAC endpoint and multiple statements had been applied, they will be concat at the end of the processing all together using an AND operator.

## Metadata Search Controller

This component is providing a query interface against the elastic search catalog that is feed by the Metadata Extraction SCDF application. This interface can be used by the preparation worker to query products and auxiliary files for the mission specific RS-Add-ons.

The following example illustrated the installation of the Chart setting the `elasticsearch.port` to `9200`.

``helm install rs-helm/rs-metadata-catalog-searchcontroller --set elastichsearch.port=9200 --version 1.0.1``

The following instance specific configurations are available for the Search Controller:

| Name                              | Description                                              | Default |
| ----------------------------------|----------------------------------------------------------|---------|
| `elasticsearch.host` | The hostname of the elastic search server that shall be used as backend and contains the catalog | `elasticsearch-master`|
| `elasticsearch.port` | The port of the elastic search server that shall be used as backend and contains the catalog | `9200` |
| `elasticsearch.connect-timeout-ms` | Timeout in milliseconds of connection to the cluster | `2000` |
| `elasticsearch.socket-timeout-ms` | Timeout in milliseconds of the socket to the cluster | `10000` |

## Eviction Manager

The part of DLM responsible for eviction is provided by the Eviction Manager service that is running independently. It works with a scheduler that runs periodically and checks for expired files and removes them from the Object Store, Metadata Catalog and Prip index accordingly.

The following command can be used in order to deploy the Eviction Manager:

``helm install rs-helm/rs-eviction-manager --version 1.0.1``

| Name | Description | Default |
| -|-|-|
| `metadata.host`| The hostname + Port of the Metadata Search Controller connected to the catalog (for deletion of metadata) | `rs-metadata-catalog-searchcontroller-svc:8080` |
| `metadata.restApiNbRetry` | Number of retries for calling the Metadata Search Controller in case of errors| `3` |
| `metadata.restApiTempoRetryMs` | Interval in miliseconds between calls to Metadata Search Controller in case of errors | `1000` |
| `elasticsearch.host` | The hostname of the elastic search server that shall be used as backend and contains the DLM index (for updates) | `elasticsearch-master`|
| `elasticsearch.port` | The port of the elastic search server that shall be used as backend and contains the DLM index | `9200` |
| `elasticsearch.connect-timeout-ms` | Timeout in milliseconds of connection to the cluster | `2000` |
| `elasticsearch.socket-timeout-ms` | Timeout in milliseconds of the socket to the cluster | `10000` |
| `elasticsearch.search-result-limit` | Limitation of search result when searching for evictable files in the DLM index. Only this amount of files will be handled in one iteration | `1000` |
| `eviction-management-worker.eviction-interval-ms` | Specifies the interval in milliseconds between invocations of the Eviction routine for searching and deleting evictable files from OBS/MDC/Prip | `600000` |

## Request Parking Lot

The component provides an interface to list and restart failed processings.

Prerequisites:

As a frontend to the COPRS DLQ sub system, the Request Parking Lot needs to access the failed processings, that are stored by the DLQ component in a MongoDB database. Thus it is required to have a MongoDB instance available and setup. For further general information regarding the creation of a secret for the  MongoDB instance, please see [COPRS MongoDB](/processing-common/doc/secrets.md)

The default configuration provided is expecting a secret `mongorequestparkinglot` in the namespace `processing` containing a field for PASSWORD and USERNAME that can be used in order to authenticate at the MongoDB.

In order to generate the secret, you can use the following commands:
``kubectl create secret generic mongorequestparkinglot --from-literal=USERNAME=<MONGO_USER> --from-literal=PASSWORD=<MONGO_PASSWORD>``

Please note that further initialization might be required. For the Request Parking Lot component please execute the following commands in the MongoDB in order to create the credentials for the secret:
``
db.createUser({user: "<USER>", pwd: "<PASSWORD>", roles: [{role: "readWrite", db: "coprs"}]})
``

Clients accessing the frontend web service, have to provide an API KEY in the request headers. This API KEY shall be stored in a field `apikey` of a secret `apikey` on the cluster.

In order to generate the secret, you can use the following commands:
`kubectl create secret generic apikey --from-literal=apikey=<API_KEY>`


The following command can be used in order to deploy the Request Parking Lot:

`helm install rs-helm/rs-request-parking-lot --version 1.0.1`

| Name | Description | Default |
| -|-|-|
| `mongodb.host`| The hostname of the MongoDB server that shall be used as backend and contains the failedProcessings collection | `mongodb-0.mongodb-headless.database.svc.cluster.local` |
| `mongodb.port` | The port of the MongoDB server that shall be used as backend and contains the failedProcessings collection | `27017` |
| `mongodb.database` | The database that contains the failedProcessings collection | `coprs` |
