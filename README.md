# Knowledge Grid — Python Execution Stack

## Quick Start

### Building the execution stack from source

    git clone https://github.com/kgrid/python-execution-stack.git

    cd python-execution-stack

#### Build and deploy the execution stack as an executable jar file:

```bash
mvn clean package -Dpackaging=jar
./target/python-execution-stack-0.0.1-SNAPSHOT.jar
```
    
#### Build and deploy a standard war file

```bash
mvn clean package    # builds a .war file by default
mvn tomcat7:deploy   # tomcat server settings taken from ~/.m2/settings.xml 
```
Or just 

#### Default configuration

```properties
# set profile based on system environment variable ('env' or 'ENV')
# if it exists -> loads application-${env}.properties from spring.config.location directory, or classpath by default
# spring.profiles.active=test  

# server port if different from default (8080) - only for executable jar
#server.port=8080  

# Context path - only for executable jar
#server.contextPath=/  

# Optional - absolute path (if external library)
#library.url=http://dlhs-fedora-dev-a.umms.med.umich.edu:8080/ObjectTeller

#stack.shelf.path=${java.io.tmpdir}

#shelf name if other than 'shelf'
#stack.shelf.name=shelf
```

### Setting up external configuration 


Release version (milestone releases) are available here: https://github.com/kgrid/python-execution-stack/releases

See https://kgrid.github.io/python-execution-stack (or the /docs folder) for more info.