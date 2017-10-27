# Changelog

## [0.0.8] - 2017-10-27

###Fixed
1. resolveParentDomainClass method in Embeddable domain class. It was replacing all the occurrences of `Em`.
Changed to replace just the first Occurrence.
2. Related test cases.

## [0.0.7] - 2017-08-24

### Added
1. Support for Collection and Map types when updating Embedded instances.

### Updated
1. Updated `gradle-code-quality` dependency version to `0.0.8`.
