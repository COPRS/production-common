S1-PDGS Cloud POC - Message queue interface client
=========================================

This client enables to centralize the access to the REST API of the applicative data catalog.

### Sources

This library proposes:
* one client per product category to manage queue messages
* one client for managing L0 jobs generations (product category EDRS_SESSIONS)
* one client for managing L1 jobs generations (product category L0_PRODUCT)

The applications shall instantiate clients by giving them:
* the configured REST template,
* the host of the applicative data catalog (example: http://app-data-catalog:8080)
* the maximal number of retries (must be between 0 and 20)
* the temporisation to apply between each retry

A mechanism of retry is applied for each REST API call. If the call returns an HTTP code different that OK or if an exception is raised by the REST client, the MQI client shall retry X times before raising an error.

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
* spring-core
* spring-web

### Create a new version

1. Replace last version by the new one in pom.xml
2. Replace last version by the new one in Dockerfile
3. Add this version in the project [repo-maven-all](https://conf.geohub.space/wo7/repo-maven-all)
   1. Add or update a existing pom file
   2. Add a build from your version in the dockerfile

### Configuration

N/A