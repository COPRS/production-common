S1-PDGS Cloud POC common library
=================================

The basic common Java objects, used by several components, are stored in this library. They are:
* the objects exchanged between the applicative catalog server and client:
** for MQI messages (applicative catalog client used by the MQI server)
** for jobs (applicative catalog clioent used by job generators) 
* the objects exchanged between the MQI server and client:
* the objects for Kakfa messages
* some utilities:
** around date conversion
** around query filtering
** around logs
** around files
* the processing errors
* several enumeration (File extension, application level, product family and category, ...)

### Builds

This project is a maven, java and spring project.

##### IDE

You can use STS (Spring Tools Suite) or Eclipse.
Required java version is >= 1.8

##### Internal dependencies

No internal dependencies

##### External dependency

This project depends on:
* spring-core

### Create a new version

1 - Replace last version by the new one in pom.xml
2 - Replace last version by the new one in Dockerfile
3 - Add this version in the project [repo-maven-common](https://conf.geohub.space/wo7/repo-maven-common)
3a - Add or update a existing pom file
3b - Add a build from your version in the dockerfile

### Configuration

No configuration
