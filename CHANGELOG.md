# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0-rc1] - 2021-11-21
### Added
- RS-15: A filter sidebar for the User Web Client
- RS-21: A interace in the RS API allowing to perform a standard product download
- RS-34: A detail bar showing additional product meta information
- RS-110: Adding a script "rs_deploy" allowing to deploy and remove specific RS addons
- RS-104:Adding basic deployment for RS API service
- RS-105: S3 workflow for processing CADU by the DDC, L0Pre and L0Post processor
- RS-106: S3 workflow for processing granules by the L0P processor
- RS-111: An init script to setup an RS environment and fetch required repositories from Github


### Changed
- RS-36: Modification of build pipes in rs-core and push artifacts into Artifactory now instead of GHCR
- RS-88: Migration of documentation from repository rs-documentatiojn into repository reference-system-documentation
- RS-109: Migration of Sentinel-3 CGS Transformation configuration into RS
- RS-113: Migrating existing S1PRO repository structure in the repository structure from the consortium

### Removed
- N/A
