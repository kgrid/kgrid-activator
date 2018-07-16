---
layout: page
navtitle: Configuration
---

## Configuration

**Activator Knowledge Object Shelf Location**

By default the activator will look for a _shelf_ in jar execution directory but the location the _shelf_ can be configured:

```bash
java -jar kgrid-activator-0.6.2.jar --kgrid.shelf.cdostore.url=filesystem:file:///data/myshelf
```

**Activator Server Port** 

To change the port:

```java -jar kgrid-activator-0.6.2.jar --server.port=9090```

Now access health

```curl http://localhost:9090/health```


**Activator Server Path** 

By default the endpoints of the activator at the root of the activator server.  To change the server root path:

```java -jar kgrid-activator-0.6.2.jar --server.contextPath=/activator```

Now access health

```curl http://localhost:8080/activator/health```