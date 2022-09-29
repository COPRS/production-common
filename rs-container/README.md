 # RS Container

# General
This folder contains a set of docker file that are used as base container for different system of the COPRS platform. When commiting on this folder and its Dockerfile GitHub Actions are executed in order to build the docker images and pushing them into the project Artifactory instance.

## Docker ACQ Simulator
Contains a simulator for the S3 Acquisition chain that are processing EDRS Sessions into L0P products.

## Docker Base
Contains a base image that is used across all SCDF images used for the RS Core and RS Add-on components. This image is based on Ubuntu image. For further information, please consult [https://github.com/COPRS/reference-system-software/tree/main/contribute]

## Docker Mock FTP
Contains a VSFTP instance that can be used to mock an EDIP endpoint to avoid running it against the operational system. Please consult [/processing-common/rs-testing/mock_ftp] for the helm chart to deploy this mock.

## Docker Mock FTP
Contains a WebDav instance that can be used to mock an XBIP endpoint to avoid running it against the operational system. Please consult [/processing-common/rs-testing/mock_webdav] for the helm chart to deploy this mock.

# Docker S1 Container
Contains the Base image for S1 IPF images.

# Docker S1 IPF AIO
Contains the IPF AIO (All-in-One Processor) for S1 L0 chain.

# Docker S1 IPF ASP
Contains the IPF ASP (Segment Processor) for S1 L0 chain.

# Docker S1 IPF Simulator
Contains a simulator for S1 that can be used to simulate processing instead of the AIO and ASP processor. This can be used especially when not enough hardware resources are available.

# Docker S3 Acquisition
Contains the wrapper for the S3 Acquisition Chain that are procesing EDRS Sessions into L0P products. This image contains the real IPFs for DDC, L0Pre and L0Post processor. The wrapper is executing all IPFs sequentially and provides a ESA Generic Interface compatible with the COPRS software.

# Docker S3 L0 IPF
Contains the IPF that is taking the products produced by the Acquisition chains and processinf them to S3 granules.

# Docker S3 IPF Simulator
Contains a simulator for S3 L0 IPF that can be used to simulate processing. This can be used especially when not enough hardware resources are available.
