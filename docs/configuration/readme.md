

## Configuration
There are several settings that you can control on the Activator.

**Activator Knowledge Object Shelf Location**

By default, the activator will look for a _shelf_ in jar execution directory but the location the _shelf_ can be configured:

```bash
java -jar kgrid-activator-0.6.2.jar --kgrid.shelf.cdostore.url=filesystem:file:///data/myshelf

java -jar kgrid-activator-0.6.2.jar --kgrid.shelf.cdostore.url=filesystem:file:///c:/Users/me/myshelf
```

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

- On startup, the Activator attempts to activate every KO on the shelf

> Once a KO has been activated, any activated endpoints will remain functional even if the KO is deleted, unless or until the activation state is refreshed (using `/refresh` or `/refresh/{naan}/{name}`). Likewise new KOs added to the shelf will *NOT* be activated unless or until the activation state is refreshed (using `/refresh` or `/refresh/{naan}/{name}`).

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

## Other Configuration
There are a few environment variables that can be set to control different aspects of the activator.
- `server.port` - Specify a particular port on which the activator should start
- `spring.profiles.active` - Set the security profile. Security is enabled by default, to disable, set this property to `dev`.
- `spring.security.user.name` - Specify the admin username. Security is enabled by default, so if this property is not set, the admin features will be inaccessible.
- `spring.security.user.password` - Specify the admin password. Security is enabled by default, so if this property is not set, the admin features will be inaccessible.
