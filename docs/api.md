#Activator API
## Request API
The Request API exposes the *micro*-API for the services provided by each KO in the service description.

### `POST /{naan}/{name}/{apiVersion}/{endpoint}`
- Execute the payload of a particular version of an endpoint
- Headers
  ```
  Accept: not required, if provided must match the OpenAPI document for the endpoint.
  Content-type: required, and must be one of the types in the OpenAPI document for the endpoint.
  ```
- Request Body
  ```text
  {"name":"Mario"} (*This depends on the inputs for the endpoint payload)
  ```
- Curl Command
  ```bash
  curl --location --request POST 'http://localhost:8080/js/simple/1.0/welcome' \
  --header 'Content-Type: application/json' \
  --data-raw '{
  "name": "Mario"
  }'
  ```
- Responses
  - 200: Will return the result of execution wrapped in a json object along with debug info containing the metadata for the KO
    ```json
    {
  	  "result": "Welcome to Knowledge Grid, Mario",
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
  		  "inputs": "{\n\t\"name\": \"Mario\"\n}"
      }
    }
    ```
- Errors
    - 404:
      ```json
      {
          "Status": "404 Not Found",
          "Instance": "uri=/js/simple/1.0/missing",
          "Title": "Endpoint not found",
          "Time": "Wed Feb 24 16:18:26 EST 2021",
          "Detail": "No active endpoints found for js/simple/missing"
      }
      ```
    - 415: If the Content-Type header does not match what the KO has described in the service description
      ```json
      {
          "Status": "415 Unsupported Media Type",
          "Instance": "uri=/js/simple/1.0/welcome",
          "Title": "Unsupported Media Type",
          "Time": "Wed Feb 24 16:20:18 EST 2021",
          "Detail": "Endpoint js/simple/1.0/welcome does not support media type text/plain. Supported Content Types: [application/json]"
      }
      ```
    - 500: (Could depend on the adapter used for execution)
      ```json
      {
          "Status": "500 Internal Server Error",
          "Instance": "uri=/js/simple/1.0/welcome",
          "Title": "General Adapter Exception",
          "Time": "Wed Feb 24 16:23:16 EST 2021",
          "Detail": "Code execution error: SyntaxError: Unexpected token b in JSON at position 11"
      }
      ```
### `POST /{naan}/{name}/{endpoint}?v={apiVersion}`
- Execute the payload of a particular version of an endpoint using a query parameter

- Headers
  ```
  Accept: not required, if provided must match the OpenAPI document for the endpoint.
  Content-type: required, and must be one of the types in the OpenAPI document for the endpoint.
  ```
- Request Body
  ```text
  {"name":"Mario"} (*This depends on the inputs for the endpoint payload)
  ```
- Curl Command
  ```bash
  curl --location --request POST 'http://localhost:8080/js/simple/welcome?v=1.0' \
  --header 'Content-Type: application/json' \
  --data-raw '{
  "name": "Mario"
  }'
  ```
- Responses
  - 200: Will return the result of execution wrapped in a json object along with debug info containing the metadata for the KO.
    ```json
    {
  	  "result": "Welcome to Knowledge Grid, Mario",
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
  		  "inputs": "{\n\t\"name\": \"Mario\"\n}"
      }
    }
    ```
- Errors
  - 404:
    ```json
    {
        "Status": "404 Not Found",
        "Instance": "uri=/js/simple/1.0/missing",
        "Title": "Endpoint not found",
        "Time": "Wed Feb 24 16:18:26 EST 2021",
        "Detail": "No active endpoints found for js/simple/missing"
    }
    ```
  - 415: If the Content-Type header does not match what the KO has described in the service description
    ```json
    {
        "Status": "415 Unsupported Media Type",
        "Instance": "uri=/js/simple/1.0/welcome",
        "Title": "Unsupported Media Type",
        "Time": "Wed Feb 24 16:20:18 EST 2021",
        "Detail": "Endpoint js/simple/1.0/welcome does not support media type text/plain. Supported Content Types: [application/json]"
    }
    ```
  - 500: (Could depend on the adapter used for execution)
    ```json
    {
        "Status": "500 Internal Server Error",
        "Instance": "uri=/js/simple/1.0/welcome",
        "Title": "General Adapter Exception",
        "Time": "Wed Feb 24 16:23:16 EST 2021",
        "Detail": "Code execution error: SyntaxError: Unexpected token b in JSON at position 11"
    }
    ```
