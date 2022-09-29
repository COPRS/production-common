S1-PDGS Cloud POC - Object storage client
=========================================

This client enables to centralize the access to the object storage.

The interface between this library and the applications does not expose any AWS S3 concept (class, attribute, methods, …).

### Sources

The interface is composed by the following objects:
* An enumeration of product families ObsFamily: enables to link object storage buckets and products
* Exchanged objects:
  * ObsDownloadObject used for downloading an object into a file
  * ObsUploadObject used for uploading a file into an object
* A client which proposes the following services:
  * doesObjectExist: true if the given object exists, false else
  * doesPrefixExist: true if at least one object exists with given prefix
  * downloadObject: download objects with a given prefix from the OBS in a local directory
  * downloadObjects: download objects with a list of prefixes from OBS in a local directory, sequentially or in parallel
  * uploadObject: upload a file or a directory into the object storage
  * uploadObjects: upload a list of files or directories into the object storage, sequentially or in parallel
* A client builder which proposes a default client implementation (based on Amazon S3 API)

##### Amazon S3 client implementation

The library offers an implementation using the Amazon s3 API.
The Amazon S3 client is configured as follow:
* Used a basic AWS credentials based on one unique user
* The used protocol is HTTP
* A retry policy defined as follow:
  * For server errors:
    * Simple retry condition that allows retries up to a certain max number of retries
    * SDK backoff strategy: the full jitter scheme for non-throttled exceptions and the equal jitter scheme for throttled exceptions.  This gives a preference to quicker response and larger retry distribution for service errors and guarantees a minimum delay for throttled exceptions.
  * For client errors:
    * Whatever the error (HTTP client error or server returns an error HTTP status), we retry X times before raising an exception


### Builds

This project is a maven, java and spring project.

##### IDE

You can use STS (Spring Tools Suite) or Eclipse.
Required java version is >= 1.8

##### Internal dependencies

This project depends on:
* [commons](https://conf.geohub.space/wo7/lib-commons) library

Please install these dependencies in your local repository before building project

##### External dependency

This project depends on:
* commons-logging
* commons-configuration
* aws-java-sdk-s3
* spring-core

### Create a new version

1. Replace last version by the new one in pom.xml
2. Replace last version by the new one in Dockerfile
3. Add this version in the project [repo-maven-all](https://conf.geohub.space/wo7/repo-maven-all)
   1. Add or update a existing pom file
   2. Add a build from your version in the dockerfile

### Configuration

All variables are taken from environment variables

Parameter                                        | Description
------------------------------------------------ | ------------- 
user.id                                          | the client identifier
user.secret                                      | the client secret
endpoint                                         | the endpoint of the OBS
endpoint.region                                  | the region of the endpoint
retry-policy.condition.max-retries               | the maximal number of retries
retry-policy.backoff.base-delay-ms               | base sleep time (milliseconds) for non-throttled exceptions.
retry-policy.backoff.throttled-base-delay-ms     | base sleep time (milliseconds) for throttled exceptions.
retry-policy.backoff.max-backoff-ms              | maximum back-off time before retrying a request
bucket.auxiliary-files                           | bucket name for auxiliary files
bucket.edrs-sessions                             | bucket name for EDRS sessions files
bucket.l0-products                               | bucket name for L0 slices
bucket.l0-acns			                        | bucket name for L0 ACNs
bucket.l1-products                               | bucket name for L1 slices
bucket.l1-acns                                   | bucket name for L1 A
timeout-s.shutdown                               | timeout in seconds of a scheduled task shutdown 
timeout-s.down-exec                              | timeout in seconds of a download of one product
timeout-s.up-exec                                | timeout in seconds of a upload of one product