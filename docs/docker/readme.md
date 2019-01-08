
## KGrid Docker Container


### Build Image
We are using [Spotify's Dockerfile Maven plug](https://github.com/spotify/dockerfile-maven) for docker image build and push.  
to create a docker image of the current activator project simply run the dockerfile:build after s build.  

``` mvn clean package dockerfile:build ```

After this run you will have a docker image 
```
~/kgrid-activator $ docker images
REPOSITORY                  TAG                   IMAGE ID            CREATED             SIZE
kgrid/activator             latest                fbe2de94cfa9        3 minutes ago       149MB

```
### Using the Image
Now using the activator image you can create a container based on the image

#### Running the Activator 

```docker run -p 8080:8080 --name activator kgrid/activator```

Mapped to local shelf and running in the background

```docker run -p 8080:8080 -v /mydirectory/shelf:/home/kgrid/shelf --name activator -d  kgrid/activator ```

#### Stop and Start Actvator

```docker stop activator```

```docker start activator```


### Good to Know

1. View Container Logs  ```docker logs activator```
1. Access container ```docker exec -it activator sh```

### Push New Image

Activator images are stored on [DockerHub](https://cloud.docker.com/u/kgrid/repository/docker/kgrid/activator) 

```mvn dockerfile:push -Ddockerfile.tag=1.0.4-rc1 -s /Users/me/.m2/my_settings.xml ```

```mvn dockerfile:push -Ddockerfile.tag=1.0.4-rc1 -s /Users/me/.m2/my_settings.xml ```

Details about KGrid Docker Hub access can be found on at [KGrid Docker](https://github.com/kgrid/kgrid-config/wiki/docker)