### `POST /{naan}/{name}/{endpoint}`
- Execute the payload of the default version of an endpoint
  - Default Version: If the endpoint version is not supplied in the above request, this particular activator has a concept of 'default version', where it will use the first endpoint it finds that matches the naan, name, and endpoint name. This is an implementation detail, and is not enforced.

- Headers
  ```
  Accept: not required, if provided must match the OpenAPI document for the endpoint.
  Content-type: required, and must be one of the types in the OpenAPI document for the endpoint.
  ```
- Request Body
  ```text
  {"name":"Mario"} (*This depends on the inputs for the endpoint payload)
  ```
- Curl Command
  ```bash
  curl --location --request POST 'http://localhost:8080/js/simple/welcome' \
  --header 'Content-Type: application/json' \
  --data-raw '{
  "name": "Mario"
  }'
  ```
- Responses
  - 200: Will return the result of execution wrapped in a json object along with debug info containing the metadata for the KO
    ```json
    {
  	  "result": "Welcome to Knowledge Grid, Mario",
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
  		  "inputs": "{\n\t\"name\": \"Mario\"\n}"
      }
    }
    ```
- Errors
  - 404:
    ```json
    {
        "Status": "404 Not Found",
        "Instance": "uri=/js/simple/1.0/missing",
        "Title": "Endpoint not found",
        "Time": "Wed Feb 24 16:18:26 EST 2021",
        "Detail": "No active endpoints found for js/simple/missing"
    }
    ```
  - 415: If the Content-Type header does not match what the KO has described in the service description
    ```json
    {
        "Status": "415 Unsupported Media Type",
        "Instance": "uri=/js/simple/1.0/welcome",
        "Title": "Unsupported Media Type",
        "Time": "Wed Feb 24 16:20:18 EST 2021",
        "Detail": "Endpoint js/simple/1.0/welcome does not support media type text/plain. Supported Content Types: [application/json]"
    }
    ```
  - 500: (Could depend on the adapter used for execution)
    ```json
    {
        "Status": "500 Internal Server Error",
        "Instance": "uri=/js/simple/1.0/welcome",
        "Title": "General Adapter Exception",
        "Time": "Wed Feb 24 16:23:16 EST 2021",
        "Detail": "Code execution error: SyntaxError: Unexpected token b in JSON at position 11"
    }
    ```
## Activation API
- The Activator's tools for loading, activating, and refreshing KOs hosted on the shelf.

### `GET /actuator/activation/refresh`
- All currently loaded endpoints (activated or not) are reactivated and replaced. 
- Curl Command
  ```bash
  curl --location --request GET 'http://localhost:8080/actuator/activation/refresh'
  ```
- Responses
  - 303: Redirects to `/endpoints`

### `GET /actuator/activation/refresh/{engine}`
- All currently loaded endpoints for a particular runtime (activated or not) are reactivated and replaced.
- Curl Command
  ```bash
  curl --location --request GET 'http://localhost:8080/actuator/activation/refresh/python'
  ```
- Responses
  - 303: Redirects to `/endpoints`

### `GET /actuator/activation/reload/{naan}/{name}/?v={version}`
- This will discard the endpoints for the specified KO, reload it from the shelf, and then activate its endpoints.
- Curl Command
  ```bash
  curl --location --request GET 'http://localhost:8080/actuator/activation/reload/js/simple/?v=v1.0'
  ```
- Responses
  - 303: Redirects to `/endpoints`

### `GET /actuator/activation/reload/{naan}/{name}/{version}`
- This will discard the endpoints for the specified KO, reload it from the shelf, and then activate its endpoints.
- Curl Command
  ```bash
  curl --location --request GET 'http://localhost:8080/actuator/activation/reload/js/simple/v1.0'
  ```
- Responses
  - 303: Redirects to `/endpoints`

  
## Endpoint Resource API
- The Activator's method of retrieving representations of endpoints as resources
### `GET /endpoints`
- Returns an array of all currently loaded endpoints, whether they are activated or not.
- Curl Command
  ```bash
  curl --location --request GET 'http://localhost:8080/endpoints'
  ```
