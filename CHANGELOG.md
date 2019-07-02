# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/).

## [Unreleased]

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

[UNRELEASED]:  https://github.com/magnetcoop/buddy-auth.jwt-oidc/compare/v0.4.0...HEAD
[0.4.0]: https://github.com/magnetcoop/buddy-auth.jwt-oidc/releases/tag/v0.4.0
[0.3.0]: https://github.com/magnetcoop/buddy-auth.jwt-oidc/releases/tag/v0.3.0
[0.2.0]: https://github.com/magnetcoop/buddy-auth.jwt-oidc/releases/tag/v0.2.0

