# Testing Mocks

# General
This directory contains mocks for the XBIP and EDIP and are normally not required in an operational environment. Normally the XBIP and EDIP are running against operational services in order to pull in the EDRS Sessions into the system. However for testing purposes it can be required to avoid the network transfer and have access on the server in order to perform tests.

Thus it might be required to le tthe XBIP and EDIP poll against services in the local cluster. The Mocks are simulating the servers by launching up a WebDAV server that is exposing the directory /var/lib/dav/data to the outside world. Within the folder structure below, you can place the products that shall be visible for the ingestion services and thus ingesting own products locally or doing other tests.

In order to deploy these services you can install the mocks by using the following command:

# Installation

In order to install these mocks, you require to add the COPRS artifactory as Helm repository into your cluster. You can archieve this by using the following command line:

``helm repo add rs-helm http://artifactory.coprs.esa-copernicus.eu/artifactory/api/helm/rs-helm``

Please verify that the helm was added successfully using:

``helm repo list``

## EDIP

In order to deploy the mock to simulate an inbox for an EDRS endpoint, please use the following command line:

``helm install rs-edip-mock rs-helm/rs-mock-edip``

You find the inboxes for this system when you go into the Pod under /data/NOMINAL/. Each mission does have an inbox.

For further information on how to use the EDIP endpoint, please have a look [here](../production-common/rs-core-examples/ingestion-edip)


## XBIP

In order to deploy the mock to simulate an inbox for the station "MTI_" for S1 you can use the following command line:

``helm install rs-xbip-mock-s1 rs-helm/rs-mock-webdav --version 1.0.1 --set mock.webdav_station="MTI_" --set mock.webdav_mission="S1"``

After deploying the chart, you find the inboxes for the mock under the directory /var/lib/dav/data. It will generate a nominal and a retransfer inbox for the station and mission that had been provided. e.g:

``/var/lib/dav/data/NOMINAL/MTI_/S1A.``

for a S1A EDRS session. To see how this inbox can be configured by the component, please have a look [here](../production-common/rs-core-examples/ingestion-xbip).
