::: tip Who is this document for?
For service providers, adminstrators, and developers installing the Knowledge Grid as part of a health care system integration use case. 
:::


## Intro

The Knowledge Grid (KGrid) Activator is an infrastructrual service component packaged as a single jar file with an embedded Tomcat or Jetty server. The Activator does three things:

* It manages embedded runtime environments (e.g. Javascript V8 engine, Graal Python) and/or proxies access to external runtimes (e.g. Node.js, Python 3.7) which implement suitable KGrid interfaces. (See #ProxyAdapter)

* It loads and registers ("activates") stateless functions (Knowledge Objects; KOs) using appropriate runtime environments, typically at startup.

* It acts as a gateway (_Request API_) to route incoming requests to the “nano” services exposed by a particular KOs. 

* It exposes an _Administrative API_ (lists of KOs & endpoints, runtime engine status, health, etc.). 

* It provides an _Import/Export API_ (mainly to support test and development)


::: tip What are Knowledge Objects 
KOs are packaged implementations of computable biomedical knowledge (CBK). Each KO provides one or more small, focused REST API endpoints, described by an OpenAPI service document, a metadata document, and a deployment descriptor for a particular runtime. (See Knowledge Object Common Packaging SPecification [KOCP])
:::

::: tip Additional Activator capabilities
The Activator gateway may also perform cross-cutting opperations like validation on inputs and outputs, handling error conditions, authentication for administration or request APIS, applying policies, logging, etc.
:::

 

## Deploying the Activator

The Activator is a Sring Boot-based web application packaged as a single jar file. It can be run directly, setup up as a service, or packaged and managed as containers (Docker/OCI). As a simple, lightweight, stateless component it can also be scaled and managed using typical orchestration solutions. 

All runtime properties are available as environment properties (via the Spring Boot property abstraction; see Externalized Configuration). All Spring Boot properties are available (e.g. server.port) and application-specific properties are namespaced under `kgrid.*` or `kgrid.activator.*`.
