
## Activator Quick Start

These instructions will get the Kgrid Activator running with sample set of Knowledge Objects.

## Overview
As a key component of Knowledge Grid, an activator allows knowledge objects to be executable against collected data.

For the information on the activator API and the usage of the activator, see [KGRID Activator API](docs/api.md)

### Prerequisites

For running the application you need:

- [Java 11 or higher](https://www.oracle.com/java/)

### Running the Activator


1. Create a new directory, named whatever you like
1. Create a directory named shelf in the working directory
1. Download kgrid-activator-#.#.#.jar from the [latest activator release](https://github.com/kgrid/kgrid-activator/releases/latest)
1. Place the kgrid-activator-#.#.#.jar into your working directory
1. Download js-simple-v1.0.zip from the [latest release of the Example Collection](https://github.com/kgrid-objects/example-collection/releases/latest) 
1. Place the js-simple-v1.0.zip into the activator/shelf directory and unzip. This will make the KO ready to load by the activator.


Directory structure should look similar to the following

```text  
 ├──  YourProjectDirectory   
 │    ├──  kgrid-activator-#.#.#.jar
 │    └──  shelf
 │         └── js  
 │             └── simple  
 │                 └── v1.0
 │                     ├── src
 │                     │  └── index.js
 │                     ├── deployment.yaml
 │                     ├── metadata.json
 │                     └── service.yaml
 └── 
```

The activator is an executable jar and can be run from the command line.  Open a terminal window and navigate to the directory where the jar and shelf are located.  

Type in the following to start the activator in dev mode: 

```bash
 java -jar kgrid-activator-#.#.#.jar --spring.profiles.active=dev
```

By default, the activator will run on port 8080. You can validate the activator is up and running using 
the [activator's health endpoint](http://localhost:8080/actuator/health) at [`http://localhost:8080/actuator/health`](http://localhost:8080/actuator/health).  The health of the Activator should display a status of **UP**.  

```yaml
{
 "status": "UP",
 "components": {
  "activationService": {
   "status": "UP",
   "details": {
    "kos": 1,
    "endpoints": 1,
    "activatedEndpoints": 1
   }
  },
  "org.kgrid.adapter.v8.JsV8Adapter": {
   "status": "UP",
   "details": {
    "engines": [
      "javascript"
    ]
   }
  },
  "shelf": {
   "status": "UP",
   "details": {
    "numberOfKOs": 1,
    "kgrid.shelf.cdostore.url": "file:///home/username/activator/shelf/"
   }
  }
 }
}
```

### Activation on startup
- On startup, the Activator loads Knowledge Objects onto a local "shelf", using a manifest of packaged KOs, or loads directly from the local "shelf".

- The Activator loads adapters and initializes any embedded runtimes. The proxy adapter is initialized and ready to register remote runtimes. Any additional adapters/runtimes are also loaded and initialized.

- When runtimes are initialized, or registered (for remote runtimes), the Activator attempts to activate KOs for each runtime. It logs a warning if an endpoint cannot be activated.

- You can see the KOs that were loaded onto the shelf at the shelf's [Get All KOs](http://localhost:8080/kos) endpoint (located at [`http://localhost:8080/kos`](http://localhost:8080/kos)). 
- You can also see the status of each endpoint the activator found at the activator's [Get All Endpoints](http://localhost:8080/endpoints) endpoint (located at [`http://localhost:8080/endpoints`](http://localhost:8080/endpoints)).

- See [Activation API](api.md#activation-api) for more info.

## Using the js-simple-v1.0 KO on the Activator 

The js-simple KO is a very simple KO with a Javascript based service that takes in a name and displays 
 a _Welcome to the Knowledge Grid_ message. 
 
First, lets look at [JS Simple's metadata](http://localhost:8080/kos/js/simple/v1.0) using the shelf endpoint [`http://localhost:8080/kos/js/simple/v1.0`](http://localhost:8080/kos/js/simple/v1.0).

The JS Simple KO has one service called _welcome_.  The welcome service expects you to pass it a json object containing the _name_ key.

For example: `{"name":"Fred Flintstone"}`.

### Using `curl` to try out a KO

```bash
curl -X POST -H "Content-Type:application/json"  \
    -d "{\"name\": \"Fred Flintstone\"}" \
     http://localhost:8080/js/simple/1.0/welcome

```

The JS Simple KO will return the following

```json
{
 "result": "Welcome to Knowledge Grid, Fred Flintstone",
 "info": {
  "ko": {
   "@id": "js/simple/v1.0",
   "@type": "koio:KnowledgeObject",
   "identifier": "ark:/js/simple/v1.0",
   "version": "v1.0",
   "title": "Hello world",
   "description": "An example of simple Knowledge Object",
   "keywords": [
    "Hello",
    "example"
   ],
   "hasServiceSpecification": "service.yaml",
   "hasDeploymentSpecification": "deployment.yaml",
   "hasPayload": "src/index.js",
   "@context": [
    "http://kgrid.org/koio/contexts/knowledgeobject.jsonld"
   ]
  },
  "inputs": "{\"name\": \"Fred Flintstone\"}"
 }
}
```
### Using the Swagger Editor to try out a KO

Navigate to [`http://localhost:8080/endpoints/js/simple/1.0/welcome`](http://localhost:8080/endpoints/js/simple/1.0/welcome) and click the swagger editor link:

"swaggerLink": [https://editor.swagger.io?url=...](https://editor.swagger.io?url=https://localhost:8080/kos/js/simple/v1.0/service.yaml)

The online Swagger Editor should load the OpenAPI service description and generate a generic client for the JS Simple KO. Click the `POST` button for the `/welcome` endpoint and then the `Try it out` button to populate a sample request. The blue `Execute` bar will send the request to your local Activator. The results will be similar to the response above.

The Swagger Editor is a great tool for getting familiar with the "service" perspective for knowledge objects!

For more advanced topics, check out:
- [API Documentation](api.md)
- [Configuration Documentation](configuration.md)
- [Containerization Documentation](containers.md)
