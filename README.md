# Knowledge Grid — Python Execution Stack

## Quick Start

    git clone https://github.com/kgrid/python-execution-stack.git

    cd python-execution-stack

Build and deploy the execution stack (the executable `.war` file can be run as a standard jar file):

    mvn clean package

    java -Dpython.import.site=false -jar target/python-execution-stack-0.0.1-SNAPSHOT.war

Or the same `.war` file can be deployed in a Tomcat 8+ container. (The `-Dpython.import.site=false` is required when running as a
bare jar file.  It is not required when the war file is deployed in a container.)

Release version (milestone releases) are available here: https://github.com/kgrid/python-execution-stack/releases

See https://kgrid.github.io/python-execution-stack (or the /docs folder) for more info.