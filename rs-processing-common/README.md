# rs-processing-common

This directory contains a set of helm charts for these services of the RS that will be used as infrastructure and running independently from the SCDF applications. They are not invoked by a request, but running rather as some kind of service for SCDF applications or providing access to the data hold by the RS.

The helm charts are supposed to be deployed into the RS cluster via Ansible or by using helm directly.

These services are:
- Metadata Search Controller
- Eviction Manager
- Native API
- RSAPI Frontend (PRIP / DDIP Frontend)
- User Web Client
- Request Repository

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
| `processing.namespace` | The namespace into that the chart shall be deployed | `processing` |
| `image.registry` | The registry from that the image shall be pulled | `artifactory.coprs.esa-copernicus.eu` |
| `image.repository` | The path within the directory from that the image shall be pulled | `werum-docker` |
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
| `ddip.dispatch.prip.host` | The hostname or IP used to connect to the PRIP | `s1pro-prip-frontend-svc.processing.svc.cluster.local` |
| `ddip.dispatch.prip.port` | The port on which the PRIP listens for requests | `8080` |
| `ddip.dispatch.collections` | value (collection name) and key (ODATA expression) pairs to define collections that can be searched via DDIP feature (filter=Collection/Name eq 'Sentinel1')  | Sentinel1: startswith(Name,'S1')</br> Sentinel3: startswith(Name,'S3') |

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

## User Web Client

The User Web Client is using the interface to the DDIP in order to provide the user a graphical interface to explore the products stored within the Reference System.

The following command can be used in order to deploy the User Web Client:
``helm install rs-helm/rs-user-web-client --version 1.0.1``

| Name                              | Description                                              | Default |
| ----------------------------------|----------------------------------------------------------|---------|
| `env.apiUrl` | The URL to the endpoint that shall be used as backend to query the products | `http://coprs.werum.de/prip/odata/v1/Products`|
| `env.mapBackground` | Allows to define as JSON the different background layers that shall be used | `"[{\"name\":\"TESTING\",\"layers\":[{\"url\":\"testing_url\",\"layerName\":\"LayerName\"}]}]"` |
| `baseHref` | Defines the HRef used within the baseHref used by the UWC | `/uwc/` |
| `keycloak` | Allows setting the information about the keycloak endpoint, realm and clientid that shall be used by the UWC | `"{\"url\":\"http://localhost:8080/auth\",\"realm\":\"master\",\"clientId\": \"user-web-client\"}` |

For further information regarding the User Web Client, please consult [https://github.com/COPRS/user-web-client].

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
