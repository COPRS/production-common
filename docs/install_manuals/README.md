:arrow_heading_up: Go back to the [Production Common Documentation Overview](../README.md) :arrow_heading_up:

# COPRS-PRO-IM RS Production Common Installation Manual

# Deployment

## RS Core Components

RS Core components are available in Artifactory as zipped contains containing the workflow specification and the factory default configuration.

```
ansible-playbook deploy-rs-addon.yaml \
    -i inventory/mycluster/hosts.ini \
    -e rs_addon_location=https://artifactory.coprs.esa-copernicus.eu/artifactory/rs-docker/<RS_ADDON> \
    -e stream_name=<STREAM_NAME>
```

For a full list of available RS Core Components and versions, please consult [Artifactory](https://artifactory.coprs.esa-copernicus.eu/ui/native/rs-docker/)

To study the source code and the factory default configuration of the RS Core components, please have a look [here](../../processing-common/README.md).


## Standalone Services

The COPRS standalone services are supposed to be installed via helm. In order to do this it is required to add the COPRS Artifactory as a helm repository into your cluster. You can archieve this by using the following command line:

``helm repo add rs-helm http://artifactory.coprs.esa-copernicus.eu/artifactory/api/helm/rs-helm``

Please verify that the helm was added successfully using:

``helm repo list``

After the repository had been added successfully, you can deploy an instance from it by using the following command line:

``helm install rs-helm/<component> --version <version>``

Further information about the charts being available and what configuration parameters can be provided to helm to customize the deployment, please consult [this page](../../rs-processing-common/README.md).