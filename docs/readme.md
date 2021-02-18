
## Activator Quick Start

These instructions will get the Kgrid Activator running with sample set of Knowledge Objects.

### Prerequisites

For running the application you need:

- [Java 11 or higher](https://www.oracle.com/java/)

### Running the Activator

Download the latest activator jar from GitHub [Latest Activator Release](https://github.com/kgrid/kgrid-activator/releases/latest).

1. Create a activator directory
1. Create a directory named shelf in the new activator directory
1. Download [kgrid-activator-#.#.#.jar](https://github.com/kgrid/kgrid-activator/releases/latest)
1. Place the kgrid-activator-#.#.#.jar into the activator directory
1. [Download js-simple-v1.0.zip](https://github.com/kgrid-objects/example-collection/releases/download/4.1.1/js-simple-v1.0.zip) from the [latest release of the Example Collection](https://github.com/kgrid-objects/example-collection/releases/latest) 
1. Place the js-simple-v1.0.zip into the activator/shelf directory and unzip. This will make the KO ready to load by the activator.


Directory structure should look similar to the following

```text  
 ├── activator   
 │  ├──  kgrid-activator-#.#.#.jar
 │  └──  shelf
 │     └── js  
 │        └── simple  
 │           └── v1.0
 │              ├── src
 │              │  └── index.js
 │              ├── deployment.yaml
 │              ├── metadata.json
 │              └── service.yaml
 └── 
```

The activator is an executable jar and can be run from the command line.  Open a terminal window and navigate to the directory where the jar and shelf are located.  

Type in the following: 

```bash
 java -jar kgrid-activator-#.#.#.jar 
```

By default, the activator will run on port 8080. You can validate the activator is up and running using 
the [activator's health endpoint](http://localhost:8080/actuator/health).  The health of the Activator should display a status of **UP**.  

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

## Using the js-simple-v1.0 KO on the Activator 

The js-simple KO is a very simple KO with a Javascript based service that takes in a name and displays 
 a _Welcome to the Knowledge Grid_ message. 
 
First, lets look at [JS Simple's metadata](http://localhost:8080/kos/js/simple/v1.0).

The JS Simple KO has one service called _welcome_.  The welcome service expects you to pass it a json object containing the _name_ key.

For example: `{"name":"Fred Flintstone"}`.  

The following is a curl POST to the JS Simple welcome endpoint.

```bash
curl -X POST -H "Content-Type:application/json"  \
    -d "{\"name\": \"Fred Flintstone\"}" \
     http://localhost:8080/js/simple/1.0/welcome

```

The JS Simple KO will return the following

```json
{
 "result" : "Welcome to Knowledge Grid, Fred Flintstone",
 "info" : {
  "ko" : {
   "@id" : "js/simple/v1.0",
   "@type" : "koio:KnowledgeObject",
   "identifier" : "ark:/js/simple/v1.0",
   "version" : "v1.0",
   "title" : "Hello world",
   "description" : "An example of simple Knowledge Object",
   "keywords" : [ "Hello", "example" ],
   "hasServiceSpecification" : "service.yaml",
   "hasDeploymentSpecification" : "deployment.yaml",
   "hasPayload" : "src/index.js",
   "@context" : [ "http://kgrid.org/koio/contexts/knowledgeobject.jsonld" ]
  },
  "inputs" : "{\"name\": \"Fred Flintstone\"}"
 }

```

