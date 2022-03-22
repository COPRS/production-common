# Testing Addon

This directory contains mocks for the XBIP and EDIP and are normally not required in an operational environment. Normally the XBIP and EDIP are running against operational services in order to pull in the EDRS Sessions into the system. However for testing purposes it can be required to avoid the network transfer and have access on the server in order to perform tests.

Thus it might be required to le tthe XBIP and EDIP poll against services in the local cluster. The Mocks are simulating the servers by launching up a WebDAV server that is exposing the directory /var/lib/dav/data to the outside world. Within the folder structure below, you can place the products that shall be visible for the ingestion services and thus ingesting own products locally or doing other tests.

In order to deploy these services you can install the mocks by using the following command:
```
rs_deploy install testing
```
All mocks will be deployed into the namespace processing then.

Keep in mind that the XBIP and EDIP instances will be configured to poll the operational systems still by default and it will be required to let the system know that you want to run them against the mocks. To make the setup easier a switch does exist in the wrapper environmental configuration file "environment-specific.sh" (https://github.com/COPRS/production-common/blob/develop/rs-deploy/templates/wrapper/environment-specific.sh)

The following options are used as default:
```
export WERUM_USE_MOCK_WEBDAV=${WERUM_USE_MOCK_WEBDAV:-true}
export WERUM_USE_MOCK_DISSEMINATION=${WERUM_USE_MOCK_DISSEMINATION:-true}
```
switch these to "true" in order to switch the configuration of the XBIP and EDIP to use the local deployed mocks by this addon. Keep in mind that the configuration will not be automatically used, but requiring a redeploy of the common components using a
```
rs_deploy uninstall common; rs_deploy install common
```
in order to be applied.