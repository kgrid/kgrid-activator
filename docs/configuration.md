---
layout: page
navtitle: Configuration
---

## Configuration
There are several settings that you can control on the Activator.

**Activator Knowledge Object Shelf Location**

By default the activator will look for a _shelf_ in jar execution directory but the location the _shelf_ can be configured:

```bash
java -jar kgrid-activator-0.6.2.jar --kgrid.shelf.cdostore.url=filesystem:file:///data/myshelf
```

**Activator Cross-Origin Resource Sharing (CORS)**
The Activator by default allows all origins access to the api. You can tighten that access via the
cors.url parameter.

To change the origins allowed:

```java -jar kgrid-activator-0.6.2.jar --cors.url=https://myservice.com```


**Activator Server Port** 

To change the port:

```java -jar kgrid-activator-0.6.2.jar --server.port=9090```


**Activator Server Path** 

By default the endpoints of the activator at the root of the activator server.  To change the server root path:

```java -jar kgrid-activator-0.6.2.jar --server.contextPath=/activator```

