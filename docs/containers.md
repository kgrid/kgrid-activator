# KGrid Docker Containers
## [Pull from DockerHub](https://docs.docker.com/engine/reference/commandline/pull/)
  ```bash
  docker pull kgrid/kgrid-activator
  ```
## [Running the Image](https://docs.docker.com/engine/reference/commandline/run)

- Running in a container mapped to port 8080 (default port for the activator)
 
```bash
  docker run -p 8080:8080 --name activator kgrid/activator
```

- [Mapped to a local shelf](https://docs.docker.com/engine/reference/commandline/run/#mount-volume--v---read-only)

```bash
  docker run -p 8080:8080 -v ${PWD}/shelf:/applications/shelf --name activator -d kgrid/activator 
```

- Example:

```bash
  docker run -it --rm --network host -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev -v ${PWD}/shelf:/application/shelf --name activator kgrid/activator:latest
```
- This example has a few things going on:
    - `--network host` [Running with a network bridge](https://docs.docker.com/engine/reference/commandline/run/#connect-a-container-to-a-network---network) (if your containerized activator needs to talk to the network, i.e. you're running an external runtime in another container)
    - `-it --rm` Running interactive and Removing the Container when stopped. can be found in the [options](https://docs.docker.com/engine/reference/commandline/run/#options)
    - `-e` [Pass Environment Variables](https://docs.docker.com/engine/reference/commandline/run/#set-environment-variables--e---env---env-file)

- Once created, you can stop and start the container using `docker stop activator` and `docker start acivator`.

## Quick start with `docker-compose`

You can also start the activator in your environment by setting up `docker-compose.yaml` file, shown below as an example
```yaml
version: "3.6"

services:
  activator:
    container_name: lion-activator
    environment:
        KGRID_CONFIG: "--kgrid.shelf.cdostore.url=filesystem:file://shelf --cors.url=*  --management.info.git.mode=full"
    image: kgrid/activator:1.5.2
    ports:
      - 8080:8080
    volumes:
      - "activator_shelf:/home/kgrid/shelf"

volumes:
  activator_shelf:
```

that uses the the `kgrid/activator:1.5.2` image, with presets for port and shelf. 

Then:

```docker-compose up```

#### Good to Know
1. View Container Logs  ```docker logs activator```
1. Start a shell in the container ```docker exec -it activator sh```
