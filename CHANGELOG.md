# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.8.0-rc1] - 2022-11-23

### Added

- RS-461: Add browse images to PRIP index in RS Core DISTRIBUTION
- RS-513: Expose S1 & S3 pending processing as gauge metric
- RS-591: Extract browse images from S1 products
- RS-593: Update RS Core DATALIFECYCLE to handle browse images
- RS-643: Implement selection policy ValIntersectWithoutDuplicates

### Changed

- RS-586: Improve the rates of sonarqube reports
- RS-639: "null" is not the default value for timeliness
- RS-662: S1 and S3 ACQ Preparation Worker seems Stuck / Very slow to read new message
- RS-686: XBIP filter cron parameters doesn't works
- RS-690: native-api queries can not reach the rs-api
- RS-706: S1 L0ASP execution worker traces do not contain segment(s) on field task.input.filename_strings
- RS-721: Correct the S3_L0p base image in Dockerfile

### Removed

- N/A

## [1.7.0-rc1] - 2022-10-16

### Added

- RS-508: Process EW L0 only inside AOI given by L0EWSliceMaskCheck (Note that the SLC products are currently not produced due to RS-661)
- RS-509: Process L1 products only inside AOI given by SeaCoverageCheck
- RS-570: Enable compression depending on the kind of input (Note: Existing configuration needs to be adjusted. Previously just a single script could be configured and become mission specific. Please check factory default configuration for an example configuration)

### Changed

- RS-418: Remove support for swift
- RS-433: Realign CFI version with F1 for S3_L0ACQ RS add-on
- RS-526: Extend housekeep to support cron based triggers
- RS-571: Fixing typo in Bearer Token using OUTH2_ACCESS_TOKEN instead of OAUTH2_ACCESS_TOKEN. Please note that you need to upgrade your configurations!
- RS-579: Remove DEM files from base image
- RS-623: [BUG] [OPS] Sentinel-1 AIO preparation job stuck in GENERATING state with mandatory files not found (Note: This is just added for documentation purposes and was fixed in 1.5.0 already)
- RS-635: [BUG] Sentinel-2 footprints are not added to PRIP index correctly
- RS-650: [BUG] RS core Compression does not compress any product. (Note: This is just added for documentation purposes and was fixed in 1.5.0 already)

### Removed

- N/A

## [1.6.0-rc1] - 2022-09-29

### Added

- RS-157: Build CFI PUG S3 container
- RS-239: Implementation of a solution for "load balance the lag" mechanism
- RS-512: Script "S3ACQWrapperScript.sh" failed
- RS-518: Implementation of RequestParkingLot API from S1PRO-RequestRepository
- RS-520: Check on overwriting existing product in OBS
- RS-528: Create S3_SR1 processor as RS add-on
- RS-530: Create S3_MW1 processor as RS add-on
- RS-532: Create S3_SL1 processor as RS add-on
- RS-533: Create S3_SL2 processor as RS add-on
- RS-560: Inventory_Metadata.xml must not be mandatory for Metadata Extraction

### Changed

- RS-337: [S1] [L0] L0ASP execution worker fails.
- RS-517: Too many loops due to symbolic links on S3 ACQ execution worker
- RS-519: RS PRO: Make EDIP trigger robust
- RS-527: Create compression topic
- RS-552: Remove affinity from pro-common Helm chart (mocks only)

### Removed

- N/A

## [1.5.0-rc1] - 2022-08-31

### Added

- RS-498: Adding a house keep service handling timeout scenarios

### Changed

- RS-426: Create catalog topics via strimzi
- RS-427: Create error-warning topic via strimzi
- RS-448: Make wrapper used by S3 ACQ simulator and real IPF using the same code base
- RS-467: Split Sonarqube reports after production-common CI improvements
- RS-497: Update documentation to be easy to use
- RS-493: Removed JSON Licence library from project and replace it with other libs
- RS-496: Renamed action DELETE by drop in DLQ
- RS-501: Changed location where RS Core Components and images are pushed to
- RS-510: Renamed t0PdgsDate in messages
- RS-511: Moved t0PdgsDate from base message to additionalFields

### Removed

- N/A

## [1.4.2-rc1] - 2022-10-17

### Added

- N/A

### Changed

- RS-623 / RS-624: [BUG] [OPS] Sentinel-1 AIO preparation job stuck in GENERATING state with mandatory files not found. Backport from 1.5.0.
  WARNING: When using this version ensure that the configuration 'app.preparation-worker.process.hostname' for the RS Add-on is not set to ${HOSTNAME}, but a static unique name.
