# RS Core - Ingestion

The RS Core Ingestion component is able to pull data from a source into the COPRS. Supported interfaces that can be used are CADIP, AUXIP and EDIP interfaces

# Overview

![overview](media/overview.png "Overview of the ingestion chain")

The Ingestion Trigger application polls the configured source and looking for the arrival of new products. If it is detecting a matching input, it will not immediatly start to download it, but generating a new message that will be send to the filter. Products that had been detected already will be written into a MongoDB database to avoid that they are detected again.

The Ingestion Filter application verifies if the product is in the time window for products that are accepted. By default the COPRS is not processing all data arriving, but just about 3%. Products that are not within the accepted time window will be discard. If they are accepted, a message will be send to the Ingestion Worker.

The Ingestion Worker application is doing the actual I/O activity and performing the download from the configured interface. Once successfully finished, the product will be uploaded into the Object Storage and a new CatalogJob generated and provided to the umcompression.

The Uncompression is used, to provide the input files uncompressed to the system. Compressed inputs will be downloaded from the OBS and uploaded in their uncompressed form to a different bucket. A new CatalogJob is then generated to notify the COPRS of the new product.

For details, please see [Ingestion Chain Design](/docs/architecture/README.md#ingestion)

# Resource Requirements

This software does have the following minimal requirements:

| Resource                    |  Ingestion Trigger    | Ingestion Filter     |   Ingestion Worker* | 
|-----------------------------|-------------|------------|------------|
| Memory request              |   477Mi     |   3500Mi   |  3500Mi   |
| CPU request                 |   500m      |   300m     |  300m      |
| Memory limit                |   1907Mi    |   4000Mi   |  4000Mi    |
| CPU limit                   |   2200m     |   1500m    |  1300m     |
| Disk volume needed          |   no        |   no       |  yes, Memory, 1500Mi       |
| Disk access                 |   no        |   no       |  ReadWriteOnce |
| Disk storage capacity       |    n/a      |   n/a      |  n/a       |
| Affinity between Pod / Node |   no       |   no       |  no       |

 *These resource requirements are applicable for one worker. There may be many instances of an extraction worker, see [COPRS Worker Scaling](/production-common/scaling.md) for more details.

# Deployment Prerequisite
Following components of the COPRS shall be installed and running
- [COPRS Infrastructure](https://github.com/COPRS/infrastructure)
OBS Buckets, Kubernetes Secrets and ES indices shall be created.
- See [COPRS OBS Bucket](/processing-common/doc/buckets.md)
- See [COPRS ES Indice](/processing-common/doc/indices.md) 
- See [COPRS Kubernetes Secret](/processing-common/doc/secret.md)

Additionally the Ingestion system needs a persistence in order to store which products had been identified in the past already and avoid firing another event for them again. Thus it is required to have a MongoDB instance available and setup. For further general information regarding the creation of a secret for the  MongoDB instance, please see [this](/processing-common/doc/secret.md) description.

The default configuration provided in the RS Core Component is expecting a secret "mongoingestion" in the namespace "processing" containing a field for PASSWORD and USERNAME that can be used in order to authenticate at the MongoDB.

Please note that further initialization might be required. For the Ingestion component please execute the following commands in the MongoDB in order to create the credentials for the secret:

``
db.createUser({user: "<USER>", pwd: "<PASSWORD>", roles: [{role: "readWrite", db: "coprs"}]})
db.inboxEntry.createIndex({"processingPod":1, "pickupURL":1, "stationName":1})
``


# Additional setup

This RS Core component does contain a generic component for ingesting products from different endpoints into the RS environment. The following types are supported:
- AUXIP
- CADIP
- EDIP
- XBIP (deprecated)

For each of these types a different type of inbox configuration needs to be setup. The following sections are giving an overview about how to use and configure the different endpoints.

## AUXIP
When using an AUXIP endpoint it will be required to provide some additional credentials in order to allow login to it. For security issues this cannot be done in the normal properties and needs to be setup as Kubernetes secret. 

Please check [this](https://github.com/COPRS/production-common/tree/develop/processing-common/doc/secret.md) documentation for giving detailed information on how to create secrets and passing the credentials.

For more details, see [AUXIP](/docs/architecture/README.md#auxip)

For an example configuration of an AUXIP endpoint, please have a look at the [example](https://github.com/COPRS/production-common/tree/develop/rs-core-examples/ingestion-auxip)

## CADIP
The CADIP ingestion is the sucessor of the XBIP based interfaces that had been used in older versions of the COPRS. The XBIP was used to harvest new sessions from a WebDav based endpoint and pull the chunks and DSIB files into the processing environment. The CADIP is now (similiar as the AUXIP as well) a OData4 based endpoint that allows to perform queries for sessions and files as well as retrieve the actual chunks from the system.

The trigger is observing the endpoint for the new sessions and once it finds one, it will query the sessions for new files and download it into the processing environment as long as a file was found within the channel that was flagged as final block. The worker understands the CADIP protocol and how to retrieve the actual payload.

Please check [this](/processing-common/doc/secret.md) documentation for giving detailed information on how to create secrets and passing the credentials.

For more details, see [CADIP](/docs/architecture/README.md#cadip)


## XBIP

*Deprecated* Be aware that the CADIP is the successor of the XBIP and will replace it functionality. More information on the CADIP can be found in the previous section. The XBIP documentation will just be kept for documentation purposes and shall not be used anymore.

The XBIP interface in the frame of COPRS is used to download raw data (chunks) for Sentinel-1,  Sentinel-2 and  Sentinel-3 missions.

Please check [this](https://github.com/COPRS/production-common/tree/develop/processing-common/doc/secret.md) documentation for giving detailed information on how to create secrets and passing the credentials.

For more details, see [XBIP](/docs/architecture/README.md#xbip)

The RS Core ingestion does contain an example for an XBIP endpoint already. For an example with multiple inboxes, please have a look at the [example](https://github.com/COPRS/production-common/tree/develop/rs-core-examples/ingestion-xbip)

## EDIP
The EDIP interface, similar to XBIP, is used by COPRS to download chunks via FTP protocol

Please check [this](https://github.com/COPRS/production-common/tree/develop/processing-common/doc/secret.md) documentation for giving detailed information on how to create secrets and passing the credentials.

For more details, see [EDIP](/docs/architecture/README.md#edip)

For an example configuration for an EDIP endpoint, please have a look at the [example](https://github.com/COPRS/production-common/tree/develop/rs-core-examples/ingestion-edip)



# Configuration

## Ingestion Trigger

### General

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-trigger.application.name``|The name of the ingestion trigger application|
|``app.ingestion-trigger.process.hostname``|The hostname of the ingestion trigger. This is recommend to be set to a static string (e.g. `ingestion-trigger-0`). |
|``app.ingestion-trigger.spring.integration.poller.fixed-delay``|The polling interval on the inbox from the trigger in seconds. Please keep in mind that a too short interval might have an impact on the polled system. A too high value might result in unexpected wait time until a product is detected.|


### Inboxes

Please note that the following parameters are grouped by an inbox. The name of the ``inbox`` can be a descriptive name.

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-trigger.ingestion-trigger.polling.\$inbox.directory``|The location of the inbox on the remote system specified by an URI. Please note that depending on the type of inbox additional configuration paramters might be required for CADIP, AUXIP (ODATA) or EDIP (FTP). A location for an CADIP might look like `https://s1pro-mock-cadip/odata/v1/`. This specifies that the trigger shall poll on the given location for new products.|
|``app.ingestion-trigger.ingestion-trigger.polling.\$inbox.matchRegex``|Regular expression that will be used to identify new products on the inbox while doing a poll attempt e.g.`^([A-Za-z_]{4}/)?([0-9A-Za-z_]{1})1([0-9A-Za-z_]{1})/([0-9A-Za-z_]+)/(ch[0\|_]?[1-2]/)?(DCS_[0-9]{2}_([a-zA-Z0-9_]*)_ch([12])_(DSDB\|DSIB).*\\.(raw|aisp|xml|RAW|AISP|XML))$`|
|``app.ingestion-trigger.ingestion-trigger.polling.\$inbox.ignoreRegex`` | A regular expression that allows to specifiy a pattern of files that shall be ignored and not considered to be valid files. This is usually used to exclude temporary files or system files. e.g. ``(^\\..*|.*\\.tmp$|db.*|^lost\+found$)``|
|``app.ingestion-trigger.ingestion-trigger.polling.\$inbox.family``|The product family of the products detected on the inbox. In case of ingestion system this will be usually: <br>* EDRS_SESSION (all missions) <br>* AUXILIARY_FILE (Sentinel-1) <br>* S2_AUX (Sentinel-2) <br>* S3_AUX (Sentinel-3) <br>This information is important for the system to know into which OBS bucket the identified product shall be uploaded to.|
|``app.ingestion-trigger.ingestion-trigger.polling.\$inbox.stationName``|The name of the station from where the products are retrieved from e.g. ``MTI_``|
|``app.ingestion-trigger.ingestion-trigger.polling.\$inbox.missionId``|The identifier of the mission from the inbox in upper case:<br>* S1<br>* S2<br>* S3|
|``app.ingestion-trigger.ingestion-trigger.polling.\$inbox.station-retention-time``|Defines after how many days entries shall be deleted from the persistence of the inbox|
|``app.ingestion-trigger.ingestion-trigger.polling.\$inbox.ignore-files-before-date``|Defines a date. All files before this date will be ignored. e.g. ``2020-11-24T08:00:00.000Z``. Please note that if a data in the future is set, the service will automatically use the current time of the system as filter. This pattern can be used to avoid that all historical products from the inbox will be pulled into the system.|


### MongoDB
  
| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-trigger.mongodb.host``|The hostname to the MongoDB instance that shall be used for persisting the events of the trigger to identify if a request had been fired already. Mongo is provided by the infrastructure layer.|
|``app.ingestion-trigger.mongodb.port``|The port of the MongoDB that shall be used when contacting the host specified under |
|``app.ingestion-trigger.mongodb.database``|The name of the database that shall be used within MongoDB|
|``app.ingestion-trigger.mongodb.username``|The username to login to the MongoDB instance|
|``app.ingestion-trigger.mongodb.password``|The password to login to the MongoDB instance|

### CADIP

#### CADIP Client
The CADIP client module is used by both trigger and worker services for both polling and downloading functionalities.

In oder to connect to multiple servers, following configuration shall be repeated  and ajdusted by adding incrementing  `host*` propeorties. The configuration parameters `host1` are described below.

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-cadip-trigger.cadip.host-configs.host1.serviceRootUri``|URI for the CADIP endpoint.Default:``https://rs-cadip-mock-s1-svc/odata/v1``|
|``app.ingestion-cadip-trigger.cadip.host-configs.host1.user``|Username for the CADIP Server. This is a referenced variable here, that shall be configured using `secret` . <br>If more than one CADIP servers are used for the missions of RS, then the name of  referenced variable for the field here `user` shall also be adjusted as configured in the secret. For Example: If another secret configured contain values `CADIP_USERNAME_2` ,the the variable referenced here shall be adjusted as `${CADIP_USERNAME_2}`. Similar goes for the all the variables here such as `${CADIP_PASSWORD}`, `${CADIP_CLIENT_ID}` and `${CADIP_CLIENT_SECRET}` Default:``${CADIP_USERNAME}``|
|``app.ingestion-cadip-trigger.cadip.host-configs.host1.pass``|Password the configured user of for the CADIP Server. This is a referenced variable here, that shall be configured using `secret` Default:``${CADIP_PASSWORD}``|
|``app.ingestion-cadip-trigger.cadip.host-configs.host1.sslValidation``|SSL validation for the server.Default:``false``|
|``app.ingestion-cadip-trigger.cadip.host-configs.host1.authType``|Authentication type for the CADIP server.Possible values: basic, oauth2, disable.Default:``oauth2``|
|``app.ingestion-cadip-trigger.cadip.host-configs.host1.scope`` | Allows to set a scope for OAuth2 identification. This will be added as additional parameter and might be required for some sites. Default: ``null``, meaning that it is not used |
|``app.ingestion-cadip-trigger.cadip-host-configs.host1.additionalHeadersAuth`` | Allows to set additional header as a map that are added to the request send to the OAuth2 endpoint when asking for a token. This can be utilized to set e.g. a basic authentication that is required to ask for a token. E.g. ``app.ingestion-cadip-trigger.cadip-host-configs.host1.additionalHeadersAuth.Authorization=Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==`` will result in an additional line in the HTTP header request of ``Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==``. No default value as not used by default.
|``app.ingestion-cadip-trigger.cadip.host-configs.host1.bearerTokenType``|A Bearer Token is an opaque string, not intended to have any meaning to clients. This can be set to ``AUTHORIZATION`` that will use a bearer token like ``BasicHeader("Authorization", "Bearer "+accessToken)`` or using ``OAUTH2_ACCESS_TOKEN`` using a bearer token like ``new BasicHeader("OAUTH2-ACCESS-TOKEN", accessToken)``. Please note that version 1.6.0-rc1 and earlier had a typo and offered OUTH2_ACCESS_TOKEN. Please don't use this option anymore! |
|``app.ingestion-cadip-trigger.cadip.host-configs.host1.oauthAuthUrl``|Authentication type for the CADIP server. Default:``https://aux1.s1pdgs.eu/auth/realms/s1pdgs/protocol/openid-connect/token``|
|``app.ingestion-cadip-trigger.cadip.host-configs.host1.oauthClientId``|Oauth2 Client it. This is a referenced variable here, that shall be configured using  Kubernetes secret .Default:``${CADIP_CLIENT_ID}``|
|``app.ingestion-cadip-trigger.cadip.host-configs.host1.oauthClientSecret``|Oauth2 Client secret. This is a referenced variable here, that shall be configured using Kubernetes secret .Default:``${CADIP_CLIENT_SECRET}``|

#### CADIP Trigger

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
``app.ingestion-cadip-trigger.spring.integration.poller.fixed-delay`` |Polling interval between two tries to the server in seconds. Default:``20s``|
|``app.ingestion-cadip-trigger.ingestion-trigger.polling.inbox1.directory``|The polling directory/url of the server. DefaultDefault:``https://rs-cadip-mock-s1/odata/v1``|
|``app.ingestion-cadip-trigger.ingestion-trigger.polling.inbox1.matchRegex``|Pattern that the trigger service shall be matching against the filenames on the server in order to create a job. The pattern shall be adjusted in the associated trigger configruation in order to  match the filenames of Sentinel-2 and Sentinel-3 auxiliaries. Default:``^S1.*(AUX_\|AMH_\|AMV_\|MPL_).*$``|
|``app.ingestion-cadip-trigger.ingestion-trigger.polling.inbox1.ignoreRegex``|Pattern for the filenames that are configured to ignored on the server.Default:``(^\\..*\|.*\\.tmp$\|db.*\|^lost\+found$)``|
|``app.ingestion-cadip-trigger.ingestion-trigger.polling.inbox1.type``|Type of Inbox. For all interfaces, the value shall be `cadip` |
|``app.ingestion-cadip-trigger.ingestion-trigger.polling.inbox1.family``|Product Family associated with the for the files found on the Server. For the CADIP this should be normally ``EDRS_SESSION``.
<br>Default:``EDRS_SESSION``|
|``app.ingestion-cadip-trigger.ingestion-trigger.polling.inbox1.stationName``|CADIP Server/station name. Default:``CADIP``|
|``app.ingestion-cadip-trigger.ingestion-trigger.polling.inbox1.missionId=S1``|Mission ID for the products to the related mission in COPRS.- `S1` for Sentinel-1 related files- `S2` for Sentinel-2 related files - `S3` for Sentinel-3 related files<br>Default:``S1``|
|``app.ingestion-cadip-trigger.cadip.start`` |Starting date for the CADIP trigger for polling files on the server based on the their publicationDate. Files prior to the configured date shall be ingored.Default:``2022-04-10T12:00:00.000000``|
|``app.ingestion-cadip-trigger.cadip.time-window-sec``|The trigger polls starting with a configured timestamp( `start`) and then systematically queries the server by using a configurable timewindow up until the current time.From then on it will stay up-to-date following all new sessions on the CADIP as long as it is running.Default:``2400``|
|``app.ingestion-cadip-trigger.cadip.time-window-overlap-sec``|The overlapping of time windows in seconds, for safety, keep smallDefault:``2400``|
|``app.ingestion-cadip-trigger.cadip.offset-from-now-sec``|The offset from now in seconds, so that the provider has some time to publish new sessions, otherwise they might not be seen. The time window will not reach now but now minus `offset-from-now-sec`.Default:``2400``|
|``app.ingestion-cadip-trigger.cadip.max-page-size``|Maximum number of new files that are that are divided as per configured page-size, if the the resultset is big.Default:``500``|

### XBIP

The configuration for the XBIP contains a set of properties that are grouped by the $host part of the following pattern:

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-trigger.xbip.host-configs.\$host``|host specifies the name of the group|
|``app.ingestion-trigger.xbip.host-configs.$host.serverName``|The hostname of the configured XBIP host|
|``app.ingestion-trigger.xbip.host-configs.$host.trustSelfSignedCertificate``|Specifies of the TLS certificate of the host is a self signed certificate|
|``app.ingestion-trigger.xbip.host-configs.$host.connect-timeout-sec``|Specifies after what amount of seconds the trigger shall give up to establish a connection|
|``app.ingestion-trigger.xbip.host-configs.$host.programmaticRecursion``|If the WebDAV does have infinity scan disabled, it can be activated programmatically here by the client|
|``app.ingestion-trigger.xbip.host-configs.$host.enablePreemptiveAuthentication``|Defines if basic auth shall use preemptive authentication|
|``app.ingestion-trigger.xbip.host-configs.$host.user``|Defines the user name that shall be used to authenticate against the XBIP instance|
|``app.ingestion-trigger.xbip.host-configs.$host.pass``|Defines the password that shall be used to authenticate against the XBIP instance|

## AUXIP

### AUXIP Client 

AUXIP client module is used by both AUXIP trigger and worker services for both polling and downloading functionalities.

In oder to connect to multiple AUXIP servers, following configuration shall be repeated  and ajdusted by adding incrementing  `host*` propeorties. The configuration parameters `host1` are described below.

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.serviceRootUri``|URI for the AUXIP Server.Default:``https://aux1.s1pdgs.eu/odata/v1``|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.user``|Username for the AUXIP Server. This is a referenced variable here, that shall be configured using `secret` . <br>If more than one  AUXIP servers are used for the missions of RS, then the name of  referenced varriable for the field here `user`  shall also be adjusted as configured in the secret. For Example: If another secret configured contain values `AUXIP_USERNAME_2` ,the the varriable referenced here shall be adjusted as `${AUXIP_USERNAME_2}`. Similar goes for the all the varriables here such as  `${AUXIP_PASSWORD}`, `${AUXIP_CLIENT_ID}` and `${AUXIP_CLIENT_SECRET}` Default:``${AUXIP_USERNAME}``|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.pass``|Password the configured user of for the AUXIP Server. This is a referenced variable here, that shall be configured using `secret` Default:``${AUXIP_PASSWORD}``|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.sslValidation``|SSL validation for the server.Default:``false``|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.authType``|Authentication type for the AUXIP server.Possible values: basic, oauth2, disable.Default:``oauth2``|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.bearerTokenType``|A Bearer Token is an opaque string, not intended to have any meaning to clients. This can be set to ``AUTHORIZATION`` that will use a bearer token like ``BasicHeader("Authorization", "Bearer "+accessToken)`` or using ``OAUTH2_ACCESS_TOKEN`` using a bearer token like ``new BasicHeader("OAUTH2-ACCESS-TOKEN", accessToken)``. Please note that version 1.6.0-rc1 and earlier had a typo and offered OUTH2_ACCESS_TOKEN. Please don't use this option anymore! |
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.oauthAuthUrl``|Authentication type for the AUXIP server. Default:``https://aux1.s1pdgs.eu/auth/realms/s1pdgs/protocol/openid-connect/token``|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.oauthClientId``|Oauth2 Client it. This is a referenced variable here, that shall be configured using  Kubernetes secret .Default:``${AUXIP_CLIENT_ID}``|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.oauthClientSecret``|Oauth2 Client secret. This is a referenced variable here, that shall be configured using Kubernetes secret .Default:``${AUXIP_CLIENT_SECRET}``|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.creationDateAttributeName``|Some PRIP providers use `PublicationDate` and other `creationDate`Default:``PublicationDate``|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.productNameAttrName``|Some PRIP providers use `Name` and other `name`. Default:``Name``|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.idAttrName``|Some PRIP providers use `Id` and other `id`. Default:``Id``|
|``app.ingestion-auxip-trigger.auxip.host-configs.host1.contentLengthAttrName``|Some PRIP providers use `ContentLength` and other `contentLength`. Default:``ContentLength``|
  

### AUXIP Trigger

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
``app.ingestion-auxip-trigger.spring.integration.poller.fixed-delay`` |Polling interval between two tries to the AUXIP server in seconds. Default:``20s``|
|``app.ingestion-auxip-trigger.ingestion-trigger.polling.inbox1.directory``|The polling directory/url of the AUXIP server.DefaultDefault:``https://aux1.s1pdgs.eu/odata/v1``|
|``app.ingestion-auxip-trigger.ingestion-trigger.polling.inbox1.matchRegex``|Pattern that the trigger service shall be matching against the filenames on the AUXIP server in order to create a job. The pattern shall be adjusted in the associated trigger configruation in order to  match the filenames of Sentinel-2 and Sentinel-3 auxiliaries. Default:``^S1.*(AUX_\|AMH_\|AMV_\|MPL_).*$``|
|``app.ingestion-auxip-trigger.ingestion-trigger.polling.inbox1.ignoreRegex``|Pattern for the filenames that are configured to ignored on the AUXIP server.Default:``(^\\..*\|.*\\.tmp$\|db.*\|^lost\+found$)``|
|``app.ingestion-auxip-trigger.ingestion-trigger.polling.inbox1.type``|Type of Inbox. For all AUXIP interfaces, the value shall be `prip` |
|``app.ingestion-auxip-trigger.ingestion-trigger.polling.inbox1.family``|Product Family associated with the for the files found on the AUXIP Server.- For S1, related it shall be `AUXILIARY_FILE_ZIP`- For S2, related it shall be `S2_AUX_ZIP`- For S3, related it shall be `S3_AUX_ZIP` <br>Default:``AUXILIARY_FILE_ZIP``|
|``app.ingestion-auxip-trigger.ingestion-trigger.polling.inbox1.stationName``|AUXIP Server/station name. Default:``PRIP``|
|``app.ingestion-auxip-trigger.ingestion-trigger.polling.inbox1.missionId=S1``|Mission ID for the products to the related mission in COPRS.- `S1` for Sentinel-1 related files- `S2` for Sentinel-2 related files - `S3` for Sentinel-3 related files<br>Default:``S1``|
|``app.ingestion-auxip-trigger.auxip.start`` |Starting date for the AUXIP trigger for polling files on the server based on the their publicationDate. Files prior to the configured date shall be ingored.Default:``2022-04-10T12:00:00.000000``|
|``app.ingestion-auxip-trigger.auxip.time-window-sec``|The AUXIP trigger polls starting with a configured timestamp( `start`) and then systematically queries the server by using a configurable timewindow up until the current time.From then on it will stay up-to-date following all new  publications on the PRIPs as long as it is running.Default:``2400``|
|``app.ingestion-auxip-trigger.auxip.time-window-overlap-sec``|The overlapping of time windows in seconds, for safety, keep smallDefault:``2400``|
|``app.ingestion-auxip-trigger.auxip.offset-from-now-sec``|The offset from now in seconds, so that the provider (AUXIP) has some time to publish new products, otherwise they might not be seen from AUXIP. The time window will not reach now but now minus `offset-from-now-sec`.Default:``2400``|
|``app.ingestion-auxip-trigger.auxip.max-page-size``|Maximum number of new files that are that are divided as per configured page-size, if the the resultset is big.Default:``500``|

### EDIP
  
#### EDIP Client

EDIP Client module is used by both EDIP triggers and workers. 

|Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-edip-trigger.edip.host-configs.host1.serverName``|EDIP server name. Default `rs-edip-mock-svc`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.user``|Variable referencing to EDIP user name in the secret `${EDIP_USERNAME}`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.pass``|Variable referencing to EDIP user pass in the secret `${EDIP_PASSWORD}`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.trustSelfSignedCertificate``|Either a self singed certificate shall be trusted or not. `Default: true`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.encryptDataChannel``|FTP data channel shall be encrypted or not. Default: `false`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.ftpsSslSessionReuse``|Default: Should an FTP session shall be reused. Default: `true`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.useExtendedMasterSecret``|Either a master secret shall used. Default: `false`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.connectTimeoutSec``|Connection Timeout in seconds. Default: `60`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.pasv``| PASV FTP protocol, where the cclient initiates the communication. Default: `true`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.enableHostnameVerification``|Hostname verification ensures that hostname in the URL matches with the one in digital certificate. Default: `false`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.buffersize`` || Setting the internal buffer used for buffered streams in Bytes. Default: `1048576`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.sslProtocol``|SLL protocol being used. Supported value: `TLSv1.2`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.explicitFtps``|EDIP client request the server to create a secured session using SSL. Default: `true`|
|``app.ingestion-edip-trigger.edip.host-configs.host1.keyManagerKeyStore``|The keystore is used to keep private keys and certificates. It is needed for FTPS encryption, otherwise can be left empty. Default: `` `` |
|``app.ingestion-edip-trigger.edip.host-configs.host1.keyManagerKeyStorePassword``|Password for `keyManagerKeyStorePassword`. Default: ``changeit``|
|``app.ingestion-edip-trigger.edip.host-configs.host1.trustManagerKeyStore``|The truststore keeps all trusted Certificate Authorities (CA) and certificates. It is necessary to import the CA or certificate for the trusted FTPS servers. Default: ``app/ssl/truststore.jks``|
|``app.ingestion-edip-trigger.edip.host-configs.host1.trustManagerKeyStorePassword``|Password for `trustManagerKeyStorePassword`. Default: ``changeit``|
|``app.ingestion-edip-trigger.edip.host-configs.host1.listingTimeoutSec``|Timeout in seconds for the robust EDIP client (if enabled) while listing directories. Default: `180`|
|``app.ingestion-edip-trigger.edip.enableRobustFtpClient``|To switch on the robust EDIP client using timeout mechanism for listing directories (#RS-519). Default `true`|
 
 #### EDIP Trigger

For EDIP trigger following two two properties need to adjusted. Rest of the properties are similar to other Triggers and needs to be adjusted for the client.

|Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-edip-trigger.ingestion-trigger.polling.inbox1.directory``| URI or directory must start with **ftps://** Default:``ftps://rs-edip-mock-svc:21/NOMINAL/``|
|``app.ingestion-edip-trigger.ingestion-trigger.polling.inbox1.type``| Type of inbox i.e `edip`|
  
## Ingestion Filter

The configuration for the XBIP contains a set of properties that are grouped by the $host part of the following pattern:

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-filter.application.name``|The name of the filter application that will be deployed. The name shall be descriptive to allow an easier identification, e.g. ``rs-ingestion-xbip-cgs01-filter`` for a filter used in XBIP context.|
|``app.ingestion-filter.process.hostname``|The hostname of the system where the application is running. This one will also used identifying the persistence database to avoid firing events twice and thus needs to be unique within the system|
|``app.ingestion-filter.ingestion-filter.config.\$mission_id.cron-definition=\$cron``|Defines the ingestion filter criteria that will be applied on the job fired by the trigger.<br>``mission_id`` must equal the mission id of the event to ensure that a specific mission filter.<br>``cron`` defines the interval when the product will be accepted and continued to be processed. It is expressed as a [Apache Cron Expression](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/util/CronExpression.html).<br>e.g. to define a filter for Sentinel-3 that will be accepting all products on Wednesday between 0 and 8 o'clock, the property needs to be set as:```app.ingestion-filter.ingestion-filter.config.S3.cron-definition=* * 0-8 ? * WED *```<br>Please note that according to the documentation 6 or 7 elements can be provided. The 7th element is specifying the year and is optional.<br>Also note that if no filter is provided at all that all products will be passing the check. Especially when having a typo in the property name and the filter is not mapped correctly this might be misleading. If configure the filter and deploy it for the first time, please consider to validate that the filter matches your expectation. During the startup the filter will dump the known configuration and giving information about the amount of filter configuration and what they are set for   |


## Ingestion Worker

|Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-worker.spring.cloud.stream.function.bindings.ingest-in-0``| Technical parameter, necessary to provide SCDF with the correct binding |
|``app.ingestion-worker.spring.cloud.stream.function.bindings.ingest-out-0``|  Technical parameter, necessary to provide SCDF with the correct binding |
|``app.ingestion-worker.spring.cloud.stream.function.definition``|  Technical parameter, necessary to provide SCDF with the correct binding |
|``app.ingestion-worker.application.name``| Name for the worker application . Example ``coprs-ingestion-xbip-cgs01-worker``|
|``app.ingestion-worker.process.hostname``| Name of host running process. Example  ``xbip-ingestion-worker-0``|
|``app.ingestion-worker.ingestion-worker.hostname``| Name of the pod that is derived from the environment variable. Value ``${HOSTNAME}``|
|``app.ingestion-worker.ingestion-worker.polling-interval-ms``| Polling interval in milli seconds. Default  ``100`` |
|``app.ingestion-worker.ingestion-worker.max-retries``| Number of maixmum retries that an application performs to the server. Default  ``10``|
|``app.ingestion-worker.ingestion-worker.tempo-retry-ms``| Duration between number of retries to the server. Default  ``10000``|
|``app.ingestion-worker.ingestion-worker.buffer-inputs``| Default  ``false``|
|``app.ingestion-worker.xbip.host-configs.host1.serverName``| URI of the server.  Example: ``rs-xbip-mock-s1-svc``|
|``app.ingestion-worker.xbip.host-configs.host1.trustSelfSignedCertificate``| Shall self signed certificates from the server be trusted. Default  ``true``|
|``app.ingestion-worker.xbip.host-configs.host1.connect-timeout-sec``| Connection time out in seconds to the server. Default  ``60``|
|``app.ingestion-worker.xbip.host-configs.host1.programmaticRecursion``| If the WebDAV does have infinity scan disabled, it can be activated programmatically here by the client. Default  ``false``|
|``app.ingestion-worker.xbip.host-configs.host1.enablePreemptiveAuthentication``| Preemptive authentication is used witg basic autherntication, wherein the server expects that the authorization credentials without providing the Unauthorized response. Default  ``false``|
|``app.ingestion-worker.xbip.host-configs.host1.user``| Username that is allowed on the server. This is a referenced variable here, that shall be configured by creating kubernetes secret .Default ``${XBIP_USERNAME}``|
|``app.ingestion-worker.xbip.host-configs.host1.pass``|Password for the user that is allowed on the server. This is a referenced variable here, that shall be configured by creating kubernetes secret. Default  ``${XBIP_PASSWORD}``|
|``app.ingestion-worker.xbip.host-configs.host1.numRetries``| Maximum number of retries that an ingestion worker will perform to server. Default  ``5``|
|``app.ingestion-worker.xbip.host-configs.host1.retrySleepMs``| Duration between number of retries that are performed to reach the server.  Default  ``3000``|

If using EDIP client:
|Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-worker.edip.host-configs.host1.listingTimeoutSec``|Timeout in seconds for the robust EDIP client (if enabled) while listing directories. Default: `180`|
|``app.ingestion-worker.edip.enableRobustFtpClient``|To switch on the robust EDIP client using timeout mechanism for listing directories (#RS-519). Default `true`|

## Uncompression worker

| Property                   				                               | Details       |
|---------------------------------------------------------------|---------------|
|``app.ingestion-uncompress.compression-worker.uncompressionCommand``| Defines the script that shall be executed for performing an uncompression activity. As default the script ``/app/uncompression.sh`` will be used. The script is mission agnostic.|
|``app.ingestion-uncompress.compression-worker.workingDirectory`` | The local directory of the worker that shall be used as temporary working directory to perform the compression activity. This is set by default to ``/tmp/compression`` |
|``app.ingestion-uncompress.compression-worker.compressionTimeout`` | The timeout in seconds when the compression process will be terminated. If it takes more time than the configured value, it will be considered to be hanging. |
|``app.ingestion-uncompress.compression-worker.requestTimeout`` | The timeout in seconds when the compression process will be terminated. If it takes more time than the configured value, it will be considered to be hanging. |
|``app.ingestion-uncompress.compression-worker.hostname`` | The timeout of the overall request. If the request takes more seconds than configured, it is considered to be hanging. |
|``app.ingestion-uncompress.compression-worker.skipUncompression`` | Flag to signal whether or not the complete uncompression-logic shall be skipped. Default: false |
  
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
