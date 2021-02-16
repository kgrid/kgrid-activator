#Activator APIs
## Request API

The Request API exposes the *micro*-API for the services provide by each KO.

##### Service Description

The OpenAPI 3 service description returned describes all the endpoints implemented by this KO.

##### Request (for each endpoint)
```
POST /{naan}/{name}/{apiVersion}/{endpoint} HTTP/1.1
POST /{naan}/{name}/{endpoint}?v={apiVersion} HTTP/1.1
POST /{naan}/{name}/{endpoint} HTTP/1.1 [Will use the default version (see below)]

Headers
Accept: not required, if provided must match the OpenAPI document for the endpoint.
Content-type: required, and must be one of the types in the OpenAPI document for the endpoint.

Request Body: {"name":"Mario"}
```

##### Response:
The response is wrapped in a JSON object. The actual KO result is available under the `result:` key.

```
{
  "result" : "Welcome to Knowledge Grid, Mario",
  "info" : {
    "ko" : {
      "@id" : "js/simple/v1.0",
      "@type" : "koio:KnowledgeObject",
      "identifier" : "ark:/js/simple/v1.0",
      "version" : "v1.0",
      "title" : "Hello world",
    ...
    }
  }
}
```

<proposed>(proposed)</proposed> Return KO output and link to provenance, tracing, etc.
Response code: HTTP/1.1 200

##### Errors:
A custom problem details resource is returned and the KO problem details or exception message will be included in the `Detail:` property.

```
{
  "Status": "500 Internal Server Error",
  "Instance": "uri=/score/calc/v0.3.0/score",
  "Title": "General Adapter Exception",
  "Time": "Fri Jun 19 16:21:23 UTC 2020",
  "Detail": "JSON parse error: Unexpected character ('{' (code 123)): was..."
}
```

