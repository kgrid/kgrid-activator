# Configuration
There are a few environment variables that can be set to control different aspects of the activator. They can be set as environment variables, or passed into the terminal command while running the activator.
## Current Configuration
### `cors.url`
- The Activator by default allows all origins access to the api.
  - Default value: none
  - Command line:
    ```bash
    java -jar kgrid-activator-#.#.#.jar --cors.url=https://myservice.com
    ```
  - Environment Variable:
    ```bash
    export CORS_URL=https://myservice.com
    ```

### `kgrid.shelf.cdostore.url`
- Specify the path to a custom shelf directory, which can be preloaded with KOs. Can be an absolute or relative path.
  - Default value: `shelf` (in current working directory)
  - Command line (absolute path):
    ```bash
    java -jar kgrid-activator-#.#.#.jar --kgrid.shelf.cdostore.url=filesystem:file:///data/myshelf
    ```
  - relative path:
    ```bash
    java -jar kgrid-activator-#.#.#.jar --kgrid.shelf.cdostore.url=filesystem:file:///c:/Users/me/myshelf
    ```
  - environment variable:
    ```bash
    export KGRID_SHELF_MANIFEST=filesystem:file:///data/myshelf
    ```
### `kgrid.shelf.manifest` 
- Specify the path to a json file that contains a list of references to KOs that will be loaded on startup. Existing KOs in the shelf directory will be overwritten if they are contained in the manifest. This can be set to a file path, or a URL.
  - Default Value: none
  - Command line (file path):
    ```bash
    java -jar kgrid-activator-#.#.#.jar --kgrid.shelf.manifest=filesystem:file:///c:/Users/me/myStuff/manifest.json
    ```
  - Command line (URL):
    ```bash
    java -jar kgrid-activator-#.#.#.jar --kgrid.shelf.manifest=https://github.com/kgrid-objects/example-collection/releases/download/4.1.1/manifest.json
    ```
  - Environment variable (URL):
    ```bash
    export KGRID_SHELF_MANIFEST=filesystem:file:///c:/Users/me/myStuff/manifest.json
    ```
### `server.port` 
- Specify a particular port on which the activator should start
    - Default value: `8080`
    - Command line:
    ```bash
    java -jar kgrid-activator-#.#.#.jar --server.port=9090
    ```
    - Environment Variable:
    ```bash
    export SERVER_PORT=9090
    ```
### `spring.profiles.active` 
- Sets the security profile, which requires a username and password to access certain endpoints. Setting to `dev` will put the activator in dev mode, with no security. 

  __Note: if not in dev mode, the username and password must be set.
  See the entries for `spring.security.user.name` and `spring.security.user.password`.__
  - Default value: Secured
  - Command line:
    ```bash
    java -jar kgrid-activator-#.#.#.jar --spring.profiles.active=dev
    ```
  - Environment Variable:
    ```bash
    export SPRING_PROFILES_ACTIVE=dev
    ```
- Endpoints secured:
  - `GET /actuator/health` (Secure endpoint shows only `UP` or `DOWN`)
  - `GET /actuator/info`
  - `GET /activate`
  - `POST /kos`
  - `POST /kos/manifest`
  - `POST /kos/manifest-list`
  - `PUT /kos/{naan}/{name}/{version}`
  - `DELETE /kos/{naan}/{name}/{version}`
### `spring.security.user.name` 
- Specify the admin username. Security is enabled by default, so if this property is not set, the admin features will be inaccessible.
  - Default value: none
  - Command line:
    ```bash
    java -jar kgrid-activator-#.#.#.jar --spring.security.user.name=AzureDiamond
    ```
  - Environment Variable:
    ```bash
    export SPRING_SECURITY_USER_NAME=AzureDiamond
    ```
### `spring.security.user.password` 
- Specify the admin password. Security is enabled by default, so if this property is not set, the admin features will be inaccessible.
  - Default value: none
  - Command line:
    ```bash
    java -jar kgrid-activator-#.#.#.jar --spring.security.password=hunter2
    ```
  - Environment Variable:
    ```bash
    export SPRING_SECURITY_PASSWORD=hunter2
    ```
### `server.contextPath`
- By default, the endpoints of the activator at the root of the activator server.  To change the server root path:
  - Default value: `/`
  - Command line:
    ```bash
    java -jar kgrid-activator-#.#.#.jar --server.contextPath=/activator  
    ```
  - Environment Variable:
    ```bash
    export SERVER_CONTEXTPATH=/activator
    ```
## Proposed Configuration
### `kgrid.activator.allowRuntimeImport`
- While running the Activator packaged KO (zip file) can be uploaded to the `/kos` endpoint to add a KO to the shelf
- While running the Activator a `manifest` (json or yaml) can be POSTed to the `/kos` endpoint to initiate loading of one or more KOs from an external path (See [Loading KOs onto the Shelf](https://kgrid.org/kgrid-shelf) in the Kgrid Shelf documentation)

### `kgrid.activator.autoActivateOnStartup`
- Toggle whether or not the activator tries to activate KOs on startup
- Currently, the activator will try to activate everything in the shelf directory, as well as everything in the manifest set to `kgrid.shelf.manifest`.
