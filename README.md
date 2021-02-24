# KGrid Activator

[![CircleCI](https://circleci.com/gh/kgrid/kgrid-activator.svg?style=svg)](https://circleci.com/gh/kgrid/kgrid-activator)
[![GitHub release](https://img.shields.io/github/release/kgrid/kgrid-activator.svg)](https://github.com/kgrid/kgrid-activator/releases/)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## Overview
As a key component of Knowledge Grid, an activator allows knowledge objects to be executable against collected data.

For the information on the activator API and the usage of the activator, see [KGRID Activator API](docs/api.md)

## Build & run the Activator locally

For development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
For building and running the application you need:

- [Java 11 or higher](https://www.oracle.com/java/)
- [Maven 3](https://maven.apache.org)

### Clone
To get started you can simply clone this repository using git:
```
git clone https://github.com/kgrid/kgrid-activator.git
cd kgrid-activator
```

### Quick Start

```
mvn clean package
export SPRING_PROFILES_ACTIVE=dev 
# set SPRING_PROFILES_ACTIVE=dev (Windows)
java -jar target/kgrid-activator*.jar
```
This  will run the activator and create an empty `./shelf` in the current working directory. By default the application starts on port 8080.

Access the activator `health` endpoint at (http://localhost:8080/actuator/health).  `status` should be `up`

You can load the local shelf with the example collection of KOs by setting the `kgrid.shelf.manifest` property at startup to point to the manifest file for the [`example-collection`](https://github.com/kgrid-objects/example-collection/releases/latest) release on Github. 

```
java -jar target/kgrid-activator*.jar --kgrid.shelf.manifest=https://github.com/kgrid-objects/example-collection/releases/download/4.1.1/manifest.json 
```

You can also point the activator to an existing `/shelf` on your local system using the  `kgrid.shelf.cdostore.url` property at startup.
```
java -jar target/kgrid-activator*.jar --kgrid.shelf.cdostore.url=filesystem:file:///path/to/local/shelf
```


## Running the tests

Unit and Integration tests can be executed via
```
mvn clean verify
```


## Deploying the Activator in cloud environments

Follow your provider's documentation. If you are building the latest version from `main` you may need to add the oss.sonatype.org SNAPSHOT repository. For example, on Heroku by using: 
```bash
MAVEN_SETTINGS_PATH=.circleci/settings.xml
```


----

#Note: We are currently using the basic github site publishing not the vuepress build

## Publish Vuepress Documentation



### Running the vuepress site locally
```
npm install
npm run docs:dev # or vuepress docs dev
```

### Build the site for publishing

```
npm run docs:build
```

To push the vuepress site to the `gh-pages` branch (if configured in the site settings on GitHub):

```bash
# navigate into the build output directory
cd docs/.vuepress/dist

git init
git add -A
git commit -m 'deploy'

# if you are deploying to https://<USERNAME>.github.io/<REPO>
git push -f https://${GITHUB_TOKEN}@github.com/kgrid/guides.git master:gh-pages
```
