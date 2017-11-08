# Changelog

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
