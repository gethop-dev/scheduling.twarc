[![Build Status](https://api.travis-ci.com/magnetcoop/scheduling.twarc.svg?branch=master)](https://travis-ci.com/magnetcoop/scheduling.twarc)
[![Clojars Project](https://img.shields.io/clojars/v/magnet/scheduling.twarc.svg)](https://clojars.org/magnet/scheduling.twarc)

# Duct Twarc

A [Duct](https://github.com/duct-framework/duct) library that provides [Integrant](https://github.com/weavejester/integrant) keys for using [Twarc](https://github.com/prepor/twarc) scheduling library, with persistent JobStore backed by a Postgresql database.

## Installation

[![Clojars Project](https://clojars.org/magnet/scheduling.twarc/latest-version.svg)](https://clojars.org/magnet/scheduling.twarc)

## Usage

This library provides an Integrant key called `:magnet.scheduling/twarc`, used to create a Twarc scheduler. It expects the following mandatory configuration keys:

* `:postgres-cfg`: a map with the Postgres database connections details, with the following mandatory keys:
  * `:host`
  * `:port`
  * `:db`
  * `:user`
  * `:password`
* `:logger`: usually a reference to `:duct/logger` key. But you can use any Integrant key derived from `:duct/logger` (such as `:duct.logger/timbre`).

It also accepts the following optional configuration keys:

* `:scheduler-name`: a string with the name for this particular scheduler. Schedulers using persistent JobStores need to have a name. If this key is not specified, the default value of `main-scheduler` is used.
* `:thread-count`: a positive integer with the size of the thread pool used by the scheduler to run scheduled jobs. If this key is not specified, the default value of 10 is used.

Key initialization returns a map with two keys:

* `scheduler`: a Twarc scheduler already started and ready to accept scheduling jobs. See [Twarc](https://github.com/prepor/twarc) documentation for job scheduling details and options.
* `logger`: the same logger value that was passed in the configuration map. The library needs it in the `halt-key!` Integrant method, so that's why it is returned here.

Halting the key stops the scheduler and its scheduled jobs, and destroys any in memory state of the scheduler. But but the scheduled jobs and triggers are safely persisted in the backing database, ready to be picked up on next key initialization. So you need to initialize the key again to get a working instance of the scheduler and its associated jobs and triggers.

**Important note**: The underlying Quartz library has a limitation when using database backed persistent JobStores (like this Duct library does). You can't use arguments for your scheduled jobs that are tied to application classes (e.g., anonymous functions, Records, etc.). The persistent JobStore uses the standard class loader and doesn't know how to find application classes, so it can't deserialize the objects persisted in the database.

Example usage:

``` edn
:magnet.scheduling/twarc {:postgres-cfg {:host #duct/env ["POSTGRES_HOST" Str]
                                         :port #duct/env ["POSTGRES_PORT" Str]
                                         :db #duct/env ["POSTGRES_DB" Str]
                                         :user #duct/env ["POSTGRES_USER" Str]
                                         :password #duct/env ["POSTGRES_PASSWORD" Str]}
                          :scheduler-name "main-scheduler"
                          :thread-count 10
                          :logger #ig/ref :duct/logger}
```

### Database migrations for persistent JobStore

#### Using the included Duct module

The library can use [duct/migrator.ragtime](https://github.com/duct-framework/migrator.ragtime) to make sure that the Postgresql tables for the persistent JobStore exist, and have the right structure and content. It implements a Duct module that adds the migrations needed by this library to the system configuration map. If no `:duct/migrator.ragtime` configuration key is found, it automatically adds it and the library own migrations.

This means that, in addition to the main configuration key, you can also specify the `:magnet.module.scheduling/twarc-pgsql` key for the Duct module. In that case, you must have a `:duct.module/sql` key (or other key derived from it) defined in your system configuration map, with the Postgresql database connection details.

This Duct module accepts the following optional configuration key, that lets you specify the table name that will store the library own migrations (so they don't collide with your own app migrations and mess with migration application order):

* `:migrations-table`: it is a string with the name of the table that will store the list of already applied Ragtime migrations. If not specified, it defaults to `ragtime_migrations_twarc`.

So a configuration example using the Duct module to handle the Postgresql tables creation would look like this (assuming Duct 0.7.0 or later):

``` edn
{:duct.profile/base
 {;; Other base profile Integrant keys..

  :magnet.scheduling/twarc {:postgres-cfg {:host #duct/env ["POSTGRES_HOST" Str]
                                           :port #duct/env ["POSTGRES_PORT" Str]
                                           :db #duct/env ["POSTGRES_DB" Str]
                                           :user #duct/env ["POSTGRES_USER" Str]
                                           :password #duct/env ["POSTGRES_PASSWORD" Str]}
                            :scheduler-name "main-scheduler"
							:thread-count 10
                            :logger #ig/ref :duct/logger}

  ;; More base profile Integrant keys..
 }

 ;; More Duct modules Integrant keys...
 :duct.module/sql {:database-url #duct/env ["JDBC_DATABASE_URL" Str]}
 :magnet.module.scheduling/twarc-pgsql {:migrations-table "ragtime_migrations_twarc"}
}

```

#### Doing it by hand

The files with the SQL sentences neede to create (and drop) the tables for Postgresqsl are located in the library resources directory. They can be accessed as resources with the relative paths "magnet.scheduling.twarc/migrations/001-quartz-pgsql.up.sql" and "magnet.scheduling.twarc/migrations/001-quartz-pgsql.down.sql" respectively.

### Important note

Remember that Integrant keys which derive from `:duct/migrator` (such as `:duct/migrator.ragtime`) are not initialized when running Duct from `-main`. So make sure you add `:duct/migrator` key to the arguments used to run the app from the uberjar (see https://github.com/duct-framework/duct/wiki/Configuration#top-level-components for additional information)

## License

Copyright (c) 2018, 2019, 2020 Magnet S Coop.

The source code for the library is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
