# Knowledge Grid — Python Execution Stack

## Quick Start

### Download and run an executable binary or war file

Download the latest release from https://github.com/kgrid/python-execution-stack/releases

Launch the executable jar, running on port 8080 by default:

```bash
./python-execution-stack-0.5.2-SNAPSHOT.jar
```
Add a library URl, e.g. (optional)
```bash
./python-execution-stack-0.5.2-SNAPSHOT.jar --library.url=https://kgrid.med.umich.edu/library
```

For the war file, see your container deployment instructions. In Tomcat, just copy to `[/path/to/tomcat/home]/webapps`

#### Test using a built in object

The activator ships a simple, built-in knowledge object for testing, the "Prescription Counter." Try this:

```curl
curl --request POST \
  --url http://localhost:8080/knowledgeObject/ark:/default/object/result \
  --header 'accept: application/json' \
  --header 'content-type: application/json' \
  --data ' {"DrugIDs":"101 204 708 406 190"}'
```
You can see the list of built-in objects by going to `http://localhost:8080/shelf`.

To add an object, use an HTTP PUT request (we use [Postman](https://www.getpostman.com/)):

```bash
PUT /shelf/ark:/hello/world2 HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept-Encoding: text/plain
```

with the following body:

```json
{
"metadata": {
   "title": "Hello World",
   "description": "Test object",
   "published": false
   },
   "uri": "ark:/hello/world",
"payload": {
   "content": "def execute(inputs):\n    name = inputs[\"name\"]\n    return \"Hello, \" + name\n\n#print execute({\"name\":\"Jerry\"})\n",
   "engineType": "Python",
   "functionName": "execute"
   },
"inputMessage": "<rdf:RDF xmlns:ot=\"http://uofm.org/objectteller/#\"\n         xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n    <rdf:Description rdf:about=\"http://uofm.org/objectteller/inputMessage\">\n        <ot:noofparams>1</ot:noofparams>\n        <ot:params>\n            <rdf:Seq>\n                <rdf:li>name</rdf:li>\n            </rdf:Seq>\n        </ot:params>\n    </rdf:Description>\n    <rdf:Description rdf:about=\"http://uofm.org/objectteller/bame/\">\n        <ot:datatype>STRING</ot:datatype>\n    </rdf:Description>\n</rdf:RDF>\n",
"outputMessage": "<rdf:RDF xmlns:ot=\"http://uofm.org/objectteller/\"\n  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n  <rdf:Description rdf:about=\"http://uofm.org/objectteller/outputMessage\">\n    <ot:returntype>STRING</ot:returntype>\n  </rdf:Description>\n</rdf:RDF>\n"
}
```

See 

### To build from source code

    git clone https://github.com/kgrid/python-execution-stack.git

    cd python-execution-stack

### Build and deploy the execution stack as an executable jar file:

```bash
mvn clean package -Dpackaging=jar
./target/python-execution-stack-0.0.1-SNAPSHOT.jar
```
    
### Build and deploy a standard war file

```bash
mvn clean package    # builds a .war file by default
mvn tomcat7:deploy   # tomcat server settings taken from ~/.m2/settings.xml 
```

#### Default configuration

```properties
# Use this as a model for externally supplied properties/config
# see: https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html

# set profile based on system environment variable ('env' or 'ENV')
# if it exists -> loads application-${env}.properties from spring.config.location directory, or classpath by default
# spring.profiles.active=test

# server port if different from default (8080) - only for executable jar
#server.port=8080

# Context path - only for executable jar
#server.contextPath=/

# Optional - absolute path (if external library)
#library.url=http://dlhs-fedora-dev-a.umms.med.umich.edu:8080/ObjectTeller

# Default shelf location is current directory, location must be readable by process user, e.g. 'tomcat'
#stack.shelf.path=${java.io.tmpdir}

#shelf name if other than 'shelf'
#stack.shelf.name=shelf

# Disable JMX export of all endpoints or set unique-names-true
# if deploying multiple instances in the same JVM
#endpoints.jmx.unique-names=true
#endpoints.jmx.enabled=false
```

### Setting up external configuration 

#### Configuring an Activator deployed as a service

Add a app.service file in a place owned by the user you want to run as (e.g. `/var/kgrid`):

```properties
# tomcat.service
  
# Systemd unit file for tomcat
# From https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-on-centos-7
[Unit]
Description=Apache Tomcat Web Application Container
After=syslog.target network.target
  
[Service]
Type=forking
  
Environment=JAVA_HOME=/usr/lib/jvm/jre
Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/tomcat
Environment=CATALINA_BASE=/opt/tomcat
Environment='CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC'
Environment='JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom'
 
ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/bin/kill -15 $MAINPID
 
User=tomcat
Group=tomcat
UMask=0007
RestartSec=10
Restart=always
 
[Install]
WantedBy=multi-user.target
```
and add an `application.properties` file in the `config` subdirectory `/var/kgrid/config`. See the [Spring docs](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) for more options.

```properties
# application.properties
#Use this as a model for externally supplied properties/config
 
# server port if different from default (8080) - only for executable jar
#server.port=8080
 
# Context path - only for executable jar
server.contextPath=/stack
 
# Optional - absolute path (if external library)
library.url=http://library.kgrid.org
 
stack.shelf.path=/var/kgrid/stack
```

#### Configuring an Activator deployed in tomcat

Basically, for per context configuration, add `stack.xml` file to `$CATALINA_BASE/conf/[enginename]/[hostname]/$APP.xml` (e.g. `/opt/tomcat/conf/Catalina/localhost/stack.xml`) for an app deployed at context `/stack` with the following contents.

```xml
<!-- stack.xml -->
 
<Context reloadable="true">
    <Parameter name="spring.config.location" value="/var/kgrid/config/stack/"/>
</Context>
```
Or, set an environment variable, `SPRING_CONFIG_LOCATION=/var/kgrid/config/stack/`. See the Stack Overflow question, [Externalizing Tomcat Webapp Config from War File](http://stackoverflow.com/questions/13956651/externalizing-tomcat-webapp-config-from-war-file) for other options.


Then add an application.properties file in `/var/kgrid/config/stack/` like this:

```properties
# application.properties
#Use this as a model for externally supplied properties/config
 
# server port if different from default (8080) - only for executable jar
#server.port=8080
 
# Context path - only for executable jar
server.contextPath=/stack
 
# Optional - absolute path (if external library)
library.url=http://library.kgrid.org
 
# the shelf will be at /var/kgrid/stack/shelf, tomcat user will new r+w to ../stack to create/update the shelf
stack.shelf.path=/var/kgrid/stack
```

Release version (milestone releases) are available here: https://github.com/kgrid/python-execution-stack/releases

See https://kgrid.github.io/python-execution-stack (or the /docs folder) for more info.
