# KGrid Activator
[![CircleCI](https://circleci.com/gh/kgrid/kgrid-activator.svg?style=svg)](https://circleci.com/gh/kgrid/kgrid-activator)
[![GitHub release](https://img.shields.io/github/release/kgrid/kgrid-activator.svg)](https://github.com/kgrid/kgrid-activator/releases/)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## Overview
As a key component of Knowledge Grid, an activator allows knowledge objects to be executable against collected data.

For the information on the activator API and the usage of the activator, see [KGRID Activator API](api.md)

## Table of Contents

1. [Build and Test Activator](#build-activator)
2. [Deploy Activator](#deploy-activator)
2. [Release Activator](#release-activator)
2. [Docker Activator](docker.md)


## Build Activator

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
For building and running the application you need:

- [Java 8 or higher](https://www.oracle.com/java/)
- [Maven 3](https://maven.apache.org)

### Clone
To get started you can simply clone this repository using git:
```
git clone https://github.com/kgrid/kgrid-activator.git
cd kgrid-activator
```

### Quick Start
This quick start will run the activator and load two example knowledge objects for testing.  This objects are located
in the _shelf_ directory at the root of the project. By default application will start up and PORT 8080.
```
mvn clean package
java -jar target/kgrid-activator*.jar
```
You can load sample KO shelf (_where to look for the KOs_)
```
java -jar target/kgrid-activator*.jar --kgrid.shelf.cdostore.url:filesystem:file://etc/shelf
```
Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```
mvn clean spring-boot:run
```

Once Running access the [Activators Health Endpoint](http://localhost:8080/health).  All _statuses_ reported should be **UP**

```json
{
  "status": "UP",
  "components": {
    "activationService": {
      "status": "UP",
      "details": {
        "kos": 12,
        "endpoints": 15,
        "activatedEndpoints": 5
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 402672611328,
        "free": 289865793536,
        "threshold": 10485760,
        "exists": true
      }
    },
    "org.kgrid.adapter.proxy.ProxyAdapter": {
      "status": "up",
      "details": {
        "types": [ ]
      }
    },
    "org.kgrid.adapter.resource.ResourceAdapter": {
      "status": "UP",
      "details": {
        "types": [
          "resource"
        ]
      }
    },
    "org.kgrid.adapter.v8.JsV8Adapter": {
      "status": "UP",
      "details": {
        "types": [
          "javascript"
        ]
      }
    },
    "ping": {
      "status": "UP"
    },
    "shelf": {
      "status": "UP",
      "details": {
        "numberOfKOs": 12,
        "kgrid.shelf.cdostore.url": "file:///app/shelf/"
      }
    }
  }
}
```
## Configuration
There are a few environment variables that can be set to control different aspects of the activator.
- `server.port` - Specify a particular port on which the activator should start
- `spring.profiles.active` - Set the security profile. Security is enabled by default, to disable, set this property to `dev`.
- `spring.security.user.name` - Specify the admin username. Security is enabled by default, so if this property is not set, the admin features will be inaccessible.
- `spring.security.user.password` - Specify the admin password. Security is enabled by default, so if this property is not set, the admin features will be inaccessible.

#### Admin Endpoints

- `GET activator-url/actuator/health`: Check activator health status
- `GET activator-url/actuator/info`: Display activator information
- `GET activator-url/activate`: Activate all Knowledge Objects
- `POST activator-url/kos/manifest`: Load and activate a single manifest of Knowledge Objects
- `POST activator-url/kos/manifest-list`: Load and activate a list of manifests of Knowledge Objects
- `POST activator-url/kos/`: Deposit a single zipped Knowledge Object
- `DELETE activator-url/kos/{naan}/{name}/{version}`: Delete a Knowledge Object from the shelf
- `PUT activator-url/kos/{naan}/{name}/{version}`: Replace a Knowledge Object on the shelf.


#### Cross Origin Requests
The app uses CorsFilter to configuration of the allowed methods, origins and associated headers. Currently, it allows all methods from all origins. 

## Running the tests


#### Automated tests
Unit and Integration tests can be executed via
```
mvn clean test
mvn clean verify
```

#### End to End Testing

Sample shelf in place the following tests can be executed against the running activator

View a Knowledge Object

```
curl http://localhost:8080/hello/world
```

View a Knowledge Object Version

```
curl http://localhost:8080/hello/world/v0.0.1
```

Run the welcome endpoint on the 99999/newko/v0.0.1 knowledge object
```
curl -X POST -H "Content-Type:application/json"  -d "{\"name\": \"Fred Flintstone\"}" http://localhost:8080/hello/world/v0.0.1/welcome
```

#### Sample Knowledge Objects

A collection of sample Knowledge Objects are stored at `/etc/collection`. The KOs, with the same {naan}/{name}/{endpoint}, may have a different api version. They can be used to try out the handling of the api version. 

## Deploy Activator
Please see the [KGrid Org Activator](http://kgrid.org/kgrid-activator/) site for details

__Important:__ When deploying to HEROKU, DO NOT use `Deploy Branch`.

__Note:__
Configuration Variable Currently set for Krid-activator on Heroku:
```
--kgrid.shelf.cdostore.url=filesystem:file://shelf --cors.url=*  --kgrid.adapter.proxy.url=https://node-express-runtime.herokuapp.com  --kgrid.adapter.proxy.self=https://kgrid-activator.herokuapp.com --management.info.git.mode=full --management.endpoints.web.exposure.include=*
```
## Release Activator
Please see the  [Kgrid Config](https://github.com/kgrid/kgrid-config/tree/master/release-code) repository for details


## Publish Documentation

Running Local Dev Docs Publish
```
npm install
npm run docs:dev
```

Build dist directory ready for publish

```
npm run docs:build`
```

CircleCi publishes the documentation using [VuePress](https://vuepress.vuejs.org/) and
the ```.circleci/vuepress_deploy.sh``` script.  The gh-pages branch is used for the publishing process and setup in the
GitHub repository's GitHub Pages.
