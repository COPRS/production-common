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

### Global configuration

The following global parameters exist per instance and can be used when deploying the Helm Chart:

| Name                              | Description                                              | Default |
| ----------------------------------|----------------------------------------------------------|---------|
| `replicaCount` | The amount of instances that shall be spawned as replica | `1`|
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

## Metadata Search Controller

This component is providing a query interface against the elastic search catalog that is feed by the Metadata Extraction SCDF application. This interface can be used by the preparation worker to query products and auxiliary files for the mission specific RS-Add-ons.

The following example illustrated the installation of the Chart setting the `elasticsearch.port` to `9200`.

``helm install rs-metadata-catalog-searchcontroller --set elastichsearch.port=9200``

The following instance specific configurations are available for the Search Controller:

| Name                              | Description                                              | Default |
| ----------------------------------|----------------------------------------------------------|---------|
| `elasticsearch.host` | The hostname of the elastic search server that shall be used as backend and contains the catalog | `elasticsearch-master`|
| `elasticsearch.port` | The port of the elastic search server that shall be used as backend and contains the catalog | `9200` |