- RS-635: Sentinel-2 footprints are not added to PRIP index correctly

### Removed

- N/A

## [1.4.1-rc1] - 2022-09-22

### Added

- N/A

### Changed

- RS-XX: Backport documentation from develop to branch 1.4
- RS-560: Inventory_Metadata.xml must not be mandatory for Metadata Extraction

### Removed

- N/A

## [1.4.0-rc1] - 2022-08-03

### Added

- RS-231: [SCDF] Implementation of a solution for "dead letter queue mechanism"
- RS-280: [BUG] PRIP does not return expected products when geographical request crosses antimeridian
- RS-344: [SCDF] Implementation long running processing feature
- RS-408: [PI] Update header traces with rs_chain_name_string & rs_chain_version_string
- RS-444: [PI] Add output to MetadataExtraction END OK trace
- RS-445: [PI] Update JobProcessing END traces with ICD Traces tailored



### Changed

- RS-429 / RS-436: Implement workaround to ignore S3 intersection error
- RS-449: Move from “filename_strings” to “filename_string” when single element
- RS-465: [BUG] bandIndexId 8A does not return result in the metadata catalog searchcontroller

### Removed
- RS-435: Move UWC helm chart CI to UWC repository

## [1.3.0-rc1] - 2022-07-06

### Added

- RS-248: Update JobProcessing traces
- RS-313: Develop "DLQ" as RS core component
- RS-314: Develop DLQ Manager application
- RS-315: Move ParkingLot from RequestRepository to RS Core DLQ restart
- RS-386: Migration of Execution Worker to SCDF Application
- RS-407: Update MetadataExtraction traces
- RS-422: Request the AUX GIP_VIEDIR on rs-metadata-catalog-searchcontroller-svc by band

### Changed

- RS-402: Missing documentation for mongodb
- RS-425: S2 HKTM and AUX_SAD metadata extraction fails with bad regex
- RS-409: Remove affinity from pro-common Helm chart

### Removed

- N/A


## [1.2.0-rc1] - 2022-06-08

### Added

- RS-387: Migration of Preparation Worker to SCDF Application
- RS-207: Develop "DATALIFECYCLE" as a RS-core component

### Changed

- RS-388: Request Chunks and DSIB through Search Metadata Controller
- RS-373: RS-Addon: ingestion and metadata add-ons have multiple wrong deployment variables
- RS-391: Update RS Core to be in line with ICD RS Core release V5.0
- RS-372: Documents, code and ICD dont form a coherent whole for RS Core Catalog
- RS-392: Update RS Core to be in line with ICD Message Format ICD V3.0

## Removed
- N/A

## [1.1.2-rc1] - 2022-05-30

### Added
- N/A

### Changed
- RS-397: Fix: compression-worker: one of two s2 auxiliary directory format is not taken into account
- RS-398: Fix: Nullpointer exception when query S2_AUX via SearchMetadata Controller

### Removed
- N/A

## [1.1.1-rc1] - 2022-05-20

### Added
- N/A

### Changed
- RS-396: Fix: Metadata extraction for EDRS session fails with NumberFormatException

### Removed
- N/A

## [1.1.0-rc1] - 2022-05-13

### Added
- RS-206: RS Core component "Distribution" was added
- RS-214: RS Core component "Compression" was added

### Changed
- Fixed RS Core component ingestion pushing to wrong named destination. Using "catalog jobs" instead to be in line with examples.
- RS-240: Improving configuration and documentation on how to set JVM XMX argument to RS Core components 
- RS-351: PRIP and metadata-catalog-worker errors for OPER_MPL_TLEPRE files
- RS-364: User web client statefulset template has no keycloak parameter
- RS-384: Ingestion jobs do not trigger the metadata catalog

### Removed
- N/A

## [1.0.1-rc1] - 2022-04-28

This version is the first delivery for V1.1 and contains migrated components for SCDF workflows for Ingestion and Metadata Extraction.

### Added
- RS-212: Develop "Catalog" as RS-Core component
- RS-321: Split MDC Worker into extraction workflow and standalone query interface
- RS-330: Create factory ingestion trigger app
- RS-331: Create factory ingestion filter app
- RS-332: Create factory ingestion worker app

### Changed
- N/A

### Removed
- N/A

