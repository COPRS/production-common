# RS Core - Ingestion Contingency example

This directories contains the example of an ingestion chain that is configured to operate against an EDIP endpoint. An EDIP is a system that provided inbox with products via ftp. This component is identical to the factory default Ingestion Core component, but contains a configuration to be operated as contingency ingestion for uncompressed XBIP and AUX products. The basic idea is to have a contingency ingestion like it was existing in S1PRO as the old approach is not possible anymore with SCDF.

The mocks used for the EDIP can be found [here](https://github.com/COPRS/production-common/tree/develop/rs-testing)

For further documentation and usage instructions, please consult the original [RS Core component](/processing-common/ingestion).