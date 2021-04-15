## KGrid Docker Containers

#### Pull from DockerHub
  ```bash
  docker pull kgrid/activator:#.#.#
  ```

### Using the Image

Now using the activator image you can create a container name ***activator*** from the `kgrid/activator` image and run it on port 8080...
 
```docker run -p 8080:8080 --name activator kgrid/activator```

or mapped to local shelf and running in the background...

```docker run -p 8080:8080 -v ${PWD}/shelf:/home/kgrid/shelf --name activator -d kgrid/activator ```

Once created, you can stop and start the container using `docker stop activator` and `docker start acivator`.

#### Quick start
If you just want to run a current activator, there is a `docker-compose.yaml` file that uses the the `kgrid/activator:latest` image, with presets for port and shelf. Try:

```docker-compose up```

which will use the local image tagged `latest` (built with `mvn clean package dockerfile:build`) or the `kgrid/activator:latest` build from Docker Hub.

#### Good to Know

1. View Container Logs  ```docker logs activator```
1. Start a shell in the container ```docker exec -it activator sh```

### Build Image

To build a local docker image...

   ```bash
   mvn spring-boot:build-image 
   ```

After this run you will have a local docker image...
```
~/kgrid-activator $ docker images
REPOSITORY                  TAG                   IMAGE ID            CREATED             SIZE
kgrid/activator             latest                fbe2de94cfa9        3 minutes ago       149MB
```

### Push New Image

Activator images are stored on [DockerHub](https://cloud.docker.com/u/kgrid/repository/docker/kgrid/activator) 

```docker push kgrid/activator:#.#.# ```

