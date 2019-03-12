# Changelog

## [2.0.1] - [Unreleased]
### Changed
- Added version for grails plugin testing to fix the version resolving issue.
## [2.0.0] - [Unreleased]
### Changed
- Upgraded the plugin to support grails 3.3.5

## [1.0.0] - [2018-04-04]

### Added
- Functionality to update "lastUpdated" timestamp field along with embedded class fields.

### Removed
- Unnecessary dependencies.

## [0.0.9] - 2018-03-26

### Added
- Functionality to externalize the scheduling of UpdateEmbeddedInstancesJob.

## [0.0.8] - 2017-10-27

###Fixed
1. resolveParentDomainClass method in Embeddable domain class. It was replacing all the occurrences of `Em`.
Changed to replace just the first Occurrence.

### Changed
1. Upgraded `gradle-code-quality` version to `1.0.0`.
2. Updated `maven` server url in `build.gradle`.
3. Updated Gradle Wrapper version from `3.0` to `3.4.1`.

### Added
- ####CircleCI configuration
    -  `.circleci/config.yml` for build automation using `CircleCI`.
    - `mavenCredsSetup.sh` for generating `gradle.properties` during the CircleCI build.

## [0.0.7] - 2017-08-24

### Added
1. Support for Collection and Map types when updating Embedded instances.

### Updated
1. Updated `gradle-code-quality` dependency version to `0.0.8`.
