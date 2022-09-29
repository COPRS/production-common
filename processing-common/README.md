:arrow_heading_up: Go back to the [Reference System Software repository](https://github.com/COPRS/reference-system-software) :arrow_heading_up:

# Processing Common

This directory contains the definitions of the RS Add-ons / Core chains that are not mission specific and used across all missions.

In the folder `template` a generic template is saved to use as a baseline for new chain definitions. 
The folder `doc` contains generic documentation that is applicable to all chains. 
The file `obs-stream-parameters.properties` is appended to each `stream-parameters.properties` in the build workflow in order to minimize maintenance efforts when changing this generic configuration. It contains the configuration used to connect to the Object Storage.

Each chain-folder is structured as described in COPRS-ICD-ADST-001139201 - ICD RS core.

```
.
├── content                
|   ├── stream-application-list.properties       # List of applications that need to be registered for this chain
|   ├── stream-definition.properties             # Stream definition(s) for this chain
|   ├── stream-parameters.properties             # Factory default configuration for this chain
└── doc                                          # Documentation files (e.g. SRN.md)
```