##### Default Version:
If the endpoint version is not supplied in the above request, this particular activator has a concept of 'default version', where it will use the first endpoint it finds that matches the naan, name, and endpoint name. This is an implementation detail, and is not enforced.
<proposed>(proposed)</proposed> Use [Problem Details for HTTP APIs — rfc7807](https://tools.ietf.org/html/rfc7807) and wrap any underlying KO response problem details.

## Activation API

##### Endpoint resources
```
GET /endpoints
GET /endpoints/{engine}
GET /endpoints/{naan}/{name} (proposed)
GET /endpoints/{naan}/{name}/{endpoint}
GET /endpoints/{naan}/{name}/{endpoint}?v={apiVersion}
GET /endpoints/{naan}/{name}/{apiVersion}/{endpointName}
```
##### Responses:
All of these endpoints return an array containing a list of Endpoints, except for `GET /endpoints/{naan}/{name}/{apiVersion}/{endpointName}`, which just returns the endpoint that was asked for.
```
[
	{
		"title": "Hello world",
		"swaggerLink": "https://editor.swagger.io?url=http://localhost:8080/kos/js/simple/v1.0/service.yaml",
		"hasServiceSpecification": "/kos/js/simple/v1.0/service.yaml",
		"activated": "2021-02-16T11:40:46.763569",
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

##### Errors:
A custom problem details resource is returned and the KO problem details or exception message will be included in the `Detail:` property.

```
{
	"Status": "400 Bad Request",
	"Instance": "uri=/endpoints/js/simple/welcome",
	"Title": "Error",
	"Time": "Tue Feb 16 15:21:50 EST 2021",
	"Detail": "Cannot find endpoint with id js/simple/nonExistentVersion/welcome"
}
```
### Activation on startup
- On startup, the Activator attempts to activate every KO on the shelf

> Once a KO has been activated, any activated endpoints will remain functional even if the KO is deleted, unless or until the activation state is refreshed (using `/refresh` or `/refresh/{naan}/{name}`). Likewise new KOs added to the shelf will *NOT* be activated unless or until the activation state is refreshed (using `/refresh` or `/refresh/{naan}/{name}`).

##### Loading Knowledge Objects and Activating Endpoints
`GET /refresh` — all endpoints (activated or not) are discarded, and the activator reactivates everything on the shelf. If two endpoints have identical coordinates (`/{naan}/{name}/{apiVersion}/{endpoint}`) the first endpoint will be overwritten, and a warning will be logged.

`GET /refresh/{engine}` — all endpoints for a particular runtime (activated or not) are discarded, and the activator reactivates everything on the shelf.

`GET /reload/{naan}/{name}/?v={version}` — This will discard the endpoints for the specified KO and reload it from the shelf

`GET /reload/{naan}/{name}/{version}` — This will discard the endpoints for the specified KO and reload it from the shelf

<proposed>(proposed)</proposed> In the future, this functionality will be behind the `actuator` namespace.


## Shelf API (read-only)

An Activator <conform>must</conform> implement at least the read-only portion of the [Shelf API](shelf-api.md). This includes listing KOs, returning representations of indivdual KOs (e.g. metadata only, with links to key components).

<proposed>(proposed)</proposed> An activator <conform>must</conform> provide a resource representation for the knowledge object that includes activation status and endpoints.

<proposed>(proposed)</proposed> An Activator <conform>should</conform> make available the service description for a knowledge object.

<proposed>(proposed)</proposed> Access to the internals of the KO (deployment and payload files) <conform>should not</conform> be exposed, except that KOs of "resource" type <conform>may</conform> specify particular payloads for client access under appropriate service paths in the service description.

<proposed>(proposed)</proposed> An activator <conform>may</conform> implement additional operations for the Shelf API to support development or demonstration of KOs. For example, uploading or importing KOs, loading KOs from a manifest file, deleting KOs. See [Shelf API](shelf-api.md) for more info.

<proposed>(proposed)</proposed> It is especially useful for Activators used in protyping and demonstration to allow a manfifest to be posted to the activator in prder to load KOs at runtime. This <conform>should not</conform> be enabled by default except in development environments as it representas an vulnberabiltiy.

### List KO resources
```
GET /kos HTTP/1.1
Accept: application/json
```
Returns a list of knowledge object resources in a minimal representation.

<proposed>(proposed)</proposed> Returns a KO manifest

```
HTTP/1.1 200
Content-Type: application/json
[{
"@id": "ipp-lowercholesterol",
"@type": "koio:KnowledgeObject",
...
},
{
"@id": "99999-fk4md04x9z",
"@type": "koio:KnowledgeObject",
...
}]
```
### Get an individual KO resource

```
GET /kos/{naan}/{name} HTTP/1.1
Accept: application/json
```
### Get a versioned individual KO resource
```
GET /kos/{naan}/{name}/{version} HTTP/1.1
Accept: application/json
```
Returns a single knowledge object resource in a minimal representation. If a version is supplied that version is returned. If no version is supplied and multiple versions of the object exist in the Activator the current default version for requests is returned (the version will be available in the returned metadata representation). See [#Default KO versions]() for more info.

```
HTTP/1.1 200
Content-Type: application/json
[{
"@id": "ipp-lowercholesterol",
"@type": "koio:KnowledgeObject",
...
},
{
"@id": "99999-fk4md04x9z",
"@type": "koio:KnowledgeObject",
...
}]
```

## Application and Health Information
Activators <conform>should</conform> provide application and health information via `/actuator/health` 
and `/actuator/info` endpoints.

### `/actuator/health` (Required)

The health endpoint <conform>must</conform> provide a `{ "status": "<UP|DOWN>" } response at a minimum. 
The `/actuator/health` endpoint <conform>should</conform>  indicate the status of individual components 
(adapters, shelf cdo store, runtimes, KOs and their activation status) to aid montioring in troubleshooting 
Activator deployments.

We suggest following the conventions in the [Spring Boot production health information guidelines](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-health).

Health information <conform>should</conform> focus on details that help understand why the Activator or 
a component is `up` or `down`. Extended information <conform>may</conform> be made available under an 
`/actuator/info` endpoint. See `/actuator/info` below.

By default, the actuator health indicator only exposes the top level status: `status: UP` unless the user is logged in, or the `dev` spring profile environment variable is active. `SPRING_PROFILES_ACTIVE=DEV`
```
GET /actuator/health HTTP/1.1
Accept: application/json
```
An `UP` response returns an overall status and a map of individual component statuses:

```
HTTP/1.1 200
Content-type: application/json
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

A `DOWN` response returns:

```
HTTP/1.1 503
Content-type: application/json
{
  "status": "UP",
  "components": {
    "activationService": {
      "status": "UP"
    },{
      "org.kgrid.adapter.javascript.JavascriptAdapter": {
      "status": "DOWN"
    },
      ...
}
```
An Activator implementation <conform>may</conform> use additional statuses as needed which can be 
documented for deployers, etc.

### `/actuator/info` (Optional)

Additional or extended information about the opertating characteristics of the Activator can be made 
available under an `/actuator/info` endpoint. 
See [Spring Boot application health information guidelines](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-application-info)
for examples of suitable patterns which can be implemented in many frameworks and languages.

We suggest that implementations provide things like a list of adapters and runtimes currently deployed,
as well as counts or lists of KOs available to the Activator, endpoints activated, perhaps total 
requests, uptime, build information, key environment variables, etc.

Be careful not to expose sensitive information. The `/actuator/health` and `/actuator/info` endpoints 
<conform>should</conform> be secured 
(e.g. with OAuth 2.0 Bearer Tokens).

The `/actuator/info` endpoint <conform>should</conform> return a map of information sets in JSON or YAML.
