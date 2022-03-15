# rs-processing-common

This directory contains a set of helm charts for these services of the RS that will be used as infrastructure and running independently from the SCDF applications. They are not invoked by a request, but running rather as some kind of service for SCDF applications or providing access to the data hold by the RS.

The helm charts are supposed to be deployed into the RS cluster via ansible or by using helm directly.

These services are:
- Metadata Search Controller
- Eviction Manager
- Native API
- RSAPI Frontend (PRIP / DDIP Frontend)
- User Web Client
- Request Repository