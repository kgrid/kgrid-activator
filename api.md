# KGrid Common Activation Specifications

## Introduction

The Activator is a reference implementation of the **activation** spec. This specification focuses on client interactions and integration with with various client systems. See runtime adapter development guide for more on how an Activator manages and interacts with runtimes.

## Responsibilities of the Activator (...)

- provide access (HTTP Restful API) to the services (exposed as endpoints) from knowledge objects deployed to the Activator
- Provide status for associated runtimes, KOs deployed, and the Activator itself
- Allow KOs to be loaded/activated from local and remote source (in suitable runtimes)
- Manage and interface with suitable runtimes — embedded, native, remote (proxy)
- Enforce some aspects of trust, provenance, and reproducibility via policy, validation, and tracing/logging.


## Purpose

Systems are being designed and built to help move biomedical knowledge into practice more quickly, more broadly, more accurately than ever before. Those systems will rely on long-term access and management of compound digital objects within digital repositories, and deployment and integration of these objects as services in a broad class of runtimes (See KGRID Activation Spec). This requires infrastructural standards and specifications that enable integration at scale.

This spec represents our recommendations on how to meet the following needs:

### ...

## Status of this document

This document is draft of a potential specification. It has no official standing of any kind and does not represent the support or consensus of any standards organisation.

# 1. Conformance

As well as sections marked as non-normative, all authoring guidelines, diagrams, examples, and notes in this specification are non-normative. Everything else in this specification is normative.

The key words <conform>may</conform>, <conform>must</conform>, <conform>must not</conform>, <conform>should</conform>, and <conform>should not</conform> are to be interpreted as described in [RFC2119].

# 2. Terminology

See [KOIO] for matching ontology terms....

#### Knowledge Object (KO):
A collection of metadata and binary files that together have a unique identifier (including version identifier). There are three required file types: a service description, a deployment description, and one or more payload files implementing the service described by the service description, deployable using the information in the deployment description. See [Activation Spec] for details of the roles of the these files.
> KOIO term — koio:KnowledgeObject

#### Shelf:

The Knowledge Grid provides repository capabilities using a abstraction called the "Shelf." KOs are added to and managed for storage and retrieval through the Shelf API. Different concrete implementations of physical storage for KOs are available and not all capabilities are implemented in every component. See [Shelf API](shelf-api.md).

#### KO Endpoint:
A service path exposed by a particular knowledge object. Takes the form `/naan/name/endpoint`.

