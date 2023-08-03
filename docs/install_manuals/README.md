### Copernicus Reference System
# COPRS-PRO-IM RS Production Common Installation Manual
### Reference System version V2.0
---
:arrow_heading_up: Go back to the [Production Common Documentation Overview](../README.md) :arrow_heading_up:

# Deployment

Please note that some components needs to be deployed before others to ensure that the system will be working as expected. The reason for this is that some databases and kafka topics needs to be initialized initially. Thus it is recommended to stick to the recommended order. Additionally it is recommended to deploy the RS Core Components before the standalone are deployed. Just if these services are running fine, you shall start with the deploying the actual RS Add-ons.

## RS Core Components

You should start with deploying the RS Core Component Metadata because it will generate the kafka topics "catalog-jobs" and "catalog-events" that are central topics of the COPRS system and used as message bus across the system. When deploying other components first, it might be that they try to subscribe to these subjects before they are created. So the following deployment order is recommended:

* Metadata
* DLQ
* Compression
* Distribution
* Datalifecycle
* Ingestion

Note that DLQ and Datalifecycle are just required when using an operational scenario in order to handle error scenarios and purging data automatically. If just processing is wanted Metadata and ingestion will be sufficient to operate a RS add-on.

RS Core components are available in Artifactory as zipped contains containing the workflow specification and the factory default configuration.

```
ansible-playbook deploy-rs-addon.yaml \
    -i inventory/mycluster/hosts.ini \
    -e rs_addon_location=https://artifactory.coprs.esa-copernicus.eu/artifactory/rs-zip/<RS_ADDON> \
    -e stream_name=<STREAM_NAME>
```

For a full list of available RS Core Components and versions, please consult [Artifactory](https://artifactory.coprs.esa-copernicus.eu/ui/native/rs-zip/)

To study the source code and the factory default configuration of the RS Core components, please have a look [here](../../processing-common/README.md).


## Standalone Services


The COPRS standalone services are supposed to be installed via helm. In order to do this it is required to add the COPRS Artifactory as a helm repository into your cluster. You can archieve this by using the following command line:

``helm repo add rs-helm http://artifactory.coprs.esa-copernicus.eu/artifactory/api/helm/rs-helm``

Please verify that the helm was added successfully using:

``helm repo list``

After the repository had been added successfully, you can deploy an instance from it by using the following command line:

``helm install rs-helm/<component> --version <version>``

The "Metadata Search Controller" is an important component that will be used by RS Add-ons and other components in order to query the internal catalog and will be required for most scenarios. The DDIP, PRIP, native API will be required when outgoing interfaces shall be exposed. It is recommend to deploy them all together as they are having a dependency on each other. The Eviction Manager will be just required in environments that are using the Data Life Cycle component. Also the Request Repository will just be needed if the DLQ is used.

There is no specific order recommend to deploy these components. Keep in mind however that the infrastructure and RS Core Components needed to deployed first to ensure all standalone components will work as expected.

Further information about the charts being available and what configuration parameters can be provided to helm to customize the deployment, please consult [this page](../../rs-processing-common/README.md).