- Responses
  - 200:
    ```json
    [
      {
  		     "title": "Hello world",
  		     "swaggerLink": "https://editor.swagger.io?url=http://localhost:8080/kos/js/simple/v1.0/service.yaml",
  		     "hasServiceSpecification": "/kos/js/simple/v1.0/service.yaml",
  		     "activated": "2021-02-25T16:30:44.804575",
  		     "status": "ACTIVATED",
  		     "engine": "javascript",
  		     "knowledgeObject": "/kos/js/simple/v1.0",
  		     "@id": "js/simple/1.0/welcome",
  		     "@context": [
  		     	"http://kgrid.org/koio/contexts/knowledgeobject.jsonld",
  		     	"http://kgrid.org/koio/contexts/implementation.jsonld"
  		     ]
      }
    ]
    ```
  
### `GET /endpoints/{engine}`
- Returns an array of all currently loaded endpoints for a particular runtime, whether they are activated or not.
- Curl Command
  ```bash
  curl --location --request GET 'http://localhost:8080/endpoints/node'
  ```
- Responses
  - 200:
    ```json
    [
      {
  		     "title": "Hello world",
  		     "swaggerLink": "https://editor.swagger.io?url=http://localhost:8080/kos/node/simple/v1.0/service.yaml",
  		     "hasServiceSpecification": "/kos/node/simple/v1.0/service.yaml",
  		     "activated": "2021-02-25T16:30:44.804575",
  		     "status": "ACTIVATED",
  		     "engine": "node",
  		     "knowledgeObject": "/kos/js/simple/v1.0",
  		     "@id": "node/simple/1.0/welcome",
  		     "@context": [
  		     	"http://kgrid.org/koio/contexts/knowledgeobject.jsonld",
  		     	"http://kgrid.org/koio/contexts/implementation.jsonld"
  		     ]
      }
    ]
    ```
### `GET /endpoints/{naan}/{name}/{endpoint}`
- Returns an array of json representations of every api version of a particular endpoint.
- Curl Command
  ```bash
  curl --location --request GET 'http://localhost:8080/endpoints/js/simple/welcome'
  ```
- Responses
  - 200:
    ```json
    [
      {
  		     "title": "Hello world",
  		     "swaggerLink": "https://editor.swagger.io?url=http://localhost:8080/kos/js/simple/v1.0/service.yaml",
  		     "hasServiceSpecification": "/kos/js/simple/v1.0/service.yaml",
  		     "activated": "2021-02-25T16:30:44.804575",
  		     "status": "ACTIVATED",
  		     "engine": "javascript",
  		     "knowledgeObject": "/kos/js/simple/v1.0",
  		     "@id": "js/simple/1.0/welcome",
  		     "@context": [
  		     	"http://kgrid.org/koio/contexts/knowledgeobject.jsonld",
  		     	"http://kgrid.org/koio/contexts/implementation.jsonld"
  		     ]
      },
      {
  		     "title": "Hello world",
  		     "swaggerLink": "https://editor.swagger.io?url=http://localhost:8080/kos/js/simple/v2/service.yaml",
  		     "hasServiceSpecification": "/kos/js/simple/v2/service.yaml",
  		     "activated": "2021-02-25T16:30:44.804575",
  		     "status": "ACTIVATED",
  		     "engine": "javascript",
  		     "knowledgeObject": "/kos/js/simple/v2",
  		     "@id": "js/simple/2.0.1/welcome",
  		     "@context": [
  		     	"http://kgrid.org/koio/contexts/knowledgeobject.jsonld",
  		     	"http://kgrid.org/koio/contexts/implementation.jsonld"
  		     ]
      }
    ]
    ```
### `GET /endpoints/{naan}/{name}/{endpoint}?v={apiVersion}`
- Returns an array of json representations of a particular api version of a particular endpoint.
- Curl Command
  ```bash
  curl --location --request GET 'http://localhost:8080/endpoints/js/simple/welcome?v=v1.0'
  ```
- Responses
  - 200:
    ```json
    [
      {
  		     "title": "Hello world",
  		     "swaggerLink": "https://editor.swagger.io?url=http://localhost:8080/kos/js/simple/v1.0/service.yaml",
  		     "hasServiceSpecification": "/kos/js/simple/v1.0/service.yaml",
  		     "activated": "2021-02-25T16:30:44.804575",
  		     "status": "ACTIVATED",
  		     "engine": "javascript",
  		     "knowledgeObject": "/kos/js/simple/v1.0",
  		     "@id": "js/simple/1.0/welcome",
  		     "@context": [
  		     	"http://kgrid.org/koio/contexts/knowledgeobject.jsonld",
  		     	"http://kgrid.org/koio/contexts/implementation.jsonld"
  		     ]
      }
    ]
    ```
