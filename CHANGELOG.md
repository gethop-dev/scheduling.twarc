# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/).

## [UNRELEASED]

## [0.6.1] - 2022-05-17
### Changed
- Moving the repository to [gethop-dev](https://github.com/gethop-dev) organization
- Source code linting using [clj-kondo](https://github.com/clj-kondo/clj-kondo)
- CI/CD solution switch from [TravisCI](https://travis-ci.org/) to [GitHub Actions](Ihttps://github.com/features/actions)

## [0.6.0] - 2020-12-01
### Changed
- Bumped 3rd party dependencies
- **BREAKING CHANGE** Replaced the old `:postgres-cfg` configuration key with the new `:postgres-url` key. The new configuration key takes a string with a JDBC connection URL, instead of the old map with separate connection details.

## [0.5.0] - 2020-05-02
### Added
- New configuration key for the Duct module, to specify the name of the table storing applied Ragtime migrations.

### Changed
- Bumped 3rd party dependencies
- Moved migration files to a qualified directory (using the library namespace). This lets external users of the library directly manage the migration files with less probability of collisions, if they don't want to use the Duct module to automatically manage them.

## [0.4.0] - 2019-07-02
### Added
- This CHANGELOG
- New configuration key to specify the scheduler thread pool size used to run scheduled jobs (was hardcoded to 1 before).

### Changed
- Bumped CIDER version dependency (devel profile only)
- Updated README.md to tell the user the need to include `:duct/migrator` argument when running the uberjar, and the limitations on the type of arguments that can be passed to scheduled jobs.
- Added specs for public functions and instrumentation when running the tests

## [0.3.0] - 2019-01-29
### Added
- Add Travis CI integration
- Add deployment configuration and integration tests CI 

### Changed
- Fix psql command to create default database 
- Update clojure to 1.10.0,

## [0.2.0] - 2019-01-28
- Initial commit (previous versions were not publicly released)

[UNRELEASED]:  https://github.com/gethop-dev/scheduling.twarc/compare/v0.6.1...HEAD
[0.6.1]: https://github.com/gethop-dev/scheduling.twarc/releases/tag/v0.6.1
[0.6.0]: https://github.com/gethop-dev/scheduling.twarc/releases/tag/v0.6.0
[0.5.0]: https://github.com/gethop-dev/scheduling.twarc/releases/tag/v0.5.0
[0.4.0]: https://github.com/gethop-dev/scheduling.twarc/releases/tag/v0.4.0
[0.3.0]: https://github.com/gehop-dev/scheduling.twarc/releases/tag/v0.3.0
[0.2.0]: https://github.com/gethop-dev/scheduling.twarc/releases/tag/v0.2.0