## [0.3.0-rc11] - 2022-03-01
### Added
- N/A

### Changed
- RS-285: Changing the way how the working directory will handle AccessDeniedExceptions

### Removed
- N/A

## [0.3.0-rc10] - 2022-02-24
### Added
- N/A

### Changed
- RS-283: Fixing typo in AUXIP configuration causing restarts

### Removed
- N/A

## [0.3.0-rc9] - 2022-02-22
### Added
- N/A

### Changed
- RS-275: Using symbolic links again to workaround K8s partition layout
- RS-275: Adding host specific configuration mock

### Removed
- N/A

## [0.3.0-rc8] - 2022-02-21
### Added
- N/A

### Changed
- RS-275: Rebuild: Modified the behaviour of the wrapper script to handle inputs

### Removed
- N/A

## [0.3.0-rc7] - 2022-02-18
### Added
- N/A

### Changed
- RS-211: Removed configuration on not used ASP topics
- RS-275: Modified the behaviour of the wrapper script to handle inputs

### Removed
- N/A

## [0.3.0-rc6] - 2022-02-14
### Added
- N/A

### Changed
- N/A (rebuild)

### Removed
- N/A

## [0.3.0-rc5] - 2022-02-02
### Added
- N/A

### Changed
- Adding boost-date-time library for S3 ACQ container

### Removed
- N/A

## [0.3.0-rc3] - 2022-02-02
### Added
- N/A

### Changed
- RS-250: Ensure the right header is used for OUTH2 authorization

### Removed
- N/A

## [0.3.0-rc2] - 2022-01-31
### Added
- N/A

### Changed
- RS-250: Old servers expecting a bearer token named "OAUTH2-ACCESS-TOKEN". New server is following the specification. Behaviour was changed according to specification.

### Removed
- N/A

## [0.3.0-rc1] - 2022-01-18
### Added
- RS-150: S1 L0 AIOP/ASP containers with real IPFs are build
- RS-151: S3 ACQ containers with real IPFs are build
- RS-156: S3 L0 containers with real IPFs are build
- RS-205: Data Lifecycle Manager had been migrated from S1PRO
- RS-108: Amount of detected security incidents was decreased
- RS-200: S3 footprints are now extracted correctly and propagated to the PRIP
- RS-144: Adding interface query list support in DDIP
- RS-183, RS-170: Allowing to specify a configuration branch different to the software branch for rs_init
- RS-152: Mock for XBIP and EDIP had been pushed to repository under rs-testing
- RS-159: Documentation updated on where to use rs_init
- RS-182: Modifying native api service to allow datetime queries using STAC API
- RS-XX:  Added ingestion filter for EDIP

### Changed
- RS-XX: Removed s1pro-env-scale.sh as it is not used anymore. Use replicaCount.yaml from template folder to setup the amount of replicas instead

### Removed
- N/A


## [0.2.0-rc1] - 2021-12-15
### Added
- RS-101: Filtering solution for S1 and S3 chunks at ingestion
- RS-105: Integration of S3 Acquisition Workflow into S1PRO software
- RS-106: Integration of S3 L0P workflow
- RS-127: Management of PUG processing of S3 L0 products
- RS-133: Integration of EDIP
- RS-134: Integration of AUXIP
- RS-135: Creation of DDIP Fascade
- RS-155: Adding additional XBIP instances to Reference System

### Changed
- RS-136: User Web Client is able to communicate with DDIP
- RS-137: Implementation of collection support within DDIP
- RS-139: Adding support for point and line queries in PRIP
- RS-159: Removing dependency on s1pro-env-init.sh
- RS-171: QCSS was removed from the base configuration to fix the reported issue

### Removed
- RS-168: Removing Kong/Ingress config from PRIP frontend

## [0.1.0-rc1] - 2021-11-17
### Added
- RS-21: RS API allowing to perform a standard product download
- RS-110: Adding a script "rs_deploy" allowing to deploy and remove specific RS addons
- RS-104: Adding basic deployment for RS API service
- RS-111: An init script to setup an RS environment and fetch required repositories from Github


### Changed
- RS-36: Modification of build pipes in rs-core and push artifacts into Artifactory now instead of GHCR
- RS-88: Migration of documentation from repository rs-documentatiojn into repository reference-system-documentation
- RS-109: Migration of Sentinel-3 CGS Transformation configuration into RS
- RS-113: Migrating existing S1PRO repository structure in the repository structure from the consortium

### Removed
- N/A
