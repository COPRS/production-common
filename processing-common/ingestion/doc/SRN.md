# RS Core - Ingestion

## General

***TODO*** Add image

The RS Core Ingestion component is able to pull data from a source into the COPRS. Supported interfaces that can be used are AUXIP, EDIP and XBIP interfaces

The Ingestion Trigger application will poll the configured source and looking for the arrival of new products. If it is detecting a matching input, it will not immediatly start to download it, but generating a new message that will be send to the filter. Products that had been detected already will be written into a MongoDB database to avoid that they are detected again.

The Ingestion Filter application will verify if the product is in the time window for products that are accepted. By default the COPRS is not processing all data arriving, but just about 3%. Products that are not within the accepted time window will be discard. If they are accepted, a message will be send to the Ingestion Worker.

The Ingestion Worker application is doing the actual I/O activity and performing the download from the configured interface. Once successfully finished, the product will be uploaded into the Object Storage and a new Ingestion Event generated to notify the COPRS that a new product had been added to the system.

## Configuration

### Ingestion Trigger
#### General

app.ingestion-trigger.status.delete-fixed-delay-ms=3000
app.ingestion-trigger.status.max-error-counter-processing=3
app.ingestion-trigger.status.max-error-counter-mqi=30
app.ingestion-trigger.status.fail-after-inactivity-for-seconds=200
TBD: Outdated?

### Ingestion Filter
The following properties can be used in order to modify the application behaviour:

***app.ingestion-filter.application.name***

The name of the filter application that will be deployed. The name shall be descriptive to allow an easier identification, e.g. ``rs-ingestion-xbip-cgs01-filter`` for a filter used in XBIP context.

***app.ingestion-filter.process.hostname***

The hostname of the system where the application is running. This one will also used identifying the persistence database to avoid firing events twice and thus needs to be unique within the system
```
***app.ingestion-filter.ingestion-filter.polling-interval-ms***

The interval in miliseconds between the 
```
TBD: This configuration does seems to be used. Might be duplicate of trigger?

***app.ingestion-filter.ingestion-filter.config.\$mission_id.cron-definition=\$cron***

Defines the ingestion filter criteria that will be applied on the job fired by the trigger. 

``mission_id`` must equal the mission id of the event to ensure that a specific mission filter.

``cron`` defines the interval when the product will be accepted and continued to be processed. It is expressed as a [Spring Cron Expression](https://spring.io/blog/2020/11/10/new-in-spring-5-3-improved-cron-expressions).

e.g. to define a filter for Sentinel-3 that will be accepting all products on Wednesday between 0 and 8 o'clock, the property needs to be set as:
```app.ingestion-filter.ingestion-filter.config.S3.cron-definition=* * 0-8 ? * WED *```



### Ingestion Worker