[![Build Status](https://travis-ci.org/magnetcoop/scheduling.twarc.svg?branch=master)](https://travis-ci.org/magnetcoop/scheduling.twarc)
# Duct Twarc
A [Duct](https://github.com/duct-framework/duct) library that provides [Integrant](https://github.com/weavejester/integrant) keys for using [Twarc](https://github.com/prepor/twarc) scheduling library, with persistent JobStore backed by a Postgresql database.

## Installation

[![Clojars Project](https://clojars.org/magnet/scheduling.twarc/latest-version.svg)](https://clojars.org/magnet/scheduling.twarc)

## Usage

This library provides two Integrant keys. The main one, called `:magnet.scheduling/twarc`, is used to create a Twarch scheduler, that expects the following configuration keys:
* `:postgres-cfg`: a map with the Postgres database connections details, with the following mandatory keys:
  * `:host`
  * `:port`
  * `:db`
  * `:user`
  * `:password`
* `:scheduler-name`: the name for this particular scheduler. Schedulers using persistent JobStores need to have a name. If this key is not specified, the default value of `main-scheduler` is used.
* `:logger`: usually a reference to `:duct/logger` key. But you can use any Integrant key derived from `:duct/logger` (such as `:duct.logger/timbre`).

Key initialization returns a scheduler already started and ready to accept scheduling jobs. See [Twarc](https://github.com/prepor/twarc) documentation for job scheduling details and options.

Halting the key stops the scheduler and its scheduled jobs, and destroys any in memory state of the scheduler. But but the scheduled jobs and triggers are safely persisted in the backing database, ready to be picked up on next key initialization. So you need to initialize the key again to get a working instance of the scheduler and its associated jobs and triggers.

Example usage:

``` edn
:magnet.scheduling/twarc {:postgres-cfg {:host #duct/env ["POSTGRES_HOST" Str]
                                         :port #duct/env ["POSTGRES_PORT" Str]
                                         :db #duct/env ["POSTGRES_DB" Str]
                                         :user #duct/env ["POSTGRES_USER" Str]
                                         :password #duct/env ["POSTGRES_PASSWORD" Str]}
                          :scheduler-name "main-scheduler"
                          :logger #ig/ref :duct/logger}
```

The library uses [duct/migrator.ragtime](https://github.com/duct-framework/migrator.ragtime) to make sure the Postgresql tables for the persistent JobStore exist, and have the right structure and content. It implements a Duct module that adds the migrations needed by this library to the collection of already existing migrations in the system configuration map. If no `:duct/migrator.ragtime` configuration key is found, it automatically adds it and the library own migrations.

This means that, in addition to the main configuration key, you also need to specify the `:magnet.module.scheduling/twarc-pgsql` key for the Duct module, and you must have a `:duct.module/sql` key defined in your system configuration map, with the Postgresql database connection details.

So a realistic configuration example would look like this:

``` edn
:duct.module/sql {:database-url #duct/env ["JDBC_DATABASE_URL" Str]}

:magnet.module.scheduling/twarc-pgsql {}

:magnet.scheduling/twarc {:postgres-cfg {:host #duct/env ["POSTGRES_HOST" Str]
                                         :port #duct/env ["POSTGRES_PORT" Str]
                                         :db #duct/env ["POSTGRES_DB" Str]
                                         :user #duct/env ["POSTGRES_USER" Str]
                                         :password #duct/env ["POSTGRES_PASSWORD" Str]}
                          :scheduler-name "main-scheduler"
                          :logger #ig/ref :duct/logger}
```

### Important note

Remember that Integrant keys which derive from `:duct/migrator` (such as `:duct/migrator.ragtime`) are not initialized when running Duct from `-main`. So make sure you add `:duct/migrator` key to the arguments used to run the app from the uberjar (see https://github.com/duct-framework/duct/wiki/Configuration#top-level-components for additional information)

## License

Copyright (c) Magnet S Coop 2018.

The source code for the library is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
