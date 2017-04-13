# Knowledge Grid — Python Execution Stack

## Quick Start

### Download an executable binary or war file

Download the latest release from https://github.com/kgrid/python-execution-stack/releases

For the executable jar

```bash
./python-execution-stack-0.5.2-SNAPSHOT.jar
```
Add a library URl, e.g.
```bash
./python-execution-stack-0.5.2-SNAPSHOT.jar --library.url=https://kgrid.med.umich.edu/library
```

For the war file, see your container deployment instructions. In Tomcat, just copy to `[/path/to/tomcat/home]/webapps`


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
