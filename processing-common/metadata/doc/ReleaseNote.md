# RS Core - Metadata

COPRS Metadata chain is responsible for extrating metadata from the products related to the missions of COPRS system and persist the information in the Catalog (ES).

# Overview

![overview](./media/overview.png "Overview")


The metadata chain does have the task to extract the metadata from products that had been either ingested or produced and store them into the elastic search. Basically two elements are contained in the chain.

The filter allows to configure some gate to prevent that products that are ingested will be processed. This can be the case to filter out e.g. reports that are not used for the processing at all. Additionally the filter can be used to create mission specific ingestion chain if this is required for load balancing.

The actual work is done by the extraction. Based on the incoming file and their product family, it will extract the metadata from the file by either evaluating a manifest or other metadata files or extracting the information from the filename. The collected data will then be added to the catalog.

The Metadata Search Controller provides REST services in order to query upon the catalog. Further information on how to deploy it can be found [here](/rs-processing-common)

For details, please see [Metadata Chain Design](https://github.com/COPRS/reference-system-documentation/blob/pro_V1.1/components/production%20common/Architecture%20Design%20Document/004%20-%20Software%20Component%20Design.md#metadata-extraction)

# Resource Requirements

This software does have the following minimal requirements:

| Resource                    |  Catalog Extract Worker* | 
|-----------------------------|---------------|
| Memory request              |     3500Mi    |
| CPU request                 |     300m      |
| Memory limit                |     4000Mi    |
| CPU limit                   |     1500m     |
| Disk volume needed          |   yes, Memory, 1500Mi  |
| Disk access                 |     ReadWriteOnce       |
| Disk storage                |  n/a         |
| Volume Mount                |  /data/local-catalog  |         
| Affinity between Pod / Node |     no       |

 *These resource requirements are applicable for one worker. There may be many instances of an extraction worker, see [scaling up workers](/processing-common/doc/scaling.md) for more details.


# Deployment Prerequisite
Following components of the COPRS shall be installed and running
- [COPRS Infrastructure](https://github.com/COPRS/infrastructure)
- See [COPRS OBS Bucket](/processing-common/doc/buckets.md)
- See [COPRS Kubernetes Secret](/processing-common/doc/secrets.md)

# Additional resources
In the scope of the COPRS it is necessary to be able to adjust the configuration of the commonly used kafka topics. As the SCDF server would create the kafka topics itself, when they aren't already present, it is necessary, that the kafka topics ``catalog-job`` and ``catalog-event`` are already created, before the SCDF streams are started.

In case the default COPRS Infrastructure is used, this will be handled by the Strimzi Operator. On deployment of this RS core chain, the deployment script will firstly create the two KafkaTopic objects into the Kubernetes cluster, which will create the topics with the preferred configuration. The configuration can be found in the folder ``additional_resources`` in the files ``catalog-job.yaml`` and ``catalog-event.yaml``.

# Configuration
## Application properties
| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.*.spring.kafka.bootstrap-servers``| It is a pair of host and port where kafka brokers are running. A Kafka client connects to these servers to bootstrap the application. Comma separated values are provided for multiple enteries.Example : ``kafka-cluster-kafka-bootstrap.infra.svc.cluster.local:9092``|
|``app.*.main.banner-mode``| Disable Spring Boot Banner Using banner-mode at System Console.Default : ``off``|
|``app.*.management.endpoint.health.show-details``| Spring Boot provides a health stats for the application. Default : ``always``|
|``app.*.logging.config``| Path to the file that describes logging configuration for the application.Default : ``log/log4j2.yml``


## Metadata filter properties

The filter component is a generic component from SCDF and further information can be found under:
[https://github.com/spring-cloud/stream-applications/tree/main/applications/processor/filter-processor]

### Elasticsearch (ES)

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.metadata-extraction.elasticsearch.host``|Elasticsearch host name running in the Kubernetes cluster. Default: ``elasticsearch-processing-es-http.database.svc.cluster.local``| 
|``app.metadata-extraction.elasticsearch.port``| Elasticsearch port name running in the Kubernetes cluster.Default: ``9200``| 
|``app.metadata-extraction.elasticsearch.connect-timeout-ms``| Timeout for a period in which this client should establish a connection Elasticsearch Service.Example: ``2000``| 
|``app.metadata-extraction.elasticsearch.socket-timeout-ms``| A maximum time of inactivity between two data packets when exchanging data with a ES server.Example: ``10000``| 


###  Metadata Catalog Extraction
| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.metadata-extraction.process.manifest-filenames.sen3``| Sentinel-3 product's Manifest name. Metadata contained within this file is extracted by the extraction Service.Default: ``xfdumanifest.xml``|
|``app.metadata-extraction.process.manifest-filenames.isip``| Sentinel-1/2/3  products in an ISIP format. Metadata contained within this file is extracted by the extraction Service.Default: ``[PRODUCTNAME]_iif.xml``|
|``app.metadata-extraction.process.manifest-filenames.safe``| Sentinel-1 SAFE format Manifest name. Metadata contained within this file is extracted by the extraction Service.Default: ``manifest.safe``|
|``app.metadata-extraction.process.manifest-filenames.s2``| Sentinel-2 products Metadata file name. Metadata contained within this file is extracted by the extraction Service. Default: ``[S2PRODUCTNAME].xml`` |
|``app.metadata-extraction.process.hostname``| Hostname of the Kubernetes pod that is running Metadata Extraction functionality.Default:``${HOSTNAME}``|
|``app.metadata-extraction.worker.product-categories.auxiliary-files.pattern-config``| Pattern that matches with the filenames of the auxiliary files supported by the service. |``app.metadata-extraction.worker.product-categories.auxiliary-files.pattern-config``\| Pattern that matches with the filenames of the auxiliary files supported by the service. <br /> Default: ``^([0-9a-z][0-9a-z])([0-9a-z_])(_(OPER\|TEST))?_(AMH_ERRMAT\|AMV_ERRMAT\|AM__ERRMAT\|AUX_CAL\|AUX_ICE\|AUX_INS\|AUX_ITC\|AUX_OBMEMC\|AUX_PP1\|AUX_PP2\|AUX_POEORB\|AUX_PREORB\|AUX_RESORB\|AUX_SCF\|AUX_SCS\|AUX_TEC\|AUX_TRO\|AUX_WAV\|AUX_WND\|MPL_ORBPRE\|MPL_ORBRES\|MPL_ORBSCT\|MSK_EW_SLC\|MSK__LAND_\|MSK_OCEAN_\|MSK_OVRPAS)_\w{1,}\.(XML\|EOF\|SAFE)(/.*)?$``|
|``app.metadata-extraction.worker.product-categories.auxiliary-files.local-directory``| The local working directory available to Metadata Extraction pod, where the auxiliary-files are retrieved from the OBS in order to extract the metadata.Default:``/data/local-catalog/auxiliary_files/``|
|``app.metadata-extraction.worker.product-categories.edrs-sessions.pattern-config=``| Pattern that matches with the raw files retrieved bothfrom EDRS/XBIP interface. <br /> Default:``^([0-9a-z_]+)/(ch[0\|_]?[1-2]/)?(DCS_[0-9]{2}_([a-zA-Z0-9_]*)_ch([12])_(DSDB\|DSIB).*\.(raw\|aisp\|xml))$``|
|``app.metadata-extraction.worker.product-categories.edrs-sessions.local-directory``| The local working directory available to Metadata Extraction pod, where the auxiliary-files are retrieved from the OBS in order to extract the metadata.Default:``/data/local-catalog/edrs_sessions/``|
|``app.metadata-extraction.worker.product-categories.edrs-sessions.path-pattern``| Pattern that matches with the path raw files retrieved bothfrom EDRS/XBIP interface. <br /> Default:``^([0-9a-z_]+)/(ch[0\|_]?[1-2]/)?(DCS_[0-9]{2}_([a-zA-Z0-9_]*)_ch([12])_(DSDB\|DSIB).*\.(raw\|aisp\|xml))$``|
|``app.metadata-extraction.worker.product-categories.edrs-sessions.path-metadata-elements.missionId``| Refers to position of mtadata element `missionId` that is going to be extracted from the session path.  Default:``2``|
|``app.metadata-extraction.worker.product-categories.edrs-sessions.path-metadata-elements.satelliteId``| Refers to position of mtadata element `satelliteId` that is going to be extracted from the session path.  Default:``3``|
|``app.metadata-extraction.worker.product-categories.edrs-sessions.path-metadata-elements.sessionId``| Refers to position of mtadata element `sessionId` that is going to be extracted from the session path.  Default:``7``|
|``app.metadata-extraction.worker.product-categories.edrs-sessions.path-metadata-elements.channelId``| Refers to position of mtadata element `channelId` that is going to be extracted from the session path. Default:``8``|
|``app.metadata-extraction.worker.product-categories.plans-and-reports.pattern-config´``| Pattern for the Plan and Report files that are supported by the system. <br/> Default:``^(S1[ABCD_]_OPER_REP_MP_MP__PDMC_\|S1[AB_]OPER_REP_MP_MPPDMC\|S1[ABCD]_OPER_MPL_SP.{4}_PDMC_\|S1[ABCD_]_OPER_MPL_FS.{4}_PDMC_\|S1[ABCD]_OPER_REP_PASS_[1-9]_.{4}_\|S[12]__OPER_SRA_EDRS_[AC]_PDMC_\|EDR_OPER_MPL_RQ[1-9]_O[AC]_\|EDR_OPER_MPL_[LM]AS_O[AC]_\|EDR_OPER_MPL_CR[1-9]_O[AC]_\|EDR_OPER_MPL_SS[1-9]_O[AC]_\|EDR_OPER_MPL_ER[1-9]_O[AC]_\|EDR_OPER_SER_SR[1-9]_O[AC]_\|S1[ABCD]_OPER_MPL_ORBOEM_\|EDR_OPER_MPL_GOB_P[AC]_\|EDR_OPER_MPL_GOB_R[AC]_\|S1[ABCD]_OPER_REP__SUP___\|S1[ABCD]_OPER_REP_STNACQ_.{4}_\|S1[ABCD_]_OPER_REP_STNUNV_.{4}_\|S[123][ABCD_]_OPER_SRA_BANSEG_PDMC_\|S1[ABCD]_OPER_TLM__REQ_[A-O]_\|S1[ABCD]_OPER_REP__SMPR__\|S1[ABCD]_OPER_MPL__SSC___\|S1[ABCD]_OPER_TLM__PSCAT_\|S1[ABCD]_OPER_MPL_OCMSAR_\|S1[ABCD]_OPER_REP__MACP__\|S1[ABCD]_OPER_REP__MCSF__\|S1[ABCD]_OPER_MPL__NPPF__\|S1[ABCD]_OPER_MPL__NPIF__\|S1[ABCD]_OPER_REP_NPIFCC_\|S[123][ABCD_]_OPER_SRA_GSUNAV_PDMC_\|S1[ABCD]_OPER_OBS_MIMG___\|S1[ABCD]_OPER_AUX_RDB____MPC__\|S1[ABCD]_OPER_MPL_SESDB[ABCD]_\|S1[ABCD]_OPER_REP__CHF___\|S1[AB]_OPER_REP__FCHF__\|S1[AB]_OPER_AM[VH_]_FAILUR_MPC__\|S1[AB]_OPER_AUX_QCSTDB_\|S1[AB_]_OPER_REP_QC...._MPC__\|S1[AB]OPER_REPSUP__\|S1[AB]OPER_REPMACP_).*(\.xml\|\.XML\|\.EOF\|\.TGZ\|\.zip\|\.ZIP)?$``|
|``app.metadata-extraction.worker.product-categories.plans-and-reports.local-directory``| The local working directory available to Metadata Extraction pod, where the Plan and reports are retrieved from the OBS in order to extract the metadata.Default:``/data/local-catalog/plans_and_reports/``
|``app.metadata-extraction.worker.product-categories.level-segments.pattern-config``| Pattern for the Sentinel-1 segments. <br /> Default:``^(S1\|AS)(A\|B)_(S[1-6]\|RF\|GP\|HK\|IW\|EW\|WV\|N[1-6]\|EN\|IM\|Z[1-6]\|ZE\|ZI\|ZW)_(SLC\|GRD\|OCN\|RAW)(F\|H\|M\|_)_(0)(A\|C\|N\|S\|_)(SH\|__\|SV\|HH\|HV\|VV\|VH\|DH\|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\w{1,}\.(SAFE)(/.*)?$``|
|``app.metadata-extraction.worker.product-categories.level-segments.local-directory``| The local working directory available to Metadata Extraction pod, where the Sentinel-1 segment are retrieved from the OBS in order to extract the metadata.Example:``/data/local-catalog/level_segments/``|
|``app.metadata-extraction.worker.product-categories.level-products.pattern-config``| Pattern for the Sentinel-1 Level Products such as L0/L1/L2. <br /> Default:``^(S1\|AS)(A\|B)_(S[1-6]\|RF\|IW\|EW\|WV\|GP\|HK\|N[1-6]\|EN\|IM)_(SLC\|GRD\|OCN\|RAW)(F\|H\|M\|_)_(0\|1\|2)(A\|C\|N\|S\|_)(SH\|SV\|HH\|HV\|VV\|VH\|DH\|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\w{1,}\.(SAFE)(/.*)?$``|
|``app.metadata-extraction.worker.product-categories.level-products.local-directory``| The local working directory available to Metadata Extraction pod, where the Sentinel-1 level products are retrieved from the OBS in order to extract the metadata.Default:``/data/local-catalog/level_products/``|
|``app.metadata-extraction.worker.product-categories.s2-products.pattern-config``| Pattern for the Sentinel-2 Level Products. <br /> Default:``^(S2)(A\|B\|_)_([A-Z0-9]{4})_(MSI_(L0_\|L1A\|L1B\|L1C)_(GR\|DS\|TL\|TC))_\w{4}_(\d{8}T\d{6})(.*)$``|
|``app.metadata-extraction.worker.product-categories.s2-products.local-directory=``| The local working directory available to Metadata Extraction pod, where the Sentinel-2 level products are retrieved from the OBS in order to extract the metadata.Default:``/data/local-catalog/s2_products/``|
|``app.metadata-extraction.worker.product-categories.s3-aux.pattern-config``| Pattern for the Sentinel-3 Auxiliaries. <br /> Default:``^(([a-zA-Z0-9][a-zA-Z0-9])(\w{1})_((OL\|SL\|SR\|DO\|MW\|GN\|SY\|TM\|AX)_(0\|1\|2\|_)_\w{4}AX)_(\d{8}T\d{6})_(\d{8}T\d{6})_(\d{8}T\d{6})_(_{17})_(\w{3})_(\w{8})\.(SEN3)\/?(.+)?$``|
|``app.metadata-extraction.worker.product-categories.s3-aux.local-directory``| The local working directory available to Metadata Extraction pod, where the Sentinel-3 Auxiliares  are retrieved from the OBS in order to extract the metadata.Default:``/data/local-catalog/s3_aux/``|
|``app.metadata-extraction.worker.product-categories.s3-products.pattern-config``| Pattern for the Sentinel-3 Auxiliaries. <br /> Default:``^([a-zA-Z0-9][a-zA-Z0-9])(\w{1})_((OL\|SL\|SR\|DO\|MW\|GN\|SY\|TM\|AX)_(0\|1\|2\|_)_\w{4}(?!AX)\w{2})_(\d{8}T\d{6})_(\d{8}T\d{6})_(\d{8}T\d{6})_(\w{17})_(\w{3})_(\w{8})\.(SEN3\|ISIP)\/?(.+)?$``|
|``app.metadata-extraction.worker.product-categories.s3-products.local-directory``| The local working directory available to Metadata Extraction pod, where the Sentinel-3 level products are retrieved from the OBS in order to extract the metadata.Default:``/data/local-catalog/s3_products/``|
|``app.metadata-extraction.worker.product-insertion.max-retries``| Number of retries the Metadata Extraction Service makes to insert the record in Elasticsearch. Default:``3``|
|``app.metadata-extraction.worker.product-insertion.tempo-retry-ms``| Time between number  of retries the Metadata Extraction Service makes to insert the record in Elasticsearch.Default:``1000``|
|``app.metadata-extraction.mdextractor.xslt-directory``| The directory available to the Extraction Service where XSLT stylesheets are located. Default:``xslt/``|
|``app.metadata-extraction.mdextractor.packet-store-types.xxxx``| These are static configuration specific to Sentinel-1 that are required for computation of timeliness. These values are taken as from MPL_OBMEMC (On-board Memory Configuration Files).|
|``app.metadata-extraction.mdextractor.type-overlapd.xxxx``| These are static configuration specific to Sentinel-1 that are required for computation of slice.|
|``app.metadata-extraction.mdextractor.type-slice.xxxx``| These are static configuration specific to Sentinel-1 that are required for computation of slice.|
|``app.metadata-extraction.mdextractor.timeliness-priority-from-high-to-low``| Order of priority for the computed Timeliness of Sentinel-1 products. Default:``PT, NRT, FAST24``|
|``app.metadata-extraction.mdextractor.fieldTypes.<metadata element>``|  Refers to the Metadata Elements that are extracted from level products and their corresponding format.  <br /> Default: absoluteStartOrbit=long  <br /> coordinates=string <br /> creationTime=date <br /> cycleNumber=long <br /> instrumentConfigurationId=string <br /> instrumentShortName=string <br /> missionDataTakeId=long <br /> operationalMode=string <br /> pass=string <br /> platformSerialIdentifier=string <br /> platformShortName=string <br /> processingDate=date <br /> processorName=string <br /> processorVersion=string <br /> productClass=string <br /> productComposition=string <br /> productConsolidation=string <br /> productType=string <br /> qualityDataObjectID=string <br /> qualityNumOfCorruptedElements=long <br /> qualityNumOfElement=long <br /> qualityNumOfMissingElements=long <br /> qualityNumOfRSCorrectedElements=long <br /> qualityNumOfRSCorrectedSymbols=long <br /> qualityNumOfRSIncorrigibleElements=long <br /> relativeStartOrbit=long <br /> safeTimeliness=string <br /> segmentStartTime=date <br /> site=string <br /> sliceNumber=long <br /> sliceProductFlag=boolean <br /> startTimeANX=double <br /> startTime=date <br /> stopTimeANX=double <br /> stopTime=date <br /> swathIdentifier=string <br /> totalNumberOfSlice=long <br /> validityStartTime=date <br /> validityStopTime=date|
|``app.metadata-extraction.timeliness.xxxx``| Product type specific timeliness tags and values in seconds. These values are printed in corresponding Metadata Extraction traces. For mission S1, HK and GP products gets the value from S1_SESSION tag. All other S1 types gets its timeliness values in consulting the timeliness parameter in the CatalogEvent. Eg. if the timeliness is PT, then the value is taken that is configured by tag S1_PT. For mission S2, HKTM and SAD products gets the value from S2_SESSION tag. Other are dependent from the level (S2_L0, S2_L1, S2_L2). For mission S3, L0, Granules and CAL products are regardet to have a NRT timeliness and therefore related with the value configured by tag S3_NRT (together with the L1_NRT and L2_NRT products). For S2 products of type NTC and STC, corresponding timeliness values are configured with tags S3_NTC and S3_STC)|

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