#### Archival Resource Key (ARK):
The Knowledge Grid currently uses [ARK](https://n2t.net/e/ark_ids.html) identifiers natively which interoperate with [EZID](http://ezid.cdlib.org) and top-level resolvers like [Name2Thing](http://www.n2t.net) and [Identifiers.org](http://www.identifiers.org). (Support for other identifiers like [DOI](http://www.doi.org)s is planned).

#### Manifest:
A representation of a collection knowledge object resources. The *minimal* representation is an array of KO metadata JSON-LD objects with an `@id` property.

#### Activation:
THe process of deploying an implementation of a KO into a suitable runtime enviromnment in order to make the service endpoints (described in the service description available in the Activator API for use by client applications.

#### Runtime

An environment capable of running the code for a particular class of KOs. Runtimes <conform>may</conform> be embbeded in an (custom) Adapter, or <conform>may</conform> communicate with the Activator through a (custom) Native or Proxy Adapter. THe refence imlementation of the Activator ships with an embedded Javascrip-runtime Adapter; also see [Activator/Runtime deployment guide]()

#### Adapter

Adapters allow particular Runtimes to interact with the Activator in order to deploy and run the code from a KO. An Adapter <conform>may</conform> handle initialization and registration of a runtime, provide access to the overall activation context and active endpoints, route client requests to [KO Endpoints](), and shutdown. Adapters implement the Adapter interface. See [Developing Runtimes]() and the documentation for your specific Runtime/Adapter combination.

# 3. APIs
## Request API

The Request API exposes the *micro*-API for the services provide by each KO.

##### Service Description

The OpenAPI 3 service description returned describes all the endpoints implemented by this KO.

##### Request (for each endpoint)
```
POST /{naan}/{name}/{endpoint} HTTP/1.1
POST /{naan}/{name}/{apiVersion}/{endpoint} HTTP/1.1
POST /{naan}/{name}/{endpoint}?v={apiVersion} HTTP/1.1

Accept: application/json
Content-type: application/json

{"age":48,"gender":"Female","risk":"low","sbp":120,"cholesterol":8,"smoker":false}
```
`Accept:` and `Content-type:` headers are required, and <conform>should</conform> be `application/json`

<proposed>(proposed)</proposed> Allow any mime-type specified in service description.

If an api version is not supplied, the default version of the endpoint will be used. (The strategy of determining the default version TBD)

##### Response:
The response is wrapped in a JSON object. The actual KO result is available under the `result:` key.

```
HTTP/1.1 200 OK
Content-type: application/json
{
  "result": {
    "cvdrisk": {
      "total": 0.0026555542778455843,
      "chd": 0.0017632437883150498,
      "nonchd": 8.923104895305345E-4
    }
  },
  "info": {
    "ko": {
      "@id": "score-calc",
      "@type": "koio:KnowledgeObject",
      "identifier": "ark:/score/calc",
      "version": "v0.3.0",
      "title": "Ten-year Fatal Cardiovascular Risk Calculation based on SCORE project (Example KO - Bundled with a npm package) ",
      "description": "Estimating ten-year risk of fatal cardiovascular disease based on SCORE project. This version bundled the javascript code with externalized data files and a Node.js library of 'lodash'",
      "keywords": [
        "SCORE, ten-year",
        "Cardiovascular risk",
        "calculation"
      ],
      "hasServiceSpecification": "service.yaml",
      "hasDeploymentSpecification": "service.yaml",
      "hasPayload": "dist/main.js",
      "@context": [
        "http://kgrid.org/koio/contexts/knowledgeobject.jsonld"
      ]
    },
    "inputs": {
      "age": 48,
      "gender": "Female",
      "risk": "low",
      "sbp": 120,
      "cholesterol": 8,
      "smoker": false
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
  "Title": "Error",
  "Time": "Fri Jun 19 16:21:23 UTC 2020",
  "Detail": "JSON parse error: Unexpected character ('{' (code 123)): was..."
}
```

<proposed>(proposed)</proposed> Use [Problem Details for HTTP APIs — rfc7807](https://tools.ietf.org/html/rfc7807) and wrap any underlying KO response problem details.

##### KO endpoint micro-APIs
The KO micro-API <conform>should</conform> be completely specified by the OpenAPI 3 service description. Clients rely on the service to use the API.

- Each KO endpoint <conform>must</conform> accept inputs as spec'ed by the service description for a specific mime-type.
- KO endpoints <conform>may</conform> do their own validation. They <conform>should</conform> not rely entirely on <proposed>(proposed)</proposed> Activator validation
- Each KO endpoint <conform>must</conform> produce outputs as spec'ed by the service description for a specific mime-type. An output schema <conform>should</conform> be specificied. If not clients will have to handle outputs of arbitrary complexity.
- If the knowledge object can't service the request it <conform>should</conform> use a well-defined scheme for responses (codes, error messages, etc.) The activator wraps KO error responses unchanged.
- Individual KOs and endpoints <conform>must</conform> be stateless.
- KOs <conform>may</conform> specifiy and use any properties in the deployment description depending on the particualr runtime.
- Endpoint paths <conform>may</conform> be arbitrarly complex within the KO but <conform>must</conform> be unique.
- Each KO <conform>may</conform> have multiple endpoints as long as the full path is unique.


## Activation API

##### Endpoint resources
`/endpoints`

`/endpoints/{naan}/{name}`

`/endpoints/{naan}/{name}/{endpoint}`


### Activation on startup

##### Loading the shelf

- If `kgrid.shelf.manifest` is set, the activator (shelf) will try to populate the shelf from the specified manifest(s).
- Existing KOs on the shelf will not be deleted and <conform>may</conform> be overwritten.

##### <proposed>(proposed)</proposed> If `kgrid.activator.allowRuntimeImport` is `true`
- While running the Activator packaged KO (zip file) can be uploaded to the `/kos` endpoint to add a KO to the shelf
- While running the Activator a `manifest` (json or yaml) can be POSTed to the `/kos` endpoint to initiate loading of one or more KOs from an external path (See [Loading KOs onto the Shelf]() in the Kgrid Shelf documentation))

> As KOs are added to the shelf, a warning is logged for each KO that is unreadable or malformed (e.g. missing `metadata.json` or deployment description)

##### <proposed>(proposed)</proposed> If `kgrid.activator.autoActivateOnStartup` is `true`
> Currently behaves as if `kgrid.activator.autoActivateOnStartup` is `true` by default

- On startup the Activator attempts to activated every KO on the shelf

> Once a KO has been activated, any activated endpoints will remain functional even if the KO is deleted, unless or until the activation state is refreshed (using `/refresh` or `/refresh/{naan}/{name}`). Likewise new KOs added to the shelf will *NOT* be activated unless or until the activation state is refreshed (using `/refresh` or `/refresh/{naan}/{name}`).

##### Runtime activation via `/refresh`
- `/refresh` — all current activations are discarded and the startup activation sequencce tries to activate every KO on the shelf
- `/refresh/{naan}/{name}` — all current activations for the default version given KO are discarded and the KO is reactivated
- `/refresh/{naan}/{name}/{version}` — all current activations for the given version KO are discarded and the KO is reactivated

##### On every activation
- When a KO is activated, a warning is logged for every endpoint that cannot be activated
- (verify) If two endpoints have identical coordinates (`/{naan}/{name}/{version}/{endpoint}`) the second endpoint will not be loaded and a warning will be logged. Compliant implementations <conform>must not</conform> allow multiple endpoints and <conform>must not</conform> have endpoint  unspecified request routing behavior.
- Each activated endpoint is provided with a context object containing global activation properties and a means to resolve and access other endpoints in the same runtime or activator.


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
Activators <conform>should</conform> provide application and health information via `/health` and `/info` endpoints.

### `/health` (Required)

The health enpoint <conform>must</conform> provide a `{ "status": "<UP|DOWN>" } response at a minimum. The `/health` endpoint <conform>should</conform>  indicate the status of individual components (adapters, shelf cdo store, runtimes, KOs and their activation status) to aid montioring in troubleshooting Activator deployments.

We suggest following the conventions in the [Spring Boot production health information guidelines](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-health).

Health information <conform>should</conform> focus on details that help understand wy the Activator or a component is `up` or `down`. Extended information <conform>may</conform> be made available under an `/info` endpoint. See `/info` below.

```
GET /health HTTP/1.1
Accept: application/json
```
An `UP` response returns an overall status and a map of indivdual componet statuses:

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
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500068036608,
        "free": 336108306432,
        "threshold": 10485760,
        "exists": true
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
An Activator implementation <conform>may</conform> use additional statuses as needed which can be documented for deployers, etc.

### `/info` (Optional)

Additional or extended information about the opertating characteristics of the Activator can be made available under an `/info` endpoint. See [Spring Boot application health information guidelines](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-application-info) for examples of suitable patterns which can be implemented in many different frameworks and languages.

We suggest that implementations provide things like a list of adapters and runtimes currently deployed, as well as counts or lists of KOs available to the Activator, endpoints activated, perhaps total requests, uptime, build information, key environemnt variables, etc.

Be careful not to expose sensitive information. The `/health` and `/info` endpoints <conform>should</conform> be secured (e.g. with OAuth 2.0 Bearer Tokens).

The `/info` endpoint <conform>should</conform> return a map of information sets in JSON or YAML.


##Notes
If a KO is changed, the activator will have to be restarted to see the
changes take effect.