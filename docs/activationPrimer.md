# KGrid Common Activation Specifications

## Introduction

The Activator is a reference implementation of the **activation** spec. This specification focuses on client interactions and integration with various client systems. See runtime adapter development guide for more on how an Activator manages and interacts with runtimes.

## Responsibilities of the Activator (...)

- provide access (HTTP Restful API) to the services (exposed as endpoints) from knowledge objects deployed to the Activator
- Provide status for associated runtimes, KOs deployed, and the Activator itself
- Allow KOs to be loaded/activated from local and remote source (in suitable runtimes)
- Manage and interface with suitable runtimes — embedded, native, remote (proxy)
- Enforce some aspects of trust, provenance, and reproducibility via policy, validation, and tracing/logging.


## Purpose

Systems are being designed and built to help move biomedical knowledge into practice more quickly, more broadly, more accurately than ever before. Those systems will rely on long-term access and management of compound digital objects within digital repositories, and deployment and integration of these objects as services in a broad class of runtimes (See KGRID Activation Spec). This requires infrastructural standards and specifications that enable integration at scale.

This spec represents our recommendations on how to meet the following needs:

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

The KO micro-API <conform>should</conform> be completely specified by the OpenAPI 3 service description. Clients rely on the service to use the API.

- Each KO endpoint <conform>must</conform> accept inputs as spec'ed by the service description for a specific mime-type.
- KO endpoints <conform>may</conform> do their own validation. They <conform>should</conform> not rely entirely on <proposed>(proposed)</proposed> Activator validation
- Each KO endpoint <conform>must</conform> produce outputs as spec'ed by the service description for a specific mime-type. An output schema <conform>should</conform> be specificied. If not clients will have to handle outputs of arbitrary complexity.
- If the knowledge object can't service the request it <conform>should</conform> use a well-defined scheme for responses (codes, error messages, etc.) The activator wraps KO error responses unchanged.
- Individual KOs and endpoints <conform>must</conform> be stateless.
- KOs <conform>may</conform> specify and use any properties in the deployment description depending on the particualr runtime.
- Endpoint paths <conform>may</conform> be arbitrarily complex within the KO but <conform>must</conform> be unique.
- Each KO <conform>may</conform> have multiple endpoints as long as the full path is unique.


#### Archival Resource Key (ARK):
The Knowledge Grid currently uses [ARK](https://n2t.net/e/ark_ids.html) identifiers natively which interoperate with [EZID](http://ezid.cdlib.org) and top-level resolvers like [Name2Thing](http://www.n2t.net) and [Identifiers.org](http://www.identifiers.org). (Support for other identifiers like [DOI](http://www.doi.org)s is planned).

#### Manifest:
A representation of a collection knowledge object resources. The *minimal* representation is an array of KO metadata JSON-LD objects with an `@id` property.

#### Activation:
THe process of deploying an implementation of a KO into a suitable runtime enviromnment in order to make the service endpoints (described in the service description available in the Activator API for use by client applications.
##### On every activation
- When a KO is activated, a warning is logged for every endpoint that cannot be activated
- Compliant implementations <conform>must not</conform> allow multiple endpoints and <conform>must not</conform> have endpoint  unspecified request routing behavior.
- Each activated endpoint is provided with a context object containing global activation properties and a means to resolve and access other endpoints in the same runtime or activator.
- If two endpoints have identical coordinates (`/{naan}/{name}/{apiVersion}/{endpoint}`) the first endpoint will be overwritten, and a warning will be logged.


#### Runtime

An environment capable of running the code for a particular class of KOs. Runtimes <conform>may</conform> be embbeded in an (custom) Adapter, or <conform>may</conform> communicate with the Activator through a (custom) Native or Proxy Adapter. THe refence imlementation of the Activator ships with an embedded Javascrip-runtime Adapter; also see [Activator/Runtime deployment guide]()

#### Adapter

Adapters allow particular Runtimes to interact with the Activator in order to deploy and run the code from a KO. An Adapter <conform>may</conform> handle initialization and registration of a runtime, provide access to the overall activation context and active endpoints, route client requests to [KO Endpoints](), and shutdown. Adapters implement the Adapter interface. See [Developing Runtimes]() and the documentation for your specific Runtime/Adapter combination.




##Notes
If a KO is changed, the activator will have to be restarted to see the
changes take effect.