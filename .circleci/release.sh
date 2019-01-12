#!/bin/bash

 git config --global user.email "$GIT_USER_EMAIL"
 git config --global user.name "$GIT_USER_NAME"
 TAG_NAME="$RELEASE"
 FILE_NAME="kgrid-activator-$TAG_NAME.jar"

 # Maven release
 mvn release:clean release:prepare -B -Dtag=$TAG_NAME -DreleaseVersion=$RELEASE -DdevelopmentVersion=$NEXT \
                          -Darguments="-Ddockerfile.tag=$TAG_NAME" release:perform -P ci -s .circleci/settings.xml

 # Create GitHub Release capture release id
 RELEASE_ID=$(curl -X POST "https://api.github.com/repos/kgrid/kgrid-activator/releases?access_token=$GITHUB_TOKEN" \
                         -H 'Content-Type:application/json' -d "{\"tag_name\":\"$TAG_NAME\",  \"name\":\"KGrid Activator $TAG_NAME\", \"prerelease\":$PRERELEASE}" | jq -r ".id")

 echo Release $RELEASE_ID created

 #Upload jar file to GitHub Release
 curl -X POST "https://uploads.github.com/repos/kgrid/kgrid-activator/releases/$RELEASE_ID/assets?access_token=$GITHUB_TOKEN&name=$FILE_NAME" \
           -H "Content-Type: application/octet-stream" \
           --data-binary "@target/$FILE_NAME"




