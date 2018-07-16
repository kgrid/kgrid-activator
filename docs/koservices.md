---
layout: page
navtitle: Accessing KO Services/Endpoints 
---
## Accessing KO Services/Endpoints in the Activator

### Service Description
Each Knowledge Object (KO) is required to have service description which is a definition of what a 
services a KO provides and how it is accessed and used. The services provided are descripbed using the
the OpenAPI Specification. This is a specification for machine-readable interface files for describing, 
producing, consuming, and visualizing RESTful web services.  The Activator uses the OpenAPI document
 to load and activate the KO services. NOTE : currently we use the YAML representation of a OpenAPI document

### Accessing Service Description
OpenAPI document for each KO can be found via the path described in the _service_ attribute of the 
KO's metadata data. For example the metadata for the hello/world KO points to a _servicedescriptor.ymal_ file.


```json
{
    "title":"Hello, World",
    "description":"description",
    "arkId":"ark:/hello/world",

    ...
    
    "service":"model/service/servicedescriptor.yaml"
}

```



Show how to start the activator to allow cross origin requests (open, or from a specific domain)

### Using the Service Description
Show how to use the kgrid-demos.github.io Swagger UI client with a local/remote activator
Swagger Inspector
### OpenAPI service description