### `GET /endpoints/{naan}/{name}/{apiVersion}/{endpointName}`
- Returns a json representation of a particular api version of a particular endpoint.
- Curl Command
  ```bash
  curl --location --request GET 'http://localhost:8080/endpoints/js/simple/v1.0/welcome'
  ```
- Responses
  - 200:
    ```json
    {
             "title": "Hello world",
             "swaggerLink": "https://editor.swagger.io?url=http://localhost:8080/kos/js/simple/v1.0/service.yaml",
             "hasServiceSpecification": "/kos/js/simple/v1.0/service.yaml",
             "activated": "2021-02-25T16:30:44.804575",
             "status": "ACTIVATED",
             "engine": "javascript",
             "knowledgeObject": "/kos/js/simple/v1.0",
             "@id": "js/simple/1.0/welcome",
             "@context": [
                "http://kgrid.org/koio/contexts/knowledgeobject.jsonld",
                "http://kgrid.org/koio/contexts/implementation.jsonld"
             ]
    }
    ```

## Health and Info API
Activators <conform>should</conform> provide application and health information via `/actuator/health` and `/actuator/info` endpoints.

### `GET /actuator/health` (Required)
- The Activator's portal into its health, as well as the status of its components and adapters
  - We suggest following the conventions in the [Spring Boot production health information guidelines](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-health).
  - The `/actuator/health` endpoint <conform>should</conform>  indicate the status of individual components (adapters, shelf cdo store, runtimes, KOs and their activation status) to aid monitoring in troubleshooting Activator deployments.
  - The health endpoint <conform>must</conform> provide a `{ "status": "<UP|DOWN>" } response at a minimum.
  - An Activator implementation <conform>may</conform> use additional statuses as needed which can be documented for deployers, etc.
  - Health information <conform>should</conform> focus on details that help understand why the Activator or a component is `up` or `down`.
  - By default, the actuator health indicator only exposes the top level status: `status: UP` unless the user is logged in, or the `dev` spring profile environment variable is active. `SPRING_PROFILES_ACTIVE=DEV`
- Curl Command
  ```bash
  curl --location --request GET 'http://localhost:8080/actuator/health'
  ```
- Responses
  - 200: (When activator and adapter are both UP)
    ```json
    {
      "status": "UP",
      "components": {
        "activationService": {
          "status": "UP",
          "details": {
            "kos": 0,
            "endpoints": 0
          }
        },
        "org.kgrid.adapter.javascript.JavascriptAdapter": {
          "status": "UP",
          "details": {
            "type": "JAVASCRIPT",
            "created": "2020-06-18T18:07:52.492425Z"
          }
        },
        ...
      }
    }
    ```
  - 200: (When an adapter is DOWN)
    ```json
    {
      "status": "UP",
      "components": {
        "activationService": {
          "status": "UP",
          "details": {
            "kos": 0,
            "endpoints": 0
          }
        },
        "org.kgrid.adapter.javascript.JavascriptAdapter": {
          "status": "DOWN"
        },
        ...
      }
    }
    ```
  - 200: (When Activator is DOWN)
    ```json
    {
      "status": "DOWN"
    }
    ```

### `GET /actuator/info` (Optional)
- An optional endpoint where extended information <conform>may</conform> be made available.
  - See [Spring Boot application health information guidelines](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-application-info)
for examples of suitable patterns which can be implemented in many frameworks and languages.
  - We suggest that implementations provide things like a list of adapters and runtimes currently deployed,
as well as counts or lists of KOs available to the Activator, endpoints activated, perhaps total
requests, uptime, build information, key environment variables, etc.
  - Be careful not to expose sensitive information. The `/actuator/health` and `/actuator/info` endpoints
<conform>should</conform> be secured
(e.g. with OAuth 2.0 Bearer Tokens).
  - The `/actuator/info` endpoint <conform>should</conform> return a map of information sets in JSON or YAML.

## Proposed API Changes
- Endpoint execution will return KO output and link to provenance, tracing, etc.
  `GET /endpoints/{naan}/{name}
